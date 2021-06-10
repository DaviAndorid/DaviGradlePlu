package com.sq.mobile.daviplu;

import android.app.Activity;
import android.view.KeyEvent;
import android.view.View;

public class Platform {

    Activity activity;


    private void demo() {

        activity.getWindow().getDecorView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        //表示按返回键时的操作
                        return true;
                    }
                }

                return false;
            }
        });


    }


}
