package io.github.justinsalzburg.medgradleplugin.processor

import java.io.File
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FUNCTION)
annotation class EventMessageDocumentation(val name: String, val description: String, val topic: String)
data class EventDocumentationParamObject(
    val name: String,
    val type: String,
    val properties: List<EventDocumentationParamObject?>
)

data class EventDocumentationParam(
    val name: String,
    val type: String,
    val properties: List<EventDocumentationParamObject?>

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
    private val eventDocumentations = mutableListOf<EventDocumentationEntry>()

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

    private fun getTypeProperties(path: MutableList<out Element>, typeName: String, kind: ElementKind): List<Element>{
        val typeFind = path.find { element -> element.toString() == typeName }
        return if(typeFind !== null){
            typeFind.enclosedElements.filter { it.kind == kind }
        }else{
            emptyList()
        }
    }

    private fun getParameter(document: Element): List<EventDocumentationParam> {
        val executableElement = document as ExecutableElement
        val path = executableElement.enclosingElement.enclosingElement.enclosedElements
//        val declaredTypeNames = executableElement.parameters.map { it.asType().toString() }
//        val declaredTypes = declaredTypeNames.map { types -> path.find { it.toString() == types } }
//        val fields = declaredTypes.map {
//            val props = it!!.enclosedElements.filter { it.kind == ElementKind.FIELD }
//        }

        return executableElement.parameters.map { param ->
            val name = param.simpleName.toString()
            val typeName = param.asType().toString()
            val type = getTypeProperties(path, typeName, ElementKind.FIELD)
            return@map EventDocumentationParam(
                name = name,
                type = typeName,
                properties = type.map { getEventDocumentationParamObject(it, path) }

            )
        }
    }

    private fun getEventDocumentationParamObject(prop: Element?, path: MutableList<out Element>): EventDocumentationParamObject? {
        return if (prop !== null) {
            EventDocumentationParamObject(
                name = prop.simpleName.toString(),
                type = prop.asType().toString(),
                properties = getTypeProperties(path, prop.asType().toString(), ElementKind.FIELD).map { getEventDocumentationParamObject(it, path) }
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
                |* param: ${it.name} type: ${it.type}
                |${getMdFromObject(it.properties)}
            """.trimMargin()
        }
    }

    private fun getMdFromObject(properties: List<EventDocumentationParamObject?>?): String {
        println(properties)
        if (properties !== emptyList<EventDocumentationParamObject>() && properties !== null) {
            return properties.joinToString ("\n") { prop ->
                if (prop !== null) {
                    """
                        |   * property: ${prop.name} type: ${prop.type}
                        |   ${getMdFromObject(prop.properties)}
                    """.trimMargin()
                }else{
                    ""
                }
            }
        }
        return ""
    }
}