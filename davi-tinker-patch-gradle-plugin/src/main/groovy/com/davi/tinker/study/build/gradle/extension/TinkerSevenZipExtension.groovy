package com.davi.tinker.study.build.gradle.extension

import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.Dependency


/**
 * The configuration properties.
 * you should only set one of them, if path is Specified, it will overwrite the artifact param
 * @author zhangshaowen
 */
public class TinkerSevenZipExtension {
    /**
     * Specifies an artifact spec for downloading the executable from
     * repositories. spec format: '<groupId>:<artifactId>:<version>'
     */
    String zipArtifact
    /**
     * Specifies a local path.
     * if path is Specified, it will overwrite the artifact param
     * such as/usr/local/bin/7za
     * if you do not set the zipArtifact and path, We will try to use 7za directly
     */
    String path

    private Project project;

    public TinkerSevenZipExtension(Project project) {
        zipArtifact = null
        path = null
        this.project = project
    }


    //指定path ？？？？
    void resolveZipFinalPath() {
        if (path != null)
            return

        //path 为null才进这里
        if (this.zipArtifact != null) {
            def groupId, finalArtifact, version
            //？？？？？？configurations.create
            Configuration config = project.configurations.create("sevenZipToolsLocator") {
                visible = false
                transitive = false
                extendsFrom = []
            }

            (groupId, finalArtifact, version) = this.zipArtifact.split(":")
            def notation = [group     : groupId,
                            name      : finalArtifact,
                            version   : version,
                            classifier: project.osdetector.classifier,
                            ext       : 'exe']
//            println "Resolving artifact: ${notation}"
            Dependency dep = project.dependencies.add(config.name, notation)
            File file = config.fileCollection(dep).singleFile
            if (!file.canExecute() && !file.setExecutable(true)) {
                throw new GradleException("Cannot set ${file} as executable")
            }
//            println "Resolved artifact: ${file}"
            this.path = file.path
        }
        //use system 7za
        if (this.path == null) {
            this.path = "7za"
        }
    }

    @Override
    public String toString() {
        """| zipArtifact = ${zipArtifact}
           | path = ${path}
        """.stripMargin()
    }
}