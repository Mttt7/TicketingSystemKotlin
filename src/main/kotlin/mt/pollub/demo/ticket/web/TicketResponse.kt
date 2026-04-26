package mt.pollub.demo.ticket.web

import mt.pollub.demo.ticket.application.GetTicket.GetTicketCommentResult
import mt.pollub.demo.ticket.application.GetTicket.GetTicketResult
import mt.pollub.demo.ticket.application.GetTicket.TicketAuthorResult
import mt.pollub.demo.ticket.application.GetTicketHistory.GetTicketHistoryResult
import mt.pollub.demo.ticket.application.GetTicketHistory.TicketHistoryEntryResult
import mt.pollub.demo.ticket.application.GetTicketStats.GetTicketStatsResult
import mt.pollub.demo.ticket.domain.TicketHistoryEventType
import mt.pollub.demo.ticket.domain.TicketPriority
import mt.pollub.demo.ticket.domain.TicketStatus
import java.time.LocalDateTime
import java.util.UUID

data class CreateTicketResponse(val ticketId: UUID)

data class TicketStatsResponse(
    val byStatus: Map<TicketStatus, Long>,
    val openedToday: Long,
    val closedToday: Long
) {
    companion object {
        fun from(result: GetTicketStatsResult) = TicketStatsResponse(
            byStatus = result.byStatus,
            openedToday = result.openedToday,
            closedToday = result.closedToday
        )
    }
}

data class AddCommentResponse(val commentId: UUID)

data class TicketResponse(
    val id: UUID,
    val title: String,
    val description: String,
    val priority: TicketPriority,
    val status: TicketStatus,
    val authorId: UUID,
    val author: TicketAuthorResponse,
    val assigneeIds: Set<UUID>,
    val category: String?,
    val dueAt: LocalDateTime?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val comments: List<TicketCommentResponse>
) {
    companion object {
        fun from(result: GetTicketResult): TicketResponse = TicketResponse(
            id = result.id,
            title = result.title,
            description = result.description,
            priority = result.priority,
            status = result.status,
            authorId = result.authorId,
            author = TicketAuthorResponse.from(result.author),
            assigneeIds = result.assigneeIds,
            category = result.category,
            dueAt = result.dueAt,
            createdAt = result.createdAt,
            updatedAt = result.updatedAt,
            comments = result.comments.map(TicketCommentResponse::from)
        )
    }
}

data class TicketAuthorResponse(
    val id: UUID,
    val firstName: String,
    val lastName: String,
    val email: String,
    val display: String
) {
    companion object {
        fun from(result: TicketAuthorResult): TicketAuthorResponse = TicketAuthorResponse(
            id = result.id,
            firstName = result.firstName,
            lastName = result.lastName,
            email = result.email,
            display = result.display
        )
    }
}

data class TicketCommentResponse(
    val id: UUID,
    val authorId: UUID,
    val content: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    companion object {
        fun from(result: GetTicketCommentResult): TicketCommentResponse = TicketCommentResponse(
            id = result.id,
            authorId = result.authorId,
            content = result.content,
            createdAt = result.createdAt,
            updatedAt = result.updatedAt
        )
    }
}

data class TicketHistoryEntryResponse(
    val id: UUID,
    val ticketId: UUID,
    val eventType: TicketHistoryEventType,
    val actorId: UUID,
    val fieldName: String?,
    val oldValue: String?,
    val newValue: String?,
    val assigneesAdded: List<UUID>,
    val assigneesRemoved: List<UUID>,
    val commentId: UUID?,
    val commentContent: String?,
    val occurredAt: LocalDateTime
) {
    companion object {
        fun from(result: TicketHistoryEntryResult): TicketHistoryEntryResponse = TicketHistoryEntryResponse(
            id = result.id,
            ticketId = result.ticketId,
            eventType = result.eventType,
            actorId = result.actorId,
            fieldName = result.fieldName,
            oldValue = result.oldValue,
            newValue = result.newValue,
            assigneesAdded = result.assigneesAdded
                ?.split(",")?.filter { it.isNotBlank() }?.map { UUID.fromString(it.trim()) }
                ?: emptyList(),
            assigneesRemoved = result.assigneesRemoved
                ?.split(",")?.filter { it.isNotBlank() }?.map { UUID.fromString(it.trim()) }
                ?: emptyList(),
            commentId = result.commentId,
            commentContent = result.commentContent,
            occurredAt = result.occurredAt
        )
    }
}

data class GetTicketHistoryResponse(val entries: List<TicketHistoryEntryResponse>) {
    companion object {
        fun from(result: GetTicketHistoryResult): GetTicketHistoryResponse =
            GetTicketHistoryResponse(entries = result.entries.map(TicketHistoryEntryResponse::from))
    }
}
