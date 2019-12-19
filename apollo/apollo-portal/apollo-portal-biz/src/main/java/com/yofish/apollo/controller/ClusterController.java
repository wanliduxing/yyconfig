package com.yofish.apollo.controller;

import com.yofish.apollo.domain.App;
import com.yofish.apollo.domain.AppEnvCluster;
import com.yofish.apollo.service.AppEnvClusterService;
import com.youyu.common.api.Result;
import common.exception.BadRequestException;
import common.utils.InputValidator;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@Api(description = "项目的集群")
@RestController
public class ClusterController {

    @Autowired
    private AppEnvClusterService appEnvClusterService;

    @ApiOperation("创建集群")
    @PostMapping(value = "apps/{appId}/envs/{env:\\d+}/clusters/{clusterName}")
    public Result<AppEnvCluster> createCluster(@PathVariable Long appId, @PathVariable String env, @PathVariable String clusterName) {

        if (!InputValidator.isValidClusterNamespace(clusterName)) {
            throw new BadRequestException(String.format("Cluster格式错误: %s", InputValidator.INVALID_CLUSTER_NAMESPACE_MESSAGE));
        }

        AppEnvCluster appEnvCluster = AppEnvCluster.builder().app(new App(appId)).env(env).name(clusterName).build();
        this.appEnvClusterService.createAppEnvCluster(appEnvCluster);
        return Result.ok(appEnvCluster);
    }

    @ApiOperation("删除集群")
    @DeleteMapping(value = "apps/{appId:\\d+}/envs/{env}/clusters/{clusterName:.+}")
    public Result deleteCluster(@PathVariable Long appId, @PathVariable String env, @PathVariable String clusterName) {
        this.appEnvClusterService.deleteAppEnvCluster(AppEnvCluster.builder().app(new App(appId)).env(env).name(clusterName).build());
        return Result.ok();
    }

    @ApiOperation("查询集群信息")
    @GetMapping(value = "apps/{appId:\\d+}/envs/{env}/clusters/{clusterName:.+}")
    public AppEnvCluster loadCluster(@PathVariable("appId") Long appId, @PathVariable String env, @PathVariable("clusterName") String clusterName) {
        AppEnvCluster appEnvCluster = this.appEnvClusterService.getAppEnvCluster(appId, env, clusterName);
        return appEnvCluster;
    }

}
