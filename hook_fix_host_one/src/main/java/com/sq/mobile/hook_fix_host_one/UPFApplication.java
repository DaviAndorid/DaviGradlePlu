package com.sq.mobile.hook_fix_host_one;


import android.app.Application;
import android.content.Context;

public class UPFApplication extends Application {

    private static Context sContext;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        sContext = base;
    }

    public static Context getContext() {
        return sContext;
    }


}
