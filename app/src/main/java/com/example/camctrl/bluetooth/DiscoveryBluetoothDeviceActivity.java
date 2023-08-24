package com.example.camctrl.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothSocket;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.camctrl.CustomApplication;
import com.example.camctrl.SettingDeviceWiFi;
import com.example.camctrl.bluetooth.Device;
import com.example.camctrl.R;
import com.example.camctrl.utils.BleHexConvert;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Set;
import java.util.UUID;


public class DiscoveryBluetoothDeviceActivity extends AppCompatActivity {


    ListView listView;
    DiscoveryAdapter mAdapter;
    BluetoothAdapter bluetoothAdapter;
    private static final int REQUEST_ENABLE_BT = 3;
    Set<BluetoothDevice> mDevices;
    int mPairedDeviceCount;
    BluetoothDevice mRemoteDevice;
    BluetoothSocket mSocket;
    InputStream mInputStream;
    OutputStream mOutputStream;
    Thread mWorkerThread;
    int readBufferPositon;      //버퍼 내 수신 문자 저장 위치
    byte[] readBuffer;      //수신 버퍼
    byte mDelimiter = 10;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mBluetoothGatt;
    public static final UUID UUID_RADIOACTIVITY_MEASUREMENT =
            UUID.fromString(GattAttributes.RADIO_ACTIVITY_MEASUREMENT);
    private BluetoothManager mBluetoothManager;
    RelativeLayout rlMain;
    RelativeLayout rlSend;
    CustomApplication customApplication;
    TextView tvName;
    EditText etNewSSID;
    EditText etNewPassword;
    TextView tvWeb;
    Button btnUpdateName;
    InputMethodManager imm;
    RelativeLayout rlProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discovery_bluetooth_device);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        listView = (ListView)findViewById(R.id.listView);
        mAdapter = new DiscoveryAdapter();
        listView.setAdapter(mAdapter);
        rlMain = findViewById(R.id.rlMain);
        rlSend = findViewById(R.id.rlSend);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                connectToSelectedDevice(mAdapter.items.get(i).address);

            }
        });
        Button btn = (Button)findViewById(R.id.btnSearchDevice);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAdapter.clear();
                bluetoothAdapter.startDiscovery();

            }
        });

        // Register the BroadcastReceiver
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy

        customApplication = (CustomApplication)getApplication();
        initView();
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter(); //블루투스 어댑터 객체 얻기
//        Intent intent = new Intent(DiscoveryBluetoothDeviceActivity.this, DiscoveryBluetoothDeviceActivity.class);
//        startActivity(intent);
        etNewSSID = findViewById(R.id.etServerAddress);
        etNewPassword = findViewById(R.id.etDeviceAddress);
//        etNewSSID.setHint(customApplication.strDeviceWiFi);
        etNewSSID.setText(customApplication.strDeviceWiFi);
        etNewPassword.setHint("New Password");
        rlProgress = findViewById(R.id.rlProgress);
        imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        findViewById(R.id.rlRoot).setOnClickListener(v -> imm.hideSoftInputFromWindow(etNewSSID.getWindowToken(), 0));
