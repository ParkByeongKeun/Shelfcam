package com.example.camctrl.utils;

import android.app.Activity;
import android.widget.Toast;

import com.example.camctrl.R;

/**
 * Created by pbj on 15. 8. 13..
 */
public class BackPressCloseHandler {

    private long mBackKeyPressedTime = 0;
    private Toast mToast;
    private final Activity mActivity;

    public BackPressCloseHandler(Activity context) {
        this.mActivity = context;
    }

    public void onBackPressed() {
        if (System.currentTimeMillis() > mBackKeyPressedTime + 2000) {
            mBackKeyPressedTime = System.currentTimeMillis();
            showGuide();
            return;
        }
        if (System.currentTimeMillis() <= mBackKeyPressedTime + 2000) {
            mActivity.finish();
            mToast.cancel();
        }
    }

    public void showGuide() {
        mToast = Toast.makeText(mActivity,this.mActivity.getResources().getString(R.string.notice_press_backkey_to_finish), Toast.LENGTH_SHORT);
        mToast.show();
    }
}