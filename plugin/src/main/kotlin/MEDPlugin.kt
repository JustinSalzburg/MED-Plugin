import org.gradle.api.Plugin
import org.gradle.api.Project

class MEDPlugin : Plugin<Project> {

    override fun apply(target: Project) {

    }

}

private fun addKpt(project: Project){
    project.pluginManager.apply("org.jetbrains.kotlin.jvm")
    project.pluginManager.apply("org.jetbrains.kotlin.kapt")
    project.configurations.getByName("kapt").dependencies.add(
        project.dependencies.create("io.github.justinsalzburg:med-plugin:1.0.0")
    )

    project.pluginManager.withPlugin("org.jetbrains.kotlin.kapt"){
        project.afterEvaluate{
            (it.tasks.findByName("kaptKotlin") ?: error("kapt task should be defined here")).let {
                it.outputs.dir(project.buildDir.absolutePath + "/docs/events")
            }
        }
    }

    project.extensions.configure<KaptExtension>("kapt") {
        it.useBuildCache = true
    }

}