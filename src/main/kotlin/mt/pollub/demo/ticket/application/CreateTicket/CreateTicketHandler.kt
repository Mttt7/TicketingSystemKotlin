package mt.pollub.demo.ticket.application.CreateTicket

import mt.pollub.demo.ticket.domain.Ticket
import mt.pollub.demo.ticket.domain.TicketComment
import mt.pollub.demo.ticket.domain.TicketHistory
import mt.pollub.demo.ticket.domain.TicketHistoryEventType
import mt.pollub.demo.ticket.domain.TicketHistoryRepository
import mt.pollub.demo.ticket.domain.TicketRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class CreateTicketHandler(
    private val ticketRepository: TicketRepository,
    private val ticketHistoryRepository: TicketHistoryRepository
) {
    @Transactional
    fun handle(command: CreateTicketCommand): UUID {
        val ticket = Ticket(
            title = command.title.trim(),
            description = command.description.trim(),
            priority = command.priority,
            authorId = command.authorId,
            category = command.category?.trim()?.ifBlank { null },
            dueAt = command.dueAt
        )

        ticket.assigneeIds.addAll(command.assigneeIds)
        command.comments.forEach { comment ->
            ticket.addComment(
                TicketComment(
                    authorId = comment.authorId,
                    content = comment.content.trim()
                )
            )
        }

        val saved = ticketRepository.save(ticket)
        val ticketId = requireNotNull(saved.id) { "Ticket ID was not generated" }

        ticketHistoryRepository.save(
            TicketHistory(
                ticket = saved,
                eventType = TicketHistoryEventType.TICKET_CREATED,
                actorId = command.authorId
            )
        )

        return ticketId
    }
}
