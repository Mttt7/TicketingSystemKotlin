package mt.pollub.demo.ticket.application.DeleteComment

import mt.pollub.demo.shared.exception.NotFoundException
import mt.pollub.demo.ticket.domain.TicketHistory
import mt.pollub.demo.ticket.domain.TicketHistoryEventType
import mt.pollub.demo.ticket.domain.TicketHistoryRepository
import mt.pollub.demo.ticket.domain.TicketRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DeleteCommentHandler(
    private val ticketRepository: TicketRepository,
    private val ticketHistoryRepository: TicketHistoryRepository
) {
    @Transactional
    fun handle(command: DeleteCommentCommand) {
        val ticket = ticketRepository.findDetailedById(command.ticketId)
            ?: throw NotFoundException("Ticket not found: ${command.ticketId}")

        val removed = ticket.removeComment(command.commentId)

        ticketHistoryRepository.save(
            TicketHistory(
                ticket = ticket,
                eventType = TicketHistoryEventType.COMMENT_DELETED,
                actorId = command.actorId,
                commentId = command.commentId,
                commentContent = removed.content
            )
        )

        ticketRepository.save(ticket)
    }
}

