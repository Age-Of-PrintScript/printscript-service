package age.of.printscript.service.lint.infrastructure.http

import age.of.printscript.service.lint.application.InvalidLintOptionsException
import age.of.printscript.service.shared.infrastructure.http.ApiErrorResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

/**
 * Translates invalid linter versions or configurations into the API's 400 error contract.
 */
@RestControllerAdvice(assignableTypes = [LinterController::class])
class LinterExceptionHandler {
    @ExceptionHandler(InvalidLintOptionsException::class)
    fun handleInvalidOptions(exception: InvalidLintOptionsException): ResponseEntity<ApiErrorResponse> =
        ResponseEntity.badRequest().body(ApiErrorResponse(exception.message.orEmpty()))
}
