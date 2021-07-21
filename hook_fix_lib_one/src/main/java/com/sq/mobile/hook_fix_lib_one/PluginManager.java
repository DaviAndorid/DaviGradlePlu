package com.sq.mobile.hook_fix_lib_one;


import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.util.Log;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import dalvik.system.DexClassLoader;

public class PluginManager {

    public final static List<PluginItem> plugins = new ArrayList<PluginItem>();

    //正在使用的Resources
    public static volatile Resources mNowResources;

    //原始的application中的BaseContext，不能是其他的，否则会内存泄漏
    public static volatile Context mBaseContext;

    /**
     * ContextImpl 中的 LoadedAPK 对象 mPackageInfo
     * 例子1：LoadedAPK 对象在资源上的应用，主要是替换系统性的Resource
     * - https://blog.csdn.net/u012911421/article/details/90215408
     * <p>
     * 例子2：LoadedAPK中的 mClassLoader  变量hook
     * - https://android.googlesource.com/platform/frameworks/base/+/master/core/java/android/app/LoadedApk.java
     */
    private static Object mPackageInfo = null;

    public static volatile ClassLoader mNowClassLoader = null;          //系统原始的ClassLoader
    public static volatile ClassLoader mBaseClassLoader = null;         //系统原始的ClassLoader

    public static void init(Application application) {
        /**
         * 初始化一些成员变量和加载已安装的插件
         * */
        //变量 mPackageInfo 是一个LoadApk对象
        mPackageInfo = RefInvoke.getFieldObject(application.getBaseContext(), "mPackageInfo");
        mBaseContext = application.getBaseContext();
        mNowResources = mBaseContext.getResources();

        mBaseClassLoader = mBaseContext.getClassLoader();
        mNowClassLoader = mBaseContext.getClassLoader();

        try {
            AssetManager assetManager = application.getAssets();
            String[] paths = assetManager.list("");
            //插件路径
            ArrayList<String> pluginPaths = new ArrayList<String>();
            for (String path : paths) {
                if (path.endsWith(".apk")) {
                    String apkName = path;
                    //把Assets里面得文件复制到 /data/data/files 目录下
                    Utils.extractAssets(mBaseContext, apkName);
                    //插件信息转换为内存对象" PluginItem "
                    PluginItem item = generatePluginItem(apkName);
                    plugins.add(item);

                    pluginPaths.add(item.pluginPath);
                }
            }

            //插件资源的处理
            reloadInstalledPluginResources(pluginPaths);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //多插件方案，classLoader
        ZeusClassLoader classLoader = new ZeusClassLoader(mBaseContext.getPackageCodePath(), mBaseContext.getClassLoader());
        File dexOutputDir = mBaseContext.getDir("dex", Context.MODE_PRIVATE);
        final String dexOutputPath = dexOutputDir.getAbsolutePath();
        for (PluginItem plugin : plugins) {
            Log.i("daviAndroid", "多插件方案 : " + plugin.pluginPath);

            DexClassLoader dexClassLoader = new DexClassLoader(plugin.pluginPath,
                    dexOutputPath, null, mBaseClassLoader);
            classLoader.addPluginClassLoader(dexClassLoader);
        }

        RefInvoke.setFieldObject(mPackageInfo, "mClassLoader", classLoader);
        Thread.currentThread().setContextClassLoader(classLoader);
        mNowClassLoader = classLoader;
    }

    private static PluginItem generatePluginItem(String apkName) {
        File file = mBaseContext.getFileStreamPath(apkName);
        PluginItem item = new PluginItem();
        item.pluginPath = file.getAbsolutePath();
        item.packageInfo = DLUtils.getPackageInfo(mBaseContext, item.pluginPath);
        item.applicationName = ApplicationHelper.loadApplication(mBaseContext, file);

        return item;
    }

    /**
     * 背景：
     * AndroidL之后资源在初始化之后可以加载，而在AndroidL之前是不可以的。因为在Android KK及以下版本，
     * addAssetPath只是把补丁包的路径添加到了mAssetPath中，
     * 而真正解析的资源包的逻辑是在app第一次执行AssetManager::getResTable的时候。
     * <p>
     * <p>
     * <p>
     * <p>
     * <p>
     * 思路：
     * 1）构造一个新的AssetManager，并通过反射调用 addAssetPath()，
     * 把这个完整的新资源包加入到AssetManager中。这样就得到了一个含有所有新资源的AssetManager
     * <p>
     * 2）找到之前引用原有AssetManager的地方，通过反射，
     * 将AssetManager类型的mAssets字段引用全部替换为新创建的 AssetManager
     * <p>
     * >> 思想地址：https://www.codenong.com/cs105931744/
     * <p>
     * <p>
     * <p>
     * <p>
     * <p>
     * <p>
     * 资源的插件化原理
     * 地址：
     * - https://juejin.cn/post/6844903810326855693
     * <p>
     * - 方案：
     * - 1）方案1：合并资源方案
     * - 将插件的所有资源添加到宿主的Resources中，这种插件方案可以访问宿主的资源
     * - >>> 37手游SDK目前用的是这个方案
     * <p>
     * - 2）方案2：构建插件资源方案
     * - 每个每个插件都构造出独立的Resources，这种方案不可以访问宿主资源
     * <p>
     * <p>
     * -
     */
    //【方案2，构建插件资源方案】
    private static void reloadInstalledPluginResources(ArrayList<String> pluginPaths) {
        try {
            //1）动态创建AssetManager
            AssetManager assetManager = AssetManager.class.newInstance();
            Method addAssetPath = AssetManager.class.getMethod("addAssetPath", String.class);

            //2）反射调用AssetManager的 addAssetPath 方法来加载插件
            //Android应用中的资源是通过AssetManager来管理的，其中addAssetPath方法可以指定资源加载路径。
            //例子：https://www.jianshu.com/p/e6125ddfaea7   （换肤的方式实现资源加载）
            addAssetPath.invoke(assetManager, mBaseContext.getPackageResourcePath());
            for (String pluginPath : pluginPaths) {
                addAssetPath.invoke(assetManager, pluginPath);
            }

            //3）该方法新创建的Resources是插件的资源 ？？
            Resources newResources = new Resources(assetManager,
                    mBaseContext.getResources().getDisplayMetrics(),
                    mBaseContext.getResources().getConfiguration());

            //反射生效（todo：注意适配..）
            RefInvoke.setFieldObject(mBaseContext, "mResources", newResources);
            //这是最主要的需要替换的，如果不支持插件运行时更新，只留这一个就可以了
            RefInvoke.setFieldObject(mPackageInfo, "mResources", newResources);

            //清除一下之前的resource的数据，释放一些内存
            //因为这个resource有可能还被系统持有着，内存都没被释放
            //clearResoucesDrawableCache(mNowResources);

            mNowResources = newResources;
            //需要清理mtheme对象，否则通过inflate方式加载资源会报错
            //如果是activity动态加载插件，则需要把activity的mTheme对象也设置为null
            RefInvoke.setFieldObject(mBaseContext, "mTheme", null);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }


}
