package com.example.camctrl.bluetooth;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.example.camctrl.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by EOM on 2016-04-10.
 */
public class BluetoothDeviceAdapter extends RecyclerView.Adapter<BluetoothDeviceViewHolder> {

    List<Device> items = new ArrayList<Device>();

    public void addAll(Set<Device> devises){
        items.addAll(devises);
    }

    public void add(Device data){
        items.add(data);
        notifyDataSetChanged();
    }

    public void clear(){
        items.clear();
        notifyDataSetChanged();
    }

    @Override
    public BluetoothDeviceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.view_bluetooth_paired, parent, false);
        BluetoothDeviceViewHolder holder = new BluetoothDeviceViewHolder(view);

        return holder;
    }

    @Override
    public void onBindViewHolder(BluetoothDeviceViewHolder holder, int position) {
        holder.setData(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
