package age.of.printscript.service.validation.application

import age.of.printscript.service.shared.application.CodeReader
import age.of.printscript.service.validation.domain.CodeValidator
import age.of.printscript.service.validation.domain.ValidationResult

class ValidateUseCase(
    private val codeReader: CodeReader,
    private val validator: CodeValidator,
) {
    fun execute(
        codeUrl: String,
        version: String,
    ): ValidationResult =
        validator.validate(
            source = codeReader.read(codeUrl),
            version = version,
        )
}
