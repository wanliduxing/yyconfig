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
package com.yofish.apollo.dto;

import com.yofish.apollo.bo.ItemChangeSets;
import com.yofish.apollo.component.txtresolver.ConfigChangeContentBuilder;
import com.yofish.apollo.model.vo.NamespaceIdentifier;
import common.dto.ItemDTO;
import lombok.Data;

import java.util.LinkedList;
import java.util.List;

/**
 * @author rache
 * @date 2020-01-02
 */
@Data
public class ItemChangeSetDto {
    private NamespaceIdentifier namespace;
    private ConfigChangeContentBuilder diffs;
    private String extInfo;

}
