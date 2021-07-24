package com.sq.mobile.hook_fix_host_one.activity;


import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.sq.mobile.hook_fix_host_one.MyApplication;
import com.sq.mobile.hook_fix_host_one.ams_hook.AMSHookHelper;
import com.sq.mobile.hook_fix_host_one.classloder_hook.LoadedApkClassLoaderHookHelper;
import com.sq.mobile.hook_fix_lib_one.Utils;

import java.io.File;


public class plu_in_solution_activity extends Activity {

    static final String TAG = "daviAndroid";

    String pluPkg = "com.sq.mobile.hook_fix_plu_one";
    String nameActivity = "plu_in_solution_activity_in_plu";


    String pluName = "plugin1-5.apk";

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
        Utils.extractAssets(newBase, pluName);
        File apkFile = getFileStreamPath(pluName);
        LoadedApkClassLoaderHookHelper.hookLoadedApkInActivityThread(apkFile, newBase);
        AMSHookHelper.hookAMN();
        AMSHookHelper.hookActivityThread();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Button t = new Button(this);
        t.setText("test button");
        setContentView(t);
        //Log.d(TAG, "context classloader: " + getApplicationContext().getClassLoader());

        t.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String activity = pluPkg + "." + nameActivity;
                    Log.i(TAG, "startActivity activity : " + activity);
                    Intent t = new Intent();
                    t.setComponent(new ComponentName(pluPkg, activity));
                    startActivity(t);
                } catch (Throwable e) {
                    e.printStackTrace();
                    Log.e(TAG, "startActivity err !!!! " + e.getMessage());
                }
            }
        });
    }


}
