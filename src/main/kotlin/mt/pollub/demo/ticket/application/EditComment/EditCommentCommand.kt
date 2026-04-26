package mt.pollub.demo.ticket.application.EditComment

import java.util.UUID

data class EditCommentCommand(
    val ticketId: UUID,
    val commentId: UUID,
    val actorId: UUID,
    val newContent: String
)

