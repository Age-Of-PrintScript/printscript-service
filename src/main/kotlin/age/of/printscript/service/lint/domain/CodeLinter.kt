package age.of.printscript.service.lint.domain

interface CodeLinter {
    fun lint(
        source: String,
        version: String,
        config: String?
    ): List<Warning>
}
