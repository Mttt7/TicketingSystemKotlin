package mt.pollub.demo.user.web

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import mt.pollub.demo.user.application.LoginUser.LoginUserCommand
import mt.pollub.demo.user.application.RegisterUser.RegisterUserCommand

data class RegisterRequest(
    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Email must be valid")
    val email: String,

    @field:Size(max = 80, message = "First name must be at most 80 characters")
    val firstName: String? = null,

    @field:Size(max = 80, message = "Last name must be at most 80 characters")
    val lastName: String? = null,

    @field:NotBlank(message = "Password is required")
    @field:Size(min = 8, message = "Password must be at least 8 characters")
    val password: String
) {
    fun toCommand() = RegisterUserCommand(
        email = email,
        firstName = firstName?.trim().orEmpty().ifBlank { "Unknown" },
        lastName = lastName?.trim().orEmpty().ifBlank { "User" },
        password = password
    )
}

data class LoginRequest(
    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Email must be valid")
    val email: String,

    @field:NotBlank(message = "Password is required")
    val password: String
) {
    fun toCommand() = LoginUserCommand(email, password)
}
