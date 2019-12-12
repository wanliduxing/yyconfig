package com.yofish.apollo.service;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.yofish.apollo.domain.ServerConfig;
//import com.yofish.apollo.model.vo.Organization;
import com.yofish.apollo.repository.ServerConfigRepository;
import com.youyu.common.helper.YyRequestInfoHelper;
import common.config.RefreshableConfig;
import common.config.RefreshablePropertySource;
import framework.apollo.core.ConfigConsts;
import framework.apollo.core.enums.Env;
import framework.apollo.tracer.Tracer;
import framework.foundation.Foundation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.util.*;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
@Component
public class PortalConfig extends RefreshableConfig {

    private static final Logger logger = LoggerFactory.getLogger(PortalConfig.class);
    private static final String LIST_SEPARATOR = ",";
    @Autowired
    private ServerConfigRepository serverConfigRepository;
    @Autowired
    private ConfigurableEnvironment environment;
    private Gson gson = new Gson();
//    private static final Type ORGANIZATION = new TypeToken<List<Organization>>() {
//    }.getType();

//  @Autowired
//  private PortalDBPropertySource portalDBPropertySource;

    public PortalConfig(String name, Map<String, Object> source) {
        super(name, source);
    }


    public PortalConfig() {
        super("DBConfig", Maps.newConcurrentMap());
    }

    @Override
    public List<RefreshablePropertySource> getRefreshablePropertySources() {
        return Collections.singletonList(null);
    }

    String getCurrentDataCenter() {
        return Foundation.server().getDataCenter();
    }

    protected void refresh() {
        Iterable<ServerConfig> dbConfigs = serverConfigRepository.findAll();

        Map<String, Object> newConfigs = Maps.newHashMap();
        //default cluster's configs
        /*for (ServerConfig config : dbConfigs) {
            if (Objects.equals(ConfigConsts.CLUSTER_NAME_DEFAULT, config.getCluster())) {
                newConfigs.put(config.getKey(), config.getValue());
            }
        }

        //data center's configs
        String dataCenter = getCurrentDataCenter();
        for (ServerConfig config : dbConfigs) {
            if (Objects.equals(dataCenter, config.getCluster())) {
                newConfigs.put(config.getKey(), config.getValue());
            }
        }

        //cluster's config
        if (!isNullOrEmpty(System.getProperty(ConfigConsts.APOLLO_CLUSTER_KEY))) {
            String cluster = System.getProperty(ConfigConsts.APOLLO_CLUSTER_KEY);
            for (ServerConfig config : dbConfigs) {
                if (Objects.equals(cluster, config.getCluster())) {
                    newConfigs.put(config.getKey(), config.getValue());
                }
            }
        }
*/
        //put to environment
//        for (Map.Entry<String, Object> config : newConfigs.entrySet()) {
//            String key = config.getKey();
//            Object value = config.getValue();
//
//            if (this.source.get(key) == null) {
//                logger.info("Load config from DB : {} = {}", key, value);
//            } else if (!Objects.equals(this.source.get(key), value)) {
//                logger.info("Load config from DB : {} = {}. Old value = {}", key,
//                        value, this.source.get(key));
//            }
//
//            this.source.put(key, value);
//
//        }

    }


    public Set<Env> publishTipsSupportedEnvs() {
        String[] configurations = getArrayProperty("namespace.publish.tips.supported.envs", null);

        Set<Env> result = Sets.newHashSet();
        if (configurations == null || configurations.length == 0) {
            return result;
        }

        for (String env : configurations) {
            result.add(Env.fromString(env));
        }

        return result;
    }


    @Override
    public String[] getArrayProperty(String key, String[] defaultValue) {
        try {
            String value = getValue(key);
            return Strings.isNullOrEmpty(value) ? defaultValue : value.split(LIST_SEPARATOR);
        } catch (Throwable e) {
            Tracer.logError("Get array property failed.", e);
            return defaultValue;
        }
    }

    @Override
    public String getValue(String key, String defaultValue) {
        try {
            return environment.getProperty(key, defaultValue);
        } catch (Throwable e) {
            Tracer.logError("Get value failed.", e);
            return defaultValue;
        }
    }

    @Override
    public String getValue(String key) {
        return environment.getProperty(key);
    }


