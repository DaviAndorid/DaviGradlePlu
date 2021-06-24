package com.davi.tinker.study.build.gradle.extension


import org.gradle.api.GradleException;


public class TinkerPatchExtension {
    /**
     * Specifies the old apk path to diff with the new apk
     */
    String oldApk

    /**
     * Specify a folder for the outputs where place the tinker patch results.
     */
    String outputFolder

    /**
     * Specify the new apk path instead of running assemble task again.
     */
    String newApk;

    /**
     * If there is loader class changes,
     * or Activity, Service, Receiver, Provider change, it will terminal
     * if ignoreWarning is false
     * default: false
     */
    boolean ignoreWarning

    /**
     *
     * Allow loader class existence in any class loader.
     *
     * This will suppress the exception like:
     * <pre>
     * loader classes are found in old secondary dex. Found classes: ...
     * loader classes are found in new secondary dex. Found classes: ...
     * </pre>
     *
     * <p>Since Android Gradle Plugin 3.3.0, there is no simply way to keep all loader classes in
     * the primary dex file if your application's min sdk version is 21 or above. In this situation, you
     * can turn the {@link #removeLoaderForAllDex} and {@link #allowLoaderInAnyDex} to true and
     * tolerate the loader classes to exists in any dex file.
     *
     * <p>default: false
     */
    boolean allowLoaderInAnyDex

    /**
     * Whether to remove loader class for every dex file. When false, we will assume the loader
     * class only exists in the main dex(classes.dex).
     *
     * <p>If the loader class may exists in any dex, you must set this to true. Otherwise, you patch will
     * cause tinker runtime load failed. But this will cause a little increment on the size
     * of the patch file.
     *
     * <p>Since Android Gradle Plugin 3.3.0, there is no simply way to keep all loader classes in
     * the primary dex file if your application's min sdk version is 21 or above. In this situation, you
     * can turn the {@link #removeLoaderForAllDex} and {@link #allowLoaderInAnyDex} to true and
     * tolerate the loader classes to exists in any dex file.
     *
     * <p>default: false
     */
    boolean removeLoaderForAllDex

    /**
     * If sign the patch file with the android signConfig
     * default: true
     */
    boolean useSign

    /**
     * whether use tinker
     * default: true
     */
    boolean tinkerEnable

    public TinkerPatchExtension() {
        oldApk = ""
        outputFolder = ""
        newApk = ""
        ignoreWarning = false
        allowLoaderInAnyDex = false
        removeLoaderForAllDex = false
        useSign = true
        tinkerEnable = true
    }

    void checkParameter() {
        if (oldApk == null) {
            throw new GradleException("old apk is null, you must set the correct old apk value!")
        }
        File apk = new File(oldApk)
        if (!apk.exists()) {
            throw new GradleException("old apk ${oldApk} is not exist, you must set the correct old apk value!")
        }
    }

    @Override
    public String toString() {
        """| oldApk = ${oldApk}
           | outputFolder = ${outputFolder}
           | newApk = ${newApk}
           | ignoreWarning = ${ignoreWarning}
           | removeLoaderForAllDex = ${removeLoaderForAllDex}
           | tinkerEnable = ${tinkerEnable}
           | useSign = ${useSign}
        """.stripMargin()
    }
}