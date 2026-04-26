package mt.pollub.demo.ticket.application.ChangeTicketStatus

import mt.pollub.demo.shared.exception.NotFoundException
import mt.pollub.demo.ticket.domain.TicketHistory
import mt.pollub.demo.ticket.domain.TicketHistoryEventType
import mt.pollub.demo.ticket.domain.TicketHistoryRepository
import mt.pollub.demo.ticket.domain.TicketRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ChangeTicketStatusHandler(
    private val ticketRepository: TicketRepository,
    private val ticketHistoryRepository: TicketHistoryRepository
) {
    @Transactional
    fun handle(command: ChangeTicketStatusCommand) {
        val ticket = ticketRepository.findById(command.ticketId).orElse(null)
            ?: throw NotFoundException("Ticket not found: ${command.ticketId}")

        val (oldStatus, newStatus) = ticket.changeStatus(command.newStatus)

        ticketHistoryRepository.save(
            TicketHistory(
                ticket = ticket,
                eventType = TicketHistoryEventType.STATUS_CHANGED,
                actorId = command.actorId,
                fieldName = "status",
                oldValue = oldStatus.name,
                newValue = newStatus.name
            )
        )

        ticketRepository.save(ticket)
    }
}

