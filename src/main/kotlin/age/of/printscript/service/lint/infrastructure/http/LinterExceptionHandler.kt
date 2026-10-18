package age.of.printscript.service.lint.infrastructure.http

import age.of.printscript.service.lint.application.InvalidLintOptionsException
import age.of.printscript.service.shared.infrastructure.http.ApiErrorResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

/**
 * A controller advice centralizes exception handling for one or more HTTP controllers,
 * allowing them to return consistent responses without duplicating error-handling logic.
 */
@RestControllerAdvice(assignableTypes = [LinterController::class])
class LinterExceptionHandler {
    @ExceptionHandler(InvalidLintOptionsException::class)
    fun handleInvalidOptions(exception: InvalidLintOptionsException): ResponseEntity<ApiErrorResponse> =
        ResponseEntity.badRequest().body(ApiErrorResponse(exception.message.orEmpty()))
}
