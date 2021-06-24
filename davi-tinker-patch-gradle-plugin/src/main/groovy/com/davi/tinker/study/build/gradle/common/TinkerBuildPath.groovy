package com.davi.tinker.study.build.gradle.common

import org.gradle.api.Project


class TinkerBuildPath {
    private static final String TINKER_INTERMEDIATES = "/intermediates/tinker_intermediates/"

    private static final String MULTIDEX_CONFIG_FILE = "tinker_multidexkeep.pro"
    private static final String PROGUARD_CONFIG_FILE = "tinker_proguard.pro"

    private static final String RESOURCE_PUBLIC_XML = "public.xml"
    private static final String RESOURCE_IDX_XML = "idx.xml"
    private static final String RESOURCE_VALUES_BACKUP = "values_backup"
    private static final String RESOURCE_PUBLIC_TXT = "public.txt"

    //it's parent dir must start with values
    private static final String RESOURCE_TO_COMPILE_PUBLIC_XML = "aapt2/res/values/tinker_public.xml"


    static String getTinkerIntermediates(Project project) {
        return "${project.buildDir}$TINKER_INTERMEDIATES"
    }

    static String getMultidexConfigPath(Project project) {
        return "${getTinkerIntermediates(project)}$MULTIDEX_CONFIG_FILE"
    }

    static String getProguardConfigPath(Project project) {
        return "${getTinkerIntermediates(project)}$PROGUARD_CONFIG_FILE"
    }

    static String getResourcePublicXml(Project project) {
        return "${getTinkerIntermediates(project)}$RESOURCE_PUBLIC_XML"
    }

    static String getResourceIdxXml(Project project) {
        return "${getTinkerIntermediates(project)}$RESOURCE_IDX_XML"
    }

    static String getResourceValuesBackup(Project project) {
        return "${getTinkerIntermediates(project)}$RESOURCE_VALUES_BACKUP"
    }

    /**
     * 1）getTinkerIntermediates
     *  - "${project.buildDir}$TINKER_INTERMEDIATES"
     *      - TINKER_INTERMEDIATES = "/intermediates/tinker_intermediates/"
     *      - project.buildDir
     *
     * 2）RESOURCE_PUBLIC_TXT = "public.txt"
     * */
    static String getResourcePublicTxt(Project project) {
        return "${getTinkerIntermediates(project)}$RESOURCE_PUBLIC_TXT"
    }

    static String getResourceToCompilePublicXml(Project project) {
        return "${getTinkerIntermediates(project)}$RESOURCE_TO_COMPILE_PUBLIC_XML"
    }
}
