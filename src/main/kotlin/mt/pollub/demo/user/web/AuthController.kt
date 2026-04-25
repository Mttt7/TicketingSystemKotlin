package mt.pollub.demo.user.web

import jakarta.validation.Valid
import mt.pollub.demo.user.application.LoginUser.LoginUserHandler
import mt.pollub.demo.user.application.RegisterUser.RegisterUserHandler
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val registerHandler: RegisterUserHandler,
    private val loginHandler: LoginUserHandler
) {
    companion object {
        private val logger = LoggerFactory.getLogger(AuthController::class.java)
    }

    @PostMapping("/register")
    fun register(@RequestBody @Valid request: RegisterRequest): ResponseEntity<RegisterResponse> {
        logger.info("Register request received for email={}", request.email)
        val userId = registerHandler.handle(request.toCommand())
        logger.info("User registered successfully, userId={}", userId)
        return ResponseEntity.status(HttpStatus.CREATED).body(RegisterResponse(userId))
    }

    @PostMapping("/login")
    fun login(@RequestBody @Valid request: LoginRequest): ResponseEntity<AuthResponse> {
        logger.info("Login request received for email={}", request.email)
        val token = loginHandler.handle(request.toCommand())
        return ResponseEntity.ok(AuthResponse(token = token))
    }
}
