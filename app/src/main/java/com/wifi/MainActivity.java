package com.wifi;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import com.wifi.interfaces.WifiStateListener;
import com.wifi.manager.WifiStateManager;
import com.wifi.model.WifiBean;
import com.wifi.ztest.WifiListAdapter;

import java.util.List;

import static android.support.v4.content.PermissionChecker.PERMISSION_GRANTED;


public class MainActivity extends AppCompatActivity implements WifiStateListener {

  String[] permissions = new String[]{
          Manifest.permission.ACCESS_COARSE_LOCATION,
          Manifest.permission.ACCESS_FINE_LOCATION,
  };

  boolean needScanWifi = true;
  boolean canScanWifi = needScanWifi;
  WifiStateManager wifiStateManager;
  RecyclerView rcWifi;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    if (needScanWifi) {
      boolean b = checkPermission();
      if (!b) {
        canScanWifi = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
          this.requestPermissions(permissions, 111);
        }
      }
    }


    wifiStateManager = WifiStateManager.instances().setActivity(this).setWifiStateListener(this).build();

    wifiStateManager.registerWifiReceiver(null);
  }

  public boolean checkPermission() {
    boolean result = true;
    for (String p : permissions) {
      if (ContextCompat.checkSelfPermission(this, p) != PERMISSION_GRANTED) {
        result = false;
        break;
      }
    }

    return result;
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    if (requestCode == 111) {
      boolean result = true;
      for (int p : grantResults) {
        if (p != PERMISSION_GRANTED) {
          result = false;
          break;
        }
      }


      if (needScanWifi) {
        if (result) {
          canScanWifi = true;
          wifiStateManager.scanWifi();
        } else {
          //这里可显示跳转设置页面提示
          Toast.makeText(this, "需要开启gps定位 ，才可以扫描wifi列表哦~", Toast.LENGTH_SHORT).show();
        }
      }
    }
  }


  @Override
  protected void onDestroy() {
    super.onDestroy();
    wifiStateManager.unregisterWifiReceiver();
  }

  @Override
  public boolean canScanWifi() {
    return canScanWifi;
  }

  @Override
  public void wifiPasswordError() {

  }

  @Override
  public void wifiListChange(List<WifiBean> wifiBeanList) {
    if (rcWifi == null) {
      rcWifi = findViewById(R.id.rc_wifi);
      LinearLayoutManager layoutManager = new LinearLayoutManager(this);
      layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
      WifiListAdapter wifiListAdapter = new WifiListAdapter(R.layout.item_wifi, wifiBeanList);
      wifiListAdapter.setItemClickListener(new WifiListAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(int position, View view, WifiBean bean) {
          wifiStateManager.linkWifi(bean);
        }
      });
      rcWifi.setLayoutManager(layoutManager);
      rcWifi.setAdapter(wifiListAdapter);

    } else {
      WifiListAdapter adapter = (WifiListAdapter) rcWifi.getAdapter();
      adapter.replaceData(wifiBeanList);
    }


    if (wifiBeanList != null) {
      Toast.makeText(this, " wifiBeanList " + wifiBeanList.size(), Toast.LENGTH_SHORT
      ).show();
    }
  }

  @Override
  public void wifiNetWorkState(int wifiState) {
    Toast.makeText(this, " wifiState " + wifiState, Toast.LENGTH_SHORT
    ).show();
  }

  @Override
  public void onReceive(Context context, Intent intent) {

  }
}
