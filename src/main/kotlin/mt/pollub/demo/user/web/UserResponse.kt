package mt.pollub.demo.user.web

import mt.pollub.demo.user.application.GetAgents.AgentItem
import mt.pollub.demo.user.application.GetAgents.GetAgentsResult
import mt.pollub.demo.user.domain.UserRole
import java.util.UUID

data class AgentResponse(
    val id: UUID,
    val firstName: String,
    val lastName: String,
    val email: String,
    val display: String,
    val role: UserRole
) {
    companion object {
        fun from(item: AgentItem) = AgentResponse(
            id = item.id,
            firstName = item.firstName,
            lastName = item.lastName,
            email = item.email,
            display = item.display,
            role = item.role
        )
    }
}

data class AgentsPageResponse(
    val items: List<AgentResponse>,
    val totalElements: Long,
    val totalPages: Int,
    val page: Int,
    val pageSize: Int
) {
    companion object {
        fun from(result: GetAgentsResult) = AgentsPageResponse(
            items = result.items.map(AgentResponse::from),
            totalElements = result.totalElements,
            totalPages = result.totalPages,
            page = result.page,
            pageSize = result.pageSize
        )
    }
}

