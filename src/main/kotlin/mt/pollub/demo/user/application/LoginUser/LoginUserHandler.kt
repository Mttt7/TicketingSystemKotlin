package mt.pollub.demo.user.application.LoginUser

import mt.pollub.demo.shared.security.JwtService
import mt.pollub.demo.user.domain.UserRepository
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class LoginUserHandler(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService
) {
    fun handle(command: LoginUserCommand): String {
        val user = userRepository.findByEmail(command.email)
            ?: throw BadCredentialsException("Invalid email or password")

        if (!passwordEncoder.matches(command.password, user.password)) {
            throw BadCredentialsException("Invalid email or password")
        }

        return jwtService.generateToken(user)
    }
}
