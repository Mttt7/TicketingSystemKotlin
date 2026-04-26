package mt.pollub.demo.user.application.GetAgents

import mt.pollub.demo.user.domain.User
import mt.pollub.demo.user.domain.UserRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.jpa.domain.Specification
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.Expression
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Root

@Service
class GetAgentsHandler(
    private val userRepository: UserRepository
) {
    @Transactional(readOnly = true)
    fun handle(query: GetAgentsQuery): GetAgentsResult {
        val currentEmail: String? = if (!query.includeSelf) {
            SecurityContextHolder.getContext().authentication?.name
        } else null

        val spec = buildSpecification(
            searchQuery = query.query.trim(),
            excludeEmail = currentEmail
        )

        val pageable = PageRequest.of(query.page, query.pageSize)
        val page = userRepository.findAll(spec, pageable)

        return GetAgentsResult(
            items = page.content.map { it.toAgentItem() },
            totalElements = page.totalElements,
            totalPages = page.totalPages,
            page = page.number,
            pageSize = page.size
        )
    }

    private fun buildSpecification(
        searchQuery: String,
        excludeEmail: String?
    ): Specification<User> {
        return Specification { root, criteriaQuery, cb ->
            val predicates = mutableListOf<Predicate>()

            // Full-text search: firstName OR lastName OR email (case-insensitive)
            if (searchQuery.isNotBlank()) {
                val pattern = "%${searchQuery.lowercase()}%"
                predicates += cb.or(
                    cb.like(cb.lower(root.get("firstName")), pattern),
                    cb.like(cb.lower(root.get("lastName")), pattern),
                    cb.like(cb.lower(root.get("email")), pattern)
                )
            }

            // Exclude current user when includeSelf=false
            if (excludeEmail != null) {
                predicates += cb.notEqual(root.get<String>("email"), excludeEmail)
            }

            // Relevance ordering: prefix match first, then alphabetical
            // Applied only on data queries, not on count queries
            val isCountQuery = criteriaQuery?.resultType == Long::class.java ||
                    criteriaQuery?.resultType == java.lang.Long::class.java
            if (!isCountQuery) {
                val orders = mutableListOf(
                    cb.asc(cb.lower(root.get<String>("firstName"))),
                    cb.asc(cb.lower(root.get<String>("lastName"))),
                    cb.asc(root.get<String>("email"))
                )
                // Prepend relevance only when there is an actual search term —
                // cb.literal(0) in ORDER BY is invalid SQL in PostgreSQL.
                if (searchQuery.isNotBlank()) {
                    orders.add(0, cb.asc(relevanceExpression(root, cb, searchQuery)))
                }
                criteriaQuery?.orderBy(orders)
            }

            if (predicates.isEmpty()) cb.conjunction()
            else cb.and(*predicates.toTypedArray())
        }
    }

    /**
     * Returns 0 when any of the searchable fields **starts with** [query],
     * 1 otherwise — used as the primary sort key to surface prefix matches first.
     * Must only be called when [query] is non-blank.
     */
    private fun relevanceExpression(
        root: Root<User>,
        cb: CriteriaBuilder,
        query: String
    ): Expression<Int> {
        val prefixPattern = "${query.lowercase()}%"
        val prefixPredicate = cb.or(
            cb.like(cb.lower(root.get("firstName")), prefixPattern),
            cb.like(cb.lower(root.get("lastName")), prefixPattern),
            cb.like(cb.lower(root.get("email")), prefixPattern)
        )
        @Suppress("UNCHECKED_CAST")
        return cb.selectCase<Int>()
            .`when`(prefixPredicate, 0)
            .otherwise(1) as Expression<Int>
    }

    private fun User.toAgentItem(): AgentItem {
        val fn = firstName?.trim().orEmpty().ifBlank { "Unknown" }
        val ln = lastName?.trim().orEmpty().ifBlank { "User" }
        return AgentItem(
            id = requireNotNull(id),
            firstName = fn,
            lastName = ln,
            email = email,
            display = "$fn $ln <$email>",
            role = role
        )
    }
}

