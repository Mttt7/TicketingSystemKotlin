package mt.pollub.demo.ticket.domain

import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import mt.pollub.demo.shared.exception.NotFoundException
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "tickets")
class Ticket(
    @Column(nullable = false, length = 200)
    var title: String,

    @Column(nullable = false, length = 4000)
    var description: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var priority: TicketPriority,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: TicketStatus = TicketStatus.ZGLOSZONE,

    @Column(nullable = false)
    var authorId: UUID,

    @Column(length = 80)
    var category: String? = null,

    var dueAt: LocalDateTime? = null,

    @Column(nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "ticket_assignees", joinColumns = [JoinColumn(name = "ticket_id")])
    @Column(name = "assignee_id", nullable = false)
    var assigneeIds: MutableSet<UUID> = mutableSetOf()

    @OneToMany(mappedBy = "ticket", cascade = [jakarta.persistence.CascadeType.ALL], orphanRemoval = true)
    var comments: MutableList<TicketComment> = mutableListOf()

    fun addComment(comment: TicketComment) {
        comment.ticket = this
        comments.add(comment)
        touch()
    }

    /**
     * Updates editable fields. Returns a map of fieldName -> Pair(oldValue, newValue)
     * for fields that actually changed.
     */
    fun edit(
        title: String? = null,
        description: String? = null,
        priority: TicketPriority? = null,
        category: String? = this.category,
        dueAt: LocalDateTime? = this.dueAt,
        categoryExplicit: Boolean = false,
        dueAtExplicit: Boolean = false
    ): Map<String, Pair<String, String>> {
        val changes = mutableMapOf<String, Pair<String, String>>()

        title?.trim()?.let {
            if (it != this.title) {
                changes["title"] = this.title to it
                this.title = it
            }
        }
        description?.trim()?.let {
            if (it != this.description) {
                changes["description"] = this.description to it
                this.description = it
            }
        }
        priority?.let {
            if (it != this.priority) {
                changes["priority"] = this.priority.name to it.name
                this.priority = it
            }
        }
        if (categoryExplicit) {
            val newCategory = category?.trim()?.ifBlank { null }
            if (newCategory != this.category) {
                changes["category"] = (this.category ?: "") to (newCategory ?: "")
                this.category = newCategory
            }
        }
        if (dueAtExplicit) {
            if (dueAt != this.dueAt) {
                changes["dueAt"] = (this.dueAt?.toString() ?: "") to (dueAt?.toString() ?: "")
                this.dueAt = dueAt
            }
        }

        if (changes.isNotEmpty()) touch()
        return changes
    }

    fun changeStatus(newStatus: TicketStatus): Pair<TicketStatus, TicketStatus> {
        val old = this.status
        this.status = newStatus
        touch()
        return old to newStatus
    }

    fun changePriority(newPriority: TicketPriority): Pair<TicketPriority, TicketPriority> {
        val old = this.priority
        this.priority = newPriority
        touch()
        return old to newPriority
    }

    /**
     * Replaces assignees with the new set.
     * Returns Pair(added UUIDs, removed UUIDs).
     */
    fun updateAssignees(newAssigneeIds: Set<UUID>): Pair<Set<UUID>, Set<UUID>> {
        val added = newAssigneeIds - assigneeIds
        val removed = assigneeIds - newAssigneeIds
        assigneeIds.clear()
        assigneeIds.addAll(newAssigneeIds)
        if (added.isNotEmpty() || removed.isNotEmpty()) touch()
        return added to removed
    }

    fun editComment(commentId: UUID, newContent: String): Pair<String, String> {
        val comment = comments.find { it.id == commentId }
            ?: throw NotFoundException("Comment not found: $commentId in ticket: $id")
        val old = comment.content
        comment.content = newContent.trim()
        comment.updatedAt = LocalDateTime.now()
        touch()
        return old to comment.content
    }

    fun removeComment(commentId: UUID): TicketComment {
        val comment = comments.find { it.id == commentId }
            ?: throw NotFoundException("Comment not found: $commentId in ticket: $id")
        comments.remove(comment)
        touch()
        return comment
    }

    fun touch() {
        updatedAt = LocalDateTime.now()
    }
}
