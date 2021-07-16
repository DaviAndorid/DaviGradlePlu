package com.sq.mobile.hookfixstudy;


import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.sq.mobile.hookfixstudy.AMNgetDefaultHook.AMSHookHelper;
import com.sq.mobile.hookfixstudy.AMNgetDefaultHook.TargetActivity;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //testhookmInstrumentation();

        //testhookAMN();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
        hookAMN();
    }

    /**
     * ****************************************************
     * <p>
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
     * <p>
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
