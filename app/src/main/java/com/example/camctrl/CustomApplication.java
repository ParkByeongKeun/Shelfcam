package com.example.camctrl;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class CustomApplication extends Application {

    private static CustomApplication instance;
    public String[] imgList;
    public String strDeviceWiFi;
    public String strAddressCtoS;
    public String strAddressDtoS;
    private SharedPreferences mPref;
    private final String PREF_NAME = "camctrl_preference";
    ArrayList<String> arrAddressCtoS;

    @Override
    public void onCreate() {
        super.onCreate();
        mPref = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        arrAddressCtoS = getStringArrayPref(this,"arrAddressCtoS");
        instance = this;
        setPref();
    }
//120 8080
    public void setPref() {
        strDeviceWiFi = mPref.getString("wifi","iptime5G_dev2G");
        //107
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

    private ArrayList<String> getStringArrayPref(Context context, String key) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String json = prefs.getString(key, null);
        ArrayList<String> urls = new ArrayList<String>();
        if (json != null) {
            try {
                JSONArray a = new JSONArray(json);
                for (int i = 0; i < a.length(); i++) {
                    String url = a.optString(i);
                    urls.add(url);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return urls;
    }
}
