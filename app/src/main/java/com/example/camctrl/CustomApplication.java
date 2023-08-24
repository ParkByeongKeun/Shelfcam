package com.example.camctrl;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.util.Log;

public class CustomApplication extends Application {

    private static CustomApplication instance;
    public String[] imgList;
    public String strDeviceWiFi;
    public String strAddressCtoS;
    public String strAddressDtoS;
    private SharedPreferences mPref;
    private final String PREF_NAME = "camctrl_preference";

    @Override
    public void onCreate() {
        super.onCreate();
        mPref = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        instance = this;
        setPref();
    }
//120 8080
    public void setPref() {
        strDeviceWiFi = mPref.getString("wifi","iptime5G_dev2G");
        strAddressCtoS = mPref.getString("addressCtoS","192.168.13.107:8080");
//        strAddressCtoS = "192.168.13.120:8080";
        strAddressDtoS = mPref.getString("addressDtoS","ijoon.iptime.org:18081");
    }

    public static Context getCurrentContext(){
        return instance;
    }


    public SharedPreferences getPref() {
        return mPref;
    }

}
