package com.sq.mobile.hookfixstudy.HmCallbackHook;


import android.app.Instrumentation;
import android.os.Handler;

import com.sq.mobile.hookfixstudy.EvilInstrumentation;
import com.sq.mobile.hookfixstudy.RefInvoke;

public class HookHelper {

    /**
     * ****************************************************
     * <p> ？对H类的mCallback字段，进行Hook
     * 例子：每次执行Activity的startActivity方法时，都打印一行日志
     * ************************************************
     */
    public static void attachBaseContext() throws Exception {

        // 先获取到当前的ActivityThread对象
        Object currentActivityThread = RefInvoke.getStaticFieldObject(
                "android.app.ActivityThread",
                "sCurrentActivityThread");

        // 由于ActivityThread一个进程只有一个,我们获取这个对象的mH
        Handler mH = (Handler) RefInvoke.getFieldObject(currentActivityThread, "mH");

        //把Handler的mCallback字段，替换为new MockClass2(mH)
        RefInvoke.setFieldObject(Handler.class, mH, "mCallback", new MockClass2(mH));
    }

    /**
     * ****************************************************
     * <p> ？Context 的 startActivity方法，进行Hook
     * 例子：startActivity方法时，都打印一行日志
     * <p>
     * - 对ActivityThread的mInstrumentation字段进行Hook
     * <p>
     * ************************************************
     */
    public static void attachContext_ActivityThread_mInstrumentation() throws Exception {
        // 先获取到当前的ActivityThread对象
        Object currentActivityThread = RefInvoke.invokeStaticMethod(
                "android.app.ActivityThread", "currentActivityThread");

        // 拿到原始的 mInstrumentation字段
        Instrumentation mInstrumentation = (Instrumentation) RefInvoke.getFieldObject(
                currentActivityThread, "mInstrumentation");

        // 创建代理对象
        Instrumentation evilInstrumentation = new EvilInstrumentation(mInstrumentation);

        // 偷梁换柱
        RefInvoke.setFieldObject(currentActivityThread, "mInstrumentation", evilInstrumentation);
    }


}
