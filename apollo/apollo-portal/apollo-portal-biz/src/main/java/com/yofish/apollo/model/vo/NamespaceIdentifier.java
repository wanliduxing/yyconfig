package com.yofish.apollo.model.vo;


import com.yofish.apollo.model.model.Verifiable;
import framework.apollo.core.enums.Env;
import common.utils.YyStringUtils;

public class NamespaceIdentifier implements Verifiable {
  private String appId;
  private String env;
  private String clusterName;
  private String namespaceName;

  public String getAppId() {
    return appId;
  }

  public void setAppId(String appId) {
    this.appId = appId;
  }

  public Env getEnv() {
    return Env.valueOf(env);
  }

  public void setEnv(String env) {
    this.env = env;
  }

  public String getClusterName() {
    return clusterName;
  }

  public void setClusterName(String clusterName) {
    this.clusterName = clusterName;
  }

  public String getNamespaceName() {
    return namespaceName;
  }

  public void setNamespaceName(String namespaceName) {
    this.namespaceName = namespaceName;
  }


  @Override
  public boolean isInvalid() {
    return YyStringUtils.isContainEmpty(env, clusterName, namespaceName);
  }

  @Override
  public String toString() {
    return "NamespaceIdentifer{" +
        "appId='" + appId + '\'' +
        ", env='" + env + '\'' +
        ", clusterName='" + clusterName + '\'' +
        ", namespaceName='" + namespaceName + '\'' +
        '}';
  }
}
