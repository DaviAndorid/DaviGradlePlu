package com.davi.tinker.study.build.gradle.extension

import org.gradle.api.GradleException
import org.gradle.api.Project


public class TinkerBuildConfigExtension {

    /**
     * Specifies the old apk's mapping file for proguard to applymapping
     */
    String applyMapping

    /**
     * Specifies the old resource id mapping(R.txt) file to applyResourceMapping
     */
    String applyResourceMapping

    /**
     * because we don't want to check the base apk with md5 in the runtime(it is slow)
     * tinkerId is use to identify the unique base apk when the patch is tried to apply.
     * we can use git rev, svn rev or simply versionCode.
     * we will gen the tinkerId in your manifest automatic
     */
    String tinkerId

    /**
     * If true, Tinker will append name of each variant output to their tinker ids.
     */
    boolean appendOutputNameToTinkerId

    /**
     * Whether tinker should treat the base apk as the one being protected by app
     * protection tools.
     * If this attribute is true, the generated patch package will contain a
     * dex including all changed classes instead of any dexdiff patch-info files.
     * default: false
     */
    boolean isProtectedApp

    /**
     * Whether tinker should support component hotplug (add new component dynamically).
     * If this attribute is true, the component added in new apk will be available after
     * patch is successfully loaded. Otherwise an error would be announced when generating patch
     * on compile-time.
     *
     * <b>Notice that currently this feature is incubating and only support NON-EXPORTED Activity</b>
     */
    boolean supportHotplugComponent

    private Project project

    boolean usingResourceMapping

    /**
     * if keepDexApply is true,class in which dex refer to the old apk.
     * open this can reduce the dex diff file size.
     */
    boolean keepDexApply

    public TinkerBuildConfigExtension(Project project) {
        this.project = project
        applyMapping = ""
        applyResourceMapping = ""
        tinkerId = null
        appendOutputNameToTinkerId = false
        usingResourceMapping = false
        keepDexApply = false
        isProtectedApp = false
    }

    void checkParameter() {
        if (tinkerId == null || tinkerId.isEmpty()) {
            throw new GradleException("you must set your tinkerId to identify the base apk!")
        }
    }


    @Override
    public String toString() {
        """| applyMapping = ${applyMapping}
           | applyResourceMapping = ${applyResourceMapping}
           | isProtectedApp = ${isProtectedApp}
           | supportHotplugComponent = ${supportHotplugComponent}
           | keepDexApply = ${keepDexApply}
           | tinkerId = ${tinkerId}
           | appendOutputNameToTinkerId = ${appendOutputNameToTinkerId}
        """.stripMargin()
    }
}