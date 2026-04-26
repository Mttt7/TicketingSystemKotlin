package mt.pollub.demo.ticket.application.EditTicket

import mt.pollub.demo.ticket.domain.TicketPriority
import java.time.LocalDateTime
import java.util.UUID

data class EditTicketCommand(
    val ticketId: UUID,
    val actorId: UUID,

    /** null = do not change */
    val title: String? = null,
    val description: String? = null,
    val priority: TicketPriority? = null,

    /** Explicit nullability: null = do not touch, present (even as null) = clear/set */
    val category: String? = null,
    val categoryExplicit: Boolean = false,

    val dueAt: LocalDateTime? = null,
    val dueAtExplicit: Boolean = false,

    /** null = do not touch, non-null = replace all assignees */
    val assigneeIds: Set<UUID>? = null
)

