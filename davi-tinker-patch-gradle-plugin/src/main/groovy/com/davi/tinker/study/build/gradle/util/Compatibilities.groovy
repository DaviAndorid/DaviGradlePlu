package com.davi.tinker.study.build.gradle.util


/***
 * 工具类：
 * 找到指定的gradle自带的task，然后进一步实现在特定的时机插入自己的任务
 * */
class Compatibilities {
    static def getApplicationId(project, variant) {
        return variant.getApplicationId()
    }

    static def getOutputManifestPath(project, manifestTask, variantOutput) {
        try {
            return new File(manifestTask.multiApkManifestOutputDirectory.get().asFile, "${variantOutput.dirName}/AndroidManifest.xml")
        } catch (Throwable ignored) {
            // Ignored.
        }
        try {
            return new File(manifestTask.manifestOutputDirectory.get().asFile, "${variantOutput.dirName}/AndroidManifest.xml")
        } catch (Throwable ignored) {
            // Ignored.
        }
        try {
            return new File(manifestTask.manifestOutputDirectory, "${variantOutput.dirName}/AndroidManifest.xml")
        } catch (Throwable ignored) {
            // Ignored
        }
        return manifestTask.manifestOutputFile
    }

    static def getInputResourcesDirectory(project, resourcesTask) {
        try {
            return resourcesTask.inputResourcesDir.getAsFile().get()
        } catch (Throwable ignored) {
            // Ignored.
        }
        try {
            return resourcesTask.inputResourcesDir.getFiles().first()
        } catch (Throwable ignored) {
            // Ignored.
        }
        return resourcesTask.resDir
    }

    /**
     * 如：processDebugManifest
     * */
    static def getProcessManifestTask(project, variant) {
        return project.tasks.findByName("process${variant.name.capitalize()}Manifest")
    }

    /**
     * 如：mergeReleaseResources
     * */
    static def getMergeResourcesTask(project, variant) {
        return project.tasks.findByName("merge${variant.name.capitalize()}Resources")
    }

    /**
     * 如：processResources
     * */
    static def getProcessResourcesTask(project, variant) {
        return project.tasks.findByName("process${variant.name.capitalize()}Resources")
    }

    /**
     * 如：assembleDebug
     * */
    static def getAssembleTask(project, variant) {
        return project.tasks.findByName("assemble${variant.name.capitalize()}")
    }
}