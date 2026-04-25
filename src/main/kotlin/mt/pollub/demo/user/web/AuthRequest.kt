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

    @field:NotBlank(message = "Password is required")
    @field:Size(min = 8, message = "Password must be at least 8 characters")
    val password: String
) {
    fun toCommand() = RegisterUserCommand(email, password)
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

