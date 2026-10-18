package age.of.printscript.service.validation.domain

sealed interface ValidationResult {
    data object Valid : ValidationResult

    data class Invalid(
        val errors: List<ValidationError>,
    ) : ValidationResult
}

data class ValidationError(
    val message: String,
)
