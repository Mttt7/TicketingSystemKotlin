package mt.pollub.demo.ticket.web

import jakarta.validation.Valid
import mt.pollub.demo.ticket.application.AddComment.AddCommentHandler
import mt.pollub.demo.ticket.application.ChangeTicketPriority.ChangeTicketPriorityHandler
import mt.pollub.demo.ticket.application.ChangeTicketStatus.ChangeTicketStatusHandler
import mt.pollub.demo.ticket.application.CreateTicket.CreateTicketHandler
import mt.pollub.demo.ticket.application.DeleteComment.DeleteCommentCommand
import mt.pollub.demo.ticket.application.DeleteComment.DeleteCommentHandler
import mt.pollub.demo.ticket.application.EditComment.EditCommentHandler
import mt.pollub.demo.ticket.application.EditTicket.EditTicketHandler
import mt.pollub.demo.ticket.application.GetTicket.GetTicketHandler
import mt.pollub.demo.ticket.application.GetTicket.GetTicketQuery
import mt.pollub.demo.ticket.application.GetTicketHistory.GetTicketHistoryHandler
import mt.pollub.demo.ticket.application.GetTicketHistory.GetTicketHistoryQuery
import mt.pollub.demo.ticket.application.GetTicketStats.GetTicketStatsHandler
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/tickets")
class TicketController(
    private val createTicketHandler: CreateTicketHandler,
    private val getTicketHandler: GetTicketHandler,
    private val editTicketHandler: EditTicketHandler,
    private val changeTicketStatusHandler: ChangeTicketStatusHandler,
    private val changeTicketPriorityHandler: ChangeTicketPriorityHandler,
    private val addCommentHandler: AddCommentHandler,
    private val editCommentHandler: EditCommentHandler,
    private val deleteCommentHandler: DeleteCommentHandler,
    private val getTicketHistoryHandler: GetTicketHistoryHandler,
    private val getTicketStatsHandler: GetTicketStatsHandler
) {

    // ── Stats ─────────────────────────────────────────────────────────────────

    @GetMapping("/stats")
    fun getStats(): ResponseEntity<TicketStatsResponse> =
        ResponseEntity.ok(TicketStatsResponse.from(getTicketStatsHandler.handle()))

    // ── Create ────────────────────────────────────────────────────────────────

    @PostMapping
    fun createTicket(@RequestBody @Valid request: CreateTicketRequest): ResponseEntity<CreateTicketResponse> {
        val ticketId = createTicketHandler.handle(request.toCommand())
        return ResponseEntity.status(HttpStatus.CREATED).body(CreateTicketResponse(ticketId))
    }

    // ── Read ──────────────────────────────────────────────────────────────────

    @GetMapping("/{id}")
    fun getTicket(@PathVariable id: UUID): ResponseEntity<TicketResponse> =
        ResponseEntity.ok(TicketResponse.from(getTicketHandler.handle(GetTicketQuery(id))))

    // ── Edit (full partial update: fields + assignees) ────────────────────────

    @PatchMapping("/{id}")
    fun editTicket(@PathVariable id: UUID, @RequestBody @Valid request: EditTicketRequest): ResponseEntity<Void> {
        editTicketHandler.handle(request.toCommand(id))
        return ResponseEntity.noContent().build()
    }

    // ── Status change ─────────────────────────────────────────────────────────

    @PatchMapping("/{id}/status")
    fun changeStatus(@PathVariable id: UUID, @RequestBody @Valid request: ChangeTicketStatusRequest): ResponseEntity<Void> {
        changeTicketStatusHandler.handle(request.toCommand(id))
        return ResponseEntity.noContent().build()
    }

    // ── Priority change ───────────────────────────────────────────────────────

    @PatchMapping("/{id}/priority")
    fun changePriority(@PathVariable id: UUID, @RequestBody @Valid request: ChangeTicketPriorityRequest): ResponseEntity<Void> {
        changeTicketPriorityHandler.handle(request.toCommand(id))
        return ResponseEntity.noContent().build()
    }

    // ── Comments ──────────────────────────────────────────────────────────────

    @PostMapping("/{id}/comments")
    fun addComment(@PathVariable id: UUID, @RequestBody @Valid request: AddCommentRequest): ResponseEntity<AddCommentResponse> {
        val commentId = addCommentHandler.handle(request.toCommand(id))
        return ResponseEntity.status(HttpStatus.CREATED).body(AddCommentResponse(commentId))
    }

    @PatchMapping("/{id}/comments/{commentId}")
    fun editComment(@PathVariable id: UUID, @PathVariable commentId: UUID, @RequestBody @Valid request: EditCommentRequest): ResponseEntity<Void> {
        editCommentHandler.handle(request.toCommand(id, commentId))
        return ResponseEntity.noContent().build()
    }

    @DeleteMapping("/{id}/comments/{commentId}")
    fun deleteComment(@PathVariable id: UUID, @PathVariable commentId: UUID, @RequestBody(required = false) actorRequest: DeleteCommentActorRequest?): ResponseEntity<Void> {
        val actorId = actorRequest?.actorId ?: UUID.fromString("00000000-0000-0000-0000-000000000000")
        deleteCommentHandler.handle(DeleteCommentCommand(ticketId = id, commentId = commentId, actorId = actorId))
        return ResponseEntity.noContent().build()
    }

    // ── History ───────────────────────────────────────────────────────────────

    @GetMapping("/{id}/history")
    fun getHistory(@PathVariable id: UUID): ResponseEntity<GetTicketHistoryResponse> =
        ResponseEntity.ok(GetTicketHistoryResponse.from(getTicketHistoryHandler.handle(GetTicketHistoryQuery(id))))
}

/** Small helper to pass actorId with DELETE /comments/{commentId} */
data class DeleteCommentActorRequest(val actorId: UUID)
