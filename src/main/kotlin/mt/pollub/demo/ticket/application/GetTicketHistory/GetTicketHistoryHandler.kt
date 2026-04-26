package mt.pollub.demo.ticket.application.GetTicketHistory

import mt.pollub.demo.shared.exception.NotFoundException
import mt.pollub.demo.ticket.domain.TicketHistoryRepository
import mt.pollub.demo.ticket.domain.TicketRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GetTicketHistoryHandler(
    private val ticketRepository: TicketRepository,
    private val ticketHistoryRepository: TicketHistoryRepository
) {
    @Transactional(readOnly = true)
    fun handle(query: GetTicketHistoryQuery): GetTicketHistoryResult {
        if (!ticketRepository.existsById(query.ticketId)) {
            throw NotFoundException("Ticket not found: ${query.ticketId}")
        }

        val entries = ticketHistoryRepository
            .findByTicketIdOrderByOccurredAtAsc(query.ticketId)
            .map { h ->
                TicketHistoryEntryResult(
                    id = requireNotNull(h.id),
                    ticketId = query.ticketId,
                    eventType = h.eventType,
                    actorId = h.actorId,
                    fieldName = h.fieldName,
                    oldValue = h.oldValue,
                    newValue = h.newValue,
                    assigneesAdded = h.assigneesAdded,
                    assigneesRemoved = h.assigneesRemoved,
                    commentId = h.commentId,
                    commentContent = h.commentContent,
                    occurredAt = h.occurredAt
                )
            }

        return GetTicketHistoryResult(entries = entries)
    }
}

