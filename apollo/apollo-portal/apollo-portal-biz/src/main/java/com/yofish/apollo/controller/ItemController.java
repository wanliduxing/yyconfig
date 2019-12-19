package com.yofish.apollo.controller;

//import com.yofish.apollo.model.model.NamespaceTextModel;
import com.yofish.apollo.domain.Item;
import com.yofish.apollo.dto.CreateItemReq;
import com.yofish.apollo.dto.ItemReq;
import com.yofish.apollo.dto.ModifyItemsByTextsReq;
import com.yofish.apollo.dto.UpdateItemReq;
import com.yofish.apollo.model.NamespaceTextModel;
import com.yofish.apollo.model.model.NamespaceSyncModel;
import com.yofish.apollo.model.vo.ItemDiffs;
import com.yofish.apollo.model.vo.NamespaceIdentifier;
import com.yofish.apollo.service.ItemService;
import com.youyu.common.api.Result;
import common.exception.BadRequestException;
import framework.apollo.core.enums.ConfigFileFormat;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

import static common.utils.RequestPrecondition.checkModel;


@RestController
@RequestMapping("item")
public class ItemController {

  @Autowired
  private ItemService itemService;


  @RequestMapping(value = "/modifyItemsByTexts", method = RequestMethod.POST, consumes = {
      "application/json"})
  public void modifyItemsByText(@RequestBody ModifyItemsByTextsReq model) {

    checkModel(model != null);
    itemService.updateConfigItemByText(model);
  }


  @RequestMapping(value = "/createItem", method = RequestMethod.POST)
  public Result<Item> createItem(@RequestBody CreateItemReq req) {
    Item item= itemService.createItem(req);
    return Result.ok(item);
  }


  @RequestMapping(value = "/updateItem", method = RequestMethod.PUT)
  public Result updateItem(@RequestBody UpdateItemReq req) {
     itemService.updateItem(req);
    return Result.ok();
  }



  @RequestMapping(value = "deleteItem", method = RequestMethod.DELETE)
  public void deleteItem(@RequestBody ItemReq req) {
    if (req.getClusterNamespaceId() <= 0) {
      throw new BadRequestException("item id invalid");
    }
    itemService.deleteItem(req);
  }


  @RequestMapping(value = "/findItems", method = RequestMethod.GET)
  public Result<List<Item>> findItems(@RequestBody ItemReq req) {
    List<Item> items = itemService.findItemsWithoutOrdered(req);
    return Result.ok(items);
  }

//todo 配置同步

  @RequestMapping(value = "updateEnv")
  public Result updateEnv(@PathVariable String appId, @PathVariable String namespaceName,
                                     @RequestBody NamespaceSyncModel model) {
    itemService.syncItems(model.getSyncToNamespaces(), model.getSyncItems());
   return Result.ok();
  }
  @PostMapping(value = "/diff")
  public List<ItemDiffs> diff(@RequestBody NamespaceSyncModel model) {

    List<ItemDiffs> itemDiffs = itemService.compare(model.getSyncToNamespaces(), model.getSyncItems());

    for (ItemDiffs diff : itemDiffs) {
      NamespaceIdentifier namespace = diff.getNamespace();
      if (namespace == null) {
        continue;
      }
    }

    return itemDiffs;
  }
  @PostMapping(value = "syntax-check")
  public ResponseEntity<Void> syntaxCheckText(@RequestBody NamespaceTextModel model) {

    doSyntaxCheck(model);

    return ResponseEntity.ok().build();
  }

  private void doSyntaxCheck(NamespaceTextModel model) {
    if (StringUtils.isBlank(model.getConfigText())) {
      return;
    }

    // only support yaml syntax check
    if (model.getFormat() != ConfigFileFormat.YAML && model.getFormat() != ConfigFileFormat.YML) {
      return;
    }

    // use YamlPropertiesFactoryBean to check the yaml syntax
    YamlPropertiesFactoryBean yamlPropertiesFactoryBean = new YamlPropertiesFactoryBean();
    yamlPropertiesFactoryBean.setResources(new ByteArrayResource(model.getConfigText().getBytes()));
    // this call converts yaml to properties and will throw exception if the conversion fails
    yamlPropertiesFactoryBean.getObject();
  }


}
