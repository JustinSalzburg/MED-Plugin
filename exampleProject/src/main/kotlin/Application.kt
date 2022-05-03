package exampleProject

import io.github.justinsalzburg.medgradleplugin.processor.EventMessageDocumentation

data class MessageString(val text: String, val from: String, val to: ToMessage)

data class ToMessage(val message: String, val to: TestMessage)

class TestMessage(message: String, from: String){
    private var message: String = ""
    private var from: Int

    init {
        this.message = message
        this.from = from.length
    }
}

class Application {

    @EventMessageDocumentation(name="Hello", description = "Test123", topic = "TTOpic")
    fun sendMessage(message: MessageString, message2: String){
        println(message.text)
        println(message2)
    }

}