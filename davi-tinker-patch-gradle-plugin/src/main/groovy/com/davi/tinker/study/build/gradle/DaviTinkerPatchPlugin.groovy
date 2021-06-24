package com.davi.tinker.study.build.gradle

import com.android.build.gradle.api.ApkVariant
import com.davi.tinker.study.build.gradle.task.TinkerManifestTask
import com.davi.tinker.study.build.gradle.task.TinkerPatchSchemaTask
import com.davi.tinker.study.build.gradle.task.TinkerProguardConfigTask
import com.davi.tinker.study.build.gradle.task.TinkerResourceIdTask
import com.davi.tinker.study.build.gradle.util.Compatibilities
import com.davi.tinker.study.build.gradle.util.FileOperation
import com.davi.tinker.study.build.gradle.util.TypedValue
import com.davi.tinker.study.build.gradle.util.Utils
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import com.davi.tinker.study.build.gradle.extension.*
import org.gradle.api.Task
import org.jetbrains.annotations.NotNull

class DaviTinkerPatchPlugin implements Plugin<Project> {

    static void aapt2(Project project) {
        /***
         * 确认是否 aapt2，如果是：
         *  - 固定id的同时，将该资源进行导出，打上public标记，供其他资源进行引用
         * */
        //TinkerBuildPath.getResourcePublicTxt(project)
        TinkerResourceIdTask.injectStableIdsFileOnDemand(project)
    }

