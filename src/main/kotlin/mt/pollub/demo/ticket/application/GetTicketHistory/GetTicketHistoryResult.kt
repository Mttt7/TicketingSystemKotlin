package mt.pollub.demo.ticket.application.GetTicketHistory

import mt.pollub.demo.ticket.domain.TicketHistoryEventType
import java.time.LocalDateTime
import java.util.UUID

data class GetTicketHistoryResult(
    val entries: List<TicketHistoryEntryResult>
)

data class TicketHistoryEntryResult(
    val id: UUID,
    val ticketId: UUID,
    val eventType: TicketHistoryEventType,
    val actorId: UUID,
    val fieldName: String?,
    val oldValue: String?,
    val newValue: String?,
    val assigneesAdded: String?,
    val assigneesRemoved: String?,
    val commentId: UUID?,
    val commentContent: String?,
    val occurredAt: LocalDateTime
)

