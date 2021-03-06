package com.sq.mobile.hook_fix_lib_one;


import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import dalvik.system.DexClassLoader;
import dalvik.system.PathClassLoader;

import static java.lang.System.arraycopy;


class ZeusClassLoader extends PathClassLoader {
    private List<DexClassLoader> mClassLoaderList = null;

    String mDexPath;

    private List<PluginItem> plugins = null;

    public ZeusClassLoader(String dexPath, ClassLoader parent, List<PluginItem> p) {
        super(dexPath, parent);
        mDexPath = dexPath;
        mClassLoaderList = new ArrayList<DexClassLoader>();
        plugins = p;
    }

    /**
     * 添加一个插件到当前的classLoader中
     */
    protected void addPluginClassLoader(DexClassLoader dexClassLoader) {
        mClassLoaderList.add(dexClassLoader);
    }

    @Override
    protected Class<?> loadClass(String className, boolean resolve) throws ClassNotFoundException {
        Class<?> clazz = null;
        try {
            //先查找parent classLoader，这里实际就是系统帮我们创建的classLoader，目标对应为宿主apk
            if (getParent() == null) {
                Log.e("daviAndroid", "失败：先查找parent classLoader");
            } else {
                clazz = getParent().loadClass(className);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("daviAndroid", "loadClass Exception ： " + e.getMessage());
        }

        if (clazz != null) {
            Log.i("daviAndroid", "【系统的classLoader】【目标对应为宿主apk】loadClass ok..");
            return clazz;
        }

        //挨个的到插件里进行查找
        Log.i("daviAndroid", "【ClassLoader】开始 挨个的到插件里进行查找 ... ");
        if (mClassLoaderList != null) {
            for (DexClassLoader classLoader : mClassLoaderList) {
                int index = mClassLoaderList.indexOf(classLoader);
                String pluginPath = plugins.get(index).pluginPath;
                Log.i("daviAndroid", "【ClassLoader】插件 --> " + index);
                Log.i("daviAndroid", "【ClassLoader】插件 -->" + pluginPath);

                try {
                    //这里只查找插件它自己的apk，不需要查parent，避免多次无用查询，提高性能
                    clazz = classLoader.loadClass(className);
                    if (clazz != null) {
                        Log.i("daviAndroid", pluginPath + "：loadClass ok..");
                        return clazz;
                    }
                } catch (Exception ignored) {
                    ignored.printStackTrace();
                    Log.e("daviAndroid", "【ClassLoader】挨个的到插件里进行查找 err : " + ignored.getMessage());
                }
            }
        }
        throw new ClassNotFoundException(className + " in loader " + this);
    }
}
