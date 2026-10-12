package age.of.printscript.service.adapters.printscript
import age.of.printscript.service.lint.CodeLinter
import age.of.printscript.service.lint.Warning
import age.of.printscript.service.lint.toServiceWarning
import linter.Linter

class EngineCodeLinter(
    private val linter: Linter
) : CodeLinter {
    override fun lint(source: String, version: String, config: String?): List<Warning> =
        linter.analyse(source, version, config)
            .map { it.toServiceWarning()}
}
