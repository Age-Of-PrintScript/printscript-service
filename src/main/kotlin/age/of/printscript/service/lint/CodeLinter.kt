package age.of.printscript.service.lint

interface CodeLinter {
    fun lint(
        source: String,
        version: String,
        config: String?
    ): List<Warning>
}
