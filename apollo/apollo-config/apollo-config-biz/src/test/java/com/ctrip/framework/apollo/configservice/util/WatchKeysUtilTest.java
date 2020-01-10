/*
 *    Copyright 2019-2020 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.ctrip.framework.apollo.configservice.util;

import com.ctrip.framework.apollo.configservice.service.AppNamespaceServiceWithCache;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.yofish.apollo.domain.App;
import com.yofish.apollo.domain.AppNamespace;
import framework.apollo.core.ConfigConsts;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collection;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
@RunWith(MockitoJUnitRunner.class)
public class WatchKeysUtilTest {
  @Mock
  private AppNamespaceServiceWithCache appNamespaceService;
  @Mock
  private AppNamespace someAppNamespace;
  @Mock
  private AppNamespace anotherAppNamespace;
  @Mock
  private AppNamespace somePublicAppNamespace;
  private WatchKeysUtil watchKeysUtil;
  private String someAppId;
  private String someCluster;
  private String someNamespace;
  private String anotherNamespace;
  private String somePublicNamespace;
  private String defaultCluster;
  private String someDC;
  private String somePublicAppId;

  @Before
  public void setUp() throws Exception {
    watchKeysUtil = new WatchKeysUtil();

    someAppId = "someId";
    someCluster = "someCluster";
    someNamespace = "someName";
    anotherNamespace = "anotherName";
    somePublicNamespace = "somePublicName";
    defaultCluster = ConfigConsts.CLUSTER_NAME_DEFAULT;
    someDC = "someDC";
    somePublicAppId = "somePublicId";
    App app = App.builder().appCode("中台支付").build();
    somePublicAppNamespace.setApp(app);
    when(someAppNamespace.getName()).thenReturn(someNamespace);
    when(someAppNamespace.getApp()).thenReturn(app);
    when(anotherAppNamespace.getName()).thenReturn(anotherNamespace);
    when(appNamespaceService.findByAppIdAndNamespaces(someAppId, Sets.newHashSet(someNamespace)))
        .thenReturn(Lists.newArrayList(someAppNamespace));
    when(appNamespaceService.findByAppIdAndNamespaces(someAppId, Sets.newHashSet(someNamespace, anotherNamespace)))
        .thenReturn(Lists.newArrayList(someAppNamespace, anotherAppNamespace));
    when(appNamespaceService.findByAppIdAndNamespaces(someAppId, Sets.newHashSet(someNamespace, anotherNamespace, somePublicNamespace)))
        .thenReturn(Lists.newArrayList(someAppNamespace, anotherAppNamespace));

    when(somePublicAppNamespace.getApp()).thenReturn(app);
    when(somePublicAppNamespace.getName()).thenReturn(somePublicNamespace);
    doReturn(Lists.newArrayList(somePublicAppNamespace)).when(appNamespaceService).findPublicNamespacesByNames(Sets.newHashSet(somePublicNamespace));
    doReturn(Lists.newArrayList(somePublicAppNamespace)).when(appNamespaceService).findPublicNamespacesByNames(Sets.newHashSet(someNamespace, somePublicNamespace));

    ReflectionTestUtils.setField(watchKeysUtil, "appNamespaceService", appNamespaceService);
  }

  @Test
  public void testAssembleAllWatchKeysWithOneNamespaceAndDefaultCluster() throws Exception {
    Set<String> watchKeys = watchKeysUtil.assembleAllWatchKeys(someAppId, defaultCluster, "dev", someNamespace, null);

    Set<String> clusters = Sets.newHashSet(defaultCluster);

    assertEquals(clusters.size(), watchKeys.size());
    assertWatchKeys(someAppId, clusters, someNamespace, watchKeys);
  }

  @Test
  public void testAssembleAllWatchKeysWithOneNamespaceAndSomeDC() throws Exception {
    Set<String> watchKeys =
        watchKeysUtil.assembleAllWatchKeys(someAppId, someDC,"dev", someNamespace, someDC);

    Set<String> clusters = Sets.newHashSet(defaultCluster, someDC);

    assertEquals(clusters.size(), watchKeys.size());
    assertWatchKeys(someAppId, clusters, someNamespace, watchKeys);
  }

  @Test
  public void testAssembleAllWatchKeysWithOneNamespaceAndSomeDCAndSomeCluster() throws Exception {
    Set<String> watchKeys =
        watchKeysUtil.assembleAllWatchKeys(someAppId, someCluster, "dev", someNamespace, someDC);

    Set<String> clusters = Sets.newHashSet(defaultCluster, someCluster, someDC);

    assertEquals(clusters.size(), watchKeys.size());
    assertWatchKeys(someAppId, clusters, someNamespace, watchKeys);
  }

  @Test
  public void testAssembleAllWatchKeysWithMultipleNamespaces() throws Exception {
    Multimap<String, String> watchKeysMap =
        watchKeysUtil.assembleAllWatchKeys(someAppId, someCluster,"dev",
                Sets.newHashSet(someNamespace, anotherNamespace), someDC);

    Set<String> clusters = Sets.newHashSet(defaultCluster, someCluster, someDC);

    assertEquals(clusters.size() * 2, watchKeysMap.size());
    assertWatchKeys(someAppId, clusters, someNamespace, watchKeysMap.get(someNamespace));
    assertWatchKeys(someAppId, clusters, anotherNamespace, watchKeysMap.get(anotherNamespace));
  }

  @Test
  public void testAssembleAllWatchKeysWithPrivateAndPublicNamespaces() throws Exception {
    Multimap<String, String> watchKeysMap =
        watchKeysUtil.assembleAllWatchKeys(someAppId, someCluster,"dev",
                Sets.newHashSet(someNamespace, anotherNamespace, somePublicNamespace), someDC);

    Set<String> clusters = Sets.newHashSet(defaultCluster, someCluster, someDC);

    assertEquals(clusters.size() * 4, watchKeysMap.size());

    assertWatchKeys(someAppId, clusters, someNamespace, watchKeysMap.get(someNamespace));
    assertWatchKeys(someAppId, clusters, anotherNamespace, watchKeysMap.get(anotherNamespace));
    assertWatchKeys(someAppId, clusters, somePublicNamespace, watchKeysMap.get(somePublicNamespace));
    assertWatchKeys(somePublicAppId, clusters, somePublicNamespace, watchKeysMap.get(somePublicNamespace));
  }

  @Test
  public void testAssembleWatchKeysForNoAppIdPlaceHolder() throws Exception {
    Multimap<String, String> watchKeysMap =
        watchKeysUtil.assembleAllWatchKeys(ConfigConsts.NO_APPID_PLACEHOLDER, someCluster,"dev",
                Sets.newHashSet(someNamespace, anotherNamespace), someDC);

    assertTrue(watchKeysMap.isEmpty());
  }

  @Test
  public void testAssembleWatchKeysForNoAppIdPlaceHolderAndPublicNamespace() throws Exception {
    Multimap<String, String> watchKeysMap =
        watchKeysUtil.assembleAllWatchKeys(ConfigConsts.NO_APPID_PLACEHOLDER, someCluster,"dev",
                Sets.newHashSet(someNamespace, somePublicNamespace), someDC);

    Set<String> clusters = Sets.newHashSet(defaultCluster, someCluster, someDC);

    assertEquals(clusters.size(), watchKeysMap.size());

    assertWatchKeys(somePublicAppId, clusters, somePublicNamespace, watchKeysMap.get(somePublicNamespace));
  }

  private void assertWatchKeys(String appId, Set<String> clusters, String namespaceName,
                               Collection<String> watchedKeys) {
    for (String cluster : clusters) {
      String key =
          Joiner.on(ConfigConsts.CLUSTER_NAMESPACE_SEPARATOR)
              .join(appId, cluster, namespaceName);
      assertTrue(watchedKeys.contains(key));
    }
  }
}
