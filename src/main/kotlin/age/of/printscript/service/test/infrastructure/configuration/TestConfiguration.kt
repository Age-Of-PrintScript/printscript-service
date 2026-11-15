package age.of.printscript.service.test.infrastructure.configuration

import age.of.printscript.service.shared.application.CodeReader
import age.of.printscript.service.test.application.TestContentReader
import age.of.printscript.service.test.application.TestUseCase
import age.of.printscript.service.test.domain.CodeExecutor
import age.of.printscript.service.test.infrastructure.printscript.EngineCodeExecutor
import age.of.printscript.service.test.infrastructure.storage.StorageTestContentReader
import engine.Engine
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient

@Configuration
class TestConfiguration {
    @Bean
    fun testContentReader(assetRestClient: RestClient): TestContentReader = StorageTestContentReader(assetRestClient)

    @Bean
    fun codeExecutor(): CodeExecutor = EngineCodeExecutor(Engine())

    @Bean
    fun testUseCase(
        codeReader: CodeReader,
        testContentReader: TestContentReader,
        codeExecutor: CodeExecutor,
    ): TestUseCase = TestUseCase(codeReader, testContentReader, codeExecutor)
}
