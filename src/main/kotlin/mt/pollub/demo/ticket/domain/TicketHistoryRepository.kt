package mt.pollub.demo.ticket.domain

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime
import java.util.UUID

interface TicketHistoryRepository : JpaRepository<TicketHistory, UUID> {
    fun findByTicketIdOrderByOccurredAtAsc(ticketId: UUID): List<TicketHistory>

    /**
     * Counts distinct tickets that had their status changed to [targetStatus]
     * at or after [since]. Uses DISTINCT ticketId to avoid counting a
     * ticket twice if it was moved to that status multiple times in one day.
     */
    @Query("""
        SELECT COUNT(DISTINCT h.ticket.id)
        FROM TicketHistory h
        WHERE h.eventType = 'STATUS_CHANGED'
          AND h.newValue  = :targetStatus
          AND h.occurredAt >= :since
    """)
    fun countDistinctTicketsWithStatusChangedTo(
        @Param("targetStatus") targetStatus: String,
        @Param("since") since: LocalDateTime
    ): Long
}

