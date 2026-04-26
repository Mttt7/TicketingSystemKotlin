package mt.pollub.demo.ticket.application.ChangeTicketPriority

import mt.pollub.demo.shared.exception.NotFoundException
import mt.pollub.demo.ticket.domain.TicketHistory
import mt.pollub.demo.ticket.domain.TicketHistoryEventType
import mt.pollub.demo.ticket.domain.TicketHistoryRepository
import mt.pollub.demo.ticket.domain.TicketRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ChangeTicketPriorityHandler(
    private val ticketRepository: TicketRepository,
    private val ticketHistoryRepository: TicketHistoryRepository
) {
    @Transactional
    fun handle(command: ChangeTicketPriorityCommand) {
        val ticket = ticketRepository.findById(command.ticketId).orElse(null)
            ?: throw NotFoundException("Ticket not found: ${command.ticketId}")

        val (oldPriority, newPriority) = ticket.changePriority(command.newPriority)

        ticketHistoryRepository.save(
            TicketHistory(
                ticket = ticket,
                eventType = TicketHistoryEventType.PRIORITY_CHANGED,
                actorId = command.actorId,
                fieldName = "priority",
                oldValue = oldPriority.name,
                newValue = newPriority.name
            )
        )

        ticketRepository.save(ticket)
    }
}

