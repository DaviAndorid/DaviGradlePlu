package com.davi.tinker.study.build.gradle.task

import com.davi.tinker.study.build.gradle.common.TinkerBuildPath
import com.davi.tinker.study.build.gradle.util.FileOperation
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction
import sun.misc.Unsafe

import java.lang.reflect.Field
import java.util.regex.Matcher
import java.util.regex.Pattern


/**
 * The configuration properties.
 *
 * @author zhangshaowen
 */
public class TinkerResourceIdTask extends DefaultTask {
    def variant
    String resDir
    String applicationId

    //if you need add public flag, set tinker.aapt2.public = true in gradle.properties
    boolean addPublicFlagForAapt2 = false

    TinkerResourceIdTask() {
        group = 'tinker'
    }

    static void injectStableIdsFileOnDemand(project) {
        /**
         * 编译和打包资源的工具
         * 一般打包过程情况下，都是由gradle自动调用aapt，将资源文件编译成二进制文件
         * 【AAPT2】
         * Android Gradle 插件 3.0.0 及更高版本默认情况下会启用 AAPT2
         * 官方：https://developer.android.com/studio/command-line/aapt2?hl=zh-cn
         * 简书：https://www.jianshu.com/p/839969887e2c
         *
         *
         * 【AAPT1】
         * 只改变了一个资源文件，也要进行全量编译
         *
         *
         * 【AAPT2】
         * 1）aapt2将原先的资源编译打包过程拆分成了两部分，即编译和链接。
         *      - 编译：将资源文件编译为二进制格式文件
         *      - 链接：将编译后的所有文件合并，打包成一个单独文件
         *
         *      好处：
         *          - 这种方式可以很好的提升资源的编译性能，比如只有一个资源文件发送改变时，
         *          你只需要重新编译改变的文件，然后将其与其他未改变的资源进行链接即可
         *
         *          - 而之前的aapt是将所有资源进行merge，merge完后将所有资源进行编译，产生一个资源ap_文件，
         *          该文件是一个压缩包，这样带来的后果就是即使只改变了一个资源文件，也要进行全量编译。
         *
         *
         * ？？？
         * 没有开启aapt2的话，为什么要《skip stable ids inject》？？？？
         * */
        if (!isAapt2EnabledCompat(project)) {
            project.logger.error('AApt2 is not enabled, skip stable ids inject.')
            return
        }


        /**
         *
         * thinker：
         *  开启了aapt2
         *  所以要 ：
         *      inject ${stableIdsFile.getAbsolutePath()} into aapt options
         *  目的是：
         *      固定id的同时，将该资源进行导出，打上public标记，供其他资源进行引用
         *
         * 知识点：
         * 1）如何使用aapt2固定资源id
         * 主要介绍如何在固定id的同时，将该资源进行导出，打上public标记，供其他资源进行引用
         * 链接：https://blog.csdn.net/omnispace/article/details/79803149
         * */
        //【关键点】指定稳定的资源id映射文件，达到固定资源id的作用
        //stableIdsFile = project.buildDir/intermediates/tinker_intermediates/public.txt
        def stableIdsFile = project.file(TinkerBuildPath.getResourcePublicTxt(project))
        if (!stableIdsFile.exists()) {
            stableIdsFile.getParentFile().mkdirs()
        } else {
            FileOperation.deleteFile(stableIdsFile)
        }
        // Create an empty file here to make aapt2 happy before stableIdsFile is generated.
        stableIdsFile.createNewFile()

        /**
         * ***********************
         * 给 aapt 执行时，添加额外参数
         * ************************
         *
         * aaptOptions.additionalParameters：
         * 类型：List< String >
         * 描述：给 aapt 执行时添加额外参数，添加的参数可通过 aapt --help 进行查看。
         * 链接：https://juejin.cn/post/6844904031060492302
         * 官方链接：https://developer.android.com/reference/tools/gradle-api/4.2/com/android/build/api/dsl/AaptOptions
         * */
        def additionalParams = project.android.aaptOptions.additionalParameters
        if (additionalParams == null) {
            additionalParams = new ArrayList<>()
            project.android.aaptOptions.additionalParameters = additionalParams
        }
        //【关键点】文件可以被–stable-ids参数使用
        additionalParams.add('--stable-ids')
        additionalParams.add(stableIdsFile.getAbsolutePath())
        project.logger.error("AApt2 is enabled, inject ${stableIdsFile.getAbsolutePath()} into aapt options.")
    }

