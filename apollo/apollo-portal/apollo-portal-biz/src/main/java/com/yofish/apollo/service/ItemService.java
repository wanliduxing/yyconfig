package com.yofish.apollo.service;

import com.yofish.apollo.bo.ItemChangeSets;
import com.yofish.apollo.component.txtresolver.ConfigTextResolver;
import com.yofish.apollo.domain.AppEnvClusterNamespace;
import com.yofish.apollo.domain.Commit;
import com.yofish.apollo.domain.Item;
import com.yofish.apollo.dto.CreateItemReq;
import com.yofish.apollo.dto.UpdateItemReq;
import com.yofish.apollo.enums.Envs;
import com.yofish.apollo.model.NamespaceTextModel;
import com.yofish.apollo.repository.AppEnvClusterNamespaceRepository;
import com.yofish.apollo.repository.CommitRepository;
import com.yofish.apollo.repository.ItemRepository;
import framework.apollo.core.enums.ConfigFileFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.alibaba.fastjson.JSON.toJSONString;

/**
 * @Author: xiongchengwei
 * @Date: 2019/11/18 下午2:48
 */

@Service
public class ItemService {

    @Autowired
    private AppEnvClusterNamespaceRepository appEnvClusterNamespaceRepository;


    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    @Qualifier("fileTextResolver")
    private ConfigTextResolver fileTextResolver;

    @Autowired
    private CommitRepository commitRepository;

    @Autowired
    @Qualifier("propertyResolver")
    private ConfigTextResolver propertyResolver;

    public void createItem(CreateItemReq createItemReq) {
        Item item = new Item(createItemReq);
        itemRepository.save(item);
    }


    public void updateItem(UpdateItemReq updateItemReq) {
        Item item = new Item(updateItemReq);
        itemRepository.save(item);
    }


    public void updateConfigItemByText(NamespaceTextModel model) {

        String appId = model.getAppId();
        Envs env = model.getEnvs();
        String clusterName = model.getClusterName();
        String namespaceName = model.getNamespaceName();
        long namespaceId = model.getNamespaceId();
        String configText = model.getConfigText();

        ConfigTextResolver resolver = findResolver(model.getFormat());

//        List<Item> items = itemRepository.findItemsByClusterNamespace_IdNamespaceAndClusterNamespace_NameNamespace(namespaceId, env.name());
        List<Item> items = null;
        ItemChangeSets changeSets = resolver.resolve(namespaceId, configText, items);
        if (changeSets.isEmpty()) {
            return;
        }

//        AppEnvClusterNamespace appEnvClusterNamespace = appEnvClusterNamespaceRepository.findByAppIdAndEnvAndClusterNameAndnamespaceName(appId, env.name(), clusterName, namespaceName);
        AppEnvClusterNamespace appEnvClusterNamespace = null;
        updateItems(appEnvClusterNamespace, changeSets);

        Commit commit = Commit.builder().appEnvClusterNamespace(appEnvClusterNamespace).changeSets(toJSONString(changeSets)).build();
        commitRepository.save(commit);
    }

    private ConfigTextResolver findResolver(ConfigFileFormat fileFormat) {
        return fileFormat == ConfigFileFormat.Properties ? propertyResolver : fileTextResolver;
    }

    private void updateItems(AppEnvClusterNamespace appEnvClusterNamespace, ItemChangeSets changeSets) {

    }


}
