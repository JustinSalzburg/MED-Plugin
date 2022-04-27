package exampleProject

import io.github.justinsalzburg.medgradleplugin.processor.EventMessageDocumentation

data class MessageString(val text: String, val from: String, val to: String)

class Application {

    @EventMessageDocumentation(name="Hello", description = "Test123", topic = "TTOpic")
    fun sendMessage(message: MessageString, message2: String){
        println(message.text)
        println(message2)
    }

}

fun main(){
    val app = Application()
    app.sendMessage(MessageString("Hello World", "Programmer", "Program"), "Hello World")
}