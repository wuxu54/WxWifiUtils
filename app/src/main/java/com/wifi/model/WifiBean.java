package com.wifi.model;

/**
 * WIFI 数据类
 */
public class WifiBean implements Comparable<WifiBean> {
  private String wifiName;
  private int level;
  private String state;  //已连接  正在连接  未连接 三种状态
  private String capabilities;//加密方式
  private boolean lock;//连接时是否需要密码

  public boolean getLock() {
    return lock;
  }

  public void setLock(boolean lock) {
    this.lock = lock;
  }

  @Override
  public String toString() {
    return "WifiBean{" +
            "wifiName='" + wifiName + '\'' +
            ", level='" + level + '\'' +
            ", state='" + state + '\'' +
            ", capabilities='" + capabilities + '\'' +
            ", lock=" + lock +
            '}';
  }

  public String getCapabilities() {
    return capabilities;
  }

  public void setCapabilities(String capabilities) {
    this.capabilities = capabilities;
  }

  public String getWifiName() {
    return wifiName;
  }

  public void setWifiName(String wifiName) {
    this.wifiName = wifiName;
  }

  public int getLevel() {
    return level;
  }

  public void setLevel(int level) {
    this.level = level;
  }

  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }

  @Override
  public int compareTo(WifiBean o) {
    int level1 = this.getLevel();
    int level2 = o.getLevel();
    return level1 - level2;
  }
}

