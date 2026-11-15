package age.of.printscript.service.format.infrastructure.configuration

import age.of.printscript.service.format.application.FormatUseCase
import age.of.printscript.service.format.domain.CodeFormatter
import age.of.printscript.service.format.infrastructure.printscript.EngineCodeFormatter
import age.of.printscript.service.shared.application.CodeReader
import age.of.printscript.service.shared.application.CodeWriter
import formatter.Formatter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class FormatConfiguration {
    @Bean
    fun formatter(): CodeFormatter = EngineCodeFormatter(Formatter.create())

    @Bean
    fun formatUseCase(
        codeReader: CodeReader,
        codeWriter: CodeWriter,
        formatter: CodeFormatter,
    ): FormatUseCase = FormatUseCase(codeReader, codeWriter, formatter)
}
