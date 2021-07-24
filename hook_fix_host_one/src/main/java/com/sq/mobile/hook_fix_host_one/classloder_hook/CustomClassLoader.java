package com.sq.mobile.hook_fix_host_one.classloder_hook;

import dalvik.system.DexClassLoader;


/**
 * 自定义的ClassLoader, 用于加载"插件"的资源和代码
 */

public class CustomClassLoader extends DexClassLoader {

    public CustomClassLoader(String dexPath, String optimizedDirectory, String libraryPath, ClassLoader parent) {
        super(dexPath, optimizedDirectory, libraryPath, parent);
    }


}
