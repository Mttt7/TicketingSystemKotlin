package mt.pollub.demo.user.application.GetAgents

data class GetAgentsQuery(
    /** Free-text search against firstName, lastName, email. Empty = no filter. */
    val query: String = "",
    val page: Int = 0,
    val pageSize: Int = 20,
    /** When false, the currently authenticated user is excluded from results. */
    val includeSelf: Boolean = true
)