    void applicationVariantsAll(project, configuration) {
        def android = project.extensions.android
        /**
         * 变体：
         * 1）https://developer.android.com/studio/build/build-variants?hl=zh-cn
         **/
        android.applicationVariants.all { ApkVariant variant ->
            def variantName = variant.name
            def capitalizedVariantName = variantName.capitalize()
            println this.getClass().getSimpleName() + ': applicationVariants.all --> variantName = ' + variantName
            println this.getClass().getSimpleName() + ': applicationVariants.all --> capitalizedVariantName = ' + capitalizedVariantName

            /***
             *
             * 1）获取instant run 的Task
             * 2）think 不支持 instant run 的模式
             * 3）instant-run模式
             *      - Instant Run的作用是使得开发过程中的改动可以不用完整编译并重新安装app就能应用，
             *      也就是更快看到改动的实际效果，节省时间
             *      - 链接：https://juejin.cn/post/6844903952287268877
             * */
            def instantRunTask = getInstantRunTask(variantName, project)


            /**
             * ***************************
             * tinkerPatchBuildTask
             * ***************************
             *     - 输入参数的准备，也就是tinkerPatch的配置（如：project.tinkerPatch） 转 InputParam
             *     - 然后转成了InputParam之后，执行核心处理方法：Runner.gradleRun(inputParam) --> 这里不在这里展开...
             *
             * 执行时机：
             *      在《assemble xxx》后执行
             * */
            TinkerPatchSchemaTask tinkerPatchBuildTask = project.tasks.create("tinkerPatch${capitalizedVariantName}", TinkerPatchSchemaTask)
            tinkerPatchBuildTask.signConfig = variant.signingConfig


            /**
             * *******************
             * tinkerManifestTask
             * *******************
             *   - add a build TINKER_ID to AndroidManifest.xml
             *
             * 执行时机：
             *  在《ProcessManifestTask》后执行
             * */
            def agpProcessManifestTask = Compatibilities.getProcessManifestTask(project, variant)
            def tinkerManifestTask = project.tasks.create("tinkerProcess${capitalizedVariantName}Manifest", TinkerManifestTask)
            tinkerManifestTask.mustRunAfter agpProcessManifestTask


            /***
             *
             *
             * */
            variant.outputs.each { variantOutput ->
                /***
                 * ***************
                 * tinkerPatchBuildTask
                 * ***************
                 * */
                //初始化 tinkerPatchBuildTask 中的 buildApkPath 参数
                setPatchNewApkPath(configuration, variantOutput, tinkerPatchBuildTask)
                //任务依赖的顺序
                tinkerPatchBuildTask.dependsOn Compatibilities.getAssembleTask(project, variant)
                //配置 tinkerPatchBuildTask中的 outputFolder 参数
                setPatchOutputFolder(configuration, variantOutput, variant, tinkerPatchBuildTask)

                /**
                 * ********************
                 * tinkerManifestTask
                 * ********************
                 * （1）找到清单文件
                 * （2）传给 tinkerManifestTask 的 outputNameToManifestMap
                 * （3）tinkerManifestTask 执行时机
                 * */
                //【dirName】Returns a subfolder name for the variant output.
                def outputName = variantOutput.dirName
                if (outputName.endsWith("/")) {
                    outputName = outputName.substring(0, outputName.length() - 1)
                }
                if (tinkerManifestTask.outputNameToManifestMap.containsKey(outputName)) {
                    throw new GradleException("Duplicate tinker manifest output name: '${outputName}'")
                }
                def manifestPath = Compatibilities.getOutputManifestPath(project, agpProcessManifestTask, variantOutput)
                tinkerManifestTask.outputNameToManifestMap.put(outputName, manifestPath)
                //tinkerManifestTask 执行时机
                def agpProcessResourcesTask = Compatibilities.getProcessResourcesTask(project, variant)
                agpProcessResourcesTask.dependsOn tinkerManifestTask

                /**
                 * *********************
                 * TinkerResourceIdTask
                 * *********************
                 * resource id
                 * 固定id的同时，将该资源进行导出，打上public标记，供其他资源进行引用
                 * */
                TinkerResourceIdTask applyResourceTask = project.tasks.create("tinkerProcess${capitalizedVariantName}ResourceId", TinkerResourceIdTask)
                applyResourceTask.variant = variant
                applyResourceTask.applicationId = Compatibilities.getApplicationId(project, variant)
                applyResourceTask.resDir = Compatibilities.getInputResourcesDirectory(project, agpProcessResourcesTask)
                //let applyResourceTask run after manifestTask
                applyResourceTask.mustRunAfter tinkerManifestTask
                agpProcessResourcesTask.dependsOn applyResourceTask
                // Fix issue-866.
                // We found some case that applyResourceTask run after mergeResourcesTask, it caused 'applyResourceMapping' config not work.
                // The task need merged resources to calculate ids.xml, it must depends on merge resources task.
                def agpMergeResourcesTask = Compatibilities.getMergeResourcesTask(project, variant)
                applyResourceTask.dependsOn agpMergeResourcesTask

                /*********************************
                 * 混淆，TinkerProguardConfigTask
                 * *******************************
                 * */
                // Add this proguard settings file to the list
                boolean proguardEnable = variant.getBuildType().buildType.minifyEnabled
                if (proguardEnable) {
                    //开启了混淆的情况下，写混淆文件
                    TinkerProguardConfigTask proguardConfigTask = project.tasks.create("tinkerProcess${capitalizedVariantName}Proguard", TinkerProguardConfigTask)
                    proguardConfigTask.applicationVariant = variant
                    proguardConfigTask.mustRunAfter tinkerManifestTask

                    //找官方混淆task
                    def obfuscateTask = getObfuscateTask(variantName, project)
                    //自定义的混淆task挂在在《官方混淆task》
                    obfuscateTask.dependsOn proguardConfigTask
                }

                //todo..
            }
        }
    }

    /****
     * 找官方混淆task
     * */
    @NotNull
    Task getObfuscateTask(String variantName, Project project) {
        /***
         * transformClassesAndResourcesWithProguardFor:
         *      - 这个task是处理类和资源混淆的
         *
         * 地址：https://cloud.tencent.com/developer/article/1160071
         * */
        String proguardTransformTaskName = "transformClassesAndResourcesWithProguardFor${variantName.capitalize()}"
        def proguardTransformTask = project.tasks.findByName(proguardTransformTaskName)
        if (proguardTransformTask != null) {
            return proguardTransformTask
        }

        /**
         * transformClassesAndResourcesWithR8ForDebug
         * */
        String r8TransformTaskName = "transformClassesAndResourcesWithR8For${variantName.capitalize()}"
        def r8TransformTask = project.tasks.findByName(r8TransformTaskName)
        if (r8TransformTask != null) {
            return r8TransformTask
        }

        String r8TaskName = "minify${variantName.capitalize()}WithR8"
        def r8Task = project.tasks.findByName(r8TaskName)
        if (r8Task != null) {
            return r8Task
        }

        String proguardTaskName = "minify${variantName.capitalize()}WithProguard"
        def proguardTask = project.tasks.findByName(proguardTaskName)
        if (proguardTask != null) {
            return proguardTask
        }

        // in case that Google changes the task name in later versions
        throw new GradleException(String.format("The minifyEnabled is enabled for '%s', but " +
                "tinker cannot find the task. Please submit issue to us: %s", variantName, ISSUE_URL))
    }


