package mt.pollub.demo.ticket.application

import mt.pollub.demo.ticket.application.CreateTicket.CreateTicketCommentData
import mt.pollub.demo.ticket.application.CreateTicket.CreateTicketCommand
import mt.pollub.demo.ticket.application.CreateTicket.CreateTicketHandler
import mt.pollub.demo.ticket.application.GetTicket.GetTicketHandler
import mt.pollub.demo.ticket.application.GetTicket.GetTicketQuery
import mt.pollub.demo.ticket.domain.TicketPriority
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.LocalDateTime
import java.util.UUID

@SpringBootTest
class TicketHandlersIntegrationTest {

    @Autowired
    private lateinit var createTicketHandler: CreateTicketHandler

    @Autowired
    private lateinit var getTicketHandler: GetTicketHandler

    @Test
    fun `should create and fetch ticket`() {
        val authorId = UUID.randomUUID()
        val assigneeId = UUID.randomUUID()
        val dueAt = LocalDateTime.now().plusDays(2)

        val ticketId = createTicketHandler.handle(
            CreateTicketCommand(
                title = "Cannot login",
                description = "User cannot login to the panel",
                priority = TicketPriority.HIGH,
                authorId = authorId,
                assigneeIds = setOf(assigneeId),
                category = "AUTH",
                dueAt = dueAt,
                comments = listOf(
                    CreateTicketCommentData(authorId = authorId, content = "Problem appeared after update")
                )
            )
        )

        val ticket = getTicketHandler.handle(GetTicketQuery(ticketId))

        assertEquals(ticketId, ticket.id)
        assertEquals("Cannot login", ticket.title)
        assertEquals(TicketPriority.HIGH, ticket.priority)
        assertEquals(authorId, ticket.authorId)
        assertTrue(ticket.assigneeIds.contains(assigneeId))
        assertEquals(1, ticket.comments.size)
    }
}

