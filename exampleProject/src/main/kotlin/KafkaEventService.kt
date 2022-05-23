package exampleProject

import java.util.UUID


typealias EventType = String

interface KafkaEvent<T>{
    val type: EventType
    val id: UUID
    val data: T
}

class KafkaEventService<T>(
    private val enable: Boolean,
    private val topic: String,
) {
    fun publishEvent(event: KafkaEvent<T>){
        println("Publish Event")
    }
}