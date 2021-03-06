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
package com.yofish.apollo.controller;

import com.google.common.base.Splitter;
import com.yofish.apollo.domain.Instance;
import com.yofish.apollo.dto.InstanceDTO;
import com.yofish.apollo.dto.InstanceNamespaceReq;
import com.yofish.apollo.service.InstanceService;
import com.yofish.apollo.service.ReleaseService;
import com.yofish.apollo.util.PageQuery;
import com.youyu.common.api.Result;

import com.youyu.common.exception.BizException;
import com.youyu.common.utils.YyAssert;
import common.dto.PageDTO;
import framework.apollo.core.enums.Env;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("instances")
public class InstanceController {
    @Autowired
    private InstanceService instanceService;

    private static final Splitter RELEASES_SPLITTER = Splitter.on(",").omitEmptyStrings().trimResults();

    @Autowired
    private ReleaseService releaeService;


    @ApiOperation("使用最新配置实例")
    @PostMapping(value = "by-release")
    public Result<PageDTO<InstanceDTO>> getByRelease(@RequestBody PageQuery<Long> releasePage) {
        Pageable pageable = PageRequest.of(releasePage.getPageNo(), releasePage.getPageSize());
        //最新的releaseId
        Long releaseId4Lastest = releasePage.getData();
        PageDTO<InstanceDTO> instances = instanceService.getByRelease(releaseId4Lastest, pageable);
        return Result.ok(instances);
    }


    @ApiOperation("所有实例")
    @PostMapping(value = "by-namespace")
    public Result<PageDTO<InstanceDTO>> getByNamespace(@RequestBody PageQuery<InstanceNamespaceReq> instancePageQuery) {
        Pageable pageable = PageRequest.of(instancePageQuery.getPageNo(), instancePageQuery.getPageSize());
        return Result.ok(instanceService.findInstancesByNamespace(instancePageQuery.getData().getNamespaceId(), pageable));

    }

    @ApiOperation("所有实例数")
    @RequestMapping(value = "/namespaceId/{namespaceId}/count", method = RequestMethod.GET)
    public Result<Number> getInstanceCountByNamespace(@PathVariable Long namespaceId) {

        int count = instanceService.getInstanceCountByNamepsace(namespaceId);
        return Result.ok(count);
    }

    @ApiOperation("使用的非最新配置的实例")
    @RequestMapping(value = "/namespaceId/{namespaceId}/releaseIds/{releaseIds}/by-namespace-and-releases-not-in", method = RequestMethod.GET)
    public Result<List<InstanceDTO>> getByReleasesNotIn(@RequestParam Long namespaceId, @RequestParam String releaseIds) {

        Set<Long> releaseIdSet = RELEASES_SPLITTER.splitToList(releaseIds).stream().map(Long::parseLong).collect(Collectors.toSet());

        if (CollectionUtils.isEmpty(releaseIdSet)) {
            throw new BizException("release ids can not be empty");
        }

        return Result.ok(instanceService.getByReleasesNotIn(namespaceId, releaseIdSet));
    }


}
