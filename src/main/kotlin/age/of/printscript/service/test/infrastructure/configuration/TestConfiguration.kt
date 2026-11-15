package age.of.printscript.service.test.infrastructure.configuration

import age.of.printscript.service.shared.application.CodeReader
import age.of.printscript.service.test.application.TestUseCase
import age.of.printscript.service.test.domain.CodeExecutor
import age.of.printscript.service.test.infrastructure.printscript.EngineCodeExecutor
import engine.Engine
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class TestConfiguration {
    @Bean
    fun codeExecutor(): CodeExecutor = EngineCodeExecutor(Engine())

    @Bean
    fun testUseCase(
        codeReader: CodeReader,
        codeExecutor: CodeExecutor,
    ): TestUseCase = TestUseCase(codeReader, codeExecutor)
}
