package age.of.printscript.service.configuration

import age.of.printscript.service.adapters.printscript.EngineCodeLinter
import age.of.printscript.service.lint.CodeLinter
import age.of.printscript.service.lint.LinterUseCase
import age.of.printscript.service.shared.CodeReader
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
