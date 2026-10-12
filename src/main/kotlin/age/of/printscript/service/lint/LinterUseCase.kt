package age.of.printscript.service.lint

import age.of.printscript.service.shared.CodeReader

class LinterUseCase (
    val codeReader: CodeReader,
    val linter: CodeLinter
) {
    fun execute(
        codeUrl: String,
        version: String,
        config: String?
    ): List<Warning> =
        linter.lint(
            codeReader.read(codeUrl),
            version,
            config
        )
}