    /***
     * Level: important
     **/
    public List<Env> portalSupportedEnvs() {
        String[] configurations = getArrayProperty("apollo.portal.envs", new String[]{"FAT", "UAT", "PRO"});
        List<Env> envs = Lists.newLinkedList();

        for (String env : configurations) {
            envs.add(Env.fromString(env));
        }

        return envs;
    }

    public List<String> superAdmins() {
        String superAdminConfig = getValue("superAdmin", "");
        if (Strings.isNullOrEmpty(superAdminConfig)) {
            return Collections.emptyList();
        }
        return splitter.splitToList(superAdminConfig);
    }

    public Set<Env> emailSupportedEnvs() {
        String[] configurations = getArrayProperty("email.supported.envs", null);

        Set<Env> result = Sets.newHashSet();
        if (configurations == null || configurations.length == 0) {
            return result;
        }

        for (String env : configurations) {
            result.add(Env.fromString(env));
        }

        return result;
    }

    public boolean isConfigViewMemberOnly(String env) {
        String[] configViewMemberOnlyEnvs = getArrayProperty("configView.memberOnly.envs", new String[0]);

        for (String memberOnlyEnv : configViewMemberOnlyEnvs) {
            if (memberOnlyEnv.equalsIgnoreCase(env)) {
                return true;
            }
        }

        return false;
    }

    /***
     * Level: normal
     **/
    public int connectTimeout() {
        return getIntProperty("api.connectTimeout", 3000);
    }

    public int readTimeout() {
        return getIntProperty("api.readTimeout", 10000);
    }

/*
    public List<Organization> organizations() {

        String organizations = getValue("organizations");
        return organizations == null ? Collections.emptyList() : gson.fromJson(organizations, ORGANIZATION);
    }
*/

    public String portalAddress() {
        return getValue("apollo.portal.address");
    }

    public boolean isEmergencyPublishAllowed(Env env) {
        String targetEnv = env.name();

        String[] emergencyPublishSupportedEnvs = getArrayProperty("emergencyPublish.supported.envs", new String[0]);

        for (String supportedEnv : emergencyPublishSupportedEnvs) {
            if (Objects.equals(targetEnv, supportedEnv.toUpperCase().trim())) {
                return true;
            }
        }

        return false;
    }


    public String consumerTokenSalt() {
        return getValue("consumer.token.salt", "apollo-portal");
    }

    public String emailSender() {
        return getValue("email.sender");
    }

    public String emailTemplateFramework() {
        return getValue("email.template.framework", "");
    }

    public String emailReleaseDiffModuleTemplate() {
        return getValue("email.template.release.module.diff", "");
    }

    public String emailRollbackDiffModuleTemplate() {
        return getValue("email.template.rollback.module.diff", "");
    }

    public String emailGrayRulesModuleTemplate() {
        return getValue("email.template.release.module.rules", "");
    }

    public String wikiAddress() {
        return getValue("wiki.address", "https://github.com/ctripcorp/apollo/wiki");
    }

    public boolean canAppAdminCreatePrivateNamespace() {
        return getBooleanProperty("admin.createPrivateNamespace.switch", true);
    }

    /***
     * The following configurations are used in ctrip profile
     **/

    public int appId() {
        return getIntProperty("ctrip.appid", 0);
    }

    //send code & template id. apply from ewatch
    public String sendCode() {
        return getValue("ctrip.email.send.code");
    }

    public int templateId() {
        return getIntProperty("ctrip.email.template.id", 0);
    }

    //email retention time in email server queue.TimeUnit: hour
    public int survivalDuration() {
        return getIntProperty("ctrip.email.survival.duration", 5);
    }

    public boolean isSendEmailAsync() {
        return getBooleanProperty("email.send.async", true);
    }

    public String portalServerName() {
        return getValue("serverName");
    }

    public String casServerLoginUrl() {
        return getValue("casServerLoginUrl");
    }

    public String casServerUrlPrefix() {
        return getValue("casServerUrlPrefix");
    }

    public String credisServiceUrl() {
        return getValue("credisServiceUrl");
    }

    public String userServiceUrl() {
        return getValue("userService.url");
    }

    public String userServiceAccessToken() {
        return getValue("userService.accessToken");
    }

    public String soaServerAddress() {
        return getValue("soa.server.address");
    }

    public String cloggingUrl() {
        return getValue("clogging.server.url");
    }

    public String cloggingPort() {
        return getValue("clogging.server.port");
    }

    public String hermesServerAddress() {
        return getValue("hermes.server.address");
    }


}
