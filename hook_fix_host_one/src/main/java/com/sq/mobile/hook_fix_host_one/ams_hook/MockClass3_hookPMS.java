package com.sq.mobile.hook_fix_host_one.ams_hook;


import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.os.Handler;
import android.os.Message;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class MockClass3_hookPMS implements InvocationHandler {

    private Object mBase;

    public MockClass3_hookPMS(Object base) {
        mBase = base;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getName().equals("getPackageInfo")) {
            return new PackageInfo();
        }
        return method.invoke(mBase, args);
    }


}
