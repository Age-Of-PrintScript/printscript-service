package age.of.printscript.service.validation.domain

interface CodeValidator {
    fun validate(
        source: String,
        version: String,
    ): ValidationResult
}
