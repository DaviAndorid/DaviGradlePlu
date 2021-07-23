package com.sq.mobile.hook_fix_host_one.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.sq.mobile.hook_fix_host_one.R;
import com.sq.mobile.hook_fix_lib_one.PluginItem;
import com.sq.mobile.hook_fix_lib_one.PluginManager;

public class EasyHookActivity extends Activity {

    static final String TAG = "daviAndroid";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_easy_hook);

        findViewById(R.id.btn_1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityInPlugin1();
            }
        });
    }

    /***
     * 启动插件的 TestActivity1
     * */
    public void startActivityInPlugin1() {
        Log.i(TAG, "-- startActivityInPlugin1 -");

        Intent intent = new Intent();
        String activityName;
        Class c = null;
        for (PluginItem item : PluginManager.plugins) {
            Log.e(TAG, "startActivityInPlugin1：" + item.pluginPath);
            activityName = item.packageInfo.packageName + ".TestActivity1";
            try {
                c = Class.forName(activityName);
                break;
            } catch (ClassNotFoundException e) {
                Log.i(TAG, "startActivityInPlugin1, ClassNotFoundException : "
                        + activityName);
            }
        }

        if (c == null) {
            Log.i(TAG, "startActivityInPlugin1, 没找到..");
        } else {
            Log.i(TAG, "startActivityInPlugin1, 找到了，启动...");
            intent.setClass(this, c);
            startActivity(intent);
        }
    }
}
