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

import dalvik.system.DexClassLoader;

public class BaseActivity extends AppCompatActivity {

    static final String TAG = "daviAndroid【host】";
    protected AssetManager mAssetManager;
    protected Resources mResources;
    protected Resources.Theme mTheme;
    protected String dexpath = null;    //apk文件地址
    protected File fileRelease = null;  //释放目录
    protected DexClassLoader classLoader = null;
    protected String apkName = "plugin1-2.apk";    //apk名称

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);

        copyFromAssets(newBase);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadPlu();
    }

    @Override
    public AssetManager getAssets() {
        if (mAssetManager == null) {
            Log.e(TAG, "【getAssets】HostApp中的资源");
            return super.getAssets();
        }

        Log.e(TAG, "【getAssets】mAssetManager is not null");
        return mAssetManager;
    }

    @Override
    public Resources getResources() {
        if (mResources == null) {
            Log.e(TAG, "【getResources】HostApp中的资源");
            return super.getResources();
        }

        Log.e(TAG, "【getResources】mResources is not null");
        return mResources;
    }

    @Override
    public Resources.Theme getTheme() {
        if (mTheme == null) {
            Log.e(TAG, "【getTheme】HostApp中的资源");
            return super.getTheme();
        }

        Log.e(TAG, "【getTheme】Theme is not null");
        return mTheme;
    }


    protected void loadResources() {
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


    void copyFromAssets(Context newBase) {
        try {
            /**
             * 把Assets里面得文件复制到 /data/data/files 目录下
             */
            Utils.extractAssets(newBase, apkName);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    void loadPlu() {
        //classLoader 的构建
        File extractFile = this.getFileStreamPath(apkName);
        dexpath = extractFile.getPath();
        fileRelease = getDir("dex", 0); //0 表示Context.MODE_PRIVATE
        Log.d(TAG, "【loadPlu】dexpath: " + dexpath);
        Log.d(TAG, "【loadPlu】fileRelease.getAbsolutePath(): " + fileRelease.getAbsolutePath());
        classLoader = new DexClassLoader(dexpath, fileRelease.getAbsolutePath(), null, getClassLoader());
    }

}
