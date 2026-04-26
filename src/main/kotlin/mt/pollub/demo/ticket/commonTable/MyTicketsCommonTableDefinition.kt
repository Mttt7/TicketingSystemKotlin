package mt.pollub.demo.ticket.commonTable

import mt.pollub.demo.shared.exception.NotFoundException
import mt.pollub.demo.ticket.domain.Ticket
import mt.pollub.demo.ticket.domain.TicketRepository
import mt.pollub.demo.user.domain.UserRepository
import org.springframework.data.jpa.domain.Specification
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class MyTicketsCommonTableDefinition(
    ticketRepository: TicketRepository,
    private val userRepository: UserRepository
) : TicketBaseCommonTableDefinition(ticketRepository, userRepository) {

    override val tableKey: String = "my-tickets"

    override fun extraSpecification(): Specification<Ticket> {
        val currentUserId = resolveCurrentUserId()
        return Specification { root, _, cb ->
            cb.isMember(currentUserId, root.get("assigneeIds"))
        }
    }

    private fun resolveCurrentUserId(): UUID {
        val email = SecurityContextHolder.getContext().authentication?.name
            ?: error("No authenticated user in security context")

        val user = userRepository.findByEmail(email)
            ?: throw NotFoundException("Authenticated user not found: $email")

        return requireNotNull(user.id) { "User has null ID: $email" }
    }
}

