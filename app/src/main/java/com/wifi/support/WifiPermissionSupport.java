package com.wifi.support;

import android.Manifest;
import android.app.Activity;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import static android.support.v4.content.PermissionChecker.PERMISSION_GRANTED;

/**
 * @author: wu.xu
 * @data: 2019/3/18/018.
 * <p>
 * 毫无BUG
 */

public class WifiPermissionSupport {
  String[] permissions = new String[]{
          Manifest.permission.ACCESS_COARSE_LOCATION,
          Manifest.permission.ACCESS_FINE_LOCATION,
  };
  private WifiPermissionSupport(){}

  private  static WifiPermissionSupport wifiPermissionSupport;

  private static synchronized final WifiPermissionSupport newInstance(){
    return new WifiPermissionSupport();
  }

  public static synchronized WifiPermissionSupport getInstance(){
    if (wifiPermissionSupport ==null){
      wifiPermissionSupport = newInstance();
    }
    return  wifiPermissionSupport;
  }

  boolean canScanWifi = false;

  public boolean isCanScanWifi() {
    return canScanWifi;
  }
  public void check(int gpsCode, Activity activity, int requestCode) {
    if (Build.VERSION.SDK_INT >= 23 && !WifiSupport.isOpenGps(activity)) {
      canScanWifi = false;
      WifiSupport.alertOpenGps(gpsCode, activity);
    } else {
      checkWifiPermission(activity, requestCode);
    }
  }


  public boolean checkWifiPermission(Activity activity, int requestCode) {
    canScanWifi= checkPermission(activity);
    if (!canScanWifi) {
      canScanWifi = false;
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        activity.requestPermissions(permissions, requestCode);
      }
    }

    return canScanWifi;
  }

  public boolean checkPermission(Activity activity) {
    boolean result = true;
    for (String p : permissions) {
      if (ContextCompat.checkSelfPermission(activity, p) != PERMISSION_GRANTED) {
        result = false;
        break;
      }
    }

    return result;
  }


  public boolean checkActivityResult(Activity activity, int requestCode) {
    canScanWifi = false;
    if (!WifiSupport.isOpenGps(activity)) {
      Toast.makeText(activity, "需要开启Gps定位，才可以扫描wifi列表哦~", Toast.LENGTH_SHORT).show();
    } else {
      canScanWifi = checkWifiPermission(activity, requestCode);
    }
    return canScanWifi;
  }

  public boolean checkPermissionResult(int[] grantResults) {
    boolean result = true;
    for (int p : grantResults) {
      if (p != PERMISSION_GRANTED) {
        result = false;
        break;
      }
    }
    canScanWifi=result;
    return result;
  }
}
