package com.wifi.ztest;

import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.wifi.R;
import com.wifi.model.WifiBean;
import com.wifi.model.WifiConstant;

import java.util.ArrayList;
import java.util.List;


public class WifiListAdapter extends RecyclerView.Adapter<WifiListAdapter.MyViewHolder> {
  private List<WifiBean> data = new ArrayList<>();
  private int layoutResId;
  private WifiListAdapter adapter;

  public WifiListAdapter( int layoutResId, @Nullable List<WifiBean> data) {
    this.data.addAll(data);
    this.layoutResId = layoutResId;
    this.adapter = this;
  }

  public void replaceData(@Nullable List<WifiBean> data) {
      this.data.clear();
      this.data.addAll(data);
      notifyDataSetChanged();
  }


  private int getLevel(int level) {
    switch (level) {
      case 4:
        return R.drawable.wifi_icon0;
      case 3:
        return R.drawable.wifi_icon1;
      case 2:
        return R.drawable.wifi_icon2;
      case 1:
        return R.drawable.wifi_icon3;
    }
    return R.drawable.wifi_icon3;
  }

  @Override
  public WifiListAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext()).inflate(layoutResId,parent,false);
    MyViewHolder holder = new MyViewHolder(view);

    return holder;
  }

  @Override
  public void onBindViewHolder(WifiListAdapter.MyViewHolder helper, final int position) {
    final WifiBean bean = data.get(position);
    final View view = helper.getView();
    view.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        clickListener.onItemClick(position,view,bean);
      }
    });

    ((TextView) helper.getView(R.id.tv_item_wifi_name)).setText(bean.getWifiName());

    View flLinkingIv = helper.getView(R.id.fl_link_iv);
    ImageView ivHook = (ImageView) helper.getView(R.id.iv_hook);
    ImageView ivLock = (ImageView) helper.getView(R.id.iv_lock);
    ImageView ivWifi = (ImageView) helper.getView(R.id.iv_wifi);
    View progressBar = helper.getView(R.id.pb_linking);
    //可以传递给adapter的数据都是经过处理的，已连接或者正在连接状态的wifi都是处于集合中的首位，所以可以写出如下判断
    if (position == 0 && (WifiConstant.WIFI_STATE_CONNECT.equals(bean.getState()) || WifiConstant.WIFI_STATE_ON_CONNECTING.equals(bean.getState()))) {
      flLinkingIv.setVisibility(View.VISIBLE);

      if (WifiConstant.WIFI_STATE_CONNECT.equals(bean.getState())) {
        ivHook.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
      } else {
        progressBar.setVisibility(View.VISIBLE);
        ivHook.setVisibility(View.GONE);
      }
    } else {
      flLinkingIv.setVisibility(View.INVISIBLE);
    }

    ivLock.setVisibility(bean.getLock() ? View.VISIBLE : View.GONE);
    ivWifi.setImageResource(getLevel(bean.getLevel()));
  }

  @Override
  public int getItemCount() {
    return data == null ? 0 : data.size();
  }

  static class MyViewHolder extends RecyclerView.ViewHolder {
    View itemView;

    public MyViewHolder(View itemView) {
      super(itemView);
      this.itemView = itemView;
    }

    public View getView() {
      return itemView;
    }

    public View getView(@IdRes int id) {
      return itemView.findViewById(id);
    }
  }

  OnItemClickListener clickListener;
  public void setItemClickListener(OnItemClickListener clickListener){
    this.clickListener = clickListener;
  }

  public interface  OnItemClickListener{
    void onItemClick(int position, View view, WifiBean bean);
  }
}
