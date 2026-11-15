package age.of.printscript.service.format.domain

interface CodeFormatter {
    fun format(
        source: String,
        version: String,
        config: String?,
    ): String
}
