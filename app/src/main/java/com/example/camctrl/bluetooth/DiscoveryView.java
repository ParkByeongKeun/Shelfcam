package com.example.camctrl.bluetooth;

import android.content.Context;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.example.camctrl.bluetooth.Device;
import com.example.camctrl.R;

/**
 * Created by EOM on 2016-04-10.
 */
public class DiscoveryView extends FrameLayout {
    public DiscoveryView(Context context) {
        super(context);
        init();
    }

    TextView textName, textAddress;
    Device deviseClass;

    private void init(){
        inflate(getContext(), R.layout.view_bluetooth_search, this);

        textName = (TextView)findViewById(R.id.text_name);
        textAddress = (TextView)findViewById(R.id.text_address);
    }

    public void setData(Device deviseClass){
        this.deviseClass = deviseClass;
        textName.setText(deviseClass.name);
        textAddress.setText(deviseClass.address);
    }
}
