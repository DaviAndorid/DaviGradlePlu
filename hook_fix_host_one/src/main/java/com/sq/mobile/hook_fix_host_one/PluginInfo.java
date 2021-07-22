package com.sq.mobile.hook_fix_host_one;


import dalvik.system.DexClassLoader;

public class PluginInfo {
    private String dexPath;
    private DexClassLoader classLoader;

    public PluginInfo(String dexPath, DexClassLoader classLoader) {
        this.dexPath = dexPath;
        this.classLoader = classLoader;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public String getDexPath() {
        return dexPath;
    }
}
