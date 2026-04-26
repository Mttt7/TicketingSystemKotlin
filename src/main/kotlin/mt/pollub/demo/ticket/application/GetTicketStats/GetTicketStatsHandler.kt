package mt.pollub.demo.ticket.application.GetTicketStats

import mt.pollub.demo.ticket.domain.TicketHistoryRepository
import mt.pollub.demo.ticket.domain.TicketRepository
import mt.pollub.demo.ticket.domain.TicketStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime

@Service
class GetTicketStatsHandler(
    private val ticketRepository: TicketRepository,
    private val ticketHistoryRepository: TicketHistoryRepository
) {
    @Transactional(readOnly = true)
    fun handle(): GetTicketStatsResult {
        // Build a map with 0 for every status, then overlay actual counts
        val byStatus: Map<TicketStatus, Long> = TicketStatus.entries
            .associateWith { 0L }
            .toMutableMap()
            .also { map ->
                ticketRepository.countGroupByStatus().forEach { row ->
                    val status = row[0] as TicketStatus
                    val count  = row[1] as Long
                    map[status] = count
                }
            }

        val startOfToday: LocalDateTime = LocalDate.now().atStartOfDay()

        val openedToday = ticketHistoryRepository.countDistinctTicketsWithStatusChangedTo(
            targetStatus = TicketStatus.OTWARTE.name,
            since = startOfToday
        )
        val closedToday = ticketHistoryRepository.countDistinctTicketsWithStatusChangedTo(
            targetStatus = TicketStatus.ZAMKNIETE.name,
            since = startOfToday
        )

        return GetTicketStatsResult(
            byStatus = byStatus,
            openedToday = openedToday,
            closedToday = closedToday
        )
    }
}

