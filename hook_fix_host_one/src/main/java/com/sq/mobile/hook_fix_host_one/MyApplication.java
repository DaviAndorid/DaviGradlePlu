package com.sq.mobile.hook_fix_host_one;


import android.app.Application;
import android.content.Context;

import com.sq.mobile.hook_fix_host_one.ams_hook.AMSHookHelper;
import com.sq.mobile.hook_fix_lib_one.PluginItem;
import com.sq.mobile.hook_fix_lib_one.PluginManager;

import java.util.HashMap;


public class MyApplication extends Application {

    private static Context sContext;

    public static HashMap<String, String> pluginActivies = new HashMap<String, String>();

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        sContext = base;

        //1）插件资源处理
        //2）插件classLoader处理
        PluginManager.init(this);

        //testHookActivity();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        //testHookApplication()
    }

    void testHookApplication() {
        /**
         * Application 的插件化解决方案
         * */
        for (PluginItem pluginItem : PluginManager.plugins) {
            try {
                Class clazz = PluginManager.mNowClassLoader.loadClass(pluginItem.applicationName);
                Application application = (Application) clazz.newInstance();

                if (application == null)
                    continue;
                application.onCreate();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }


    public static void testHookActivity() {
        //get json data from server
        mockData();

        try {
            //activity  清单文件 欺骗AMS
            AMSHookHelper.hookAMN();
            //activity 启动 欺骗AMS
            AMSHookHelper.hookActivityThread();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }


    static void mockData() {
        pluginActivies.put("jianqiang.com.plugin1.ActivityA", "jianqiang.com.hostapp.SingleTopActivity1");
        pluginActivies.put("jianqiang.com.plugin1.TestActivity1", "jianqiang.com.hostapp.SingleTaskActivity2");
    }


    public static Context getContext() {
        return sContext;
    }


}
