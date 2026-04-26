package mt.pollub.demo.ticket.web

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import mt.pollub.demo.ticket.application.AddComment.AddCommentCommand
import mt.pollub.demo.ticket.application.ChangeTicketPriority.ChangeTicketPriorityCommand
import mt.pollub.demo.ticket.application.ChangeTicketStatus.ChangeTicketStatusCommand
import mt.pollub.demo.ticket.application.CreateTicket.CreateTicketCommand
import mt.pollub.demo.ticket.application.CreateTicket.CreateTicketCommentData
import mt.pollub.demo.ticket.application.EditComment.EditCommentCommand
import mt.pollub.demo.ticket.application.EditTicket.EditTicketCommand
import mt.pollub.demo.ticket.domain.TicketPriority
import mt.pollub.demo.ticket.domain.TicketStatus
import java.time.LocalDateTime
import java.util.UUID

data class CreateTicketRequest(
    @field:NotBlank(message = "Title is required")
    @field:Size(max = 200, message = "Title must be at most 200 characters")
    val title: String,

    @field:NotBlank(message = "Description is required")
    @field:Size(max = 4000, message = "Description must be at most 4000 characters")
    val description: String,

    @field:NotNull(message = "Priority is required")
    val priority: TicketPriority,

    @field:NotNull(message = "Author is required")
    val authorId: UUID,

    val assigneeIds: Set<UUID>? = emptySet(),

    @field:Size(max = 80, message = "Category must be at most 80 characters")
    val category: String? = null,

    val dueAt: LocalDateTime? = null,

    @field:Valid
    val comments: List<CreateTicketCommentRequest>? = emptyList()
) {
    fun toCommand(): CreateTicketCommand = CreateTicketCommand(
        title = title,
        description = description,
        priority = priority,
        authorId = authorId,
        assigneeIds = assigneeIds.orEmpty(),
        category = category,
        dueAt = dueAt,
        comments = comments.orEmpty().map { it.toData() }
    )
}

data class CreateTicketCommentRequest(
    @field:NotNull(message = "Comment author is required")
    val authorId: UUID,

    @field:NotBlank(message = "Comment content is required")
    @field:Size(max = 3000, message = "Comment content must be at most 3000 characters")
    val content: String
) {
    fun toData(): CreateTicketCommentData = CreateTicketCommentData(authorId = authorId, content = content)
}

/**
 * All fields optional – only provided non-null fields are applied.
 * To explicitly clear category or dueAt, pass them as null.
 */
data class EditTicketRequest(
    @field:NotNull(message = "Actor (who performs the edit) is required")
    val actorId: UUID,

    @field:Size(max = 200, message = "Title must be at most 200 characters")
    val title: String? = null,

    @field:Size(max = 4000, message = "Description must be at most 4000 characters")
    val description: String? = null,

    val priority: TicketPriority? = null,

    @field:Size(max = 80, message = "Category must be at most 80 characters")
    val category: String? = null,

    /** When true, category value (even null) is applied */
    val categoryExplicit: Boolean = false,

    val dueAt: LocalDateTime? = null,

    /** When true, dueAt value (even null) is applied */
    val dueAtExplicit: Boolean = false,

    /** When non-null, replaces the full set of assignees (multiselect) */
    val assigneeIds: Set<UUID>? = null
) {
    fun toCommand(ticketId: UUID): EditTicketCommand = EditTicketCommand(
        ticketId = ticketId,
        actorId = actorId,
        title = title,
        description = description,
        priority = priority,
        category = category,
        categoryExplicit = categoryExplicit,
        dueAt = dueAt,
        dueAtExplicit = dueAtExplicit,
        assigneeIds = assigneeIds
    )
}

data class ChangeTicketStatusRequest(
    @field:NotNull(message = "Actor is required")
    val actorId: UUID,

    @field:NotNull(message = "New status is required")
    val newStatus: TicketStatus
) {
    fun toCommand(ticketId: UUID): ChangeTicketStatusCommand = ChangeTicketStatusCommand(
        ticketId = ticketId,
        actorId = actorId,
        newStatus = newStatus
    )
}

data class ChangeTicketPriorityRequest(
    @field:NotNull(message = "Actor is required")
    val actorId: UUID,

    @field:NotNull(message = "New priority is required")
    val newPriority: TicketPriority
) {
    fun toCommand(ticketId: UUID): ChangeTicketPriorityCommand = ChangeTicketPriorityCommand(
        ticketId = ticketId,
        actorId = actorId,
        newPriority = newPriority
    )
}

data class AddCommentRequest(
    @field:NotNull(message = "Author is required")
    val authorId: UUID,

    @field:NotNull(message = "Actor is required")
    val actorId: UUID,

    @field:NotBlank(message = "Content is required")
    @field:Size(max = 3000, message = "Content must be at most 3000 characters")
    val content: String
) {
    fun toCommand(ticketId: UUID): AddCommentCommand = AddCommentCommand(
        ticketId = ticketId,
        actorId = actorId,
        authorId = authorId,
        content = content
    )
}

data class EditCommentRequest(
    @field:NotNull(message = "Actor is required")
    val actorId: UUID,

    @field:NotBlank(message = "Content is required")
    @field:Size(max = 3000, message = "Content must be at most 3000 characters")
    val content: String
) {
    fun toCommand(ticketId: UUID, commentId: UUID): EditCommentCommand = EditCommentCommand(
        ticketId = ticketId,
        commentId = commentId,
        actorId = actorId,
        newContent = content
    )
}
