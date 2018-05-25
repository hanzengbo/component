package tv.xiaoka.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project


class ComBuild implements Plugin<Project> {

    //默认是app，直接运行assembleRelease的时候，等同于运行app:assembleRelease
    String compilemodule = "app"

    void apply(Project project) {
        //project.extensions.create('combuild', ComExtension)

        String taskNames = project.gradle.startParameter.taskNames.toString()
        System.out.println("taskNames is " + taskNames)
        String module = project.path.replace(":", "")
        System.out.println("current module is " + module)
    }

}