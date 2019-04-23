package com.wifi;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import com.wifi.interfaces.WifiStateListener;
import com.wifi.manager.WifiStateManager;
import com.wifi.model.WifiBean;
import com.wifi.support.WifiPermissionSupport;
import com.wifi.ztest.WifiListAdapter;

import java.util.List;


public class MainActivity extends AppCompatActivity implements WifiStateListener {
  int requestCode = 111;
  int GPS_REQUEST_CODE = 123;
  boolean needScanWifi = true;
  WifiStateManager wifiStateManager;
  RecyclerView rcWifi;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    if (needScanWifi) {
      WifiPermissionSupport.getInstance().check(GPS_REQUEST_CODE, this, requestCode);
    }


    wifiStateManager = WifiStateManager.instances().setActivity(this).setWifiStateListener(this).build();

    wifiStateManager.registerWifiReceiver(null);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == this.GPS_REQUEST_CODE){
      if (WifiPermissionSupport.getInstance().checkActivityResult(this, this.requestCode)) {
        wifiStateManager.scanWifi();
      }
    }
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    if (WifiPermissionSupport.getInstance().checkPermissionResult(grantResults)) {
      wifiStateManager.scanWifi();
    } else {
      //这里可显示跳转设置页面提示
      Toast.makeText(this, "需要开启位置权限 ，才可以扫描wifi列表哦~ ", Toast.LENGTH_SHORT).show();
    }
  }


  @Override
  protected void onDestroy() {
    super.onDestroy();
    wifiStateManager.unregisterWifiReceiver();
  }

  @Override
  public boolean canScanWifi() {
    Toast.makeText(this, "canScanWifi----- "+WifiPermissionSupport.getInstance().isCanScanWifi(), Toast.LENGTH_SHORT).show();
    return WifiPermissionSupport.getInstance().isCanScanWifi();
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
