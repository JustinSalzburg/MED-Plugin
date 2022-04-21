
data class MessageString(val text: String, val from: String, val to: String)

class Application {

    fun sendMessage(message: MessageString){
        println(message.text)
    }

}

fun main(){
    val app = Application()
    app.sendMessage(MessageString("Hello World", "Programmer", "Program"))
}