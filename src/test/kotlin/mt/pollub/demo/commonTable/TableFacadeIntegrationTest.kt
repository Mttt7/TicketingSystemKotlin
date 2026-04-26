package mt.pollub.demo.commonTable

import mt.pollub.demo.commonTable.model.CommonTableRequest
import mt.pollub.demo.commonTable.model.FilterOperator
import mt.pollub.demo.commonTable.model.FilterRule
import mt.pollub.demo.commonTable.model.SortDirection
import mt.pollub.demo.commonTable.model.SortRule
import mt.pollub.demo.ticket.application.CreateTicket.CreateTicketCommand
import mt.pollub.demo.ticket.application.CreateTicket.CreateTicketHandler
import mt.pollub.demo.ticket.domain.TicketPriority
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.util.UUID

@SpringBootTest
class TableFacadeIntegrationTest {

    @Autowired
    private lateinit var createTicketHandler: CreateTicketHandler

    @Autowired
    private lateinit var tableFacade: TableFacade

    @Test
    fun `should return table rows for tickets key using legacy filters`() {
        createTicketHandler.handle(
            CreateTicketCommand(
                title = "Common table ticket",
                description = "Created to test common table",
                priority = TicketPriority.MEDIUM,
                authorId = UUID.randomUUID(),
                category = "SUPPORT"
            )
        )

        val response = tableFacade.handle(
            CommonTableRequest(
                tableKey = "tickets",
                page = 0,
                pageSize = 10,
                sortBy = "createdAt",
                filters = mapOf("title" to "Common table")
            )
        )

        assertEquals("tickets", response.tableKey)
        assertTrue(response.rows.isNotEmpty())
        assertTrue(response.columns.any { it.key == "title" })
    }

    @Test
    fun `should support filter operators and multi sort`() {
        val token = UUID.randomUUID().toString().substring(0, 8)

        createTicketHandler.handle(
            CreateTicketCommand(
                title = "A-common-$token",
                description = "Created to test operator filters",
                priority = TicketPriority.HIGH,
                authorId = UUID.randomUUID(),
                category = "SUPPORT"
            )
        )

        createTicketHandler.handle(
            CreateTicketCommand(
                title = "B-common-$token",
                description = "Created to test operator filters",
                priority = TicketPriority.LOW,
                authorId = UUID.randomUUID(),
                category = "SUPPORT"
            )
        )

        val response = tableFacade.handle(
            CommonTableRequest(
                tableKey = "tickets",
                page = 0,
                pageSize = 10,
                sorts = listOf(
                    SortRule(field = "title", direction = SortDirection.ASC),
                    SortRule(field = "createdAt", direction = SortDirection.DESC)
                ),
                filterRules = listOf(
                    FilterRule(field = "title", operator = FilterOperator.CONTAINS, value = "common-$token"),
                    FilterRule(field = "priority", operator = FilterOperator.IN, values = listOf("HIGH", "LOW"))
                )
            )
        )

        assertEquals("tickets", response.tableKey)
        assertTrue(response.rows.size >= 2)
        assertEquals("A-common-$token", response.rows.first()["title"])
    }
}
