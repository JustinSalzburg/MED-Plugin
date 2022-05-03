package io.github.justinsalzburg.medgradleplugin

import io.github.justinsalzburg.medgradleplugin.processor.EventDocumentationEntry
import io.github.justinsalzburg.medgradleplugin.processor.EventDocumentationParam
import io.github.justinsalzburg.medgradleplugin.processor.EventDocumentationParamObject

class MdWriter(private val annotation: List<EventDocumentationEntry>) {
    fun getMdString(): String {
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
                |* param: ${it.name} type: ${it.type} ${getMdFromObject(it.properties, 1)}
            """.trimMargin()
        }
    }

    private fun getMdFromObject(properties: List<EventDocumentationParamObject?>?, tab: Int): String {
        println(properties)
        if (properties !== emptyList<EventDocumentationParamObject>() && properties !== null) {
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