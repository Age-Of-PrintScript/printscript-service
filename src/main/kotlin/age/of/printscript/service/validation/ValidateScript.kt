package age.of.printscript.service.validation

class ValidateScript(
    private val codeReader: CodeReader,
    private val validator: ScriptValidator,
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
