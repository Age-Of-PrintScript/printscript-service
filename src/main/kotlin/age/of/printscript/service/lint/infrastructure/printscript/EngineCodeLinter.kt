package age.of.printscript.service.lint.infrastructure.printscript

import age.of.printscript.service.lint.application.InvalidLintOptionsException
import age.of.printscript.service.lint.domain.CodeLinter
import age.of.printscript.service.lint.domain.Warning
import age.of.printscript.service.lint.domain.toServiceWarning
import kotlinx.serialization.SerializationException
import linter.Linter

class EngineCodeLinter(
    private val linter: Linter,
) : CodeLinter {
    override fun lint(
        source: String,
        version: String,
        config: String?,
    ): List<Warning> =
        try {
            linter
                .analyse(source, version, config)
                .map { it.toServiceWarning() }
        } catch (exception: SerializationException) {
            throw InvalidLintOptionsException("The lint configuration is invalid.", exception)
        } catch (exception: IllegalArgumentException) {
            throw InvalidLintOptionsException(
                exception.message ?: "The lint version or configuration is invalid.",
                exception,
            )
        }
}
