package com.sq.mobile.hookfixstudy.activity;


import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.sq.mobile.hookfixstudy.AMNgetDefaultHook.AMSHookHelper;
import com.sq.mobile.hookfixstudy.AMNgetDefaultHook.TargetActivity;
import com.sq.mobile.hookfixstudy.EvilInstrumentation;
import com.sq.mobile.hookfixstudy.HmCallbackHook.HookHelper;
import com.sq.mobile.hookfixstudy.RefInvoke;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //testhookmInstrumentation();

        //testhookAMN();

        //test_hook_H_mCallback();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
        //hookAMN();

        //hook_H_mCallback();
    }

    /**
     * ****************************************************
     * <p> ？对H类的mCallback字段进行Hook
     * 例子：每次执行Activity的startActivity方法时，都打印一行日志
     * ************************************************
     */
    void hook_H_mCallback() {
        try {
            // 在这里进行Hook
            HookHelper.attachBaseContext();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void test_hook_H_mCallback() {
        Button tv = new Button(this);
        tv.setText("测试界面");

        setContentView(tv);

        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SecondActivity.class);
                getApplicationContext().startActivity(intent);
            }
        });
    }


    /**
     * ****************************************************
     * <p> 对AMN的getDefault方法进行hook
     * 例子：每次执行Activity的startActivity方法时，都打印一行日志
     * ************************************************
     */
    void hookAMN() {
        try {
            AMSHookHelper.hookAMN();
        } catch (Throwable throwable) {
            throw new RuntimeException("hook failed", throwable);
        }
    }

    void testhookAMN() {
        Button button = new Button(this);
        button.setText("启动TargetActivity");

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, TargetActivity.class);
                startActivity(intent);
            }
        });
        setContentView(button);
    }


    /**
     * ***********************************************
     * <p> 对Activity的 mInstrumentation 字段进行Hook
     * 例子：每次执行Activity的startActivity方法时，都打印一行日志
     * ************************************************
     */
    void hookmInstrumentation() {
        /**
         * 例子：希望在每次执行Activity的startActivity方法时，都打印一行日志。
         * 方案1：hook mInstrumentation
         * */
        Instrumentation mInstrumentation = (Instrumentation) RefInvoke.getFieldObject(
                Activity.class,
                this,
                "mInstrumentation");
        Instrumentation evilInstrumentation = new EvilInstrumentation(mInstrumentation);

        RefInvoke.setFieldObject(Activity.class, this, "mInstrumentation", evilInstrumentation);

    }

    void testhookmInstrumentation() {
        hookmInstrumentation();

        Button tv = new Button(this);
        tv.setText("测试界面");
        setContentView(tv);

        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SecondActivity.class);
                startActivity(intent);
            }
        });
    }


}
