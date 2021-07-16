package com.sq.mobile.hookfixstudy.AMNgetDefaultHook;


import android.util.Log;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;


public class MockClass1 implements InvocationHandler {

    private static final String TAG = "MockClass1";

    Object mBase;

    public MockClass1(Object base) {
        mBase = base;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        if ("startActivity".equals(method.getName())) {

            Log.e("davi", method.getName());

            return method.invoke(mBase, args);
        }

        return method.invoke(mBase, args);
    }


}
