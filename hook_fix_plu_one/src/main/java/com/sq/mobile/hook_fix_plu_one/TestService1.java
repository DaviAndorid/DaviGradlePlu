package com.sq.mobile.hook_fix_plu_one;


import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;


public class TestService1 extends Service {

    private static final String TAG = "daviAndroid";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG, "onCreate");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, TAG + " onDestroy");
    }


}
