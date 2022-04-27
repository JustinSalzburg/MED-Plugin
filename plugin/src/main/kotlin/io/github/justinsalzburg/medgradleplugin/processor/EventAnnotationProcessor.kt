package io.github.justinsalzburg.medgradleplugin.processor

import java.io.File
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FUNCTION)
annotation class EventMessageDocumentation(val name: String, val description: String, val topic: String)
data class EventDocumentationParamObject(
    val name: String,
    val type: String,
    val properties: EventDocumentationParamObject?
)

data class EventDocumentationParam(
    val name: String,
    val type: String,
    val properties: EventDocumentationParamObject?

)

data class EventDocumentationEntry(
    val functionName: String,
    val name: String,
    val description: String,
    val parameter: List<EventDocumentationParam>,
    val topic: String
)

@SupportedSourceVersion(SourceVersion.RELEASE_16)
@SupportedAnnotationTypes
@SupportedOptions(EventAnnotationProcessor.EVENT_OUTPUT_DIR)
class EventAnnotationProcessor : AbstractProcessor() {

    companion object {
        const val EVENT_OUTPUT_DIR = "eventannotationprocessor.outputdir"
    }

    private var outputDir: File? = null
    val eventDocumentations = mutableListOf<EventDocumentationEntry>()

    override fun getSupportedOptions() = setOf(EVENT_OUTPUT_DIR)


    private fun outputDir(): String {
        processingEnv.options[EVENT_OUTPUT_DIR]?.let {
            return it
        }
        processingEnv.messager.printMessage(
            Diagnostic.Kind.ERROR, "Event output directory $EVENT_OUTPUT_DIR not set"
        )
        error("Event output directory $EVENT_OUTPUT_DIR not set")
    }

    private val supportedTypes = setOf(EventMessageDocumentation::class.java.canonicalName)

    override fun getSupportedAnnotationTypes(): Set<String> = supportedTypes

    override fun process(annotations: MutableSet<out TypeElement>, roundEnv: RoundEnvironment): Boolean {

        val documented = roundEnv.getElementsAnnotatedWith(EventMessageDocumentation::class.java) ?: emptySet()
        val newEventDocumentations: List<EventDocumentationEntry> = documented.map { document ->
            val annotation = document.getAnnotation(EventMessageDocumentation::class.java)
            return@map EventDocumentationEntry(
                functionName = fullClassName(document),
                name = annotation.name,
                description = annotation.description,
                parameter = getParameter(document),
                topic = annotation.topic
            )
        }


        eventDocumentations.addAll(newEventDocumentations)
        if (outputDir == null) {
            outputDir = File(outputDir())
            outputDir?.mkdirs()
        }
        if (roundEnv.processingOver()) {
            getMdString(eventDocumentations).let {
                File(outputDir!!.absolutePath + "/events.md").writeText(it)
            }
        }

        return false
    }

    private fun fullClassName(it: Element) = "${it.enclosingElement.simpleName}.${it.simpleName}"

    private fun getParameter(document: Element): List<EventDocumentationParam> {
        val executableElement = document as ExecutableElement
//        val declaredTypeNames = executableElement.parameters.map { it.asType().toString() }
        val path = executableElement.enclosingElement.enclosingElement.enclosedElements
//        val declaredTypes = declaredTypeNames.map { types -> path.find { it.toString() == types } }
//        val fields = declaredTypes.map {
//            val props = it!!.enclosedElements.filter { it.kind == ElementKind.FIELD }
//        }

        return executableElement.parameters.map {
            val name = it.simpleName.toString()
            val typeName = it.asType().toString()
            val type = path.find{it.toString() == typeName}
//            println(type!!.enclosedElements.filter{it.kind == ElementKind.FIELD})
            println(name)
            println(type)
            return@map EventDocumentationParam(
                name = name,
                type = typeName,
                properties = getEventDocumentationParamObject(type)

            )
        }

    }

    private fun getEventDocumentationParamObject(prop: Element?): EventDocumentationParamObject? {
        return if (prop !== null) {
            EventDocumentationParamObject(
                name = prop.simpleName.toString(),
                type = prop.asType().toString(),
                properties = null
            )
        } else {
            null
        }

    }

    private fun getMdString(annotation: List<EventDocumentationEntry>): String {
        return annotation.joinToString("\n") {
            """#${it.name}
                |##Description:
                |${it.description}
                |${
                if (it.topic !== "") {
                    "#Topic: ${it.topic}"
                } else {
                    ""
                }
            }
                |##Parameter:
                |${getMdFromParameter(it.parameter)}
                
            """.trimMargin()
        }
    }

    private fun getMdFromParameter(parameter: List<EventDocumentationParam>): String {
        return parameter.joinToString("\n") {
            """
                |* ${it.name} type: ${it.type}
                |${getMdFromObject(it.properties)}
            """.trimMargin()
        }
    }

    private fun getMdFromObject(properties: EventDocumentationParamObject?): String {
        if (properties !== null) {
            return """
                |   * ${properties.name} type: ${properties.type}
                |    ${getMdFromObject(properties.properties)}
            """.trimMargin()
        } else {
            return ""
        }
    }
}