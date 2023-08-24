package com.example.camctrl.bluetooth;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.example.camctrl.bluetooth.Device;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by EOM on 2016-04-10.
 */
public class DiscoveryAdapter extends BaseAdapter{

    List<Device> items = new ArrayList<Device>();

    public void add(Device deviseClass){
        items.add(deviseClass);
        notifyDataSetChanged();

    }

    public void clear(){
        items.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        DiscoveryView view;
        if (convertView == null) {
            view = new DiscoveryView(parent.getContext());
        } else {
            view = (DiscoveryView)convertView;
        }
        view.setData(items.get(position));
        return view;
    }
}
