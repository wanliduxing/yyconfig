package com.ctrip.framework.apollo.configservice.controller;

import com.ctrip.framework.apollo.configservice.util.NamespaceUtil;
import com.ctrip.framework.apollo.configservice.util.WatchKeysUtil;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.cache.*;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.gson.Gson;
import com.yofish.apollo.domain.ReleaseMessage;
import com.yofish.apollo.grayReleaseRule.GrayReleaseRulesHolder;
import com.yofish.apollo.message.ReleaseMessageListener;
import com.yofish.apollo.message.Topics;
import framework.apollo.core.ConfigConsts;
import framework.apollo.core.dto.ApolloConfig;
import framework.apollo.core.utils.PropertiesUtil;
import framework.apollo.tracer.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
@RestController
@RequestMapping("/configfiles")
public class ConfigFileController implements ReleaseMessageListener {
    private static final Logger logger = LoggerFactory.getLogger(ConfigFileController.class);
    private static final Joiner STRING_JOINER = Joiner.on(ConfigConsts.CLUSTER_NAMESPACE_SEPARATOR);
    private static final Splitter X_FORWARDED_FOR_SPLITTER = Splitter.on(",").omitEmptyStrings()
            .trimResults();
    private static final long MAX_CACHE_SIZE = 50 * 1024 * 1024; // 50MB
    private static final long EXPIRE_AFTER_WRITE = 30;
    private final HttpHeaders propertiesResponseHeaders;
    private final HttpHeaders jsonResponseHeaders;
    private final ResponseEntity<String> NOT_FOUND_RESPONSE;
    private Cache<String, String> localCache;
    private final Multimap<String, String>
            watchedKeys2CacheKey = Multimaps.synchronizedSetMultimap(HashMultimap.create());
    private final Multimap<String, String>
            cacheKey2WatchedKeys = Multimaps.synchronizedSetMultimap(HashMultimap.create());
    private static final Gson gson = new Gson();

    @Autowired
    private QueryConfigController queryConfigController;

    @Autowired
    private NamespaceUtil namespaceUtil;

    @Autowired
    private WatchKeysUtil watchKeysUtil;

    @Autowired
    private GrayReleaseRulesHolder grayReleaseRulesHolder;

