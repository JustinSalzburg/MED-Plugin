package io.github.justinsalzburg.medgradleplugin

import io.github.justinsalzburg.medgradleplugin.processor.EventDocumentationEntry
import io.github.justinsalzburg.medgradleplugin.processor.EventDocumentationProps

class MdWriter(private val annotation: List<EventDocumentationEntry>) {
    fun getMdString(): String {
        return annotation.joinToString("\n") {
            """###${it.name}
                |***Beschreibung:***
                |
                |${it.description}
                |${
                if (it.topic !== "") {
                    "####Topic: ${it.topic}"
                } else {
                    ""
                }
                }
                |
                |---
                || Key | Datentyp | Beschreibung |
                || --- | -------- | ------------ |
                |${getMdFromProperties(it.properties)}
                
            """.trimMargin()
        }
    }

    private fun getMdFromProperties(parameter: List<EventDocumentationProps>): String {
        return parameter.joinToString("\n |") {
            """
                |${it.name} | ${it.type} | ${it.description} | ${if(it.properties !== emptyList<EventDocumentationProps>()){"\n" + getMdFromProperties(it.properties)}else{""}}
            """.trimIndent()
        }
    }

    private fun getMdFromObject(properties: List<EventDocumentationProps?>?, tab: Int): String {
        println(properties)
        if (properties !== emptyList<EventDocumentationProps>() && properties !== null) {
            return properties.joinToString ("\n") { prop ->
                if (prop !== null) {
                    """
                        |
                        |${"\t".repeat(tab)}* property: ${prop.name} type: ${prop.type} ${getMdFromObject(prop.properties, tab+1)}
                    """.trimMargin()
                }else{
                    "\n"
                }
            }
        }
        return "\n"
    }
}