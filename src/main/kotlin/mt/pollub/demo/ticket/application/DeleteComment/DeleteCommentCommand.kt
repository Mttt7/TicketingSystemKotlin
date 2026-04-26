package mt.pollub.demo.ticket.application.DeleteComment

import java.util.UUID

data class DeleteCommentCommand(
    val ticketId: UUID,
    val commentId: UUID,
    val actorId: UUID
)

