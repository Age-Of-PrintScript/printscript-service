package age.of.printscript.service.lint.infrastructure.printscript

import age.of.printscript.service.lint.domain.CodeLinter
import age.of.printscript.service.lint.domain.Warning
import age.of.printscript.service.lint.domain.toServiceWarning
import linter.Linter

class EngineCodeLinter(
    private val linter: Linter
) : CodeLinter {
    override fun lint(source: String, version: String, config: String?): List<Warning> =
        linter.analyse(source, version, config)
            .map { it.toServiceWarning()}
}
