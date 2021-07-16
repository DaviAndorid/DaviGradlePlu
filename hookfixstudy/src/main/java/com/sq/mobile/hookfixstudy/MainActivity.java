package com.sq.mobile.hookfixstudy;


import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        /**
         * 例子：希望在每次执行Activity的startActivity方法时，都打印一行日志。
         * */
        Instrumentation mInstrumentation = (Instrumentation) RefInvoke.getFieldObject(
                Activity.class,
                this,
                "mInstrumentation");
        Instrumentation evilInstrumentation = new EvilInstrumentation(mInstrumentation);

        RefInvoke.setFieldObject(Activity.class, this, "mInstrumentation", evilInstrumentation);

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
