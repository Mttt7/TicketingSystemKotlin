package mt.pollub.demo.ticket.application.EditTicket

import mt.pollub.demo.shared.exception.NotFoundException
import mt.pollub.demo.ticket.domain.TicketHistory
import mt.pollub.demo.ticket.domain.TicketHistoryEventType
import mt.pollub.demo.ticket.domain.TicketHistoryRepository
import mt.pollub.demo.ticket.domain.TicketRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class EditTicketHandler(
    private val ticketRepository: TicketRepository,
    private val ticketHistoryRepository: TicketHistoryRepository
) {
    @Transactional
    fun handle(command: EditTicketCommand) {
        val ticket = ticketRepository.findDetailedById(command.ticketId)
            ?: throw NotFoundException("Ticket not found: ${command.ticketId}")

        // Edit basic fields
        val fieldChanges = ticket.edit(
            title = command.title,
            description = command.description,
            priority = command.priority,
            category = command.category,
            dueAt = command.dueAt,
            categoryExplicit = command.categoryExplicit,
            dueAtExplicit = command.dueAtExplicit
        )

        // Record history for each changed field
        fieldChanges.forEach { (field, change) ->
            ticketHistoryRepository.save(
                TicketHistory(
                    ticket = ticket,
                    eventType = TicketHistoryEventType.TICKET_EDITED,
                    actorId = command.actorId,
                    fieldName = field,
                    oldValue = change.first,
                    newValue = change.second
                )
            )
        }

        // Handle assignees
        command.assigneeIds?.let { newAssignees ->
            val (added, removed) = ticket.updateAssignees(newAssignees)
            if (added.isNotEmpty() || removed.isNotEmpty()) {
                ticketHistoryRepository.save(
                    TicketHistory(
                        ticket = ticket,
                        eventType = TicketHistoryEventType.ASSIGNEES_CHANGED,
                        actorId = command.actorId,
                        assigneesAdded = added.joinToString(","),
                        assigneesRemoved = removed.joinToString(",")
                    )
                )
            }
        }

        ticketRepository.save(ticket)
    }
}

