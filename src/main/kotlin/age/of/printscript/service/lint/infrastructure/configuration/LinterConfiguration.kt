package age.of.printscript.service.lint.infrastructure.configuration

import age.of.printscript.service.lint.application.LinterUseCase
import age.of.printscript.service.lint.domain.CodeLinter
import age.of.printscript.service.lint.infrastructure.printscript.EngineCodeLinter
import age.of.printscript.service.shared.application.CodeReader
import linter.Linter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class LinterConfiguration {
    @Bean
    fun linter() : CodeLinter = EngineCodeLinter(Linter.create())

    @Bean
    fun linterUseCase(
        codeReader: CodeReader,
        linter: CodeLinter
    ) : LinterUseCase = LinterUseCase(codeReader, linter)
}
