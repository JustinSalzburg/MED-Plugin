package exampleProject

import java.util.*

class EventService(
    private val archiveEventService: KafkaEventService<SomeEventData>
) {
    fun publishSomeEvent(id: UUID){
        archiveEventService.publishEvent(SomeArchivedEvent(id))
    }
}