package io.github.justinsalzburg.medgradleplugin

import io.github.justinsalzburg.medgradleplugin.processor.EventAnnotationProcessor
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.plugin.KaptExtension

class MEDPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        addKapt(project = target)
        target.subprojects{
            addKapt(it)
        }

        target.tasks.register("buildDocs", BuildDocsTask::class.java){
            it.dependsOn("compilerKotlin")
            it.description = "Builds the documentation"
            it.group = "local"
        }
    }

}

private fun addKapt(project: Project){
    project.pluginManager.apply("org.jetbrains.kotlin.jvm")
    project.pluginManager.apply("org.jetbrains.kotlin.kapt")
//    project.configurations.getByName("kapt").dependencies.add(
//        project.dependencies.create("io.github.justinsalzburg:med-gradle-plugin")
//    )

    project.pluginManager.withPlugin("org.jetbrains.kotlin.kapt"){
        project.afterEvaluate{
            (it.tasks.findByName("kaptKotlin") ?: error("kapt task should be defined here")).let {
                it.outputs.dir(project.buildDir.absolutePath + "/docs/events")
            }
        }
    }

    project.extensions.configure<KaptExtension>("kapt") {
        it.useBuildCache = true
        it.arguments{
            arg(EventAnnotationProcessor.EVENT_OUTPUT_DIR, project.buildDir.absolutePath + "/docs/events")
        }
    }

}