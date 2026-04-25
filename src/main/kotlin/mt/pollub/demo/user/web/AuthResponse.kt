package mt.pollub.demo.user.web

import java.util.UUID

data class RegisterResponse(val userId: UUID)

data class AuthResponse(
    val token: String,
    val type: String = "Bearer",
    val expiresIn: Long = 86400
)

