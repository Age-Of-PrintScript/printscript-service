package age.of.printscript.service.lint.application

import age.of.printscript.service.lint.domain.CodeLinter
import age.of.printscript.service.lint.domain.Warning
import age.of.printscript.service.shared.application.CodeReader

class LinterUseCase(
    val codeReader: CodeReader,
    val linter: CodeLinter,
) {
    fun execute(
        codeUrl: String,
        version: String,
        config: String?,
    ): List<Warning> =
        linter.lint(
            codeReader.read(codeUrl),
            version,
            config,
        )
}
