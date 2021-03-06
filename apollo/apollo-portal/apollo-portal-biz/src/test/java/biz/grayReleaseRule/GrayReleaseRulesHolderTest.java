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
package biz.grayReleaseRule;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.yofish.apollo.domain.GrayReleaseRule;
import com.yofish.apollo.domain.ReleaseMessage;
import com.yofish.apollo.grayReleaseRule.GrayReleaseRulesHolder;
import com.yofish.apollo.message.Topics;
import com.yofish.apollo.repository.GrayReleaseRuleRepository;
import com.yofish.apollo.service.PortalConfig;
import common.constants.NamespaceBranchStatus;
import common.dto.GrayReleaseRuleItemDTO;
import framework.apollo.core.ConfigConsts;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.*;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
@RunWith(MockitoJUnitRunner.class)
public class GrayReleaseRulesHolderTest {
  private static final Joiner STRING_JOINER = Joiner.on(ConfigConsts.CLUSTER_NAMESPACE_SEPARATOR);
  private GrayReleaseRulesHolder grayReleaseRulesHolder;
  @Mock
  private PortalConfig bizConfig;
  @Mock
  private GrayReleaseRuleRepository grayReleaseRuleRepository;
  private Gson gson = new Gson();
  private AtomicLong idCounter;

  @Before
  public void setUp() throws Exception {
    grayReleaseRulesHolder = spy(new GrayReleaseRulesHolder());
    ReflectionTestUtils.setField(grayReleaseRulesHolder, "bizConfig", bizConfig);
    ReflectionTestUtils.setField(grayReleaseRulesHolder, "grayReleaseRuleRepository", grayReleaseRuleRepository);
    idCounter = new AtomicLong();
  }

  @Test
  public void testScanGrayReleaseRules() throws Exception {
    /*String someAppId = "someAppId";
    String someClusterName = "someClusterName";
    String someNamespaceName = "someNamespaceName";
    String anotherNamespaceName = "anotherNamespaceName";

    Long someReleaseId = 1L;
    int activeBranchStatus = NamespaceBranchStatus.ACTIVE;

    String someClientAppId = "clientAppId1";
    String someClientIp = "1.1.1.1";
    String anotherClientAppId = "clientAppId2";
    String anotherClientIp = "2.2.2.2";

    GrayReleaseRule someRule = assembleGrayReleaseRule(someAppId, someClusterName,
        someNamespaceName, Lists.newArrayList(assembleRuleItem(someClientAppId, Sets.newHashSet
            (someClientIp))), someReleaseId, activeBranchStatus);

    when(bizConfig.grayReleaseRuleScanInterval()).thenReturn(30);
    when(grayReleaseRuleRepository.findFirst500ByIdGreaterThanOrderByIdAsc(0L)).thenReturn(Lists
        .newArrayList(someRule));

    //scan rules
    grayReleaseRulesHolder.afterPropertiesSet();

    assertEquals(someReleaseId, grayReleaseRulesHolder.findReleaseIdFromGrayReleaseRule
        (someClientAppId, someClientIp, someAppId, someClusterName, someNamespaceName));
    assertNull(grayReleaseRulesHolder.findReleaseIdFromGrayReleaseRule(someClientAppId,
        anotherClientIp, someAppId, someClusterName, someNamespaceName));

    assertNull(grayReleaseRulesHolder.findReleaseIdFromGrayReleaseRule(anotherClientAppId,
        someClientIp, someAppId, someClusterName, someNamespaceName));
    assertNull(grayReleaseRulesHolder.findReleaseIdFromGrayReleaseRule(anotherClientAppId,
        anotherClientIp, someAppId, someClusterName, someNamespaceName));

    assertTrue(grayReleaseRulesHolder.hasGrayReleaseRule(someClientAppId, someClientIp,
        someNamespaceName));
    assertFalse(grayReleaseRulesHolder.hasGrayReleaseRule(someClientAppId, anotherClientIp,
        someNamespaceName));
    assertFalse(grayReleaseRulesHolder.hasGrayReleaseRule(someClientAppId, someClientIp,
        anotherNamespaceName));

    assertFalse(grayReleaseRulesHolder.hasGrayReleaseRule(anotherClientAppId, anotherClientIp,
        someNamespaceName));
    assertFalse(grayReleaseRulesHolder.hasGrayReleaseRule(anotherClientAppId, anotherClientIp,
        anotherNamespaceName));

    GrayReleaseRule anotherRule = assembleGrayReleaseRule(someAppId, someClusterName,
        someNamespaceName, Lists.newArrayList(assembleRuleItem(anotherClientAppId, Sets.newHashSet
            (anotherClientIp))), someReleaseId, activeBranchStatus);

    when(grayReleaseRuleRepository.findByAppIdAndClusterNameAndNamespaceName(someAppId,
        someClusterName, someNamespaceName)).thenReturn(Lists.newArrayList(anotherRule));

    //send message
    grayReleaseRulesHolder.handleReleaseMessage(assembleReleaseMessage(someAppId, someClusterName,
        someNamespaceName), Topics.APOLLO_RELEASE_TOPIC);

    assertNull(grayReleaseRulesHolder.findReleaseIdFromGrayReleaseRule
        (someClientAppId, someClientIp, someAppId, someClusterName, someNamespaceName));
    assertEquals(someReleaseId, grayReleaseRulesHolder.findReleaseIdFromGrayReleaseRule
        (anotherClientAppId, anotherClientIp, someAppId, someClusterName, someNamespaceName));

    assertFalse(grayReleaseRulesHolder.hasGrayReleaseRule(someClientAppId, someClientIp,
        someNamespaceName));
    assertFalse(grayReleaseRulesHolder.hasGrayReleaseRule(someClientAppId, someClientIp,
        anotherNamespaceName));

    assertTrue(grayReleaseRulesHolder.hasGrayReleaseRule(anotherClientAppId, anotherClientIp,
        someNamespaceName));
    assertFalse(grayReleaseRulesHolder.hasGrayReleaseRule(anotherClientAppId, someClientIp,
        someNamespaceName));
    assertFalse(grayReleaseRulesHolder.hasGrayReleaseRule(anotherClientAppId, anotherClientIp,
        anotherNamespaceName));*/
  }

  private GrayReleaseRule assembleGrayReleaseRule(String appId, String clusterName, String
      namespaceName, List<GrayReleaseRuleItemDTO> ruleItems, long releaseId, int branchStatus) {
    GrayReleaseRule rule = new GrayReleaseRule();
    rule.setId(idCounter.incrementAndGet());
//    rule.setAppId(appCode);
//    rule.setClusterName(clusterName);
//    rule.setNamespaceName(namespaceName);
//    rule.setBranchName("someBranch");
//    rule.setRules(gson.toJson(ruleItems));
//    rule.setReleaseId(releaseId);
    rule.setBranchStatus(branchStatus);

    return rule;
  }

  private GrayReleaseRuleItemDTO assembleRuleItem(String clientAppId, Set<String> clientIpList) {
    return new GrayReleaseRuleItemDTO(clientAppId, clientIpList);
  }

  private ReleaseMessage assembleReleaseMessage(String appId, String clusterName, String
      namespaceName) {
    String message = STRING_JOINER.join(appId, clusterName, namespaceName);
    ReleaseMessage releaseMessage = new ReleaseMessage(message);

    return releaseMessage;
  }
}
