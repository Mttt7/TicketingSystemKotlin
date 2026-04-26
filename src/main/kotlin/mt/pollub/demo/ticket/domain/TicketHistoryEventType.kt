package mt.pollub.demo.ticket.domain

enum class TicketHistoryEventType {
    TICKET_CREATED,
    TICKET_EDITED,
    STATUS_CHANGED,
    PRIORITY_CHANGED,
    ASSIGNEES_CHANGED,
    COMMENT_ADDED,
    COMMENT_EDITED,
    COMMENT_DELETED
}

