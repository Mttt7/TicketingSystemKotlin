package mt.pollub.demo.ticket.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "ticket_history")
class TicketHistory(

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    val ticket: Ticket,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    val eventType: TicketHistoryEventType,

    @Column(nullable = false)
    val actorId: UUID,

    /** Affected field name, e.g. "title", "status", "priority" */
    @Column(length = 100)
    val fieldName: String? = null,

    @Column(length = 500)
    val oldValue: String? = null,

    @Column(length = 500)
    val newValue: String? = null,

    /** For ASSIGNEES_CHANGED: comma-separated UUIDs that were added */
    @Column(length = 2000)
    val assigneesAdded: String? = null,

    /** For ASSIGNEES_CHANGED: comma-separated UUIDs that were removed */
    @Column(length = 2000)
    val assigneesRemoved: String? = null,

    /** For COMMENT_* events: the affected comment id */
    val commentId: UUID? = null,

    /** For COMMENT_* events: snapshot of comment content at event time */
    @Column(length = 3000)
    val commentContent: String? = null,

    @Column(nullable = false)
    val occurredAt: LocalDateTime = LocalDateTime.now()
) {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null
}

