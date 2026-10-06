package age.of.printscript.service.validation

interface ScriptValidator {
    fun validate(
        source: String,
        version: String,
    ): ValidationResult
}
