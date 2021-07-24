package com.sq.mobile.hook_fix_host_one.ams_hook;


import android.os.Build;
import android.os.Handler;
import android.util.Log;


import com.sq.mobile.hook_fix_lib_one.RefInvoke;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;


//todo 不同的安卓系统源码，hook需要适配下...

public class AMSHookHelper {

    public static final String EXTRA_TARGET_INTENT = "extra_target_intent";

    static final String TAG = "daviAndroid";

    /**
     * Hook AMS
     * 主要完成的操作是  "把真正要启动的Activity临时替换为在AndroidManifest.xml中声明的替身Activity",进而骗过AMS
     *
     * <p>
     * 【解决】
     * have you declared this activity in your AndroidManifest.xml?
     * <p>
     * <p>
     * todo：
     * 1）安卓5可以
     * 2）安卓10机子不行
     */
    public static void hookAMN() {


        /**
         * 作用：
         * 1）获取AMN的gDefault单例gDefault，gDefault是final静态的  !!!!!
         *
         * 适配：
         * 1）ActivityManagerNative这个类的gDefault字段，这个字段在API 25以及之前的版本
         * 2）Google在Android O中，把ActivityManagerNative中的这个gDefault字段删除了
         * 3）转移到了ActivityManager类中，但此时，这个字段改名为 IActivityManagerSingleton，
         * */
        Object gDefault;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.i(TAG, "hookAMN  :  >= Build.VERSION_CODES.O ");
            gDefault = RefInvoke.getStaticFieldObject("android.app.ActivityManager", "IActivityManagerSingleton");
        } else {
            Log.i(TAG, "hookAMN  :  < Build.VERSION_CODES.O ");
            gDefault = RefInvoke.getStaticFieldObject("android.app.ActivityManagerNative", "gDefault");
        }


        // gDefault是一个 android.util.Singleton<T>对象; 我们取出这个单例里面的mInstance字段
        Object mInstance = RefInvoke.getFieldObject("android.util.Singleton", gDefault, "mInstance");

        // 创建一个这个对象的代理对象MockClass1, 然后替换这个字段, 让我们的代理对象帮忙干活
        Class<?> classB2Interface = null;
        try {
            classB2Interface = Class.forName("android.app.IActivityManager");
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "hookAMN err : ClassNotFoundException : IActivityManager");
        }
        if (classB2Interface == null) {
            Log.e(TAG, "hookAMN err :  IActivityManager fail!!!");
            return;
        }

        Object proxy = Proxy.newProxyInstance(
                Thread.currentThread().getContextClassLoader(),
                new Class<?>[]{classB2Interface},
                new MockClass1_hookAMN(mInstance));

        if (gDefault == null) {
            Log.e(TAG, "hookAMN err : ActivityManagerNative  fail!!!");
            return;
        }
        //把gDefault的mInstance字段，修改为proxy
        Class class1 = gDefault.getClass();
        RefInvoke.setFieldObject("android.util.Singleton", gDefault, "mInstance", proxy);

        Log.i(TAG, "hookAMN ok ");
    }

    /**
     * 由于之前我们用替身欺骗了AMS; 现在我们要换回我们真正需要启动的Activity
     * 不然就真的启动替身了, 狸猫换太子...
     * 到最终要启动Activity的时候,会交给ActivityThread 的一个内部类叫做 H 来完成
     * H 会完成这个消息转发; 最终调用它的callback
     */
    public static void hookActivityThread() {

        // 先获取到当前的ActivityThread对象
        Object currentActivityThread = RefInvoke.getStaticFieldObject("android.app.ActivityThread", "sCurrentActivityThread");

        // 由于ActivityThread一个进程只有一个,我们获取这个对象的mH
        Handler mH = (Handler) RefInvoke.getFieldObject(currentActivityThread, "mH");

        //把Handler的mCallback字段，替换为new MockClass2_hookActivityThread(mH)
        RefInvoke.setFieldObject(Handler.class, mH, "mCallback", new MockClass2_hookActivityThread(mH));

        Log.i(TAG, "hookActivityThread ok ");
    }


}
