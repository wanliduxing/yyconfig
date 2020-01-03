package com.yofish.apollo.model.vo;


import com.yofish.apollo.bo.ItemChangeSets;
import lombok.Data;

@Data
public class ItemDiffs {
  private NamespaceIdentifier namespace;
  private ItemChangeSets diffs;
  private String extInfo;

  public ItemDiffs(NamespaceIdentifier namespace) {
    this.namespace = namespace;
  }

  public NamespaceIdentifier getNamespace() {
    return namespace;
  }

  public void setNamespace(NamespaceIdentifier namespace) {
    this.namespace = namespace;
  }

  public ItemChangeSets getDiffs() {
    return diffs;
  }

  public void setDiffs(ItemChangeSets diffs) {
    this.diffs = diffs;
  }

  public String getExtInfo() {
    return extInfo;
  }

  public void setExtInfo(String extInfo) {
    this.extInfo = extInfo;
  }
}
