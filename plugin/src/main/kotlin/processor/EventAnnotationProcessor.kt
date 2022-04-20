package processor

import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FUNCTION)
annotation class EventMessageDocumentation(val name: String, val description: String, val topic: String)

data class EventDocumentationEntry(
    val functionName: String,
    val name: String,
    val description: String,
    val topic: String
)

@SupportedSourceVersion(SourceVersion.RELEASE_11)
@SupportedAnnotationTypes
@SupportedOptions
class EventAnnotationProcessor : AbstractProcessor() {

    val eventDocumentations = mutableListOf<EventDocumentationEntry>()

    override fun process(annotations: MutableSet<out TypeElement>?, roundEnv: RoundEnvironment?): Boolean {

        val documented = roundEnv!!.getElementsAnnotatedWith(EventMessageDocumentation::class.java) ?: emptySet()

        val newEventDocumentations: List<EventDocumentationEntry> = documented.map {
            val annotation = it.getAnnotation(EventMessageDocumentation::class.java)
            EventDocumentationEntry(
                functionName = fullClassName(it),
                name = annotation.name,
                description = annotation.description,
                topic = annotation.topic
            )
        }

        eventDocumentations.addAll(newEventDocumentations)

        return false
    }

    private fun fullClassName(it: Element) = "${it.enclosingElement.simpleName}.${it.simpleName}"
}