    void ensureStableIdsArgsWasInjected(agpProcessResourcesTask) {
        if (!isAapt2EnabledCompat(project)) {
            return
        }
        def aaptOptions
        try {
            aaptOptions = agpProcessResourcesTask.aaptOptions
        } catch (Throwable ignored) {
            aaptOptions = null
        }
        if (aaptOptions == null) {
            def currClazz = agpProcessResourcesTask.getClass().getSuperclass()
            while (true) {
                try {
                    def field = currClazz.getDeclaredField('aaptOptions')
                    field.setAccessible(true)
                    aaptOptions = field.get(agpProcessResourcesTask)
                    break
                } catch (NoSuchFieldException ignored) {
                    if (!currClazz.equals(Object.class)) {
                        currClazz = currClazz.getSuperclass()
                    } else {
                        break
                    }
                }
            }
        }
        // It's wired that only AGP 3.5.x needs this ensurance logic. In newer version of AGP, aaptOptions field
        // is gone, which let us skip the rest logic.
        if (aaptOptions != null) {
            def additionalParameters = aaptOptions.additionalParameters
            if (additionalParameters == null) {
                additionalParameters = new ArrayList<String>()
                replaceFinalField(aaptOptions.getClass(), 'additionalParameters', aaptOptions, additionalParameters)
            }
            if (!additionalParameters.contains('--stable-ids')) {
                additionalParameters.add('--stable-ids')
                def stableIdsFile = project.file(TinkerBuildPath.getResourcePublicTxt(project))
                additionalParameters.add(stableIdsFile.getAbsolutePath())
                project.logger.error("AApt2 is enabled, and tinker ensures that ${stableIdsFile.getAbsolutePath()} is injected into aapt options.")
            }
        }
    }

    static void replaceFinalField(Class<?> clazz, String fieldName, Object instance, Object fieldValue) {
        Class currClazz = clazz
        Field field
        while (true) {
            try {
                field = currClazz.getDeclaredField(fieldName)
                break
            } catch (NoSuchFieldException e) {
                if (currClazz.equals(Object.class)) {
                    throw e
                } else {
                    currClazz = currClazz.getSuperclass()
                }
            }
        }
        final Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe")
        unsafeField.setAccessible(true)
        final Unsafe unsafe = (Unsafe) unsafeField.get(null)
        final long fieldOffset = unsafe.objectFieldOffset(field)
        unsafe.putObject(instance, fieldOffset, fieldValue)
    }

    /**
     * get android gradle plugin version by reflect
     */
    static String getAndroidGradlePluginVersionCompat() {
        String version = null
        try {
            Class versionModel = Class.forName("com.android.builder.model.Version")
            def versionFiled = versionModel.getDeclaredField("ANDROID_GRADLE_PLUGIN_VERSION")
            versionFiled.setAccessible(true)
            version = versionFiled.get(null)
        } catch (Exception e) {

        }
        return version
    }

    /**
     * get enum obj by reflect
     */
    static <T> T resolveEnumValue(String value, Class<T> type) {
        for (T constant : type.getEnumConstants()) {
            if (constant.toString().equalsIgnoreCase(value)) {
                return constant
            }
        }
        return null
    }

