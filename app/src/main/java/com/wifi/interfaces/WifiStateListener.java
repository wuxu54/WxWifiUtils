package com.wifi.interfaces;

import android.content.Context;
import android.content.Intent;

import com.wifi.model.WifiBean;

import java.util.List;

/**
 * @author: wu.xu
 * @data: 2019/3/14/014.
 * <p>
 * 毫无BUG
 */

public interface WifiStateListener {
  boolean canScanWifi();

  void wifiPasswordError();

  void wifiListChange(List<WifiBean> wifiBeanList);

  void wifiNetWorkState(int wifiState);//wifi 网络连接状态

  void onReceive(Context context, Intent intent);
}
