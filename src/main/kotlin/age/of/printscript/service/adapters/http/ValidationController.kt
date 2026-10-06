package age.of.printscript.service.adapters.http

import age.of.printscript.service.validation.ValidateScript
import age.of.printscript.service.validation.ValidationError
import age.of.printscript.service.validation.ValidationResult
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/validations")
class ValidationController(
    private val validateScript: ValidateScript,
) {
    @PostMapping
    fun validate(
        @Valid @RequestBody request: ValidateRequest,
    ): ResponseEntity<*> =
        when (val result = validateScript.execute(request.codeUrl, request.version)) {
            ValidationResult.Valid -> ResponseEntity.noContent().build<Any>()
            is ValidationResult.Invalid -> ResponseEntity.unprocessableEntity().body(result.toResponse())
        }
}

data class ValidateRequest(
    @field:NotBlank val codeUrl: String,
    @field:NotBlank val version: String,
)

data class ValidationErrorResponse(
    val errors: List<ValidationError>,
)

private fun ValidationResult.Invalid.toResponse(): ValidationErrorResponse = ValidationErrorResponse(errors)