    /**
     * get com.android.build.gradle.options.ProjectOptions obj by reflect
     */
    static def getProjectOptions(Project project) {
        try {
            def basePlugin = project.getPlugins().hasPlugin('com.android.application') ? project.getPlugins().findPlugin('com.android.application') : project.getPlugins().findPlugin('com.android.library')
            return Class.forName("com.android.build.gradle.BasePlugin").getMetaClass().getProperty(basePlugin, 'projectOptions')
        } catch (Exception e) {
        }
        return null
    }

    /**
     * get whether aapt2 is enabled
     */
    static boolean isAapt2EnabledCompat(Project project) {
        if (getAndroidGradlePluginVersionCompat() >= '3.3.0') {
            //when agp' version >= 3.3.0, use aapt2 default and no way to switch to aapt.
            return true
        }
        boolean aapt2Enabled = false
        try {
            def projectOptions = getProjectOptions(project)
            Object enumValue = resolveEnumValue("ENABLE_AAPT2", Class.forName("com.android.build.gradle.options.BooleanOption"))
            aapt2Enabled = projectOptions.get(enumValue)
        } catch (Exception e) {
            try {
                //retry for agp <= 2.3.3
                //when agp <= 2.3.3, the field is store in com.android.build.gradle.AndroidGradleOptions
                Class classAndroidGradleOptions = Class.forName("com.android.build.gradle.AndroidGradleOptions")
                def isAapt2Enabled = classAndroidGradleOptions.getDeclaredMethod("isAapt2Enabled", Project.class)
                isAapt2Enabled.setAccessible(true)
                aapt2Enabled = isAapt2Enabled.invoke(null, project)
            } catch (Exception e1) {
                //if we can't get it, it means aapt2 is not support current.
                aapt2Enabled = false
            }
        }
        return aapt2Enabled
    }

    /**
     * get real name for all resources in R.txt by values files
     */
    Map<String, String> getRealNameMap() {
        Map<String, String> realNameMap = new HashMap<>()
        def mergeResourcesTask = Compatibilities.getMergeResourcesTask(project, variant)
        List<File> resDirCandidateList = new ArrayList<>()
        try {
            def output = mergeResourcesTask.outputDir
            if (output instanceof File) {
                resDirCandidateList.add(output)
            } else {
                resDirCandidateList.add(output.getAsFile().get())
            }
        } catch (Exception ignore) {

        }

        resDirCandidateList.add(new File(mergeResourcesTask.getIncrementalFolder(), "merged.dir"))
        resDirCandidateList.each {
            it.eachFileRecurse(FileType.FILES) {
                if (it.getParentFile().getName().startsWith("values") && it.getName().startsWith("values") && it.getName().endsWith(".xml")) {
                    File destFile = new File(project.file(TinkerBuildPath.getResourceValuesBackup(project)), "${it.getParentFile().getName()}/${it.getName()}")
                    GFileUtils.deleteQuietly(destFile)
                    GFileUtils.mkdirs(destFile.getParentFile())
                    GFileUtils.copyFile(it, destFile)
                }
            }
        }
        project.file(TinkerBuildPath.getResourceValuesBackup(project)).eachFileRecurse(FileType.FILES) {
            new XmlParser().parse(it).each {
                String originalName = "${it.@name}".toString()
                //replace . to _ for all types with the same converting rule
                if (originalName.contains('.') || originalName.contains(':')) {
                    // only record names with '.' or ':', for sake of memory
                    String sanitizeName = originalName.replaceAll("[.:]", "_");
                    realNameMap.put(sanitizeName, originalName)
                }
            }
        }
        return realNameMap
    }

