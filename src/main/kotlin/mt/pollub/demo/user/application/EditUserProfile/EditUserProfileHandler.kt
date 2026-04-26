package mt.pollub.demo.user.application.EditUserProfile

import mt.pollub.demo.shared.exception.NotFoundException
import mt.pollub.demo.user.domain.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class EditUserProfileHandler(
    private val userRepository: UserRepository
) {
    @Transactional
    fun handle(command: EditUserProfileCommand) {
        val user = userRepository.findById(command.userId).orElse(null)
            ?: throw NotFoundException("User not found: ${command.userId}")

        user.firstName = command.firstName.trim()
        user.lastName = command.lastName.trim()

        userRepository.save(user)
    }
}

