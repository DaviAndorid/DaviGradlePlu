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
import com.sq.mobile.hookfixstudy.plu_start_activity.plu_start_activity_AMSHookHelper;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //testhookmInstrumentation();

        //testhookAMN();

        //test_hook_H_mCallback();

        //test_hook_activityThread_instrument();

        //test_plu_start_activity();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
        //hookAMN();

        //hook_H_mCallback();

        //plu_start_activity();
    }

    /************************************************
     ** 启动没有在AndroidManifest中声明的 activity
     **********************************************
     * */

    void plu_start_activity() {
        try {
            plu_start_activity_AMSHookHelper.hookAMN();
            plu_start_activity_AMSHookHelper.hookActivityThread();
        } catch (Throwable throwable) {
            throw new RuntimeException("hook failed", throwable);
        }
    }


    void test_plu_start_activity() {
        Button button = new Button(this);
        button.setText("启动TargetActivity");

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // 启动目标Activity; 注意这个Activity是没有在AndroidManifest.xml中显式声明的
                // 但是调用者并不需要知道, 就像一个普通的Activity一样
                startActivity(new Intent(MainActivity.this, TargetActivity.class));
            }
        });
        setContentView(button);
    }


    /**
     *
     * =====================================================》
     * Context Context Context Context Context Context
     * ======================================================》
     */


    /**
     * ****************************************************
     * <p> ？Context 的 startActivity方法，进行Hook
     * 例子：startActivity方法时，都打印一行日志
     * <p>
     * - 对ActivityThread的mInstrumentation字段进行Hook
     * <p>
     * ************************************************
     */
    void hook_activityThread_instrument() {
        try {
            // 在这里进行Hook
            HookHelper.attachContext_ActivityThread_mInstrumentation();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void test_hook_activityThread_instrument() {
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

    //对AMN的getDafault方法进行Hook是  >>>>一劳永逸<<<<<  的，Activity模块的代码是一样的


    /**
     * ======================================================》
     * Activity Activity Activity Activity Activity Activity
     * ======================================================》
     */


    /**
     * ****************************************************
     * <p> ？对H类的mCallback字段，进行Hook
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
     * <p> 对AMN的getDefault方法，进行hook
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
     * <p> 对Activity的 mInstrumentation 字段，进行Hook
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
