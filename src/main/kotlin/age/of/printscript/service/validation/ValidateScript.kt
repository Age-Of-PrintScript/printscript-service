package age.of.printscript.service.validation

import age.of.printscript.service.shared.CodeReader

class ValidateScript(
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
