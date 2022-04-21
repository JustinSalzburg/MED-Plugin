import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import java.io.File

class BuildDocsTask: DefaultTask() {
    @Input
    var systemDescriptionPath: String = "description.md"

    init {
        checkForNeededDoc()

        if(File(project.projectDir.absolutePath + "/docs").exists()){
            inputs.dir(project.projectDir.absolutePath + "/docs")
        }
        if(File(project.projectDir.absolutePath + "/docs/system.puml").exists()){
            inputs.file(project.projectDir.absolutePath + "/docs/system.puml")
        }
        project.subprojects{
            if(File(it.buildDir.absolutePath + "/docs/events/").exists()){
                inputs.dir(File(it.buildDir.absolutePath + "/docs/events"))
            }
        }
        outputs.dir(project.buildDir.absolutePath + "/docs")
        outputs.dir(project.rootProject.buildDir.absolutePath + "/docs/system/${project.name.toLowerCase()}")
    }

    private fun checkForNeededDoc() {
        if (!File(project.projectDir.absolutePath + "/" + systemDescriptionPath).exists()) {
            error("System Description file: $systemDescriptionPath not found.")
        }
        inputs.file(project.projectDir.absolutePath + "/" + systemDescriptionPath)
    }

}