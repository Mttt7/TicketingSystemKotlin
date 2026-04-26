package mt.pollub.demo.ticket.application.GetTicket

import mt.pollub.demo.shared.exception.NotFoundException
import mt.pollub.demo.ticket.domain.TicketRepository
import mt.pollub.demo.user.domain.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GetTicketHandler(
    private val ticketRepository: TicketRepository,
    private val userRepository: UserRepository
) {
    @Transactional(readOnly = true)
    fun handle(query: GetTicketQuery): GetTicketResult {
        val ticket = ticketRepository.findDetailedById(query.ticketId)
            ?: throw NotFoundException("Ticket not found: ${query.ticketId}")

        val ticketId = requireNotNull(ticket.id) { "Ticket has null ID" }

        val author = userRepository.findById(ticket.authorId).orElse(null)
        val firstName = author?.firstName ?: "Unknown"
        val lastName = author?.lastName ?: "User"
        val email = author?.email ?: "unknown@example.com"

        return GetTicketResult(
            id = ticketId,
            title = ticket.title,
            description = ticket.description,
            priority = ticket.priority,
            status = ticket.status,
            authorId = ticket.authorId,
            author = TicketAuthorResult(
                id = ticket.authorId,
                firstName = firstName,
                lastName = lastName,
                email = email,
                display = "$firstName $lastName <$email>"
            ),
            assigneeIds = ticket.assigneeIds.toSet(),
            category = ticket.category,
            dueAt = ticket.dueAt,
            createdAt = ticket.createdAt,
            updatedAt = ticket.updatedAt,
            comments = ticket.comments.map { comment ->
                GetTicketCommentResult(
                    id = requireNotNull(comment.id) { "Comment has null ID" },
                    authorId = comment.authorId,
                    content = comment.content,
                    createdAt = comment.createdAt,
                    updatedAt = comment.updatedAt
                )
            }
        )
    }
}