    @Override
    public void apply(Project project) {
        println this.getClass().getSimpleName() + ': apply'

        //自定义拓展
        configExtensions(project)

        //配置android属性
        configAndroidExtensions(project)


        project.afterEvaluate {
            println this.getClass().getSimpleName() + ': afterEvaluate -->'
            //准备参数
            def configuration = project.tinkerPatch

            aapt2(project)

            applicationVariantsAll(project, configuration)
        }
    }

    static Task getInstantRunTask(String variantName, Project project) {
        String instantRunTask = "transformClassesWithInstantRunFor${variantName.capitalize()}"
        return project.tasks.findByName(instantRunTask)
    }

    /**
     * 初始化 tinkerPatchBuildTask 中的参数
     *      - outputFolder
     * */
    void setPatchOutputFolder(configuration, output, variant, tinkerPatchBuildTask) {
        println this.getClass().getSimpleName() + ': setPatchOutputFolder --> '

        //build/outputs/apk/release/app-release-unsigned.apk
        File parentFile = output.outputFile

        //配置的路径
        String outputFolder = "${configuration.outputFolder}"

        if (!Utils.isNullOrNil(outputFolder)) {
            println this.getClass().getSimpleName() + ': setPatchOutputFolder --> 配置了 outputFolder'

            //配置了
            outputFolder = "${outputFolder}/${TypedValue.PATH_DEFAULT_OUTPUT}/${variant.dirName}"
        } else {
            println this.getClass().getSimpleName() + ': setPatchOutputFolder --> 没有配置则基于 output.outputFile 构建'

            //没有配置则基于 output.outputFile 构建
            outputFolder =
                    "${parentFile.getParentFile().getParentFile().getAbsolutePath()}/${TypedValue.PATH_DEFAULT_OUTPUT}/${variant.dirName}"
        }

        tinkerPatchBuildTask.outputFolder = outputFolder
    }

    /**
     * 初始化 tinkerPatchBuildTask 中的参数
     *      - buildApkPath
     */
    void setPatchNewApkPath(configuration, output, tinkerPatchBuildTask) {
        def newApkPath = configuration.newApk
        println this.getClass().getSimpleName() + ': setPatchNewApkPath --> newApkPath = ' + newApkPath

        if (!Utils.isNullOrNil(newApkPath)) {
            //newApkPath存在
            if (FileOperation.isLegalFileOrDirectory(newApkPath)) {
                tinkerPatchBuildTask.buildApkPath = newApkPath
                println this.getClass().getSimpleName() + ': setPatchNewApkPath --> ok..'
                return
            }
        }

        //newApkPath 不存在，直接用output.outputFile
        tinkerPatchBuildTask.buildApkPath = output.outputFile
        println this.getClass().getSimpleName() + ': setPatchNewApkPath --> newApkPath 不存在，直接用output.outputFile'
    }


    /**
     * 自定义拓展
     * */
    void configExtensions(Project project) {
        println 'configExtensions -->'

        project.extensions.create('tinkerPatch', TinkerPatchExtension)
        project.tinkerPatch.extensions.create('buildConfig', TinkerBuildConfigExtension, project)
        project.tinkerPatch.extensions.create('dex', TinkerDexExtension, project)
        project.tinkerPatch.extensions.create('lib', TinkerLibExtension)
        project.tinkerPatch.extensions.create('res', TinkerResourceExtension)
        project.tinkerPatch.extensions.create("arkHot", TinkerArkHotExtension)
        project.tinkerPatch.extensions.create('packageConfig', TinkerPackageConfigExtension, project)
        project.tinkerPatch.extensions.create('sevenZip', TinkerSevenZipExtension, project)
    }


