package com.sq.mobile.hook_fix_host_one.activity;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.sq.mobile.hook_fix_host_one.R;

public class ActivityA extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_easy_hook);

        TextView textView = findViewById(R.id.tv);
        textView.setText("我是宿主：ActivityA");
    }


}
