package mt.pollub.demo.ticket.application.AddComment

import java.util.UUID

data class AddCommentCommand(
    val ticketId: UUID,
    val actorId: UUID,
    val authorId: UUID,
    val content: String
)

