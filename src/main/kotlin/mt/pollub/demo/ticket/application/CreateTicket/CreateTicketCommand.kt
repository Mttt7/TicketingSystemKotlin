package mt.pollub.demo.ticket.application.CreateTicket

import mt.pollub.demo.ticket.domain.TicketPriority
import java.time.LocalDateTime
import java.util.UUID

data class CreateTicketCommand(
    val title: String,
    val description: String,
    val priority: TicketPriority,
    val authorId: UUID,
    val assigneeIds: Set<UUID> = emptySet(),
    val category: String? = null,
    val dueAt: LocalDateTime? = null,
    val comments: List<CreateTicketCommentData> = emptyList()
)

data class CreateTicketCommentData(
    val authorId: UUID,
    val content: String
)

