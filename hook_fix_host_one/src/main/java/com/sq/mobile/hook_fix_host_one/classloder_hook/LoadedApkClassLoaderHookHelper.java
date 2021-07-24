package com.sq.mobile.hook_fix_host_one.classloder_hook;


import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.util.Log;

import com.sq.mobile.hook_fix_lib_one.RefInvoke;
import com.sq.mobile.hook_fix_lib_one.*;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;


public class LoadedApkClassLoaderHookHelper {

    static final String TAG = "daviAndroid";

    public static Map<String, Object> sLoadedApk = new HashMap<String, Object>();

    /***
     * 链接：
     *  - https://juejin.cn/post/6893740587678187528
     *  - 关键字：mPackages
     *
     * 背景：
     *  - Activity 调用 startActivity 之后最后会在 ActivityThread 执行 handleLaunchActivity
     *      -  Activity 初始化就是由 LoadedApk 里面的 mClassLoader进行加载的
     *          - 在回调 handleLaunchActivity() 之前会通过 getPackageInfoNoCheck() 方法初始化我们的 LoadedApk 对象，
     *          并存放如全局的变量 mPackages 中，所以我们要创建一个插件的 LoadedApk 并且把它添加到 mPackages 这个集合中就行了。
     *
     *应用：
     *  - 由此得到我们的Hook点，我们如果自定定义一个LoadedApk
     *  并把里面的 mClassLoader 给替换为我们自己的 ClassLoader 就可以加载插件的类了
     *
     *
     * hook LoadedApk
     *
     * */
    public static void hookLoadedApkInActivityThread(File apkFile,  Context appContext) {
        Log.i(TAG, "hookLoadedApkInActivityThread ...");


        /**【ActivityThread】
         * 1）先获取到当前的 ActivityThread 对象
         * */
        Object currentActivityThread = RefInvoke.invokeStaticMethod("android.app.ActivityThread",
                "currentActivityThread");

        /***
         * 【mPackages】
         * 1）ActivityThread类有一个ArrayMap类型的变量mPackages，保存了 packageName 到 LoadedApk对象 的映射
         *  - get LoadedApk
         *      - mPackages.get(packageName);
         *  - 获得LoadedApk，进而可以获得源classloader，
         * 2）mPackages 这个静态成员变量, 这里缓存了dex包的信息
         *
         * */
        Map mPackages = (Map) RefInvoke.getFieldObject(currentActivityThread, "mPackages");

        /***
         * 准备两个参数：
         * 1）android.content.res.CompatibilityInfo
         * 2）ApplicationInfo
         * */
        Object defaultCompatibilityInfo = RefInvoke.getStaticFieldObject("android.content.res.CompatibilityInfo",
                "DEFAULT_COMPATIBILITY_INFO");
        ApplicationInfo applicationInfo = generateApplicationInfo(apkFile);//好复杂的反射..
        if (applicationInfo == null) {
            Log.e(TAG, "hookLoadedApkInActivityThread err  : applicationInfo is null..");
            return;
        }

        //调用ActivityThread的 getPackageInfoNoCheck 方法loadedApk得到，上面两个数据都是用来做参数的
        Class<?> mCompatibilityInfo = null;
        try {
            mCompatibilityInfo = Class.forName("android.content.res.CompatibilityInfo");
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "ClassNotFoundException : CompatibilityInfo");
        }
        if (mCompatibilityInfo == null) {
            Log.e(TAG, "hookLoadedApkInActivityThread err : mCompatibilityInfo is null");
            return;
        }

        Class[] p1 = {ApplicationInfo.class, mCompatibilityInfo};
        Object[] v1 = {applicationInfo, defaultCompatibilityInfo};
        Object loadedApk = RefInvoke.invokeInstanceMethod(currentActivityThread, "getPackageInfoNoCheck", p1, v1);

        //为插件造一个新的ClassLoader
        String odexPath = Utils.getPluginOptDexDir(applicationInfo.packageName, appContext).getPath();
        String libDir = Utils.getPluginLibDir(applicationInfo.packageName, appContext).getPath();
        ClassLoader classLoader = new CustomClassLoader(apkFile.getPath(), odexPath, libDir,
                ClassLoader.getSystemClassLoader());
        RefInvoke.setFieldObject(loadedApk, "mClassLoader", classLoader);

