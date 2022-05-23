package exampleProject

import io.github.justinsalzburg.medgradleplugin.processor.EventDocumentation
import io.github.justinsalzburg.medgradleplugin.processor.EventKeyDocumentation
import java.util.UUID

data class SomeEventData(
    val id: UUID,
    val name: String
){
    companion object{
        fun fromVariant(id: UUID) = SomeEventData(
            id,
            "HokusPokus"
        )
    }
}
@EventDocumentation(name = "Event - VARIANT-DELECTION", description = "Some Description", topic = "SOME_DELETION")
class SomeArchivedEvent(val variantId: UUID): KafkaEvent<SomeEventData> {
    @EventKeyDocumentation(description = "SOME_DELETATION")
    override val type: EventType
        get() = "SOME_DELETION"
    @EventKeyDocumentation(description = "Eine UUID")
    override val id: UUID
        get() = variantId
    @EventKeyDocumentation(description = "SomeEventData...")
    override val data: SomeEventData
        get() = SomeEventData.fromVariant(variantId)
}