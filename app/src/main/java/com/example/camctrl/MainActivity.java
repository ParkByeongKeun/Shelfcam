package com.example.camctrl;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.camctrl.objdetection.PrePostProcessor;
import com.example.camctrl.objdetection.Result;
import com.example.camctrl.objdetection.ResultView;
import com.example.camctrl.utils.CustomDevice;
import com.example.camctrl.utils.DeviceAdapter;
import com.example.camctrl.widget.XListView;

import org.pytorch.IValue;
import org.pytorch.LiteModuleLoader;
import org.pytorch.Module;
import org.pytorch.Tensor;
import org.pytorch.torchvision.TensorImageUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements Runnable, XListView.IXListViewListener {

    private long mLastClickTime = 0;
    private Handler mRefreshHandler;
    private ArrayList<CustomDevice> mItems = new ArrayList<CustomDevice>();
    private int mImageIndex = 0;
    private String[] mTestImages = {"bg_wait.jpg"};
    XListView listView;
    CustomApplication customApplication;
    private ImageView mImageView;
    private ResultView mResultView;
//    private Button mButtonDetect;
//    private ProgressBar mProgressBar;
    private TextView mProgressBarMain;
    private Bitmap mBitmap = null;
    private Module mModule = null;
    private float mImgScaleX, mImgScaleY, mIvScaleX, mIvScaleY, mStartX, mStartY;
    RelativeLayout rlProgress;
    DeviceAdapter mAdapter;
    ActionBar mTitle;

    public static String assetFilePath(Context context, String assetName) throws IOException {
        File file = new File(context.getFilesDir(), assetName);
        if (file.exists() && file.length() > 0) {
            return file.getAbsolutePath();
        }

        try (InputStream is = context.getAssets().open(assetName)) {
            try (OutputStream os = new FileOutputStream(file)) {
                byte[] buffer = new byte[4 * 1024];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    os.write(buffer, 0, read);
                }
                os.flush();
            }
            return file.getAbsolutePath();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTitle = getSupportActionBar();

        customApplication = (CustomApplication) getApplicationContext();
        rlProgress = findViewById(R.id.rlProgress);
        rlProgress.setVisibility(View.GONE);
        mRefreshHandler = new Handler();
        listView = findViewById(R.id.listView_);
        listView.setPullRefreshEnable(true);
        listView.setPullLoadEnable(true);
        listView.setAutoLoadEnable(true);
        listView.setXListViewListener(this);
        listView.setRefreshTime(getTime());
        getBlePermission();


        mAdapter = new DeviceAdapter(MainActivity.this, R.layout.vw_list_item, mItems);
        listView.setAdapter(mAdapter);
        getImageList();

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
        }

        try {
            mBitmap = BitmapFactory.decodeStream(getAssets().open(mTestImages[mImageIndex]));
        } catch (IOException e) {
            Log.e("Object Detection", "Error reading assets", e);
            finish();
        }


        mImageView = findViewById(R.id.imageView);
        mImageView.setImageBitmap(mBitmap);
        mResultView = findViewById(R.id.resultView);
        mResultView.setVisibility(View.INVISIBLE);
        mProgressBarMain = findViewById(R.id.tvClick);
        stretchVideo();
        listView.setOnItemClickListener((parent, view, position, id) -> {
            String imgName = customApplication.imgList[position - 1].replace("\"","").replace("[","").replace("]","");

            for(int i = 0 ; i < customApplication.imgList.length ; i ++) {
                Log.d("yot132","img = " + customApplication.imgList[i]);
            }
            try {
//                SharedPreferences sharedPreferences = customApplication.getPref();
                String address = customApplication.strAddressDtoS;
                URL url = new URL("http://"+ address + "/image/"+imgName);
                setDetectionImage(url);
            } catch (MalformedURLException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(),e.toString(),Toast.LENGTH_SHORT).show();
                runOnUiThread(()-> {
                    rlProgress.setVisibility(View.GONE);
                });
            }
        });
        final Button buttonTest = findViewById(R.id.btnRequest);
        buttonTest.setOnClickListener(v -> {
            if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                return;
            }
            mLastClickTime = SystemClock.elapsedRealtime();
            if (rlProgress.getVisibility() == View.VISIBLE) {
                return;
            }
            runOnUiThread(() -> {
                rlProgress.setVisibility(View.VISIBLE);
            });
            try {
                String address = customApplication.strAddressCtoS;
                Log.d("yot132","address = " + address);
                URL url1 = new URL("http://" + address + "/image");
                new Thread(()-> {
                    try {
                        HttpURLConnection conn = (HttpURLConnection) url1.openConnection();
                        conn.setDoInput(true);
                        conn.connect();
                        InputStream is = conn.getInputStream();
                        mBitmap = BitmapFactory.decodeStream(is);
                        try {
                            Thread.sleep(100);
                            mResultView.setVisibility(View.INVISIBLE);
                            mImageIndex = (mImageIndex + 1) % mTestImages.length;
                            runOnUiThread(()-> {
                                mImageView.setImageBitmap(mBitmap);
                                mImgScaleX = (float)mBitmap.getWidth() / PrePostProcessor.mInputWidth;
                                mImgScaleY = (float)mBitmap.getHeight() / PrePostProcessor.mInputHeight;
                                mIvScaleX = (mBitmap.getWidth() > mBitmap.getHeight() ? (float)mImageView.getWidth() / mBitmap.getWidth() : (float)mImageView.getHeight() / mBitmap.getHeight());
                                mIvScaleY  = (mBitmap.getHeight() > mBitmap.getWidth() ? (float)mImageView.getHeight() / mBitmap.getHeight() : (float)mImageView.getWidth() / mBitmap.getWidth());
                                mStartX = (mImageView.getWidth() - mIvScaleX * mBitmap.getWidth())/2;
                                mStartY = (mImageView.getHeight() -  mIvScaleY * mBitmap.getHeight())/2;
                                Thread thread = new Thread(MainActivity.this);
                                thread.start();
                                getImageList();
                            });

                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(),e.toString(),Toast.LENGTH_SHORT).show();
                            runOnUiThread(()-> {
                                rlProgress.setVisibility(View.GONE);
                            });
                        }
                    } catch(IOException e) {
                        runOnUiThread(()-> {
                            Toast.makeText(getApplicationContext(),"not found device",Toast.LENGTH_SHORT).show();
                            runOnUiThread(()-> {
                                rlProgress.setVisibility(View.GONE);
                            });
                        });
                        e.printStackTrace();
                    }
                }).start();

            } catch (MalformedURLException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(),e.toString(),Toast.LENGTH_SHORT).show();
                runOnUiThread(() -> {
                    rlProgress.setVisibility(View.GONE);
                });
            }
        });


