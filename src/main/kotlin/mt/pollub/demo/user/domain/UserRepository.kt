package mt.pollub.demo.user.domain

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import java.util.UUID

interface UserRepository : JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {
    fun findByEmail(email: String): User?
    fun existsByEmail(email: String): Boolean
}

