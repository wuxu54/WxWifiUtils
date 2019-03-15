package com.wifi.manager;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.util.Log;

import com.wifi.interfaces.WifiStateListener;
import com.wifi.model.WifiBean;
import com.wifi.model.WifiConstant;
import com.wifi.support.WifiListSupport;
import com.wifi.support.WifiSupport;

/**
 * @author: wu.xu
 * @data: 2019/3/14/014.
 * <p>
 * 毫无BUG
 */

public class WifiStateManager {
  private static final String TAG = "WifiManager";

  private WifiStateManager.WifiBroadcastReceiver wifiReceiver;
  private Activity context;

  private boolean canScanWifi = false;

  WifiListSupport wifiListSupport;

  WifiStateListener wifiStateListener;

  public static Build instances(){
    return new Build();
  }

  private WifiStateManager(Build build) {

    this.wifiStateListener = build.wifiStateListener;

    this.context = build.activity;

    this.canScanWifi = wifiStateListener.canScanWifi();

    this.wifiListSupport = new WifiListSupport(context);
  }

  public void scanWifi(){
    this.canScanWifi = wifiStateListener.canScanWifi();
    if (canScanWifi) {
      wifiListSupport.wifiListChange();
      wifiStateListener.wifiListChange(wifiListSupport.getRealWifiList());
    }
  }

  public void changeWifi(){
    this.canScanWifi = wifiStateListener.canScanWifi();
    if (canScanWifi) {
      wifiListSupport.wifiListChange();
      wifiStateListener.wifiListChange(wifiListSupport.getRealWifiList());
    }
  }

  public void registerWifiReceiver(IntentFilter filter) {
    try {
      if (wifiReceiver == null) {
        wifiReceiver = new WifiBroadcastReceiver();
      }
      if (filter==null){
        filter = new IntentFilter();
        filter.addAction(android.net.wifi.WifiManager.WIFI_STATE_CHANGED_ACTION);//监听wifi是开关变化的状态
        filter.addAction(android.net.wifi.WifiManager.NETWORK_STATE_CHANGED_ACTION);//监听wifi连接状态广播,是否连接了一个有效路由
        filter.addAction(android.net.wifi.WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);//监听wifi列表变化（开启一个热点或者关闭一个热点）
        filter.addAction(android.net.wifi.WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);//监听密码连接
      }
      if (context != null)
        this.context.registerReceiver(wifiReceiver, filter);

      if (wifiStateListener.canScanWifi()){
        wifiListSupport.sortScaResult();
      }
    } catch (Exception e) {
      Log.e(TAG, "createWifiBroadcastReceiver-----" + e.toString());
    }
  }

  public void unregisterWifiReceiver() {
    try {
      if (wifiReceiver != null && this.context != null) {
        this.context.unregisterReceiver(wifiReceiver);
      }
    } catch (Exception e) {
      Log.e(TAG, "unregisterReceiver-----" + e.toString());
    }
  }


  public void linkWifi(WifiBean wifiBean){
    wifiListSupport.linkWifi(wifiBean);

  }


