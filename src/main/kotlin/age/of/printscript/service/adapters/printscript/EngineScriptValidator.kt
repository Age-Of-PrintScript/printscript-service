package age.of.printscript.service.adapters.printscript

import age.of.printscript.service.validation.ScriptValidator
import age.of.printscript.service.validation.ValidationError
import age.of.printscript.service.validation.ValidationResult
import engine.Engine
import engine.ExitCode
import engine.Logger

class EngineScriptValidator(
    private val engine: Engine,
) : ScriptValidator {
    override fun validate(
        source: String,
        version: String,
    ): ValidationResult {
        val messages = mutableListOf<String>()
        val logger =
            object : Logger {
                override fun log(string: String) {
                    messages.add(string)
                }
            }

        return when (engine.validate(source, logger, version)) {
            ExitCode.SUCCESS -> ValidationResult.Valid
            ExitCode.FAILURE ->
                ValidationResult.Invalid(
                    errors = messages
                        .filterNot { it == BUILD_FAILED_MESSAGE }
                        .map(::ValidationError),
                )
        }
    }

    private companion object {
        const val BUILD_FAILED_MESSAGE = "Build Failed"
    }
}
