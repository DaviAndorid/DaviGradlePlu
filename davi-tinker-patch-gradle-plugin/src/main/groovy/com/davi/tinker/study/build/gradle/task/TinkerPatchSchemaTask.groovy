package com.davi.tinker.study.build.gradle.task

import com.davi.tinker.study.build.gradle.extension.TinkerPatchExtension
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

public class TinkerPatchSchemaTask extends DefaultTask {
    /**
     * project.tinkerPatch
     * 外面传进来的配置
     * */
    TinkerPatchExtension configuration

    /**
     * 配置
     * project.extensions.android
     * */
    def android

    String buildApkPath

    String outputFolder

    def signConfig

    public TinkerPatchSchemaTask() {
        description = 'Assemble Tinker Patch'
        group = 'tinker'
        /**
         * 现象：
         *  upToDateWhen使用false，表示 TinkerPatchSchemaTask 这个task将会一直被执行，并不会使用到缓存
         *
         * 链接：
         *  https://segmentfault.com/a/1190000039218616
         *
         *  thinker为了保证配置的实时性，所以做了这个配置，确保配置读取的不是缓存的
         * */
        outputs.upToDateWhen { false }

        configuration = project.tinkerPatch
        android = project.extensions.android
    }

    void check() {
        println getClass().getSimpleName() + '---> check...'

        //校验 oldApk
        configuration.checkParameter()
        //校验 tinkerId
        configuration.buildConfig.checkParameter()
        //校验 largeModSize（默认100kb）
        configuration.res.checkParameter()
        //校验是否raw/jar模式，都不是则抛异常
        configuration.dex.checkDexMode()
        //指定path ？？？？
        configuration.sevenZip.resolveZipFinalPath()
    }

    /**
     * task中的3大要素之一的任务执行
     * */
    @TaskAction
    def tinkerPatch() {
        println getClass().getSimpleName() + '---> TaskAction ---> tinkerPatch'

        check()

        /**
         * *********
         * 暂时不展开
         * *********
         *  Runner.gradleRun(inputParam)
         *  1）输入参数的准备，也就是tinkerPatch的配置转 InputParam
         *  2）tinker-patch-lib 基础模块中的Builder
         * */
        /*
        InputParam.Builder builder = new InputParam.Builder()
        if (configuration.useSign) {
            if (signConfig == null) {
                throw new GradleException("can't the get signConfig for this build")
            }
            builder.setSignFile(signConfig.storeFile)
                    .setKeypass(signConfig.keyPassword)
                    .setStorealias(signConfig.keyAlias)
                    .setStorepass(signConfig.storePassword)
        }

        def buildApkFile = new File(buildApkPath)
        def oldApkFile = new File(configuration.oldApk)
        def newApks = [] as TreeSet<File>
        def oldApks = [] as TreeSet<File>
        def oldApkNames = [] as HashSet<String>
        def newApkNames = [] as HashSet<String>
        if (buildApkFile.isDirectory() && oldApkFile.isDirectory()) {
            // Directory mode
            oldApkFile.eachFile {
                if (it.name.endsWith('.apk')) {
                    oldApks << it
                    oldApkNames << it.getName()
                }
            }
            buildApkFile.eachFile {
                if (it.name.endsWith('.apk')) {
                    newApks << it
                    newApkNames << it.getName()
                }
            }

            def unmatchedOldApkNames = new HashSet<>(oldApkNames)
            unmatchedOldApkNames.removeAll(newApkNames)

            def unmatchedNewApkNames = new HashSet<>(newApkNames)
            unmatchedNewApkNames.removeAll(oldApkNames)

            if (!unmatchedOldApkNames.isEmpty() || !unmatchedNewApkNames.isEmpty()) {
                throw new GradleException("Both oldApk and newApk args are directories"
                        + " but apks inside them are not matched.\n"
                        + " unmatched old apks: ${unmatchedOldApkNames}\n"
                        + " unmatched new apks: ${unmatchedNewApkNames}."
                )
            }
        } else if (buildApkFile.isFile() && oldApkFile.isFile()) {
            // File mode
            newApks << buildApkFile
            oldApks << oldApkFile
        } else {
            throw new GradleException("oldApk [${oldApkFile.getAbsolutePath()}] and newApk [${buildApkFile.getAbsolutePath()}] must be both files or directories.")
        }

        def tmpDir = new File("${project.buildDir}/tmp/tinkerPatch")
        tmpDir.mkdirs()
        def outputDir = new File(outputFolder)
        outputDir.mkdirs()

        for (def i = 0; i < newApks.size(); ++i) {
            def oldApk = oldApks[i] as File
            def newApk = newApks[i] as File

            def packageConfigFields = new HashMap<String, String>(configuration.packageConfig.getFields())
            packageConfigFields.putAll(configuration.packageConfig.getApkSpecFields(newApk.getName()))

            builder.setOldApk(oldApk.getAbsolutePath())
                    .setNewApk(newApk.getAbsolutePath())
                    .setOutBuilder(tmpDir.getAbsolutePath())
                    .setIgnoreWarning(configuration.ignoreWarning)
                    .setAllowLoaderInAnyDex(configuration.allowLoaderInAnyDex)
                    .setRemoveLoaderForAllDex(configuration.removeLoaderForAllDex)
                    .setDexFilePattern(new ArrayList<String>(configuration.dex.pattern))
                    .setIsProtectedApp(configuration.buildConfig.isProtectedApp)
                    .setIsComponentHotplugSupported(configuration.buildConfig.supportHotplugComponent)
                    .setDexLoaderPattern(new ArrayList<String>(configuration.dex.loader))
                    .setDexIgnoreWarningLoaderPattern(new ArrayList<String>(configuration.dex.ignoreWarningLoader))
                    .setDexMode(configuration.dex.dexMode)
                    .setSoFilePattern(new ArrayList<String>(configuration.lib.pattern))
                    .setResourceFilePattern(new ArrayList<String>(configuration.res.pattern))
                    .setResourceIgnoreChangePattern(new ArrayList<String>(configuration.res.ignoreChange))
                    .setResourceIgnoreChangeWarningPattern(new ArrayList<String>(configuration.res.ignoreChangeWarning))
                    .setResourceLargeModSize(configuration.res.largeModSize)
                    .setUseApplyResource(configuration.buildConfig.usingResourceMapping)
                    .setConfigFields(packageConfigFields)
                    .setSevenZipPath(configuration.sevenZip.path)
                    .setUseSign(configuration.useSign)
                    .setArkHotPath(configuration.arkHot.path)
                    .setArkHotName(configuration.arkHot.name)

            InputParam inputParam = builder.create()
            Runner.gradleRun(inputParam)

            def prefix = newApk.name.take(newApk.name.lastIndexOf('.'))
            tmpDir.eachFile(FileType.FILES) {
                if (!it.name.endsWith(".apk")) {
                    return
                }
                final File dest = new File(outputDir, "${prefix}-${it.name}")
                it.renameTo(dest)
            }
        }*/
    }
}