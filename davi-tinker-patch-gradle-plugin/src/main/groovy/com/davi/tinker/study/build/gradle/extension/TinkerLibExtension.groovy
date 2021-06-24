
package com.davi.tinker.study.build.gradle.extension

public class TinkerLibExtension {
    /**
     * the library file patterns, which files will be deal to gen patch
     * such as [lib/armeabi/*.so, assets/libs/*.so]
     */
    Iterable<String> pattern;


    public TinkerLibExtension() {
        pattern = []
    }

    @Override
    public String toString() {
        """| pattern = ${pattern}
        """.stripMargin()
    }
}