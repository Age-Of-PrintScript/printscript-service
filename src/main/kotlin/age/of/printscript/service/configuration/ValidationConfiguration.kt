package age.of.printscript.service.configuration

import age.of.printscript.service.adapters.printscript.EngineCodeValidator
import age.of.printscript.service.shared.CodeReader
import age.of.printscript.service.validation.CodeValidator
import age.of.printscript.service.validation.ValidateScript
import engine.Engine
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

// esta clase declara objetos que Spring debe gestionar
@Configuration
class ValidationConfiguration {
    @Bean
    fun scriptValidator(): CodeValidator = EngineCodeValidator(Engine())

    @Bean
    fun validateScript(
        codeReader: CodeReader,
        codeValidator: CodeValidator,
    ): ValidateScript = ValidateScript(codeReader, codeValidator)
}
