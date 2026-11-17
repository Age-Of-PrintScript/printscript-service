package age.of.printscript.service.format.application

import age.of.printscript.service.format.domain.CodeFormatter
import age.of.printscript.service.shared.application.CodeReader
import age.of.printscript.service.shared.application.CodeWriter

class FormatUseCase(
    private val codeReader: CodeReader,
    private val codeWriter: CodeWriter,
    private val formatter: CodeFormatter,
) {
    fun execute(
        codeUrl: String,
        version: String,
        config: String?,
    ): String {
        val source = codeReader.read(codeUrl)
        val formatted = formatter.format(source, version, config)
        codeWriter.write(codeUrl, formatted)
        return formatted
    }
}
