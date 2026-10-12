package age.of.printscript.service.validation

interface CodeValidator {
    fun validate(
        source: String,
        version: String,
    ): ValidationResult
}
