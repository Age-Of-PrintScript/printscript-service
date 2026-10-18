package age.of.printscript.service.validation.infrastructure.http

import age.of.printscript.service.validation.application.ValidateSourceUseCase
import age.of.printscript.service.validation.application.ValidateUseCase
import age.of.printscript.service.validation.domain.ValidationError
import age.of.printscript.service.validation.domain.ValidationResult
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/validate")
class ValidationController(
    private val validate: ValidateUseCase,
    private val validateSource: ValidateSourceUseCase
) {
    @PostMapping
    fun validate(
        @Valid @RequestBody request: ValidateRequest,
    ): ResponseEntity<*> =
        when (val result = validate.execute(request.codeUrl, request.version)) {
            ValidationResult.Valid -> ResponseEntity.noContent().build<Any>()
            is ValidationResult.Invalid -> ResponseEntity.ok(result.toResponse())
        }

    @PostMapping("/new")
    fun validateNew(
        @Valid @RequestBody request: ValidateNewRequest,
    ): ResponseEntity<*> =
        when (val result = validateSource.execute(request.code, request.version)) {
            ValidationResult.Valid -> ResponseEntity.noContent().build<Any>()
            is ValidationResult.Invalid -> ResponseEntity.ok(result.toResponse())
        }
}

data class ValidateNewRequest(
    @field:NotBlank val code: String,
    @field:NotBlank
    @field:Pattern(regexp = "^(1\\.0|1\\.1)$", message = "must be 1.0 or 1.1")
    val version: String,
)

data class ValidateRequest(
    @field:NotBlank val codeUrl: String,
    @field:NotBlank
    @field:Pattern(regexp = "^(1\\.0|1\\.1)$", message = "must be 1.0 or 1.1")
    val version: String,
)

data class ValidationErrorResponse(
    val errors: List<ValidationError>,
)

private fun ValidationResult.Invalid.toResponse(): ValidationErrorResponse = ValidationErrorResponse(errors)
