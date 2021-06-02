package com.davi.pkg.task

import com.davi.pkg.bean.ConfigBean
import com.davi.pkg.DaviPluDemo
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction


class DaviTaskPlu extends DefaultTask {

    DaviTaskPlu() {
        group = 'davi'
        description = '自定义插件中的任务：DaviTaskPlu'
    }

    /**
     * TaskAction
     * - 被这个注解过的，那么方法就会在gradle的生命周期中的执行阶段被执行
     * */
    @TaskAction
    void onAction() {
        println '【DaviTaskPlu】【onAction】---> start'
        println '【DaviTaskPlu】【onAction】---> end'
    }


    @TaskAction
    void onActionExtensions() {
        /***
         *  实现gradle文件中传进来的参数，进行打印
         * */
        ConfigBean b = project.getExtensions().getByName(DaviPluDemo.NAME_PLU_CONFIG)
        println '【DaviTaskPlu】【onAction】实现gradle文件中传进来的参数，进行打印, b.name = ' + b.name
    }


}