    /**
     * get the sorted stable id lines
     */
    /*
    ArrayList<String> getSortedStableIds(Map<RDotTxtEntry.RType, Set<RDotTxtEntry>> rTypeResourceMap) {
        List<String> sortedLines = new ArrayList<>()
        Map<String, String> realNameMap = getRealNameMap()
        rTypeResourceMap?.each { key, entries ->
            entries.each {
                //the name in R.txt which has replaced . to _
                //so we should get the original name for it
                def name = realNameMap.get(it.name) ?: it.name
                if (it.type == RDotTxtEntry.RType.STYLEABLE) {
                    //ignore styleable type, also public.xml ignore it.
                    return
                } else {
                    sortedLines.add("${applicationId}:${it.type}/${name} = ${it.idValue}")

                    //there is a special resource type for drawable which called nested resource.
                    //such as avd_hide_password and avd_show_password resource in support design sdk.
                    //the nested resource is start with $, such as $avd_hide_password__0 and $avd_hide_password__1
                    //but there is none nested resource in R.txt, so ignore it just now.
                }
            }
        }
        //sort it and see the diff content conveniently
        Collections.sort(sortedLines)
        return sortedLines
    }*/

    /**
     * convert public.txt to public.xml
     */
    @SuppressWarnings("GrMethodMayBeStatic")
    void convertPublicTxtToPublicXml(File publicTxtFile, File publicXmlFile, boolean withId) {
        if (publicTxtFile == null) {
            return
        }
        GFileUtils.deleteQuietly(publicXmlFile)
        GFileUtils.mkdirs(publicXmlFile.getParentFile())
        GFileUtils.touch(publicXmlFile)

        publicXmlFile.append("<!-- AUTO-GENERATED FILE.  DO NOT MODIFY -->")
        publicXmlFile.append("\n")
        publicXmlFile.append("<resources>")
        publicXmlFile.append("\n")
        Pattern linePattern = Pattern.compile(".*?:(.*?)/(.*?)\\s+=\\s+(.*?)")

        publicTxtFile?.eachLine { def line ->
            Matcher matcher = linePattern.matcher(line)
            if (matcher.matches() && matcher.groupCount() == 3) {
                String resType = matcher.group(1)
                String resName = matcher.group(2)
                if (resName.startsWith('$')) {
                    project.logger.error("ignore convert to public res ${resName} because it's a nested resource")
                } else if (resType.equalsIgnoreCase("styleable")) {
                    project.logger.error("ignore convert to public res ${resName} because it's a styleable resource")
                } else {
                    if (withId) {
                        publicXmlFile.append("\t<public type=\"${resType}\" name=\"${resName}\" id=\"${matcher.group(3)}\" />\n")
                    } else {
                        publicXmlFile.append("\t<public type=\"${resType}\" name=\"${resName}\" />\n")
                    }
                }
            }
        }
        publicXmlFile.append("</resources>")
    }

    /**
     * compile xml file to flat file
     */
    void compileXmlForAapt2(File xmlFile) {
        if (xmlFile == null || !xmlFile.exists()) {
            return
        }

        def variantData = variant.getMetaClass().getProperty(variant, 'variantData')
        def variantScope = variantData.getScope()
        def globalScope = variantScope.getGlobalScope()
        def androidBuilder = globalScope.getAndroidBuilder()
        def targetInfo = androidBuilder.getTargetInfo()
        def buildTools = targetInfo.getBuildTools()
        Map paths = buildTools.getMetaClass().getProperty(buildTools, "mPaths")
        String aapt2Path = paths.get(resolveEnumValue("AAPT2", Class.forName('com.android.sdklib.BuildToolInfo$PathId')))

        if (aapt2Path == null || aapt2Path.isEmpty()) {
            try {
                //may be from maven, the flat magic number don't match. so we should also use the aapt2 from maven.
                Class aapt2MavenUtilsClass = Class.forName("com.android.build.gradle.internal.res.Aapt2MavenUtils")
                def getAapt2FromMavenMethod = aapt2MavenUtilsClass.getDeclaredMethod("getAapt2FromMaven", Class.forName("com.android.build.gradle.internal.scope.GlobalScope"))
                getAapt2FromMavenMethod.setAccessible(true)
                def aapt2FromMaven = getAapt2FromMavenMethod.invoke(null, globalScope)
                //noinspection UnnecessaryQualifiedReference
                aapt2Path = aapt2FromMaven.singleFile.toPath().resolve(com.android.SdkConstants.FN_AAPT2)
            } catch (Throwable thr) {
                throw new GradleException('Fail to get aapt2 path', thr)
            }
        }

        project.logger.error("tinker get aapt2 path ${aapt2Path}")
        def mergeResourcesTask = Compatibilities.getMergeResourcesTask(project, variant)
        if (xmlFile.exists()) {
            project.exec { def execSpec ->
                execSpec.executable "${aapt2Path}"
                execSpec.args("compile")
                execSpec.args("--legacy")
                execSpec.args("-o")
                execSpec.args("${mergeResourcesTask.outputDir}")
                execSpec.args("${xmlFile}")
            }
        }
    }

