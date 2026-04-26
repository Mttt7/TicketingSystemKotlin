package mt.pollub.demo.user.application.EditUserProfile

import java.util.UUID

data class EditUserProfileCommand(
    val userId: UUID,
    val firstName: String,
    val lastName: String
)

