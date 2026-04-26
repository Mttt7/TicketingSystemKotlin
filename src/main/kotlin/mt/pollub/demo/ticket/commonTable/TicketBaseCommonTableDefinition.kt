package mt.pollub.demo.ticket.commonTable

import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.Path
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Root
import mt.pollub.demo.commonTable.definition.BaseCommonTableDefinition
import mt.pollub.demo.commonTable.definition.ColumnDefinition
import mt.pollub.demo.commonTable.model.CommonTableRequest
import mt.pollub.demo.commonTable.model.FilterOperator
import mt.pollub.demo.commonTable.model.FilterRule
import mt.pollub.demo.shared.utils.GlobalUtils
import mt.pollub.demo.ticket.domain.Ticket
import mt.pollub.demo.ticket.domain.TicketPriority
import mt.pollub.demo.ticket.domain.TicketRepository
import mt.pollub.demo.ticket.domain.TicketStatus
import mt.pollub.demo.user.domain.UserRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import java.time.LocalDateTime
import java.util.UUID

/**
 * Shared base for all Ticket-based CommonTable definitions.
 * Provides columns, specification building and filter logic.
 * Subclasses only need to declare [tableKey] and may override
 * [extraSpecification] to inject mandatory predicates (e.g. "current user only").
 */
abstract class TicketBaseCommonTableDefinition(
    private val ticketRepository: TicketRepository,
    private val userRepository: UserRepository
) : BaseCommonTableDefinition<Ticket>() {

    override fun defaultSortPath(): String = "createdAt"

    override fun columns(): List<ColumnDefinition<Ticket>> = listOf(
        ColumnDefinition.builder<Ticket>("id")
            .displayName("ID")
            .path("id")
            .sortable(true)
            .filterable(true)
            .value { it.id }
            .build(),
        ColumnDefinition.builder<Ticket>("title")
            .displayName("Title")
            .path("title")
            .sortable(true)
            .filterable(true)
            .value { it.title }
            .build(),
        ColumnDefinition.builder<Ticket>("priority")
            .displayName("Priority")
            .path("priority")
            .sortable(true)
            .filterable(true)
            .value { it.priority.name }
            .build(),
        ColumnDefinition.builder<Ticket>("status")
            .displayName("Status")
            .path("status")
            .sortable(true)
            .filterable(true)
            .value { it.status.name }
            .build(),
        ColumnDefinition.builder<Ticket>("author")
            .displayName("Author")
            .path("authorId")
            .sortable(true)
            .filterable(true)
            .value { ticket ->
                val user = userRepository.findById(ticket.authorId).orElse(null)
                if (user != null) {
                    val firstName = user.firstName?.trim().orEmpty().ifBlank { "Unknown" }
                    val lastName = user.lastName?.trim().orEmpty().ifBlank { "User" }
                    "$firstName $lastName <${user.email}>"
                } else {
                    ticket.authorId.toString()
                }
            }
            .build(),
        ColumnDefinition.builder<Ticket>("category")
            .displayName("Category")
            .path("category")
            .sortable(true)
            .filterable(true)
            .value { it.category }
            .build(),
        ColumnDefinition.builder<Ticket>("dueAt")
            .displayName("Due At")
            .path("dueAt")
            .sortable(true)
            .filterable(true)
            .value { GlobalUtils.formatDateTime(it.dueAt) }
            .build(),
        ColumnDefinition.builder<Ticket>("createdAt")
            .displayName("Created At")
            .path("createdAt")
            .sortable(true)
            .filterable(true)
            .value { GlobalUtils.formatDateTime(it.createdAt) }
            .build(),
        ColumnDefinition.builder<Ticket>("updatedAt")
            .displayName("Updated At")
            .path("updatedAt")
            .sortable(true)
            .filterable(true)
            .value { GlobalUtils.formatDateTime(it.updatedAt) }
            .build()
    )

    /**
     * Hook for subclasses to inject mandatory predicates (e.g. filter by assignee).
     * Return null when no extra constraint is needed.
     */
    protected open fun extraSpecification(): Specification<Ticket>? = null

    override fun findPage(request: CommonTableRequest, pageable: Pageable): Page<Ticket> {
        val requestSpec = buildSpecification(request)
        val extra = extraSpecification()
        val spec = if (extra != null) requestSpec.and(extra) else requestSpec
        return ticketRepository.findAll(spec, pageable)
    }

    // ── Specification building ────────────────────────────────────────────────

    private fun buildSpecification(request: CommonTableRequest): Specification<Ticket> {
        return Specification { root, _, cb ->
            val predicates = mutableListOf<Predicate>()

            // Legacy filters (backward compatible)
            request.filters.orEmpty().forEach { (key, value) ->
                if (value.isBlank()) return@forEach
                when (key) {
                    "title", "category" -> predicates += containsIgnoreCase(cb, root.get(key), value)
                    "status" -> parseStatus(value)?.let { predicates += cb.equal(root.get<TicketStatus>("status"), it) }
                    "priority" -> parsePriority(value)?.let { predicates += cb.equal(root.get<TicketPriority>("priority"), it) }
                    "authorId", "author" -> parseUuid(value)?.let { predicates += cb.equal(root.get<UUID>("authorId"), it) }
                }
            }

            // New operator-based filters
            request.filterRules.orEmpty().forEach { rule ->
                applyRule(rule, root, cb)?.let { predicates += it }
            }

            cb.and(*predicates.toTypedArray())
        }
    }

    // ── Rule dispatching ──────────────────────────────────────────────────────

    private fun applyRule(rule: FilterRule, root: Root<Ticket>, cb: CriteriaBuilder): Predicate? =
        when (rule.field) {
            "id" -> applyUuidRule(rule, root.get("id"), cb)
            "title", "category" -> applyStringRule(rule, root.get(rule.field), cb)
            "status" -> applyStatusRule(rule, root.get("status"), cb)
            "priority" -> applyPriorityRule(rule, root.get("priority"), cb)
            "authorId", "author" -> applyUuidRule(rule, root.get("authorId"), cb)
            "createdAt", "updatedAt", "dueAt" -> applyDateTimeRule(rule, root.get(rule.field), cb)
            else -> null
        }

    // ── Per-type filter helpers ───────────────────────────────────────────────

    private fun applyStringRule(rule: FilterRule, path: Path<String>, cb: CriteriaBuilder): Predicate? =
        when (rule.operator ?: FilterOperator.EQ) {
            FilterOperator.EQ -> rule.value?.takeIf { it.isNotBlank() }
                ?.let { cb.equal(cb.lower(path), it.lowercase()) }
            FilterOperator.CONTAINS -> rule.value?.takeIf { it.isNotBlank() }
                ?.let { containsIgnoreCase(cb, path, it) }
            FilterOperator.IN -> {
                val values = rule.values.orEmpty().map { it.trim() }.filter { it.isNotBlank() }.map { it.lowercase() }
                if (values.isEmpty()) null else cb.lower(path).`in`(values)
            }
            else -> null
        }

    private fun applyStatusRule(rule: FilterRule, path: Path<TicketStatus>, cb: CriteriaBuilder): Predicate? =
        when (rule.operator ?: FilterOperator.EQ) {
            FilterOperator.EQ -> parseStatus(rule.value ?: "")?.let { cb.equal(path, it) }
            FilterOperator.IN -> {
                val values = rule.values.orEmpty().mapNotNull(::parseStatus)
                if (values.isEmpty()) null else path.`in`(values)
            }
            else -> null
        }

    private fun applyPriorityRule(rule: FilterRule, path: Path<TicketPriority>, cb: CriteriaBuilder): Predicate? =
        when (rule.operator ?: FilterOperator.EQ) {
            FilterOperator.EQ -> parsePriority(rule.value ?: "")?.let { cb.equal(path, it) }
            FilterOperator.IN -> {
                val values = rule.values.orEmpty().mapNotNull(::parsePriority)
                if (values.isEmpty()) null else path.`in`(values)
            }
            else -> null
        }

    private fun applyUuidRule(rule: FilterRule, path: Path<UUID>, cb: CriteriaBuilder): Predicate? =
        when (rule.operator ?: FilterOperator.EQ) {
            FilterOperator.EQ -> parseUuid(rule.value ?: "")?.let { cb.equal(path, it) }
            FilterOperator.IN -> {
                val values = rule.values.orEmpty().mapNotNull(::parseUuid)
                if (values.isEmpty()) null else path.`in`(values)
            }
            else -> null
        }

    private fun applyDateTimeRule(rule: FilterRule, path: Path<LocalDateTime>, cb: CriteriaBuilder): Predicate? =
        when (rule.operator ?: FilterOperator.EQ) {
            FilterOperator.EQ -> parseDateTime(rule.value ?: "")?.let { cb.equal(path, it) }
            FilterOperator.GTE -> parseDateTime(rule.value ?: "")?.let { cb.greaterThanOrEqualTo(path, it) }
            FilterOperator.LTE -> parseDateTime(rule.value ?: "")?.let { cb.lessThanOrEqualTo(path, it) }
            FilterOperator.BETWEEN -> {
                val from = parseDateTime(rule.from ?: "")
                val to = parseDateTime(rule.to ?: "")
                when {
                    from != null && to != null -> cb.between(path, from, to)
                    from != null -> cb.greaterThanOrEqualTo(path, from)
                    to != null -> cb.lessThanOrEqualTo(path, to)
                    else -> null
                }
            }
            else -> null
        }

    // ── Parsing utilities ─────────────────────────────────────────────────────

    private fun parseStatus(value: String): TicketStatus? =
        runCatching { TicketStatus.valueOf(value.trim().uppercase()) }.getOrNull()

    private fun parsePriority(value: String): TicketPriority? =
        runCatching { TicketPriority.valueOf(value.trim().uppercase()) }.getOrNull()

    private fun parseUuid(value: String): UUID? =
        runCatching { UUID.fromString(value.trim()) }.getOrNull()

    private fun parseDateTime(value: String): LocalDateTime? =
        runCatching { LocalDateTime.parse(value.trim()) }.getOrNull()

    private fun containsIgnoreCase(cb: CriteriaBuilder, path: Path<String>, value: String): Predicate =
        cb.like(cb.lower(path), "%${value.lowercase()}%")
}

