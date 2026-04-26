package mt.pollub.demo.ticket.application.ChangeTicketPriority

import mt.pollub.demo.ticket.domain.TicketPriority
import java.util.UUID

data class ChangeTicketPriorityCommand(
    val ticketId: UUID,
    val actorId: UUID,
    val newPriority: TicketPriority
)

