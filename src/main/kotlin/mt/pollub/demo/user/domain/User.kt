package mt.pollub.demo.user.domain

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "app_users")
class User(

    @Column(unique = true, nullable = false)
    val email: String,

    @Column(length = 80)
    var firstName: String? = null,

    @Column(length = 80)
    var lastName: String? = null,

    @Column(nullable = false)
    var password: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val role: UserRole = UserRole.USER,

    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()

) {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null
}