    @TaskAction
    def applyResourceId() {
        //todo


        /*

        String resourceMappingFile = project.extensions.tinkerPatch.buildConfig.applyResourceMapping

        // Parse the public.xml and ids.xml
        if (!FileOperation.isLegalFile(resourceMappingFile)) {
            project.logger.error("apply resource mapping file ${resourceMappingFile} is illegal, just ignore")
            return
        }
        project.logger.error("we build ${project.getName()} apk with apply resource mapping file ${resourceMappingFile}")
        project.extensions.tinkerPatch.buildConfig.usingResourceMapping = true
        Map<RDotTxtEntry.RType, Set<RDotTxtEntry>> rTypeResourceMap = PatchUtil.readRTxt(resourceMappingFile)


        if (!isAapt2EnabledCompat(project)) {
            String idsXml = resDir + "/values/ids.xml";
            String publicXml = resDir + "/values/public.xml";
            FileOperation.deleteFile(idsXml);
            FileOperation.deleteFile(publicXml);
            List<String> resourceDirectoryList = new ArrayList<String>()
            resourceDirectoryList.add(resDir)

            AaptResourceCollector aaptResourceCollector = AaptUtil.collectResource(resourceDirectoryList, rTypeResourceMap)
            PatchUtil.generatePublicResourceXml(aaptResourceCollector, idsXml, publicXml)
            File publicFile = new File(publicXml)
            if (publicFile.exists()) {
                String resourcePublicXml = TinkerBuildPath.getResourcePublicXml(project)
                FileOperation.copyFileUsingStream(publicFile, project.file(resourcePublicXml))
                project.logger.error("tinker gen resource public.xml in ${resourcePublicXml}")
            }
            File idxFile = new File(idsXml)
            if (idxFile.exists()) {
                String resourceIdxXml = TinkerBuildPath.getResourceIdxXml(project)
                FileOperation.copyFileUsingStream(idxFile, project.file(resourceIdxXml))
                project.logger.error("tinker gen resource idx.xml in ${resourceIdxXml}")
            }
        } else {
            File stableIdsFile = project.file(TinkerBuildPath.getResourcePublicTxt(project))
            FileOperation.deleteFile(stableIdsFile);
            ArrayList<String> sortedLines = getSortedStableIds(rTypeResourceMap)

            sortedLines?.each {
                stableIdsFile.append("${it}\n")
            }

            def processResourcesTask = Compatibilities.getProcessResourcesTask(project, variant)
            processResourcesTask.doFirst {
                ensureStableIdsArgsWasInjected(processResourcesTask)

                if (project.hasProperty("tinker.aapt2.public")) {
                    addPublicFlagForAapt2 = project.ext["tinker.aapt2.public"]?.toString()?.toBoolean()
                }

                if (addPublicFlagForAapt2) {
                    //if we need add public flag for resource, we need to compile public.xml to .flat file
                    //it's parent dir must start with values
                    File publicXmlFile = project.file(TinkerBuildPath.getResourceToCompilePublicXml(project))
                    //convert public.txt to public.xml
                    convertPublicTxtToPublicXml(stableIdsFile, publicXmlFile, false)
                    //dest file is mergeResourceTask output dir
                    compileXmlForAapt2(publicXmlFile)
                }
            }
        }

        */
    }
}