//        tvName.setText(customApplication.getUser().getCompany());
        tvWeb.setOnClickListener(v -> {
//            Intent intent1 = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.ijoon.net/"));
//            startActivity(intent1);
            sendMessage("50G",false);
        });
        btnUpdateName.setOnClickListener(v -> {
            if(!etNewSSID.getText().toString().equals("") || !etNewPassword.getText().toString().equals("")) {
                sendMessage("10"+etNewSSID.getText().toString(),false);
                Thread thread = new Thread(() -> {
                    try {
                        Thread.sleep(500);
                        sendMessage("20"+etNewPassword.getText().toString(),false);
                        SharedPreferences.Editor editor = customApplication.getPref().edit();
                        editor.putString("wifi", etNewSSID.getText().toString());
                        editor.apply();
                        customApplication.setPref();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });
                thread.start();
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

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() { //각각의 디바이스로부터 정보를 받으려면 만들어야함
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(BluetoothDevice.ACTION_FOUND.equals(action)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Device deviseClass = new Device();


                boolean isCheck = false;
                for(int i = 0 ; i < mAdapter.items.size() ; i ++) {
                    if(device.getAddress().equals(mAdapter.items.get(i).address)) {
                        isCheck = true;
                    }
                }
                if(isCheck) {
                    return;
                }
                if(device.getName() == null) {
                    deviseClass.name = "Unknown device";
                }else {
                    deviseClass.name = device.getName();
                }

                deviseClass.address = device.getAddress();
                mAdapter.add(deviseClass);
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
         unregisterReceiver(mReceiver);
         if(mBluetoothGatt != null){
             mBluetoothGatt.disconnect();
         }
    }


    private void connectToSelectedDevice(final String selectedDeviceName) {
        //블루투스 기기에 연결하는 과정이 시간이 걸리기 때문에 그냥 함수로 수행을 하면 GUI에 영향을 미친다
        //따라서 연결 과정을 thread로 수행하고 thread의 수행 결과를 받아 다음 과정으로 넘어간다.

        //handler는 thread에서 던지는 메세지를 보고 다음 동작을 수행시킨다.
        final Handler mHandler = new Handler() {
            public void handleMessage(Message msg) {
                if (msg.what == 1) // 연결 완료
                {
                    try {
                        //연결이 완료되면 소켓에서 outstream과 inputstream을 얻는다. 블루투스를 통해
                        //데이터를 주고 받는 통로가 된다.
                        mOutputStream = mSocket.getOutputStream();
                        mInputStream = mSocket.getInputStream();
                        // 데이터 수신 준비
                        beginListenForData();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {    //연결 실패
                    Toast.makeText(getApplicationContext(),"Please check the device", Toast.LENGTH_SHORT).show();
                    try {
                        mSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        //연결과정을 수행할 thread 생성
        Thread thread = new Thread(new Runnable() {
            public void run() {
                if (mBluetoothManager == null) {
                    mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
                }

                mBluetoothAdapter = mBluetoothManager.getAdapter();
                final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(selectedDeviceName);
//                if (device == null) {
//                    Log.w(TAG, "Device not found.  Unable to connect.");
//                    return false;
//                }
                // We want to directly connect to the device, so we are setting the autoConnect
                // parameter to false.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    mBluetoothGatt = device.connectGatt(DiscoveryBluetoothDeviceActivity.this, false, mGattCallback, BluetoothDevice.TRANSPORT_LE);
                }
                else {
                    mBluetoothGatt = device.connectGatt(DiscoveryBluetoothDeviceActivity.this, false, mGattCallback);
                }
                Log.d("yot132", "Trying to create a new connection.");


//                //선택된 기기의 이름을 갖는 bluetooth device의 object
//                mRemoteDevice = getDeviceFromBondedList(selectedDeviceName);
//                UUID uuid = UUID.fromString("0000fff4-0000-1000-8000-00805f9b34fb");
//
//                try {
//                    // 소켓 생성
//                    mSocket = mRemoteDevice.createRfcommSocketToServiceRecord(uuid);
//                    // RFCOMM 채널을 통한 연결, socket에 connect하는데 시간이 걸린다. 따라서 ui에 영향을 주지 않기 위해서는
//                    // Thread로 연결 과정을 수행해야 한다.
//                    Log.d("yot132","?");
//                    mSocket.connect();
//                    mHandler.sendEmptyMessage(1);
//                } catch (Exception e) {
//                    // 블루투스 연결 중 오류 발생
//                    mHandler.sendEmptyMessage(-1);
//                }
            }
        });

        //연결 thread를 수행한다
        thread.start();
    }

//기기에 저장되어 있는 해당 이름을 갖는 블루투스 디바이스의 bluetoothdevice 객채를 출력하는 함수
//bluetoothdevice객채는 기기의 맥주소뿐만 아니라 다양한 정보를 저장하고 있다.

    BluetoothDevice getDeviceFromBondedList(String name) {
        BluetoothDevice selectedDevice = null;
        mDevices = bluetoothAdapter.getBondedDevices();
        //pair 목록에서 해당 이름을 갖는 기기 검색, 찾으면 해당 device 출력
        for (BluetoothDevice device : mDevices) {
            if (name.equals("CamCtrl")) {
                selectedDevice = device;
                break;
            }
        }
        return selectedDevice;
    }

    //블루투스 데이터 수신 Listener
    protected void beginListenForData() {
        final Handler handler = new Handler();
        readBuffer = new byte[1024];  //  수신 버퍼
        readBufferPositon = 0;        //   버퍼 내 수신 문자 저장 위치

        // 문자열 수신 쓰레드
        mWorkerThread = new Thread(new Runnable() {
            @Override
            public void run() {

                while (!Thread.currentThread().isInterrupted()) {

                    try {

                        int bytesAvailable = mInputStream.available();
                        if (bytesAvailable > 0) { //데이터가 수신된 경우
                            byte[] packetBytes = new byte[bytesAvailable];
                            mInputStream.read(packetBytes);
                            for (int i = 0; i < bytesAvailable; i++) {
                                byte b = packetBytes[i];
                                if (b == mDelimiter) {
                                    byte[] encodedBytes = new byte[readBufferPositon];
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                    final String data = new String(encodedBytes, "US-ASCII");
                                    readBufferPositon = 0;
                                    handler.post(new Runnable() {
                                        public void run() {
                                            //수신된 데이터는 data 변수에 string으로 저장!! 이 데이터를 이용하면 된다.

                                            char[] c_arr = data.toCharArray(); // char 배열로 변환
                                            if (c_arr[0] == 'a') {
                                                if (c_arr[1] == '1') {

                                                    //a1이라는 데이터가 수신되었을 때

                                                }
                                                if (c_arr[1] == '2') {

                                                    //a2라는 데이터가 수신 되었을 때
                                                }
                                            }
                                        }
                                    });
                                } else {
                                    readBuffer[readBufferPositon++] = b;
                                }
                            }
                        }
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        //데이터 수신 thread 시작
        mWorkerThread.start();
    }

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
//                mConnectionState = STATE_CONNECTED;
//                handler.removeCallbacks(timeoutRunnable);
                Log.i("yot132", "Connected to GATT server.");

                // Attempts to discover services after successful connection.
                Log.i("yot132", "Attempting to start service discovery:" +
                        mBluetoothGatt.discoverServices());

            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {

                // Enable RADone notification
                List<BluetoothGattService> services = gatt.getServices();
                for (BluetoothGattService service : services) {
                    if (service.getUuid().equals(UUID_RADIOACTIVITY_MEASUREMENT)) {
                        UUID notificationUUID = UUID.fromString(GattAttributes.MDOSE_CHARACTERISTIC_NOTI);
                        for (BluetoothGattDescriptor descriptor : service.getCharacteristic(notificationUUID).getDescriptors()) {
                            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                            mBluetoothGatt.writeDescriptor(descriptor);
                        }
                        gatt.setCharacteristicNotification(service.getCharacteristic(notificationUUID), true);

                        runOnUiThread(() -> {
                            rlMain.setVisibility(View.GONE);
                            rlSend.setVisibility(View.VISIBLE);
                            rlProgress.setVisibility(View.GONE);


                        });

//                        new Thread(() -> {
//                            try {
//                                Thread.sleep(100);
//                            } catch (InterruptedException e) {
//                                e.printStackTrace();
//                            }
//
//                            sendMessage("ALDOSE.LEV?", false);
//                        }).start();

                    }
                }
            } else {
                Log.w("yot132", "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            Log.d("yot132", "onCharacteristicRead");
        }

        private void broadcastUpdate(String action) {
            Intent intent = new Intent(action);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            UUID noti = UUID.fromString(GattAttributes.MDOSE_CHARACTERISTIC_NOTI);
            UUID rw = UUID.fromString(GattAttributes.MDOSE_CHARACTERISTIC_READ_WRITE);

            if(characteristic.getUuid().equals(noti)){
                final byte[] data = characteristic.getValue();
                if (data != null && data.length > 0) {
                    final StringBuilder stringBuilder = new StringBuilder(data.length);
                    for (byte byteChar : data) {
                        String temp = String.format("%02X", byteChar);
                        stringBuilder.append(temp);
                    }
                    String response = new String(data);
                    String convertData = BleHexConvert.bytesToHexString(data);
                    Log.d("yot132","response = " + convertData);
                }
            }else{
                Log.d("yot132","read = " + characteristic.getUuid());

                final byte[] data = characteristic.getValue();
                if (data != null && data.length > 0) {
                    final StringBuilder stringBuilder = new StringBuilder(data.length);
                    for (byte byteChar : data) {
                        String temp = String.format("%02X", byteChar);
                        stringBuilder.append(temp);
                    }
                    String response = new String(data);
                    String convertData = BleHexConvert.bytesToHexString(data);
                    Log.d("yot132","response = " + convertData);
                }
            }
        }
    };

    public void sendMessage(String message, boolean isBlockWhenNotConfigured) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w("yot132", "BluetoothAdapter or mBluetoothGatt not initialized");
            return;
        }
        /*check if the service is available on the device*/
        BluetoothGattService gattService = mBluetoothGatt.getService(UUID_RADIOACTIVITY_MEASUREMENT);

        if(gattService == null){
            Log.w("yot132", "Custom BLE Service not found");
            return;
        }

        Log.d("yot132", "request: " + message + " called");
        UUID rw = UUID.fromString(GattAttributes.MDOSE_CHARACTERISTIC_READ_WRITE);
        BluetoothGattCharacteristic writeCharacteristic = gattService.getCharacteristic(rw);
        writeCharacteristic.setValue(message); // 명령어 byte 보내기
        if(!mBluetoothGatt.writeCharacteristic(writeCharacteristic)) {
            Log.w("yot132", message +", Failed to write characteristic");
        }
    }
}
