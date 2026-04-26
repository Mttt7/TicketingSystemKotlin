package mt.pollub.demo.shared.exception

import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.orm.ObjectOptimisticLockingFailureException
import java.time.LocalDateTime
import java.util.UUID

data class ErrorResponse(
    val status: Int,
    val error: String,
    val message: String,
    val path: String,
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val errorId: String? = null
)

@RestControllerAdvice
class GlobalExceptionHandler {

    companion object {
        private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)
    }

    @ExceptionHandler(NotFoundException::class)
    fun handleNotFound(e: NotFoundException, request: HttpServletRequest): ResponseEntity<ErrorResponse> {
        logger.warn("Not found: path={}, message={}", request.requestURI, e.message)
        return buildError(HttpStatus.NOT_FOUND, e.message ?: "Resource not found", request)
    }

    @ExceptionHandler(ConflictException::class)
    fun handleConflict(e: ConflictException, request: HttpServletRequest): ResponseEntity<ErrorResponse> {
        logger.warn("Conflict: path={}, message={}", request.requestURI, e.message)
        return buildError(HttpStatus.CONFLICT, e.message ?: "Conflict", request)
    }

    @ExceptionHandler(BadCredentialsException::class, UsernameNotFoundException::class)
    fun handleBadCredentials(e: RuntimeException, request: HttpServletRequest): ResponseEntity<ErrorResponse> {
        // Celowo bez szczegółów w odpowiedzi, żeby nie ujawniać istnienia kont.
        logger.warn("Unauthorized login attempt: path={}, reason={}", request.requestURI, e.message)
        return buildError(HttpStatus.UNAUTHORIZED, "Invalid email or password", request)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(e: MethodArgumentNotValidException, request: HttpServletRequest): ResponseEntity<ErrorResponse> {
        val errors = e.bindingResult.allErrors.joinToString(", ") { error ->
            if (error is FieldError) "${error.field}: ${error.defaultMessage}"
            else error.defaultMessage ?: "Validation error"
        }
        logger.warn("Validation failed: path={}, errors={}", request.requestURI, errors)
        return buildError(HttpStatus.BAD_REQUEST, errors, request)
    }

    @ExceptionHandler(DataIntegrityViolationException::class)
    fun handleDataIntegrity(e: DataIntegrityViolationException, request: HttpServletRequest): ResponseEntity<ErrorResponse> {
        logger.warn("Data integrity violation: path={}, message={}", request.requestURI, e.mostSpecificCause?.message)
        return buildError(HttpStatus.CONFLICT, "Data integrity violation", request)
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(e: IllegalArgumentException, request: HttpServletRequest): ResponseEntity<ErrorResponse> {
        logger.warn("Illegal argument: path={}, message={}", request.requestURI, e.message)
        return buildError(HttpStatus.BAD_REQUEST, e.message ?: "Invalid request", request)
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException::class)
    fun handleOptimisticLocking(
        e: ObjectOptimisticLockingFailureException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        logger.warn("Optimistic locking failure: path={}, message={}", request.requestURI, e.message)
        return buildError(HttpStatus.CONFLICT, "Concurrent update conflict. Please retry.", request)
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleNotReadable(
        e: HttpMessageNotReadableException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        logger.warn("Malformed JSON: path={}, message={}", request.requestURI, e.mostSpecificCause?.message)
        val message = e.mostSpecificCause?.message?.take(200) ?: "Malformed or invalid JSON request"
        return buildError(HttpStatus.BAD_REQUEST, message, request)
    }

    @ExceptionHandler(Exception::class)
    fun handleGeneric(e: Exception, request: HttpServletRequest): ResponseEntity<ErrorResponse> {
        val errorId = UUID.randomUUID().toString()
        logger.error(
            "Unhandled exception: errorId={}, method={}, path={}",
            errorId,
            request.method,
            request.requestURI,
            e
        )
        return buildError(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "An unexpected error occurred. Contact support with errorId=$errorId",
            request,
            errorId
        )
    }

    private fun buildError(
        status: HttpStatus,
        message: String,
        request: HttpServletRequest,
        errorId: String? = null
    ): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(status).body(
            ErrorResponse(
                status = status.value(),
                error = status.reasonPhrase,
                message = message,
                path = request.requestURI,
                errorId = errorId
            )
        )
    }
}
