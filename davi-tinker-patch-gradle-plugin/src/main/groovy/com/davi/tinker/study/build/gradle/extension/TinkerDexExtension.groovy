
package com.davi.tinker.study.build.gradle.extension

import org.gradle.api.GradleException
import org.gradle.api.Project


public class TinkerDexExtension {
    /**
     * raw or jar, if you want to support below 4.0, you should use jar
     * default: raw, keep the orginal file type
     */
    String dexMode;

    /**
     * the dex file patterns, which dex or jar files will be deal to gen patch
     * such as [classes.dex, classes-*.dex, assets/multiDex/*.jar]
     */
    Iterable<String> pattern;
    /**
     * the loader files, they will be removed during gen patch main dex
     * and they should be at the primary dex
     * such as [com.tencent.tinker.loader.*, com.tinker.sample.MyApplication]
     */
    Iterable<String> loader;

    Iterable<String> ignoreWarningLoader;

    private Project project;

    public TinkerDexExtension(Project project) {
        dexMode = "jar"
        pattern = []
        loader = []
        ignoreWarningLoader = []
        this.project = project
    }


    /**
     * 两种模式的区别：
     *  - http://www.mianquan.net/tutorial/tinker/spilt.8.6c807b33fd90c0e9.md
     * */
    void checkDexMode() {
        if (!dexMode.equals("raw") && !dexMode.equals("jar")) {
            throw new GradleException("dexMode can be only one of 'jar' or 'raw'!")
        }
    }

    @Override
    public String toString() {
        """| dexMode = ${dexMode}
           | pattern = ${pattern}
           | loader = ${loader}
           | ignoreWarningLoader = ${ignoreWarningLoader}
        """.stripMargin()
    }
}