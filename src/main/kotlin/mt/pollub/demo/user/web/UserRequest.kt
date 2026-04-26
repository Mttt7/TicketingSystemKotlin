package mt.pollub.demo.user.web

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import mt.pollub.demo.user.application.EditUserProfile.EditUserProfileCommand
import java.util.UUID

data class EditUserProfileRequest(
    @field:NotBlank(message = "First name is required")
    @field:Size(min = 2, max = 80, message = "First name must be between 2 and 80 characters")
    val firstName: String,

    @field:NotBlank(message = "Last name is required")
    @field:Size(min = 2, max = 80, message = "Last name must be between 2 and 80 characters")
    val lastName: String
) {
    fun toCommand(userId: UUID) = EditUserProfileCommand(
        userId = userId,
        firstName = firstName,
        lastName = lastName
    )
}

