package com.example.camctrl;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.camctrl.bluetooth.DiscoveryBluetoothDeviceActivity;

import java.util.ArrayList;

public class SettingDeviceWiFi extends Activity {

    CustomApplication customApplication;
    TextView tvName;
    EditText etNewSSID;
    EditText etNewPassword;
    TextView tvWeb;
    Button btnUpdateName;
    InputMethodManager imm;
    RelativeLayout rlProgress;
    BluetoothAdapter mBluetoothAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_device_wifi);
        customApplication = (CustomApplication)getApplication();
        initView();
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter(); //블루투스 어댑터 객체 얻기
        bluetoothOn();
        Intent intent = new Intent(SettingDeviceWiFi.this, DiscoveryBluetoothDeviceActivity.class);
        startActivity(intent);
        etNewSSID = findViewById(R.id.etServerAddress);
        etNewPassword = findViewById(R.id.etDeviceAddress);
        etNewSSID.setHint("");
        etNewSSID.setText(customApplication.strDeviceWiFi);
        rlProgress = findViewById(R.id.rlProgress);
        imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        findViewById(R.id.rlRoot).setOnClickListener(v -> imm.hideSoftInputFromWindow(etNewSSID.getWindowToken(), 0));
//        tvName.setText(customApplication.getUser().getCompany());
        tvWeb.setOnClickListener(v -> {
            Intent intent1 = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.ijoon.net/"));
            startActivity(intent1);
        });
        btnUpdateName.setOnClickListener(v -> {
            if(!etNewSSID.getText().toString().equals("") || !etNewPassword.getText().toString().equals("")) {
//                scanLeDevice(true);
//                customApplication.mBluetoothLeService.sendMessage("01iptime5G_dev2G", false);
//
            }else {
                Toast.makeText(getApplicationContext(),"Confirm Information",Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void initView() {
        tvWeb = findViewById(R.id.tvWeb);
        btnUpdateName = findViewById(R.id.btnUpdateUser);
    }

    private static final int REQUEST_ENABLE_BT = 0;

    private void bluetoothOn(){
        if(!mBluetoothAdapter.isEnabled()){ //블루트스 어댑터를 사용가능하게 하기
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }else {
            Toast.makeText(getApplicationContext(), "Already on", Toast.LENGTH_SHORT).show();
        }
    }

    public void bluetoothOff(){
        mBluetoothAdapter.disable();
        Toast.makeText(getApplicationContext(),"Turned off" ,Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        bluetoothOff();
    }
}