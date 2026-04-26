package mt.pollub.demo.user.web

import jakarta.validation.Valid
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import mt.pollub.demo.user.application.EditUserProfile.EditUserProfileHandler
import mt.pollub.demo.user.application.GetAgents.GetAgentsHandler
import mt.pollub.demo.user.application.GetAgents.GetAgentsQuery
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@Validated
@RestController
@RequestMapping("/api")
class UserController(
    private val editUserProfileHandler: EditUserProfileHandler,
    private val getAgentsHandler: GetAgentsHandler
) {

    /**
     * PATCH /api/users/{id}/profile
     * Edits first name and last name. Email is immutable.
     */
    @PatchMapping("/users/{id}/profile")
    fun editProfile(
        @PathVariable id: UUID,
        @RequestBody @Valid request: EditUserProfileRequest
    ): ResponseEntity<Void> {
        editUserProfileHandler.handle(request.toCommand(id))
        return ResponseEntity.noContent().build()
    }

    /**
     * GET /api/agents
     * Returns a paginated, searchable list of users eligible for ticket assignment.
     * Used to populate the AssigneeMultiselectComponent.
     */
    @GetMapping("/agents")
    fun getAgents(
        @RequestParam(defaultValue = "") query: String,
        @RequestParam(defaultValue = "0") @Min(0) page: Int,
        @RequestParam(defaultValue = "20") @Min(1) @Max(50) pageSize: Int,
        @RequestParam(defaultValue = "true") includeSelf: Boolean
    ): ResponseEntity<AgentsPageResponse> {
        val result = getAgentsHandler.handle(
            GetAgentsQuery(
                query = query,
                page = page,
                pageSize = pageSize,
                includeSelf = includeSelf
            )
        )
        return ResponseEntity.ok(AgentsPageResponse.from(result))
    }
}
