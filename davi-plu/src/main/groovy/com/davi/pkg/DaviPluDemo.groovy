package com.davi.pkg

import com.davi.pkg.bean.ConfigBean
import com.davi.pkg.task.DaviTaskPlu
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionContainer

/**
 * gradle 插件，类似gradle文件里面写一样的
 * 只是这样组织，随着gradle功能原来越多，这样可读性，维护性等比较直观（google牛逼）
 * 使用上的区别：
 * （1）gradle文件中写，修改的时候只需要同步下即可
 * （2）gradle插件，修改了，还需要改版本号，编译上传maven，最后使用插件模块也要对应修改这样
 * */
public class DaviPluDemo implements Plugin<Project> {

    public static String NAME_PLU_CONFIG = 'pluConfig'

    @Override
    void apply(Project project) {
        println '【DaviPluDemo】-- apply start-- ' + project.name
        onTask(project)
        println '【DaviPluDemo】-- apply end-- ' + project.name
    }

    void onTask(Project project) {
        /**
         * 自定义task的执行
         * 创建了一个名为 name-davi 的任务，然后映射到我们自定义的任务的类 DaviTaskPlu。
         * */
        project.getTasks().create("name-davi", DaviTaskPlu.class, new Action<DaviTaskPlu>() {
            @Override
            void execute(DaviTaskPlu t) {
                t.onAction()
            }
        })
    }

    void onExtensions(Project project) {
        /**
         * 【定义】自定义插件传参数
         * 1）创建一个名为 pluConfig 的Extension，数据结构为《ConfigBean》
         *
         *
         * Gradle 的 Extension，翻译成中文意思就叫扩展。
         *  它的作用就是通过实现自定义的 Extension，
         *  可以在 Gradle 脚本中增加类似 android 这样命名空间的配置，
         *  Gradle 可以识别这种配置，并读取里面的配置内容。
         *
         *
         *  例子：
         *  android {*      compileSdkVersion 26
         *}*
         *
         * */
        project.extensions.create(NAME_PLU_CONFIG, ConfigBean)
    }


}