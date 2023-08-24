package com.example.camctrl.bluetooth;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.camctrl.R;

/**
 * Created by EOM on 2016-04-10.
 */
public class BluetoothDeviceViewHolder extends RecyclerView.ViewHolder {


    TextView textName;
    TextView textAddress;
    Device devise;

    public BluetoothDeviceViewHolder(View itemView) {
        super(itemView);

        textName = (TextView)itemView.findViewById(R.id.text_name);
        textAddress = (TextView)itemView.findViewById(R.id.text_address);

    }

    public void setData(Device devise){
        this.devise = devise;

        textName.setText(devise.name);
        textAddress.setText(devise.address);
    }
}
