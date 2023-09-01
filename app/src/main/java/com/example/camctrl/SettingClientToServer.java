package com.example.camctrl;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.camctrl.utils.PullToRefreshListView;
import com.example.camctrl.utils.RecordListViewAdapter;

import org.json.JSONArray;

import java.util.ArrayList;

public class SettingClientToServer extends Activity {

    CustomApplication customApplication;
    TextView tvServerAddress;
    EditText etNewAddress;
    EditText etSerial;
    TextView tvWeb;
    Button btnUpdateName;
    InputMethodManager imm;
    private PullToRefreshListView mListView;
    RecordListViewAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_to_server);
        customApplication = (CustomApplication)getApplication();

        mListView = findViewById(R.id.layout_listview);
        adapter = new RecordListViewAdapter();
        runOnUiThread(() -> mListView.setAdapter(adapter));

        for(int i = 0 ; i < customApplication.arrAddressCtoS.size() ; i++) {
            String[] spl = customApplication.arrAddressCtoS.get(i).split("#");
            adapter.addItem(spl[1],spl[0]);
        }

        mListView.setOnRefreshListener(() -> {
            updateList();
        });

        findViewById(R.id.btnOK).setOnClickListener(v -> {
            boolean isCheck = false;
            String temp = etSerial.getText().toString() + "#" + etNewAddress.getText().toString();
            for(int i = 0 ; i < customApplication.arrAddressCtoS.size() ; i ++) {
                String[] spl = customApplication.arrAddressCtoS.get(i).split("#");
                if(etNewAddress.getText().toString().equals(spl[1])) {
                    isCheck = true;
                }
                if(etSerial.getText().toString().equals(spl[0])) {
                    isCheck = true;
                }
            }
            if(isCheck) {
                Toast.makeText(getApplicationContext(),"Please check the Address or CAM ID again.",Toast.LENGTH_SHORT).show();
            }else {
                if(!etNewAddress.getText().toString().equals("") && !etSerial.getText().toString().equals("")) {
                    customApplication.arrAddressCtoS.add(temp);
                    adapter.addItem(etNewAddress.getText().toString(),etSerial.getText().toString());
                    setStringArrayPref(SettingClientToServer.this,"arrAddressCtoS",customApplication.arrAddressCtoS);
                    updateList();
                }else {
                    Toast.makeText(getApplicationContext(),"Confirm information",Toast.LENGTH_SHORT).show();
                }
            }
        });

        mListView.setOnItemLongClickListener((parent, view, position, id) -> {
            showDeleteRegistererAlertDialog(customApplication.arrAddressCtoS.get(position) + " Are you sure you want to delete?",position);
            return true;
        });

        mListView.setOnItemClickListener((parent, view, position, id) -> {
            if(customApplication.arrAddressCtoS.isEmpty()) {
                return;
            }
            String[] spl_address = customApplication.arrAddressCtoS.get(position).split("#");
            SharedPreferences.Editor editor = customApplication.getPref().edit();
            editor.putString("addressCtoS", spl_address[1]);
            editor.apply();
            customApplication.setPref();
            Toast.makeText(getApplicationContext(),"Success",Toast.LENGTH_SHORT).show();
            finish();
        });

        initView();
        btnUpdateName = findViewById(R.id.btnUpdateUser);
        etNewAddress = findViewById(R.id.etServerAddress);
        etSerial = findViewById(R.id.etSerial);
        tvServerAddress = findViewById(R.id.tvServerAddress);
//        tvServerAddress.setText("Edit Address (client to server) ex: 192.168.111.222:8080");
        etNewAddress.setHint("");
        etNewAddress.setText(customApplication.strAddressCtoS);
        etSerial.setText("cam");
        imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        findViewById(R.id.rlRoot).setOnClickListener(v -> imm.hideSoftInputFromWindow(etNewAddress.getWindowToken(), 0));
//        tvName.setText(customApplication.getUser().getCompany());
        tvWeb.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.ijoon.net/"));
            startActivity(intent);
        });
        btnUpdateName.setOnClickListener(v -> {

        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateList();
    }


    public void showDeleteRegistererAlertDialog(String message, int position) {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(
                this);
        alertDialog.setTitle("알림");
        alertDialog.setMessage(message);
        alertDialog.setPositiveButton("확인",
                (dialog, which) -> {
                    customApplication.arrAddressCtoS.remove(position);
                    updateList();
                    setStringArrayPref(SettingClientToServer.this,"arrAddressCtoS",customApplication.arrAddressCtoS);
                });
        runOnUiThread(() -> alertDialog.show());
    }

    public void updateList() {
        adapter.clear();
        for(int i = 0 ; i < customApplication.arrAddressCtoS.size() ; i++) {
            String[] spl = customApplication.arrAddressCtoS.get(i).split("#");
            adapter.addItem(spl[1],spl[0]);
        }
        mListView.onRefreshComplete();
        adapter.notifyDataSetChanged();
    }

    public void showAlertDialog(String message) {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("알림");
        alertDialog.setMessage(message);
        alertDialog.setPositiveButton("확인", (dialog, which) -> dialog.cancel());
        runOnUiThread(() -> alertDialog.show());
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

    private void setStringArrayPref(Context context, String key, ArrayList<String> values) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        JSONArray a = new JSONArray();
        for (int i = 0; i < values.size(); i++) {
            a.put(values.get(i));
        }
        if (!values.isEmpty()) {
            editor.putString(key, a.toString());
        } else {
            editor.putString(key, null);
        }
        editor.apply();
    }
}