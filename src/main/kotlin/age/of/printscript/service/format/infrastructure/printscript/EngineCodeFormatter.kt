package age.of.printscript.service.format.infrastructure.printscript

import age.of.printscript.service.format.application.InvalidFormatException
import age.of.printscript.service.format.domain.CodeFormatter
import formatter.FormatError
import formatter.FormatSuccess
import formatter.Formatter

class EngineCodeFormatter(
    private val formatter: Formatter,
) : CodeFormatter {
    override fun format(
        source: String,
        version: String,
        config: String?,
    ): String =
        when (val result = formatter.format(source, version, config)) {
            is FormatSuccess -> result.value
            is FormatError -> throw InvalidFormatException(result.value)
        }
}
