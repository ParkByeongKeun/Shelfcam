package com.example.camctrl;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

public class SettingClientToServer extends Activity {

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
        tvServerAddress.setText("Edit Address (client to server) ex: 192.168.111.222:8080");
        etNewAddress.setHint("");
        etNewAddress.setText(customApplication.strAddressCtoS);
        imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        findViewById(R.id.rlRoot).setOnClickListener(v -> imm.hideSoftInputFromWindow(etNewAddress.getWindowToken(), 0));
//        tvName.setText(customApplication.getUser().getCompany());
        tvWeb.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.ijoon.net/"));
            startActivity(intent);
        });
        btnUpdateName.setOnClickListener(v -> {
            if(!etNewAddress.getText().toString().equals("")) {
                SharedPreferences.Editor editor = customApplication.getPref().edit();
                editor.putString("addressCtoS", etNewAddress.getText().toString());
                editor.apply();
                customApplication.setPref();
                Toast.makeText(getApplicationContext(),"Success",Toast.LENGTH_SHORT).show();
                finish();

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
//    void updateCompany(String newCompanyName) {
//        new Thread(() -> {
//            Call<ResponseUpdateUser> call = RetrofitClient
//                    .getApiService()
//                    .updateUser(customApplication.getToken(),
//                            customApplication.getUser().getName(),
//                            customApplication.getUser().getSex(),
//                            newCompanyName);
//            call.enqueue(new Callback<ResponseUpdateUser>() {
//                @Override
//                public void onResponse(Call<ResponseUpdateUser> call, Response<ResponseUpdateUser> response) {
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
//                }
//                @Override
//                public void onFailure(Call<ResponseUpdateUser> call, Throwable t) {
//                    Log.d("yot132","ResponseUpdateUser = " + t);
//                }
//            });
//        }).start();
//    }
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