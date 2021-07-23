package com.sq.mobile.hook_fix_plu_one;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.sq.mobile.hook_fix_lib_one.ZeusBaseActivity;


/**
 * 这里启动宿主？？？？
 */
public class TestActivity1 extends ZeusBaseActivity {

    String pkg = "com.sq.mobile.hook_fix_host_one.activity";
    String activityName = pkg + ".ActivityA";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test1);

        findViewById(R.id.button1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent = new Intent();
                    intent.setComponent(new ComponentName(pkg, activityName));
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }


}
