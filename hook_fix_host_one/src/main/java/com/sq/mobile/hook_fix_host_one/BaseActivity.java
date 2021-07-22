package com.sq.mobile.hook_fix_host_one;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.lang.reflect.Method;
import java.util.HashMap;

import dalvik.system.DexClassLoader;

public class BaseActivity extends AppCompatActivity {

    static final String TAG = "daviAndroid【host】";
    protected AssetManager mAssetManager;
    protected Resources mResources;
    protected Resources.Theme mTheme;
    protected DexClassLoader classLoader = null;

    /**
     * 插件apk的名字
     */
    protected static final String[] PLU_APK_NAME_LIST = {
            "plugin1-3.apk",
            "plugin2-0.apk"
    };

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
        copyFromAssets(newBase);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        genegatePluginInfo();
    }

    @Override
    public AssetManager getAssets() {
        if (mAssetManager == null) {
            //Log.e(TAG, "【getAssets】HostApp中的资源");
            return super.getAssets();
        }

        //Log.e(TAG, "【getAssets】mAssetManager 包含插件的");
        return mAssetManager;
    }

    @Override
    public Resources getResources() {
        if (mResources == null) {
            //Log.e(TAG, "【getResources】HostApp中的资源");
            return super.getResources();
        }

        //Log.e(TAG, "【getResources】mResources 包含插件的");
        return mResources;
    }

    @Override
    public Resources.Theme getTheme() {
        if (mTheme == null) {
            //Log.e(TAG, "【getTheme】HostApp中的资源");
            return super.getTheme();
        }

        ///Log.e(TAG, "【getTheme】Theme 包含插件的");
        return mTheme;
    }


    protected void loadResources(String dexpath) {
        //把插件Plugin的路径，添加到这个AssetManager对象中
        try {
            AssetManager assetManager = AssetManager.class.newInstance();
            Method addAssetPath = assetManager.getClass().getMethod("addAssetPath", String.class);
            addAssetPath.invoke(assetManager, dexpath);
            mAssetManager = assetManager;
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "【loadResources】Exception --> " + e.getMessage());
        }

        //这个AssetManager对象的基础上，创建相应的 Resources 和 Theme 对象
        mResources = new Resources(mAssetManager,
                super.getResources().getDisplayMetrics(),
                super.getResources().getConfiguration());
        mTheme = mResources.newTheme();
        mTheme.setTo(super.getTheme());
    }

    /**
     * 把Assets里面得文件复制到 /data/data/files 目录下
     */
    void copyFromAssets(Context newBase) {
        for (int i = 0; i < PLU_APK_NAME_LIST.length; i++) {
            Utils.extractAssets(newBase, PLU_APK_NAME_LIST[i]);
        }
    }

    /**
     * 根据插件，生成classLoader等插件信息
     */
    protected void genegatePluginInfo() {
        for (int i = 0; i < PLU_APK_NAME_LIST.length; i++) {
            String pluginName = PLU_APK_NAME_LIST[i];
            File extractFile = this.getFileStreamPath(pluginName);
            File fileRelease = getDir("dex", 0);
            String dexpath = extractFile.getPath();
            DexClassLoader classLoader = new DexClassLoader(dexpath, fileRelease.getAbsolutePath(),
                    null, getClassLoader());
            plugins.put(pluginName, new PluginInfo(dexpath, classLoader));
        }
    }

    protected HashMap<String, PluginInfo> plugins = new HashMap<String, PluginInfo>();


}