    public ConfigFileController() {
        localCache = CacheBuilder.newBuilder()
                .expireAfterWrite(EXPIRE_AFTER_WRITE, TimeUnit.MINUTES)
                .weigher(new Weigher<String, String>() {
                    @Override
                    public int weigh(String key, String value) {
                        return value == null ? 0 : value.length();
                    }
                })
                .maximumWeight(MAX_CACHE_SIZE)
                .removalListener(new RemovalListener<String, String>() {
                    @Override
                    public void onRemoval(RemovalNotification<String, String> notification) {
                        String cacheKey = notification.getKey();
                        logger.debug("removing cache key: {}", cacheKey);
                        if (!cacheKey2WatchedKeys.containsKey(cacheKey)) {
                            return;
                        }
                        //create a new list to avoid ConcurrentModificationException
                        List<String> watchedKeys = new ArrayList<>(cacheKey2WatchedKeys.get(cacheKey));
                        for (String watchedKey : watchedKeys) {
                            watchedKeys2CacheKey.remove(watchedKey, cacheKey);
                        }
                        cacheKey2WatchedKeys.removeAll(cacheKey);
                        logger.debug("removed cache key: {}", cacheKey);
                    }
                })
                .build();
        propertiesResponseHeaders = new HttpHeaders();
        propertiesResponseHeaders.add("Content-Type", "text/plain;charset=UTF-8");
        jsonResponseHeaders = new HttpHeaders();
        jsonResponseHeaders.add("Content-Type", "application/json;charset=UTF-8");
        NOT_FOUND_RESPONSE = new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @RequestMapping(value = "/{appId}/{clusterName}/{namespace:.+}", method = RequestMethod.GET)
    public ResponseEntity<String> queryConfigAsProperties(@PathVariable String appId,
                                                          @PathVariable String clusterName,
                                                          @PathVariable String namespace,
                                                          @RequestParam(value = "dataCenter", required = false) String dataCenter,
                                                          @RequestParam(value = "ip", required = false) String clientIp,
                                                          HttpServletRequest request,
                                                          HttpServletResponse response)
            throws IOException {

        String result = queryConfig(ConfigFileOutputFormat.PROPERTIES, appId, clusterName, namespace, dataCenter, clientIp, request, response);

        if (result == null) {
            return NOT_FOUND_RESPONSE;
        }

        return new ResponseEntity<>(result, propertiesResponseHeaders, HttpStatus.OK);
    }

    @RequestMapping(value = "/json/{appId}/{clusterName}/{namespace:.+}", method = RequestMethod.GET)
    public ResponseEntity<String> queryConfigAsJson(@PathVariable String appId,
                                                    @PathVariable String clusterName,
                                                    @PathVariable String namespace,
                                                    @RequestParam(value = "dataCenter", required = false) String dataCenter,
                                                    @RequestParam(value = "ip", required = false) String clientIp,
                                                    HttpServletRequest request,
                                                    HttpServletResponse response) throws IOException {

        String result =
                queryConfig(ConfigFileOutputFormat.JSON, appId, clusterName, namespace, dataCenter,
                        clientIp, request, response);

        if (result == null) {
            return NOT_FOUND_RESPONSE;
        }

        return new ResponseEntity<>(result, jsonResponseHeaders, HttpStatus.OK);
    }

    String queryConfig(ConfigFileOutputFormat outputFormat, String appId, String clusterName,
                       String namespace, String dataCenter, String clientIp,
                       HttpServletRequest request,
                       HttpServletResponse response) throws IOException {

        String queryConfigRs = null;

        //strip out .properties suffix
        namespace = namespaceUtil.filterNamespaceName(namespace);
        //fix the character case issue, such as FX.apollo <-> fx.apollo
        namespace = namespaceUtil.normalizeNamespace(appId, namespace);

        if (isNullOrEmpty(clientIp)) {
            clientIp = tryToGetClientIp(request);
        }

        boolean hasGrayReleaseRule4CurrenClient = hasGrayRule4CurrentClient(appId, namespace, clientIp);
        if (hasGrayReleaseRule4CurrenClient) {
            return loadConfigByGrayRule(outputFormat, appId, clusterName, namespace, dataCenter, clientIp, request, response);
        }

        String cacheKey = assembleCacheKey(outputFormat, appId, clusterName, namespace, dataCenter);
        if (cacheExists(cacheKey, queryConfigRs)) {
            return queryConfigRs;
        }

        if (isNullOrEmpty(queryConfigRs)) {
            Tracer.logEvent("ConfigFile.Cache.Miss", cacheKey);
            queryConfigRs = loadConfigFromConfigController(outputFormat, appId, clusterName, namespace, dataCenter, clientIp, request, response);

            if (queryConfigRs == null) {
                return null;
            }
            //5. Double check if this client needs to load gray release, if yes, load from db again
            //This step is mainly to avoid cache pollution
            if (grayReleaseRulesHolder.hasGrayReleaseRule(appId, clientIp, namespace)) {
                Tracer.logEvent("ConfigFile.Cache.GrayReleaseConflict", cacheKey);
                return loadConfigFromConfigController(outputFormat, appId, clusterName, namespace, dataCenter, clientIp,
                        request, response);
            }

            localCache.put(cacheKey, queryConfigRs);
            logger.debug("adding cache for key: {}", cacheKey);

            Set<String> watchedKeys =
                    watchKeysUtil.assembleAllWatchKeys(appId, clusterName, namespace, dataCenter);

            for (String watchedKey : watchedKeys) {
                watchedKeys2CacheKey.put(watchedKey, cacheKey);
            }

            cacheKey2WatchedKeys.putAll(cacheKey, watchedKeys);
            logger.debug("added cache for key: {}", cacheKey);
        }

        return queryConfigRs;
    }

    private boolean cacheExists(String cacheKey, String queryConfigRs) {
        queryConfigRs = localCache.getIfPresent(cacheKey);
        return queryConfigRs != null;
    }

    private String loadConfigByGrayRule(ConfigFileOutputFormat outputFormat, String appId, String clusterName, String namespace, String dataCenter, String clientIp, HttpServletRequest request, HttpServletResponse response) throws IOException {
        return loadConfigFromConfigController(outputFormat, appId, clusterName, namespace, dataCenter, clientIp, request, response);
    }

    private boolean hasGrayRule4CurrentClient(String appId, String namespace, String clientIp) {
        return grayReleaseRulesHolder.hasGrayReleaseRule(appId, clientIp, namespace);
    }

    private String loadConfigFromConfigController(ConfigFileOutputFormat outputFormat, String appId, String clusterName,
                                                  String namespace, String dataCenter, String clientIp,
                                                  HttpServletRequest request,
                                                  HttpServletResponse response) throws IOException {
        ApolloConfig apolloConfig = queryConfigController.queryConfig4Client(appId, clusterName, namespace, dataCenter, "-1", clientIp, null, request, response);

        if (apolloConfig == null || apolloConfig.getConfigurations() == null) {
            return null;
        }

        String result = null;

        switch (outputFormat) {
            case PROPERTIES:
                Properties properties = new Properties();
                properties.putAll(apolloConfig.getConfigurations());
                result = PropertiesUtil.toString(properties);
                break;
            case JSON:
                result = gson.toJson(apolloConfig.getConfigurations());
                break;
        }

        return result;
    }

    String assembleCacheKey(ConfigFileOutputFormat outputFormat, String appId, String clusterName,
                            String namespace,
                            String dataCenter) {
        List<String> keyParts =
                Lists.newArrayList(outputFormat.getValue(), appId, clusterName, namespace);
        if (!isNullOrEmpty(dataCenter)) {
            keyParts.add(dataCenter);
        }
        return STRING_JOINER.join(keyParts);
    }

    @Override
    public void handleReleaseMessage(ReleaseMessage message, String channel) {
        logger.info("message received - channel: {}, message: {}", channel, message);

        String content = message.getMessage();
        if (!Topics.APOLLO_RELEASE_TOPIC.equals(channel) || isNullOrEmpty(content)) {
            return;
        }

        if (!watchedKeys2CacheKey.containsKey(content)) {
            return;
        }

        //create a new list to avoid ConcurrentModificationException
        List<String> cacheKeys = new ArrayList<>(watchedKeys2CacheKey.get(content));

        for (String cacheKey : cacheKeys) {
            logger.debug("invalidate cache key: {}", cacheKey);
            localCache.invalidate(cacheKey);
        }
    }

    enum ConfigFileOutputFormat {
        PROPERTIES("properties"), JSON("json");

        private String value;

        ConfigFileOutputFormat(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    private String tryToGetClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-FORWARDED-FOR");
        if (!isNullOrEmpty(forwardedFor)) {
            return X_FORWARDED_FOR_SPLITTER.splitToList(forwardedFor).get(0);
        }
        return request.getRemoteAddr();
    }
}
