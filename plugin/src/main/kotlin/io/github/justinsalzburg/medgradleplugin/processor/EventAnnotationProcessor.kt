package io.github.justinsalzburg.medgradleplugin.processor

import io.github.justinsalzburg.medgradleplugin.MdWriter
import java.io.File
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.TypeMirror
import javax.tools.Diagnostic

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
annotation class EventDocumentation(val name: String, val description: String, val topic: String)

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class EventKeyDocumentation(val description: String)

data class EventDocumentationProps(
    val name: String,
    val description: String,
    val type: String,
    val properties: List<EventDocumentationProps>
)

data class EventDocumentationEntry(
    val name: String,
    val description: String,
    val topic: String,
    val properties: List<EventDocumentationProps>

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

    private val supportedTypes = setOf(EventDocumentation::class.java.canonicalName)

    override fun getSupportedAnnotationTypes(): Set<String> = supportedTypes

    override fun process(annotations: MutableSet<out TypeElement>, roundEnv: RoundEnvironment): Boolean {

        val documented = roundEnv.getElementsAnnotatedWith(EventDocumentation::class.java) ?: emptySet()

        documented.map{document ->
            val annotation = document.getAnnotation(EventDocumentation::class.java)
            println("Name: " + annotation.name)
            println("Beschreibung: " + annotation.description)
            println("Topic: " + annotation.topic)
        }

        documented.map{document ->
            println("EnclosedElements: " + document.enclosedElements.filter{it.kind === ElementKind.METHOD})
        }

        val newEventDocumentations: List<EventDocumentationEntry> = documented.map { document ->
            val annotation = document.getAnnotation(EventDocumentation::class.java)
            document as TypeElement
            val methods = document.enclosedElements.filter { it.kind === ElementKind.METHOD }
            val annotationFilter = methods.filter { it.getAnnotation(EventKeyDocumentation::class.java) !== null }
            val annotatedMethodNames = annotationFilter.map { it.simpleName.split("$").first() }

            val path = document.enclosingElement.enclosedElements

            println(annotatedMethodNames.map{name -> return@map methods.find{it.simpleName.toString() == name}})

//            println("Properties: " + document.enclosedElements.map{it.simpleName})
//            println("PropertieTypes: " + document.enclosedElements.map{it.kind})
            println("Mehtods with KeyDoc: " + annotationFilter)
//            println("Methods Return Type " + annotatedMethodNames)
//            println("Filtered Properties: " + executableElement2.filter{annotatedMethodNames.contains(it.simpleName.toString())}.map{it.returnType})
            return@map EventDocumentationEntry(
                name = annotation.name,
                description = annotation.description,
                topic = annotation.topic,
                properties = getProperties(methods, path, mutableListOf()),

                )
        }

//        println(newEventDocumentations)
        eventDocumentations.addAll(newEventDocumentations)
        if (outputDir == null) {
            outputDir = File(outputDir())
            outputDir?.mkdirs()
        }
        if (roundEnv.processingOver()) {
            MdWriter(eventDocumentations).getMdString().let {
                File(outputDir!!.absolutePath + "/events.md").writeText(it)
            }
        }

        return false
    }

    private fun fullClassName(it: Element) = "${it.enclosingElement.simpleName}.${it.simpleName}"

    private fun getAnnotatedMethods(methods: List<Element>): List<Element> =
        methods.filter { it.getAnnotation(EventKeyDocumentation::class.java) !== null }

    private fun isMethodAnnotated(annotatedMethods: List<Element>, method: Element): Boolean{
        val annotatedMethodNames = annotatedMethods.map { it.simpleName.split("$").first()}
        return annotatedMethodNames.contains(method.simpleName.toString())
    }

    private fun getAnnotationOfMethod(method: Element, annotated:List<Element>): Element{
        return annotated.find {it.simpleName.split("$").first() == method.simpleName.toString()}!!
    }

    private fun hasProperty(type: TypeMirror, path: List<out Element>,basepath: List<String> ): List<EventDocumentationProps>{
        val pathNames = path.map{it.toString()}
        println("Basepath: " + basepath)
        println("PathNames: " + pathNames)
        println("type: " + type.toString())
        if(pathNames.contains(type.toString())){
            type as DeclaredType
            val fields = type.asElement().enclosedElements.filter{ it.kind == ElementKind.FIELD }
            return getClassProperties(fields, path, basepath)
        }else{
            return emptyList()
        }
    }

    private fun getProperties(props: List<Element>, path: List<out Element>, basepath: List<String>): List<EventDocumentationProps> {
        val executable = props.map { it as ExecutableElement }
        val annotated = getAnnotatedMethods(executable)
        return executable.filter{isMethodAnnotated(annotated, it)}.map {
            val annotatedMethod = getAnnotationOfMethod(it, annotated).getAnnotation(EventKeyDocumentation::class.java)
            println(basepath)

            return@map EventDocumentationProps(
                name = (basepath + it.simpleName.toString()).joinToString("."),
                description = annotatedMethod.description,
                type = it.returnType.toString(),
                properties = hasProperty(it.returnType, path,
                    basepath + it.simpleName.toString()
                )
            )
        }
    }

    private fun getClassProperties(props: List<Element>, path: List<out Element>, basepath: List<String>): List<EventDocumentationProps>{
        return props.map {
            EventDocumentationProps(
                name = (basepath + it.simpleName.toString()).joinToString("."),
                description = "",
                type = it.asType().toString(),
                properties = hasProperty(it.asType(), path, basepath + it.simpleName.toString())
            )
        }
    }

//    private fun getParameter(document: Element): List<EventDocumentationProps> {
//
//        val executableElement = document as ExecutableElement
//        val path = executableElement.enclosingElement.enclosingElement.enclosedElements
//
//        return executableElement.parameters.map { param ->
//            val name = param.simpleName.toString()
//            val typeName = param.asType().toString()
//            val type = getTypeProperties(path, typeName, ElementKind.FIELD)
//            return@map EventDocumentationProps(
//                name = name,
//                type = typeName,
//                properties = type.map { getEventDocumentationParamObject(it, path) }
//
//            )
//        }
//    }
//
//    private fun getEventDocumentationParamObject(
//        prop: Element?,
//        path: MutableList<out Element>
//    ): EventDocumentationProps? {
//        return if (prop !== null) {
//            EventDocumentationProps(
//                name = prop.simpleName.toString(),
//                type = prop.asType().toString(),
//                properties = getTypeProperties(
//                    path,
//                    prop.asType().toString(),
//                    ElementKind.FIELD
//                ).map { getEventDocumentationParamObject(it, path) }
//            )
//        } else {
//            null
//        }
//
//    }
//
//    private fun getTypeProperties(path: MutableList<out Element>, typeName: String, kind: ElementKind): List<Element> {
//        val typeFind = path.find { element -> element.toString() == typeName }
//        return if (typeFind !== null) {
//            typeFind.enclosedElements.filter { it.kind == kind }
//        } else {
//            emptyList()
//        }
//    }


}
