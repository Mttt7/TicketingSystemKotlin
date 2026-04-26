package mt.pollub.demo.ticket.application.ChangeTicketStatus

import mt.pollub.demo.ticket.domain.TicketStatus
import java.util.UUID

data class ChangeTicketStatusCommand(
    val ticketId: UUID,
    val actorId: UUID,
    val newStatus: TicketStatus
)

