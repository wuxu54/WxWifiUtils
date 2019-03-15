package com.wifi.support;

import android.app.Activity;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;

import com.wifi.R;
import com.wifi.model.WifiBean;
import com.wifi.model.WifiConstant;
import com.wifi.dialog.WifiLinkDialog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author: wu.xu
 * @data: 2019/3/14/014.
 * <p>
 * 毫无BUG
 */

public class WifiListSupport {
  WifiLinkDialog linkDialog;
  Activity context;
  List<WifiBean> realWifiList = new ArrayList<>();

  int connectType;

  public WifiListSupport(Activity activity) {
    this.context = activity;
  }

  public List<WifiBean> getRealWifiList(){
    return realWifiList;
  }


  public void setListDisconnect() {
    if (realWifiList.size() > 0) {
      for (int i = 0; i < realWifiList.size(); i++) {//没连接上将 所有的连接状态都置为“未连接”
        realWifiList.get(i).setState(WifiConstant.WIFI_STATE_UNCONNECT);
      }
    }
  }

  /**
   * //网络状态发生改变 调用此方法！
   */
  public void wifiListChange() {
    sortScaResult();
    WifiInfo connectedWifiInfo = WifiSupport.getConnectedWifiInfo(context);
    if (connectedWifiInfo != null) {
      wifiListSet(connectedWifiInfo.getSSID(), connectType);
    }
  }

  /**
   * 将"已连接"或者"正在连接"的wifi热点放置在第一个位置
   *
   * @param wifiName
   * @param type
   */
  public List<WifiBean> wifiListSet(String wifiName, int type) {
    int index = -1;
    WifiBean wifiInfo = new WifiBean();
    connectType=type;
    if (isNullOrEmpty(realWifiList)) {
      return realWifiList;
    }
    for (int i = 0; i < realWifiList.size(); i++) {
      realWifiList.get(i).setState(WifiConstant.WIFI_STATE_UNCONNECT);
    }
    Collections.sort(realWifiList);//根据信号强度排序
    for (int i = 0; i < realWifiList.size(); i++) {
      WifiBean wifiBean = realWifiList.get(i);
      if (index == -1 && ("\"" + wifiBean.getWifiName() + "\"").equals(wifiName)) {
        index = i;
        wifiInfo.setLevel(wifiBean.getLevel());
        wifiInfo.setWifiName(wifiBean.getWifiName());
        wifiInfo.setCapabilities(wifiBean.getCapabilities());
        if (type == 1) {
          wifiInfo.setState(WifiConstant.WIFI_STATE_CONNECT);
        } else {
          wifiInfo.setState(WifiConstant.WIFI_STATE_ON_CONNECTING);
        }
      }
    }
    if (index != -1) {
      realWifiList.remove(index);
      realWifiList.add(0, wifiInfo);
      //adapter.notifyDataSetChanged();
    }
    return realWifiList;
  }

  /**
   * 获取wifi列表然后将bean转成自己定义的WifiBean
   */
  public List<WifiBean> sortScaResult() {
    List<ScanResult> scanResults = WifiSupport.noSameName(WifiSupport.getWifiScanResult(context));
    realWifiList.clear();
    if (!isNullOrEmpty(scanResults)) {
      for (int i = 0; i < scanResults.size(); i++) {
        WifiBean wifiBean = new WifiBean();
        wifiBean.setWifiName(scanResults.get(i).SSID);
        wifiBean.setState(WifiConstant.WIFI_STATE_UNCONNECT);   //只要获取都假设设置成未连接，真正的状态都通过广播来确定
        wifiBean.setCapabilities(scanResults.get(i).capabilities);
        wifiBean.setLevel(WifiSupport.getLevel(scanResults.get(i).level));
        wifiBean.setLock(setPwdState(wifiBean));
        realWifiList.add(wifiBean);
        //排序
//        Collections.sort(realWifiList);
      }
    }
    return realWifiList;
  }
  /**
   * 判断是否需要密码
   *
   * @param wifiBean
   * @return
   */
  public boolean setPwdState(WifiBean wifiBean) {
    if (wifiBean == null) return true;
    if (wifiBean.getState().equals(WifiConstant.WIFI_STATE_UNCONNECT) || wifiBean.getState().equals(WifiConstant.WIFI_STATE_CONNECT)) {
      String capabilities = wifiBean.getCapabilities();
      if (WifiSupport.getWifiCipher(capabilities) == WifiSupport.WifiCipherType.WIFICIPHER_NOPASS) {//无需密码
        return false;
      } else {   //需要密码，弹出输入密码dialog
        WifiConfiguration tempConfig = WifiSupport.isExsits(wifiBean.getWifiName(), context);
        if (tempConfig == null) {
          return true;
        } else {
          return false;
        }
      }
    }
    return true;
  }



  /**
   * 密码输入界面
   *
   * @param wifiBean
   */
  public void noConfigurationWifi(WifiBean wifiBean) {//之前没配置过该网络， 弹出输入密码界面
    if (wifiBean == null) return;
    if (null == linkDialog) {
      linkDialog = new WifiLinkDialog(context, R.style.dialog_wifi, wifiBean.getWifiName(), wifiBean.getCapabilities());
    } else {
      linkDialog.setInfo(wifiBean.getWifiName(), wifiBean.getCapabilities());
    }
//    WifiLinkDialog linkDialog = new WifiLinkDialog(context, R.style.dialog_wifi, wifiBean.getWifiName(), wifiBean.getCapabilities());
    if (!linkDialog.isShowing()) {
      linkDialog.show();
    }
  }

  public boolean clearConfig(){
    for (int i = 0; i < realWifiList.size(); i++) {
      if (null != realWifiList) {
        WifiBean wifiBean = realWifiList.get(i);
        WifiConfiguration config = WifiSupport.getConfig();
        if (null != wifiBean && null != config) {
          String wifiName = "\"" + wifiBean.getWifiName() + "\"";
          if (config.SSID.equals(wifiName)) {
            WifiSupport.clearConfig(context);
            return true;
          }
        }
      }
    }

    return false;
  }

  /**
   * wifi连接
   *
   * @param wifiBean
   */
  public void linkWifi(WifiBean wifiBean) {
    if (wifiBean == null) return;
    if (wifiBean.getState().equals(WifiConstant.WIFI_STATE_UNCONNECT) || wifiBean.getState().equals(WifiConstant.WIFI_STATE_CONNECT)) {
      String capabilities = wifiBean.getCapabilities();
      if (WifiSupport.getWifiCipher(capabilities) == WifiSupport.WifiCipherType.WIFICIPHER_NOPASS) {//无需密码
        WifiConfiguration tempConfig = WifiSupport.isExsits(wifiBean.getWifiName(), context);
        if (tempConfig == null) {
          WifiConfiguration exits = WifiSupport.createWifiConfig(wifiBean.getWifiName(), null, WifiSupport.WifiCipherType.WIFICIPHER_NOPASS);
          WifiSupport.addNetWork(exits, context);
        } else {
          WifiSupport.addNetWork(tempConfig, context);
        }
      } else {   //需要密码，弹出输入密码dialog
        WifiConfiguration tempConfig = WifiSupport.isExsits(wifiBean.getWifiName(), context);
        if (tempConfig == null) {
          noConfigurationWifi(wifiBean);
        } else {
          WifiSupport.addNetWork(tempConfig, context);
        }
      }
    }
  }

  /**
   * 判断集合是否为null或者0个元素
   *
   * @param c
   * @return
   */
  private   boolean isNullOrEmpty(Collection c) {
    if (null == c || c.isEmpty()) {
      return true;
    }
    return false;
  }
}
