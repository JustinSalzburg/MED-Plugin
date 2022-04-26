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

data class EventDocumentationEntry(
    val functionName: String,
    val name: String,
    val description: String,
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
        val newEventDocumentations: List<EventDocumentationEntry> = documented.map {
            val executableElement = it as ExecutableElement
            val declaredType = executableElement.parameters[0].asType()
            println(declaredType)
            val annotation = it.getAnnotation(EventMessageDocumentation::class.java)
            EventDocumentationEntry(
                functionName = fullClassName(it),
                name = annotation.name,
                description = annotation.description,
                topic = annotation.topic
            )
        }


        eventDocumentations.addAll(newEventDocumentations)
        if (outputDir == null) {
            outputDir = File(outputDir())
            outputDir?.mkdirs()
        }
        if (roundEnv.processingOver()) {
            eventDocumentations.joinToString("\n") { "* ${it.name}: ${it.description} [ ${it.topic} ]" }.let {
                File(outputDir!!.absolutePath + "/events.md").writeText(it)
            }
        }

        return false
    }

    private fun fullClassName(it: Element) = "${it.enclosingElement.simpleName}.${it.simpleName}"
}