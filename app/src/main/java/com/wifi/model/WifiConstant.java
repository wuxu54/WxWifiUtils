package com.wifi.model;

public class WifiConstant {
  public static final int STATE_CONNECTED = 111;//"连接成功..."
  public static final int STATE_DISCONNECTED = 112;//"未连接上..."
  public static final int STATE_CONNECTING = 113;//"连接中..."
  public static final int STATE_AUTHENTICATING = 114;//"正在验证身份信息..."
  public static final int STATE_OBTAINING_IPADDR = 115;//"正在获取IP地址..."
  public static final int STATE_FAILED = 116;//"连接失败"

  public static final String WIFI_STATE_CONNECT = "已连接";
  public static final String WIFI_STATE_ON_CONNECTING = "正在连接";
  public static final String WIFI_STATE_UNCONNECT = "未连接";

  public static final String WIFI_SSID_PREFIX= "gvmedia";//wifi识别SSID前缀



}
