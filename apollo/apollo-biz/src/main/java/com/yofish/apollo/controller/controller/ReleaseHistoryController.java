package com.yofish.apollo.controller.controller;


import com.ctrip.framework.apollo.component.PermissionValidator;
import com.ctrip.framework.apollo.core.enums.Env;
import com.ctrip.framework.apollo.model.bo.ReleaseHistoryBO;
import com.ctrip.framework.apollo.service.ReleaseHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
public class ReleaseHistoryController {

  @Autowired
  private ReleaseHistoryService releaseHistoryService;
  @Autowired
  private PermissionValidator permissionValidator;

  @RequestMapping(value = "/apps/{appId}/envs/{env}/clusters/{clusterName}/namespaces/{namespaceName}/releases/histories",
      method = RequestMethod.GET)
  public List<ReleaseHistoryBO> findReleaseHistoriesByNamespace(@PathVariable String appId,
                                                                @PathVariable String env,
                                                                @PathVariable String clusterName,
                                                                @PathVariable String namespaceName,
                                                                @RequestParam(value = "page", defaultValue = "0") int page,
                                                                @RequestParam(value = "size", defaultValue = "10") int size) {

    if (permissionValidator.shouldHideConfigToCurrentUser(appId, env, namespaceName)) {
      return Collections.emptyList();
    }

   return releaseHistoryService.findNamespaceReleaseHistory(appId, Env.valueOf(env), clusterName ,namespaceName, page, size);
  }

}
