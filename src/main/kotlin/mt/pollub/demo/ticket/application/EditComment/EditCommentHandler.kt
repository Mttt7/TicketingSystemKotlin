package mt.pollub.demo.ticket.application.EditComment

import mt.pollub.demo.shared.exception.NotFoundException
import mt.pollub.demo.ticket.domain.TicketHistory
import mt.pollub.demo.ticket.domain.TicketHistoryEventType
import mt.pollub.demo.ticket.domain.TicketHistoryRepository
import mt.pollub.demo.ticket.domain.TicketRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class EditCommentHandler(
    private val ticketRepository: TicketRepository,
    private val ticketHistoryRepository: TicketHistoryRepository
) {
    @Transactional
    fun handle(command: EditCommentCommand) {
        val ticket = ticketRepository.findDetailedById(command.ticketId)
            ?: throw NotFoundException("Ticket not found: ${command.ticketId}")

        val (oldContent, newContent) = ticket.editComment(command.commentId, command.newContent)

        ticketHistoryRepository.save(
            TicketHistory(
                ticket = ticket,
                eventType = TicketHistoryEventType.COMMENT_EDITED,
                actorId = command.actorId,
                commentId = command.commentId,
                oldValue = oldContent,
                newValue = newContent,
                commentContent = newContent
            )
        )

        ticketRepository.save(ticket)
    }
}

