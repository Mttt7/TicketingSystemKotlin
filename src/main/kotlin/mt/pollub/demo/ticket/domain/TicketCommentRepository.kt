package mt.pollub.demo.ticket.domain

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface TicketCommentRepository : JpaRepository<TicketComment, UUID> {
    fun findByIdAndTicketId(commentId: UUID, ticketId: UUID): TicketComment?
}

