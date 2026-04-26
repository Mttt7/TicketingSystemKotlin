package mt.pollub.demo.ticket.application.GetTicket

import mt.pollub.demo.ticket.domain.TicketPriority
import mt.pollub.demo.ticket.domain.TicketStatus
import java.time.LocalDateTime
import java.util.UUID

data class GetTicketResult(
    val id: UUID,
    val title: String,
    val description: String,
    val priority: TicketPriority,
    val status: TicketStatus,
    val authorId: UUID,
    val author: TicketAuthorResult,
    val assigneeIds: Set<UUID>,
    val category: String?,
    val dueAt: LocalDateTime?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val comments: List<GetTicketCommentResult>
)

data class TicketAuthorResult(
    val id: UUID,
    val firstName: String,
    val lastName: String,
    val email: String,
    val display: String
)

data class GetTicketCommentResult(
    val id: UUID,
    val authorId: UUID,
    val content: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)
