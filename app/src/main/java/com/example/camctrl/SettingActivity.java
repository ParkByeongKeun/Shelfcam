package com.example.camctrl;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.Html;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;

import com.example.camctrl.bluetooth.DiscoveryBluetoothDeviceActivity;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SettingActivity extends Activity {

    CustomApplication mCustomApplication;
    View view;
    RelativeLayout mRlDeviceWiFi;
    RelativeLayout mRlAddressCtoS;
    RelativeLayout mRlAddressDtoS;
    TextView mTvDeviceWiFi;
    TextView mTvAddressCtoS;
    TextView mTvAddressDtoS;
    private long mLastClickTime = 0;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        mCustomApplication = (CustomApplication) getApplicationContext();

        mRlDeviceWiFi = findViewById(R.id.rlDeviceWiFi);
        mRlAddressCtoS = findViewById(R.id.rlAddressClientToServer);
        mRlAddressDtoS = findViewById(R.id.rlAddressDeviceToServer);
        mTvDeviceWiFi = findViewById(R.id.tvDeviceWiFi);
        mTvAddressCtoS = findViewById(R.id.tvAddressCtoS);
        mTvAddressDtoS = findViewById(R.id.tvAddressDtoS);

//        view = getWindow().getDecorView();
//        view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            getWindow().setStatusBarColor(Color.parseColor("#f2f2f2"));
//        }
//        ActionBar actionBar = this.getSupportActionBar();
//        if (actionBar != null) {
//            actionBar.setDisplayHomeAsUpEnabled(true);
//            actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back);
//            actionBar.setTitle(Html.fromHtml("<font color='#000000'>" + getResources().getString(R.string.textSetting) + "</font>"));
//        }
        mRlDeviceWiFi.setOnClickListener(v -> {
            if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                return;
            }
            mLastClickTime = SystemClock.elapsedRealtime();
            Intent intent = new Intent(SettingActivity.this, DiscoveryBluetoothDeviceActivity.class);
            startActivity(intent);


//            showLogoutAlertDialog(getResources().getString(R.string.textAreYouSureWantToLogout));
        });
        mRlAddressCtoS.setOnClickListener(v -> {
            if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                return;
            }
            mLastClickTime = SystemClock.elapsedRealtime();
            Intent intent = new Intent(SettingActivity.this,SettingClientToServer.class);
            startActivity(intent);
        });
        mRlAddressDtoS.setOnClickListener(v -> {
            if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                return;
            }
            mLastClickTime = SystemClock.elapsedRealtime();
            Intent intent = new Intent(SettingActivity.this,SettingDeviceToServer.class);
            startActivity(intent);
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        mTvDeviceWiFi.setText(mCustomApplication.strDeviceWiFi);
        mTvAddressCtoS.setText(mCustomApplication.strAddressCtoS);
        mTvAddressDtoS.setText(mCustomApplication.strAddressDtoS);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }

}