  //监听wifi状态
  private class WifiBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
      wifiStateListener.onReceive(context, intent);
      switch (intent.getAction()) {
        /**
         * SUPPLICANT_STATE_CHANGED_ACTION    wifi 密码错误
         * WIFI_STATE_CHANGED_ACTION          wifi状态改变
         * NETWORK_STATE_CHANGED_ACTION       网络状态改变
         * SCAN_RESULTS_AVAILABLE_ACTION      wifi列表改变
         * WIFI_STATE_UNKNOWN                 未知
         */
        case android.net.wifi.WifiManager.SUPPLICANT_STATE_CHANGED_ACTION: {
          if (canScanWifi) {
            int linkWifiResult = intent.getIntExtra(android.net.wifi.WifiManager.EXTRA_SUPPLICANT_ERROR, -1);
            if (linkWifiResult == android.net.wifi.WifiManager.ERROR_AUTHENTICATING) {
              boolean b = wifiListSupport.clearConfig();
              if (b) {
                wifiStateListener.wifiPasswordError();
              }
            }
          }
          break;
        }
        case android.net.wifi.WifiManager.WIFI_STATE_CHANGED_ACTION: {
          int state = intent.getIntExtra(android.net.wifi.WifiManager.EXTRA_WIFI_STATE, 0);
          switch (state) {
            /**
             * WIFI_STATE_DISABLED    WLAN已经关闭
             * WIFI_STATE_DISABLING   WLAN正在关闭
             * WIFI_STATE_ENABLED     WLAN已经打开
             * WIFI_STATE_ENABLING    WLAN正在打开
             * WIFI_STATE_UNKNOWN     未知
             */
            case android.net.wifi.WifiManager.WIFI_STATE_DISABLED: {
              Log.d(TAG, "已经关闭");
              break;
            }
            case android.net.wifi.WifiManager.WIFI_STATE_DISABLING: {
              Log.d(TAG, "正在关闭");
              break;
            }
            case android.net.wifi.WifiManager.WIFI_STATE_ENABLED: {
              Log.d(TAG, "已经打开");
              if (canScanWifi) {
                WifiSupport.scanWifi(context);
                wifiStateListener.wifiListChange(wifiListSupport.sortScaResult());
              }
              break;
            }
            case android.net.wifi.WifiManager.WIFI_STATE_ENABLING: {
              Log.d(TAG, "正在打开");
              break;
            }
            case android.net.wifi.WifiManager.WIFI_STATE_UNKNOWN: {
              Log.d(TAG, "未知状态");
              break;
            }
          }
          break;
        }
        case android.net.wifi.WifiManager.NETWORK_STATE_CHANGED_ACTION: {
          NetworkInfo info = intent.getParcelableExtra(android.net.wifi.WifiManager.EXTRA_NETWORK_INFO);
          if (NetworkInfo.State.DISCONNECTED == info.getState()) {//wifi没连接上
            wifiStateListener.wifiNetWorkState(WifiConstant.STATE_DISCONNECTED);
            if (canScanWifi) {
              wifiListSupport.setListDisconnect();
              wifiStateListener.wifiListChange(wifiListSupport.getRealWifiList());
            }
          } else if (NetworkInfo.State.CONNECTED == info.getState()) {//wifi连接上了
            wifiStateListener.wifiNetWorkState(WifiConstant.STATE_CONNECTED);
            if (canScanWifi) {
              WifiInfo connectedWifiInfo = WifiSupport.getConnectedWifiInfo(context);
              //连接成功 跳转界面 传递ip地址
              wifiListSupport.wifiListSet(connectedWifiInfo.getSSID(), 1);
              wifiStateListener.wifiListChange(wifiListSupport.getRealWifiList());
            }
          } else {
            NetworkInfo.DetailedState state = info.getDetailedState();
            if (state == state.CONNECTING) {
              wifiStateListener.wifiNetWorkState(WifiConstant.STATE_CONNECTING);
              if (canScanWifi) {
                WifiInfo connectedWifiInfo = WifiSupport.getConnectedWifiInfo(context);
                wifiListSupport.wifiListSet(connectedWifiInfo.getSSID(), 2);
                wifiStateListener.wifiListChange(wifiListSupport.getRealWifiList());
              }
            } else if (state == state.AUTHENTICATING) {
              wifiStateListener.wifiNetWorkState(WifiConstant.STATE_AUTHENTICATING);
            } else if (state == state.OBTAINING_IPADDR) {
              wifiStateListener.wifiNetWorkState(WifiConstant.STATE_OBTAINING_IPADDR);
            } else if (state == state.FAILED) {
              wifiStateListener.wifiNetWorkState(WifiConstant.STATE_FAILED);
              if (canScanWifi) {
                WifiSupport.clearConfig(context);
              }
            }
          }
          break;
        }

        case android.net.wifi.WifiManager.SCAN_RESULTS_AVAILABLE_ACTION: {
          Log.d(TAG, "网络列表变化了");
          if (canScanWifi) {
            wifiListSupport.wifiListChange();
            wifiStateListener.wifiListChange(wifiListSupport.getRealWifiList());
          }
          break;
        }
        case android.net.wifi.WifiManager.EXTRA_SUPPLICANT_CONNECTED: {
        }
        break;
        default:
          break;
      }
    }
  }


  public static class Build {
    public Activity activity;

    public WifiStateListener wifiStateListener;

    public Build setActivity(Activity activity) {
      this.activity = activity;
      return this;
    }

    public Build setWifiStateListener(WifiStateListener wifiStateListener) {
      this.wifiStateListener = wifiStateListener;
      return this;
    }

    public WifiStateManager build() {
      return new WifiStateManager(this);
    }

  }


}
