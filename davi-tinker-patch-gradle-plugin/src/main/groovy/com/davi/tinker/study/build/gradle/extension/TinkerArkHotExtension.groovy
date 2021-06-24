package com.davi.tinker.study.build.gradle.extension

public class TinkerArkHotExtension {
    String path;
    String name;

    public TinkerArkHotExtension() {
        path = "arkHot";
        name = "patch.apk";
    }

    @Override
    public String toString() {
        """| path= ${path}
           | name= ${name}
         """.stripMargin()
    }
}