    /**
     * 配置android属性
     * */
    void configAndroidExtensions(Project project) {
        println 'configAndroidExtensions -->'

        def android = project.extensions.android
        try {
            /***
             * dexOptions的属性：
             * https://juejin.cn/post/6844904013142441991#heading-4
             * */
            /**
             * 预编译 dex 库。在我们build的时候会快些，但clean时便会慢，默认开启
             * */
            android.dexOptions.preDexLibraries = false
            /**
             * 每个 dex 的字符串索引也有限制，正常情况下为 2^16 个
             * 开启jumboMode 模式，可以支持到 2^32
             * 值得注意的是，这个并不是 64K方法数量限制问题的一种方案。
             * */
            android.dexOptions.jumboMode = true

            /**
             * 关闭《dex archive》
             * 减少dex的大小
             * */
            disableArchiveDex(project)

            /**
             * 功能：
             * 将所有带有运行时注解的类保留在主dex中。
             * 默认开启，主要是解决java.lang.reflect.Field.getDeclaredAnnotations导致无法获取崩溃问题
             *
             * tinker是禁止的，禁止打了运行时注解的类全部打到主dex中
             * tinker降低主dex大小
             * */
            android.dexOptions.keepRuntimeAnnotatedClasses = false
        } catch (Throwable e) {
            e.printStackTrace()
        }
    }

    /**
     **********
     * 知识点：
     **********
     * 1）Dex 文件规范明确指出：单个 dex 文件内引用的方法总数只能为 65536
     * 链接：https://zhuanlan.zhihu.com/p/323237913
     *
     * 2）gradle3.0.0分包把指定的class放到maindex里
     * 链接：https://blog.csdn.net/qq_17265737/article/details/79074494
     * PS：只能看编译之后生成的 mainDexList.txt，然后凭借经验去掉一些看起来可能 “前期不需要” 的 class，
     * 但稍微不慎都有可能导致 crash 产生
     *
     * 3）maindexlist.txt是怎么产生的
     * 链接：http://mouxuejie.com/blog/2016-06-28/multidex-maindexlist-make-analysis/
     *
     *************
     * tinker相关：
     * ***********
     *
     * ENABLE_DEX_ARCHIVE 设置为 false
     * ENABLE_DEX_ARCHIVE 属性可以减少dex的大小，但tinker把这个属性的默认值改为了false
     * ？？？：
     *  AGP 升级到 3.5.2版本后，ENABLE_DEX_ARCHIVE属性已经被废弃了，
     *  默认使用 D8MainDexListTransform，请问这会对tinker造成影响吗？
     * ？？？？：
     * minSDKVersion < 21 的情况下：
     *  在 dalvik 机器上会有影响，
     *  因为 dex archive 会破坏multidex的maindex规则，导致 loader 类分散到其他 dex 里引起 patch 加载失败。
     * 如果 minSDKVersion >= 21 就没问题了：
     *  不过要开启 tinkerPatch {} 中的 allowLoaderInAnyDex 和 removeLoaderForAllDex 设置。
     *
     * */
    void disableArchiveDex(Project project) {
        println 'disableArchiveDex -->'

        try {
            def booleanOptClazz = Class.forName('com.android.build.gradle.options.BooleanOption')
            def enableDexArchiveField = booleanOptClazz.getDeclaredField('ENABLE_DEX_ARCHIVE')
            enableDexArchiveField.setAccessible(true)
            def enableDexArchiveEnumObj = enableDexArchiveField.get(null)
            def defValField = enableDexArchiveEnumObj.getClass().getDeclaredField('defaultValue')
            defValField.setAccessible(true)
            defValField.set(enableDexArchiveEnumObj, false)
        } catch (Throwable thr) {
            // To some extends, class not found means we are in lower version of android gradle
            // plugin, so just ignore that exception.
            if (!(thr instanceof ClassNotFoundException)) {
                project.logger.error("reflectDexArchiveFlag error: ${thr.getMessage()}.")
            }
        }
    }

}



