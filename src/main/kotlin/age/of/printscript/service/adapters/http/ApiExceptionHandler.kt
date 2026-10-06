package age.of.printscript.service.adapters.http

import age.of.printscript.service.validation.CodeNotFoundException
import age.of.printscript.service.validation.CodeReaderUnavailableException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ApiExceptionHandler {
    @ExceptionHandler(CodeNotFoundException::class)
    fun handleCodeNotFound(exception: CodeNotFoundException): ResponseEntity<ApiErrorResponse> =
        error(HttpStatus.NOT_FOUND, exception.message.orEmpty())

    @ExceptionHandler(CodeReaderUnavailableException::class)
    fun handleUnavailableReader(exception: CodeReaderUnavailableException): ResponseEntity<ApiErrorResponse> =
        error(HttpStatus.SERVICE_UNAVAILABLE, exception.message.orEmpty())

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleInvalidRequest(exception: MethodArgumentNotValidException): ResponseEntity<ApiErrorResponse> {
        val message = exception.bindingResult.fieldErrors.joinToString(", ") { "${it.field} ${it.defaultMessage}" }
        return error(HttpStatus.BAD_REQUEST, message)
    }

    private fun error(
        status: HttpStatus,
        message: String,
    ): ResponseEntity<ApiErrorResponse> = ResponseEntity.status(status).body(ApiErrorResponse(message))
}

data class ApiErrorResponse(
    val message: String,
)