        //把插件LoadedApk对象放入缓存
        WeakReference weakReference = new WeakReference(loadedApk);
        mPackages.put(applicationInfo.packageName, weakReference);

        // 由于是弱引用, 因此我们必须在某个地方存一份, 不然容易被GC; 那么就前功尽弃了.
        sLoadedApk.put(applicationInfo.packageName, loadedApk);

        Log.i(TAG, "hookLoadedApkInActivityThread ok ");
    }

    /**
     * 这个方法的最终目的是调用
     * android.content.pm.PackageParser#generateActivityInfo
     * (android.content.pm.PackageParser.Activity,
     * int,
     * android.content.pm.PackageUserState,
     * int)
     */
    public static ApplicationInfo generateApplicationInfo(File apkFile) {

        Class<?> packageParserClass = null, packageParser$PackageClass = null, packageUserStateClass = null;

        // 找出需要反射的核心类: android.content.pm.PackageParser
        try {
            packageParserClass = Class.forName("android.content.pm.PackageParser");
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "ClassNotFoundException : PackageParser");
        }
        try {
            packageParser$PackageClass = Class.forName("android.content.pm.PackageParser$Package");
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "ClassNotFoundException : PackageParser$Package");
        }
        try {
            packageUserStateClass = Class.forName("android.content.pm.PackageUserState");
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "ClassNotFoundException : PackageUserState");
        }


        if (packageParserClass == null || packageParser$PackageClass == null || packageUserStateClass == null) {
            Log.e(TAG, "generateApplicationInfo default....");
            return null;
        }


        // 我们的终极目标: android.content.pm.PackageParser#generateApplicationInfo(
        // android.content.pm.PackageParser.Package,
        // int,
        // android.content.pm.PackageUserState
        // )
        // 要调用这个方法, 需要做很多准备工作; 考验反射技术的时候到了 - -!
        // 下面, 我们开始这场Hack之旅吧!

        // 首先拿到我们得终极目标: generateApplicationInfo方法
        // API 23 !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        // public static ApplicationInfo generateApplicationInfo(Package p, int flags,
        //    PackageUserState state) {
        // todo：其他Android版本不保证也是如此.


        // 首先, 我们得创建出一个Package对象出来供这个方法调用
        // 而这个需要得对象可以通过 android.content.pm.PackageParser#parsePackage 这个方法返回得 Package对象得字段获取得到
        // 创建出一个PackageParser对象供使用
        Object packageParser = null;
        try {
            packageParser = packageParserClass.newInstance();
        } catch (IllegalAccessException e) {
            Log.e(TAG, "IllegalAccessException");
        } catch (InstantiationException e) {
            Log.e(TAG, "InstantiationException");
        }
        if (packageParser == null) {
            Log.e(TAG, "generateApplicationInfo default....");
            return null;
        }

        // 调用 PackageParser.parsePackage 解析apk的信息
        // 实际上是一个 android.content.pm.PackageParser.Package 对象
        Class[] p1 = {File.class, int.class};
        Object[] v1 = {apkFile, 0};
        Object packageObj = RefInvoke.invokeInstanceMethod(packageParser, "parsePackage", p1, v1);


        // 第三个参数 mDefaultPackageUserState 我们直接使用默认构造函数构造一个出来即可
        Object defaultPackageUserState = null;
        try {
            defaultPackageUserState = packageUserStateClass.newInstance();
        } catch (IllegalAccessException e) {
            Log.e(TAG, "IllegalAccessException");
        } catch (InstantiationException e) {
            Log.e(TAG, "InstantiationException");
        }
        if (defaultPackageUserState == null) {
            Log.e(TAG, "generateApplicationInfo default....");
            return null;
        }

        // 万事具备!!!!!!!!!!!!!!
        Class[] p2 = {packageParser$PackageClass, int.class, packageUserStateClass};
        Object[] v2 = {packageObj, 0, defaultPackageUserState};
        ApplicationInfo applicationInfo = (ApplicationInfo) RefInvoke.invokeInstanceMethod(packageParser, "generateApplicationInfo", p2, v2);

        String apkPath = apkFile.getPath();
        applicationInfo.sourceDir = apkPath;
        applicationInfo.publicSourceDir = apkPath;

        return applicationInfo;
    }


}
