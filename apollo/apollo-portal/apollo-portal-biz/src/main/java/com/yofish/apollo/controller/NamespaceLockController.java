package com.yofish.apollo.controller;

import com.yofish.apollo.entity.vo.LockInfo;
import framework.apollo.core.enums.Env;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class NamespaceLockController {
/*

  @Autowired
  private NamespaceLockService namespaceLockService;

  @Deprecated
  @RequestMapping(value = "/apps/{appId}/envs/{env}/clusters/{clusterName}/namespaces/{namespaceName}/lock", method = RequestMethod.GET)
  public NamespaceLock getNamespaceLock(@PathVariable String appId, @PathVariable String env,
                                        @PathVariable String clusterName, @PathVariable String namespaceName) {

    return namespaceLockService.getNamespaceLock(appId, Env.valueOf(env), clusterName, namespaceName);
  }

  @RequestMapping(value = "/apps/{appId}/envs/{env}/clusters/{clusterName}/namespaces/{namespaceName}/lock-info", method = RequestMethod.GET)
  public LockInfo getNamespaceLockInfo(@PathVariable String appId, @PathVariable String env,
                                       @PathVariable String clusterName, @PathVariable String namespaceName) {

    return namespaceLockService.getNamespaceLockInfo(appId, Env.fromString(env), clusterName, namespaceName);

  }
*/


}