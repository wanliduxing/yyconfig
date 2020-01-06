package com.yofish.apollo.service;

import com.yofish.apollo.config.ServerConfigKey;
import com.yofish.apollo.domain.ServerConfig;
import com.yofish.apollo.repository.ServerConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author WangSongJun
 * @date 2019-12-10
 */
@Service
public class ServerConfigService {
    @Autowired
    private ServerConfigRepository serverConfigRepository;

    public List<String> getActiveEnvs() {
        ServerConfig apolloPortalEnvs = this.serverConfigRepository.findByKey(ServerConfigKey.APOLLO_PORTAL_ENVS);
        if (ObjectUtils.isEmpty(apolloPortalEnvs) || StringUtils.isEmpty(apolloPortalEnvs.getValue())) {
            return null;
        } else {
            return new ArrayList<>(Arrays.asList(apolloPortalEnvs.getValue().split(",")));
        }
    }
}
