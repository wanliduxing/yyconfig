package com.yofish.apollo;

import com.google.common.collect.Sets;
import com.yofish.apollo.domain.*;
import com.yofish.apollo.repository.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @Author: xiongchengwei
 * @Date: 2019/11/12 下午2:44
 */
@Transactional
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {JpaApplication.class})
public class InstanceConfigRepositoryTest {
    @Autowired
    private InstanceConfigRepository instanceConfigRepository;
    @Autowired
    private AppEnvClusterNamespaceRepository namespaceRepository;
    @Autowired
    private InstanceRepository instanceRepository;

    @Autowired
    private AppEnvClusterRepository appEnvClusterRepository;


    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void test(){

        AppEnvClusterNamespace namespace = namespaceRepository.findAll().get(0);
        Instance instance = createInstance();
        InstanceConfig instanceConfig = createInstanceConfig(namespace, instance);
        instanceConfigRepository.save(instanceConfig);
        Page<InstanceConfig> instanceConfigs = instanceConfigRepository.findByNamespaceId(namespace.getId(), Pageable.unpaged());
        System.out.println(instanceConfigs);
    }

    private Instance createInstance() {
        Instance instance = new Instance();
        instance.setIp("10.0.11.18");
        instance.setDataCenter("shanghai");
        instance.setAppEnvCluster(null);
        instanceRepository.save(instance);
        return instance;
    }

    private InstanceConfig createInstanceConfig(AppEnvClusterNamespace namespace, Instance instance) {
        InstanceConfig instanceConfig = new InstanceConfig();
        instanceConfig.setNamespace(namespace);
        instanceConfig.setInstance(instance);
        instanceConfigRepository.save(instanceConfig);
        return instanceConfig;
    }

}