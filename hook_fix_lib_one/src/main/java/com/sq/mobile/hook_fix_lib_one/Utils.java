package com.sq.mobile.hook_fix_lib_one;


import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class Utils {

    static final String TAG = "daviAndroid";

    /**
     * 把Assets里面得文件复制到 /data/data/files 目录下
     *
     * @param context
     * @param sourceName
     */
    public static void extractAssets(Context context, String sourceName) {
        AssetManager am = context.getAssets();
        InputStream is = null;
        FileOutputStream fos = null;
        try {
            is = am.open(sourceName);
            File extractFile = context.getFileStreamPath(sourceName);
            fos = new FileOutputStream(extractFile);
            byte[] buffer = new byte[1024];
            int count = 0;
            while ((count = is.read(buffer)) > 0) {
                fos.write(buffer, 0, count);
            }
            fos.flush();

            Log.i(TAG, "extractAssets ok ");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeSilently(is);
            closeSilently(fos);
        }

    }

    // --------------------------------------------------------------------------
    private static void closeSilently(Closeable closeable) {
        if (closeable == null) {
            return;
        }
        try {
            closeable.close();
        } catch (Throwable e) {
            // ignore
        }
    }


    /***
     *
     *
     * */
    private static File sBaseDir;

    /**
     * 待加载插件经过opt优化之后存放odex得路径
     */
    public static File getPluginOptDexDir(String packageName, Context appContext) {
        return enforceDirExists(new File(getPluginBaseDir(packageName, appContext), "odex"));
    }

    // 需要加载得插件得基本目录 /data/data/<package>/files/plugin/
    private static File getPluginBaseDir(String packageName, Context appContext) {
        if (sBaseDir == null) {
            sBaseDir = appContext.getFileStreamPath("plugin");
            enforceDirExists(sBaseDir);
        }
        return enforceDirExists(new File(sBaseDir, packageName));
    }

    private static synchronized File enforceDirExists(File sBaseDir) {
        if (!sBaseDir.exists()) {
            boolean ret = sBaseDir.mkdir();
            if (!ret) {
                throw new RuntimeException("create dir " + sBaseDir + "failed");
            }
        }
        return sBaseDir;
    }

    /**
     * 插件得lib库路径, 这个demo里面没有用
     */
    public static File getPluginLibDir(String packageName, Context appContext) {
        return enforceDirExists(new File(getPluginBaseDir(packageName, appContext), "lib"));
    }


}
