package com.davi.tinker.study.build.gradle.task

import com.davi.tinker.study.build.gradle.common.TinkerBuildPath
import com.davi.tinker.study.build.gradle.util.FileOperation
import com.davi.tinker.study.build.gradle.util.IOHelper
import groovy.xml.Namespace
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction

public class TinkerManifestTask extends DefaultTask {
    static final String TINKER_ID = "TINKER_ID"
    static final String TINKER_ID_PREFIX = "tinker_id_"

    /**
     * 外部有put
     * */
    final Map<String, File> outputNameToManifestMap = new HashMap<>()

    TinkerManifestTask() {
        group = 'tinker'
    }

    @TaskAction
    def updateManifest() {
        // Parse the AndroidManifest.xml
        String tinkerValue = project.extensions.tinkerPatch.buildConfig.tinkerId
        boolean appendOutputNameToTinkerId = project.extensions.tinkerPatch.buildConfig.appendOutputNameToTinkerId

        if (tinkerValue == null || tinkerValue.isEmpty()) {
            throw new GradleException('tinkerId is not set!!!')
        }

        tinkerValue = TINKER_ID_PREFIX + tinkerValue

        def agpIntermediatesDir = new File(project.buildDir, 'intermediates')
        outputNameToManifestMap.each { String outputName, File manifest ->
            def manifestPath = manifest.getAbsolutePath()
            def finalTinkerValue = tinkerValue
            if (appendOutputNameToTinkerId && !outputName.isEmpty()) {
                finalTinkerValue += "_${outputName}"
            }

            project.logger.error("tinker add ${finalTinkerValue} to your AndroidManifest.xml ${manifestPath}")

            writeManifestMeta(manifestPath, TINKER_ID, finalTinkerValue)
            addApplicationToLoaderPattern(manifestPath)
            File manifestFile = new File(manifestPath)
            if (manifestFile.exists()) {
                def manifestRelPath = agpIntermediatesDir.toPath().relativize(manifestFile.toPath()).toString()
                def manifestDestPath = new File(project.file(TinkerBuildPath.getTinkerIntermediates(project)), manifestRelPath)
                FileOperation.copyFileUsingStream(manifestFile, manifestDestPath)
                project.logger.error("tinker gen AndroidManifest.xml in ${manifestDestPath}")
            }
        }
    }

    static void writeManifestMeta(String manifestPath, String name, String value) {
        def ns = new Namespace("http://schemas.android.com/apk/res/android", "android")
        def isr = null
        def pw = null
        try {
            isr = new InputStreamReader(new FileInputStream(manifestPath), "utf-8")
            def xml = new XmlParser().parse(isr)
            def application = xml.application[0]
            if (application) {
                def metaDataTags = application['meta-data']

                // remove any old TINKER_ID elements
                def tinkerId = metaDataTags.findAll {
                    it.attributes()[ns.name].equals(name)
                }.each {
                    it.parent().remove(it)
                }

                // Add the new TINKER_ID element
                application.appendNode('meta-data', [(ns.name): name, (ns.value): value])

                // Write the manifest file
                pw = new PrintWriter(manifestPath, "utf-8")
                def printer = new XmlNodePrinter(pw)
                printer.preserveWhitespace = true
                printer.print(xml)
            }
        } finally {
            IOHelper.closeQuietly(pw)
            IOHelper.closeQuietly(isr)
        }
    }

    void addApplicationToLoaderPattern(String manifestPath) {
        Iterable<String> loader = project.extensions.tinkerPatch.dex.loader
        String applicationName = readManifestApplicationName(manifestPath)

        if (applicationName != null && !loader.contains(applicationName)) {
            loader.add(applicationName)
            project.logger.error("tinker add ${applicationName} to dex loader pattern")
        }
        String loaderClass = "com.tencent.tinker.loader.*"
        if (!loader.contains(loaderClass)) {
            loader.add(loaderClass)
            project.logger.error("tinker add ${loaderClass} to dex loader pattern")
        }
    }

    static String readManifestApplicationName(String manifestPath) {
        def isr = null
        try {
            isr = new InputStreamReader(new FileInputStream(manifestPath), "utf-8")
            def xml = new XmlParser().parse(isr)
            def ns = new Namespace("http://schemas.android.com/apk/res/android", "android")

            def application = xml.application[0]
            if (application) {
                return application.attributes()[ns.name]
            } else {
                return null
            }
        } finally {
            IOHelper.closeQuietly(isr)
        }
    }
}

