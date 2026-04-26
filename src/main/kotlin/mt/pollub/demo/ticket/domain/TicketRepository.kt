package mt.pollub.demo.ticket.domain

import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import java.util.UUID

interface TicketRepository : JpaRepository<Ticket, UUID>, JpaSpecificationExecutor<Ticket> {
    /**
     * Fetches the ticket with its comments in a single JOIN query.
     * assigneeIds is intentionally left out of the graph — fetching two
     * collection joins simultaneously causes a cartesian product that
     * duplicates rows. assigneeIds is a lazy @ElementCollection and will
     * be loaded by a separate SELECT within the same transaction.
     */
    @EntityGraph(attributePaths = ["comments"])
    fun findDetailedById(id: UUID): Ticket?

    /** Returns pairs of (status, count) for all existing tickets. */
    @Query("SELECT t.status, COUNT(t) FROM Ticket t GROUP BY t.status")
    fun countGroupByStatus(): List<Array<Any>>
}
