package mt.pollub.demo.user.web

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.UUID

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthControllerIntegrationTest {

    @LocalServerPort
    private var port: Int = 0

    @Test
    fun `should register user and return 201`() {
        val email = "user-${UUID.randomUUID()}@example.com"
        val body = """
            {
              "email": "$email",
              "password": "strongPassword123"
            }
        """.trimIndent()

        val request = HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:$port/api/auth/register"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build()

        val response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString())

        assertEquals(201, response.statusCode())
        assertTrue(response.body().contains("userId"))
    }
}
