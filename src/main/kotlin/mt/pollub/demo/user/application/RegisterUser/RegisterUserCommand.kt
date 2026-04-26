package mt.pollub.demo.user.application.RegisterUser

data class RegisterUserCommand(
    val email: String,
    val firstName: String,
    val lastName: String,
    val password: String
)
