package mt.pollub.demo.ticket.commonTable

import mt.pollub.demo.ticket.domain.TicketRepository
import mt.pollub.demo.user.domain.UserRepository
import org.springframework.stereotype.Component

/**
 * CommonTable definition for "All Tickets" list.
 * All shared logic lives in [TicketBaseCommonTableDefinition].
 */
@Component
class TicketCommonTableDefinition(
    ticketRepository: TicketRepository,
    userRepository: UserRepository
) : TicketBaseCommonTableDefinition(ticketRepository, userRepository) {

    override val tableKey: String = "tickets"
}
