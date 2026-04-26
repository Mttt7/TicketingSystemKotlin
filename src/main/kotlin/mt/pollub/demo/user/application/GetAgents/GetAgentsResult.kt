package mt.pollub.demo.user.application.GetAgents

import mt.pollub.demo.user.domain.UserRole
import java.util.UUID

data class GetAgentsResult(
    val items: List<AgentItem>,
    val totalElements: Long,
    val totalPages: Int,
    val page: Int,
    val pageSize: Int
)

data class AgentItem(
    val id: UUID,
    val firstName: String,
    val lastName: String,
    val email: String,
    val display: String,
    val role: UserRole
)

