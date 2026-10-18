package age.of.printscript.service.validation.application

import age.of.printscript.service.validation.domain.CodeValidator
import age.of.printscript.service.validation.domain.ValidationResult

class ValidateSourceUseCase(
    private val validator: CodeValidator,
) {
    fun execute(
        code: String,
        version: String,
    ): ValidationResult =
        validator.validate(
            source = code,
            version = version,
        )
}
