import io.github.justinsalzburg.medgradleplugin.processor.EventMessageDocumentation

data class MessageString(val text: String, val from: String, val to: String)

class Application {

    @EventMessageDocumentation(name="Hello", description = "Test123", topic = "TTopic")
    fun sendMessage(message: MessageString){
        println(message.text)
    }

}

fun main(){
    val app = Application()
    app.sendMessage(MessageString("Hello World", "Programmer", "Program"))
}