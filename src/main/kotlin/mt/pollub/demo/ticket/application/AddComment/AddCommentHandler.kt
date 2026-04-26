package mt.pollub.demo.ticket.application.AddComment

import mt.pollub.demo.shared.exception.NotFoundException
import mt.pollub.demo.ticket.domain.TicketComment
import mt.pollub.demo.ticket.domain.TicketHistory
import mt.pollub.demo.ticket.domain.TicketHistoryEventType
import mt.pollub.demo.ticket.domain.TicketHistoryRepository
import mt.pollub.demo.ticket.domain.TicketRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class AddCommentHandler(
    private val ticketRepository: TicketRepository,
    private val ticketHistoryRepository: TicketHistoryRepository
) {
    @Transactional
    fun handle(command: AddCommentCommand): UUID {
        val ticket = ticketRepository.findDetailedById(command.ticketId)
            ?: throw NotFoundException("Ticket not found: ${command.ticketId}")

        val comment = TicketComment(
            authorId = command.authorId,
            content = command.content.trim()
        )
        ticket.addComment(comment)
        ticketRepository.save(ticket)

        val savedComment = ticket.comments.last()
        val commentId = requireNotNull(savedComment.id) { "Comment ID was not generated" }

        ticketHistoryRepository.save(
            TicketHistory(
                ticket = ticket,
                eventType = TicketHistoryEventType.COMMENT_ADDED,
                actorId = command.actorId,
                commentId = commentId,
                commentContent = savedComment.content
            )
        )

        return commentId
    }
}