//        final Button buttonSelect = findViewById(R.id.selectButton);
//        buttonSelect.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                mResultView.setVisibility(View.INVISIBLE);
//
//                final CharSequence[] options = { "Choose from Photos", "Take Picture", "Cancel" };
//                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
//                builder.setTitle("New Test Image");
//
//                builder.setItems(options, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int item) {
//                        if (options[item].equals("Take Picture")) {
//                            Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                            startActivityForResult(takePicture, 0);
//                        }
//                        else if (options[item].equals("Choose from Photos")) {
//                            Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
//                            startActivityForResult(pickPhoto , 1);
//                        }
//                        else if (options[item].equals("Cancel")) {
//                            dialog.dismiss();
//                        }
//                    }
//                });
//                builder.show();
//            }
//        });
//
//        final Button buttonLive = findViewById(R.id.liveButton);
//        buttonLive.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                final Intent intent = new Intent(MainActivity.this, ObjectDetectionActivity.class);
//                startActivity(intent);
//            }
//        });
//
//        mButtonDetect = findViewById(R.id.detectButton);
//        mButtonDetect.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                mButtonDetect.setEnabled(false);
//                mProgressBar.setVisibility(ProgressBar.VISIBLE);
//                mButtonDetect.setText(getString(R.string.run_model));
//
//                mImgScaleX = (float)mBitmap.getWidth() / PrePostProcessor.mInputWidth;
//                mImgScaleY = (float)mBitmap.getHeight() / PrePostProcessor.mInputHeight;
//
//                mIvScaleX = (mBitmap.getWidth() > mBitmap.getHeight() ? (float)mImageView.getWidth() / mBitmap.getWidth() : (float)mImageView.getHeight() / mBitmap.getHeight());
//                mIvScaleY  = (mBitmap.getHeight() > mBitmap.getWidth() ? (float)mImageView.getHeight() / mBitmap.getHeight() : (float)mImageView.getWidth() / mBitmap.getWidth());
//
//                mStartX = (mImageView.getWidth() - mIvScaleX * mBitmap.getWidth())/2;
//                mStartY = (mImageView.getHeight() -  mIvScaleY * mBitmap.getHeight())/2;
//
//                Thread thread = new Thread(MainActivity.this);
//                thread.start();
//            }
//        });

        try {
            mModule = LiteModuleLoader.load(MainActivity.assetFilePath(getApplicationContext(), "yolov5s.torchscript.ptl"));
            BufferedReader br = new BufferedReader(new InputStreamReader(getAssets().open("classes.txt")));
            String line;
            List<String> classes = new ArrayList<>();
            while ((line = br.readLine()) != null) {
                classes.add(line);
            }
            PrePostProcessor.mClasses = new String[classes.size()];
            classes.toArray(PrePostProcessor.mClasses);
        } catch (IOException e) {
            Log.e("Object Detection", "Error reading assets", e);
            finish();
        }
    }

    public void getImageList() {
        final StringBuilder sb = new StringBuilder();
        listView.autoRefresh();
        mItems.clear();
//        mAdapter = new DeviceAdapter(MainActivity.this, R.layout.vw_list_item, mItems);
//        listView.setAdapter(mAdapter);
//        mAdapter.notifyDataSetChanged();
        Thread mThread = new Thread() {
            @Override
            public void run() {
                try {
//                    SharedPreferences sharedPreferences = customApplication.getPref();
                    String address = customApplication.strAddressDtoS;

                    Log.d("yot132","address " + address);
                    URL url1 = new URL("http://" + address + "/list");
                    try {
                        HttpURLConnection conn = (HttpURLConnection) url1.openConnection();
                        conn.setDoInput(true);
                        conn.connect();
                        Log.d("yot132","conn.getResponseCode() = " + conn.getResponseCode());
                        if(conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                            // 결과 값 읽어오는 부분
                            BufferedReader br = new BufferedReader(new InputStreamReader(
                                    conn.getInputStream(), "utf-8"
                            ));
                            String line;
                            while ((line = br.readLine()) != null) {
                                sb.append(line);
                            }
                            // 버퍼리더 종료
                            br.close();
                            customApplication.imgList = sb.toString().split(",");
                            for(int i = 0 ; i < customApplication.imgList.length ; i ++) {
                                customApplication.imgList[i] = customApplication.imgList[i].replace("\"","").replace("[","").replace("]","");
                            }
                            Arrays.sort(customApplication.imgList, Collections.reverseOrder(new Comparator<String>() {
                                @Override
                                public int compare(String o1, String o2) {
                                    String[] spl1 = o1.split("_");
                                    String[] spl2 = o2.split("_");
                                    String resultDate1 = "";
                                    String resultDate2 = "";
                                    if (spl1.length == 2) {
                                        resultDate1 = spl1[1];
                                    }else {
                                        resultDate1 = o1;
                                    }
                                    if (spl2.length == 2) {
                                        resultDate2 = spl2[1];
                                    }else {
                                        resultDate2 = o2;
                                    }
                                    String date1 = resultDate1.replaceAll(".jpg","");
                                    String date2 = resultDate2.replaceAll(".jpg","");
                                    if(Double.parseDouble(date1) > Double.parseDouble(date2)) {
                                        return 1;
                                    }
                                    if(Double.parseDouble(date1) < Double.parseDouble(date2)) {
                                        return -1;
                                    }
                                    return 0;
                                }
                            }));

                            int position = 1;
                            for(int i = 0 ; i < customApplication.imgList.length ; i ++) {

                                try {
                                    String imgName = customApplication.imgList[position - 1];
                                    String date = imgName.replaceAll(".jpg","");
                                    String[] split_date = date.split("_");
                                    String resultDate = "";
                                    if (split_date.length == 2) {
                                        resultDate = split_date[1];
                                    }else {
                                        resultDate = date;
                                    }
                                    CustomDevice customDevice2 = new CustomDevice("http://" + address + "/image/"+imgName,imgName,MillToDate(Long.parseLong(resultDate)/1000000),String.valueOf(position));
                                    position += 1;
//                                    mItems.add(customDevice2);
                                    runOnUiThread(() -> {
                                        mAdapter.add(customDevice2);
                                    });

                                }catch (NumberFormatException e) {
                                    Log.d("yot132",e.toString());
                                }
                            }
                        }
                    } catch(IOException e) {
                        runOnUiThread(()-> {
                            Toast.makeText(getApplicationContext(),"Address(device to server) error",Toast.LENGTH_SHORT).show();
                        });
                        e.printStackTrace();
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }

        };
        mThread.start();

    }

    public String MillToDate(long mills) {
        String pattern = "yyyy-MM-dd HH:mm:ss";
        SimpleDateFormat formatter = new SimpleDateFormat(pattern);
        Date timeInDate = new Date(mills);

        String date = (String) formatter.format(timeInDate);

        return date;
    }

    private void stretchVideo() {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        int maxWidth = metrics.widthPixels;
        int maxHeight = metrics.widthPixels * 9 / 16;
        Log.d("yot132","wid = " + maxWidth + ", " + maxHeight);
        mImageView.setLayoutParams(new RelativeLayout.LayoutParams(maxWidth, maxHeight));
//        mImageView.updateVideoDimens(maxHeight, maxWidth);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_setting,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.menu_setting: {
                Intent intent = new Intent(MainActivity.this, SettingActivity.class);
                startActivity(intent);
                return true;
            }
            default: {
                finish();
                return true;
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_CANCELED) {
            switch (requestCode) {
                case 0:
                    if (resultCode == RESULT_OK && data != null) {
                        mBitmap = (Bitmap) data.getExtras().get("data");
                        Matrix matrix = new Matrix();
                        matrix.postRotate(90.0f);
                        mBitmap = Bitmap.createBitmap(mBitmap, 0, 0, mBitmap.getWidth(), mBitmap.getHeight(), matrix, true);
                        mImageView.setImageBitmap(mBitmap);
                    }
                    break;
                case 1:
                    if (resultCode == RESULT_OK && data != null) {
                        Uri selectedImage = data.getData();
                        String[] filePathColumn = {MediaStore.Images.Media.DATA};
                        if (selectedImage != null) {
                            Cursor cursor = getContentResolver().query(selectedImage,
                                    filePathColumn, null, null, null);
                            if (cursor != null) {
                                cursor.moveToFirst();
                                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                                String picturePath = cursor.getString(columnIndex);
                                mBitmap = BitmapFactory.decodeFile(picturePath);
                                Matrix matrix = new Matrix();
                                matrix.postRotate(90.0f);
                                mBitmap = Bitmap.createBitmap(mBitmap, 0, 0, mBitmap.getWidth(), mBitmap.getHeight(), matrix, true);
                                mImageView.setImageBitmap(mBitmap);
                                cursor.close();
                            }
                        }
                    }
                    break;
            }
        }
    }

    @Override
    public void run() {
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(mBitmap, PrePostProcessor.mInputWidth, PrePostProcessor.mInputHeight, true);
        final Tensor inputTensor = TensorImageUtils.bitmapToFloat32Tensor(resizedBitmap, PrePostProcessor.NO_MEAN_RGB, PrePostProcessor.NO_STD_RGB);
        IValue[] outputTuple = mModule.forward(IValue.from(inputTensor)).toTuple();
        final Tensor outputTensor = outputTuple[0].toTensor();
        final float[] outputs = outputTensor.getDataAsFloatArray();
        final ArrayList<Result> results =  PrePostProcessor.outputsToNMSPredictions(outputs, mImgScaleX, mImgScaleY, mIvScaleX, mIvScaleY, mStartX, mStartY);

        runOnUiThread(() -> {
//            mButtonDetect.setEnabled(true);
//            mButtonDetect.setText(getString(R.string.detect));
//            mProgressBar.setVisibility(ProgressBar.INVISIBLE);
            mResultView.setResults(results);
            mResultView.invalidate();
            mResultView.setVisibility(View.VISIBLE);
            if(mProgressBarMain.getVisibility() == View.VISIBLE) {
                mProgressBarMain.setVisibility(View.GONE);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        for(int i = 0 ; i < customApplication.arrAddressCtoS.size() ; i++) {
            if (customApplication.arrAddressCtoS.get(i).contains(customApplication.strAddressCtoS)) {
                mTitle.setTitle(customApplication.arrAddressCtoS.get(i));
            }
        }
    }

    @Override
    public void onRefresh() {
        mRefreshHandler.postDelayed(() -> {
            onLoad();
            runOnUiThread(() -> {
                rlProgress.setVisibility(View.GONE);
            });
        }, 2500);
    }

    @Override
    public void onLoadMore() {
        mRefreshHandler.postDelayed(() -> onLoad(), 2500);
    }

    private void onLoad() {
//        mItems.clear();
        listView.stopRefresh();
        listView.stopLoadMore();
        listView.setRefreshTime(getTime());
//        getImageList();

    }

    private String getTime() {
        return new SimpleDateFormat("MM-dd HH:mm", Locale.KOREA).format(new Date());
    }

    public void setDetectionImage(URL imageURL) {
        Thread mThread = new Thread() {
            @Override
            public void run() {
                try {
                    HttpURLConnection conn = (HttpURLConnection) imageURL.openConnection();
                    conn.setDoInput(true);
                    conn.connect();

                    InputStream is = conn.getInputStream();
                    mBitmap = BitmapFactory.decodeStream(is);
                } catch(IOException e) {
                    e.printStackTrace();
                }
            }

        };
        mThread.start();

        mResultView.setVisibility(View.INVISIBLE);
        mImageIndex = (mImageIndex + 1) % mTestImages.length;
//                buttonTest.setText(String.format("Text Image %d/%d", mImageIndex + 1, mTestImages.length));

        try {
            mThread.join();
            mImageView.setImageBitmap(mBitmap);

//                mProgressBar.setVisibility(ProgressBar.VISIBLE);
            mImgScaleX = (float)mBitmap.getWidth() / PrePostProcessor.mInputWidth;
            mImgScaleY = (float)mBitmap.getHeight() / PrePostProcessor.mInputHeight;
            mIvScaleX = (mBitmap.getWidth() > mBitmap.getHeight() ? (float)mImageView.getWidth() / mBitmap.getWidth() : (float)mImageView.getHeight() / mBitmap.getHeight());
            mIvScaleY  = (mBitmap.getHeight() > mBitmap.getWidth() ? (float)mImageView.getHeight() / mBitmap.getHeight() : (float)mImageView.getWidth() / mBitmap.getWidth());
            mStartX = (mImageView.getWidth() - mIvScaleX * mBitmap.getWidth())/2;
            mStartY = (mImageView.getHeight() -  mIvScaleY * mBitmap.getHeight())/2;
            Thread thread = new Thread(MainActivity.this);
            thread.start();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
//        getImageList();
    }

    public void getBlePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requestPermissions(
                    new String[]{
                            Manifest.permission.BLUETOOTH,
                            Manifest.permission.BLUETOOTH_SCAN,
                            Manifest.permission.BLUETOOTH_ADVERTISE,
                            Manifest.permission.BLUETOOTH_CONNECT,
                            Manifest.permission.ACCESS_FINE_LOCATION


                    },
                    1);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(
                    new String[]{
                            Manifest.permission.BLUETOOTH

                    },
                    1);
        }
    }
}
