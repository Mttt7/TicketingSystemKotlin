package mt.pollub.demo.ticket.application.GetTicketStats

import mt.pollub.demo.ticket.domain.TicketStatus

data class GetTicketStatsResult(
    /** Count per every known status value (0 when no tickets exist for a status). */
    val byStatus: Map<TicketStatus, Long>,
    /** Tickets moved to OTWARTE today (since midnight). */
    val openedToday: Long,
    /** Tickets moved to ZAMKNIETE today (since midnight). */
    val closedToday: Long
)

