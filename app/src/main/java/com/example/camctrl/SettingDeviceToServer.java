package com.example.camctrl;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.camctrl.RestApi.EndpointRequest;
import com.example.camctrl.RestApi.EndpointResponse;
import com.example.camctrl.RestApi.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SettingDeviceToServer extends Activity {

    CustomApplication customApplication;
    TextView tvServerAddress;
    EditText etNewAddress;
    TextView tvWeb;
    Button btnUpdateName;
    InputMethodManager imm;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_address);
        customApplication = (CustomApplication)getApplication();
        initView();
        btnUpdateName = findViewById(R.id.btnUpdateUser);
        etNewAddress = findViewById(R.id.etServerAddress);
        tvServerAddress = findViewById(R.id.tvServerAddress);
        tvServerAddress.setText("Edit Address (device to server) ex: 192.168.111.222:8080");
        etNewAddress.setHint("");
        etNewAddress.setText(customApplication.strAddressDtoS);
        imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        findViewById(R.id.rlRoot).setOnClickListener(v -> imm.hideSoftInputFromWindow(etNewAddress.getWindowToken(), 0));
//        tvName.setText(customApplication.getUser().getCompany());
        tvWeb.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.ijoon.net/"));
            startActivity(intent);
        });
        btnUpdateName.setOnClickListener(v -> {
            if(!etNewAddress.getText().toString().equals("")) {
                updateAddress("http://" + etNewAddress.getText().toString() + "/uploads");
                SharedPreferences.Editor editor = customApplication.getPref().edit();
                editor.putString("addressDtoS", etNewAddress.getText().toString());
                editor.apply();
                customApplication.setPref();
            }else {
                Toast.makeText(getApplicationContext(),"Confirm information",Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void initView() {
//        tvName = findViewById(R.id.tvTitle);
//        etNewName = findViewById(R.id.etRename);
        tvWeb = findViewById(R.id.tvWeb);
        btnUpdateName = findViewById(R.id.btnUpdateUser);
    }
//
    void updateAddress(String newAddress) {
        new Thread(() -> {
//            SharedPreferences sharedPreferences = customApplication.getPref();
            String address = customApplication.strAddressDtoS;
            Call<EndpointResponse> call = RetrofitClient.getApiService("http://" + address + "/")
                    .postRawJson(new EndpointRequest(newAddress));
            call.enqueue(new Callback<EndpointResponse>() {
                @Override
                public void onResponse(Call<EndpointResponse> call, Response<EndpointResponse> response) {
                    Toast.makeText(getApplicationContext(),"Success",Toast.LENGTH_SHORT).show();
                    finish();
//                    ResponseUpdateUser body = response.body();
//                    if(body == null) {
//                        Toast.makeText(getApplicationContext(),getResources().getString(R.string.textCheckInformation),Toast.LENGTH_SHORT).show();
//                        return;
//                    }
//                    Toast.makeText(getApplicationContext(),body.getEnmsg(),Toast.LENGTH_SHORT).show();
//                    if(body.getCode() == 0) {
//                        tvName.setText(newCompanyName);
//                        getUserInfo();
//                    }
                }
                @Override
                public void onFailure(Call<EndpointResponse> call, Throwable t) {
                    Toast.makeText(getApplicationContext(),"not found address",Toast.LENGTH_SHORT).show();
                    Log.d("yot132","Response = " + t);
                }
            });
        }).start();
    }
//
//    void getUserInfo() {
//        new Thread(() -> {
//            Call<ResponseGetUserInfo> call = RetrofitClient
//                    .getApiService()
//                    .getUserInfo(customApplication.getToken());
//            call.enqueue(new Callback<ResponseGetUserInfo>() {
//                @Override
//                public void onResponse(Call<ResponseGetUserInfo> call, Response<ResponseGetUserInfo> response) {
//                    ResponseGetUserInfo body = response.body();
//                    if(body == null) {
//                        Toast.makeText(getApplicationContext(),getResources().getString(R.string.textCheckInformation),Toast.LENGTH_SHORT).show();
//                        return;
//                    }
//                    if(body.getCode() == 0) {
//                        customApplication.setUser(body.getUser());
//                    }
//                    finish();
//                }
//                @Override
//                public void onFailure(Call<ResponseGetUserInfo> call, Throwable t) {
//                    Log.d("yot132","ResponseGetUserInfo = " + t);
//                }
//            });
//        }).start();
//    }
}