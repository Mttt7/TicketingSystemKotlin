package mt.pollub.demo.user.application.RegisterUser

import mt.pollub.demo.shared.exception.ConflictException
import mt.pollub.demo.user.domain.User
import mt.pollub.demo.user.domain.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class RegisterUserHandler(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {
    @Transactional
    fun handle(command: RegisterUserCommand): UUID {
        if (userRepository.existsByEmail(command.email)) {
            throw ConflictException("Email is already in use: ${command.email}")
        }

        val user = User(
            email = command.email,
            password = requireNotNull(passwordEncoder.encode(command.password)) { "Password encoding failed" }
        )

        val savedUser = userRepository.save(user)
        return requireNotNull(savedUser.id) { "User ID was not generated" }
    }
}
