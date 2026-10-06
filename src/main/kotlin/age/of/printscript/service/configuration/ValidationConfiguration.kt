package age.of.printscript.service.configuration

import age.of.printscript.service.adapters.printscript.EngineScriptValidator
import age.of.printscript.service.adapters.storage.UnavailableCodeReader
import age.of.printscript.service.validation.CodeReader
import age.of.printscript.service.validation.ScriptValidator
import age.of.printscript.service.validation.ValidateScript
import engine.Engine
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

// esta clase declara objetos que Spring debe gestionar
@Configuration
class ValidationConfiguration {
    @Bean
    @ConditionalOnMissingBean(CodeReader::class)
    fun codeReader(): CodeReader = UnavailableCodeReader()

    @Bean
    fun scriptValidator(): ScriptValidator = EngineScriptValidator(Engine())

    @Bean
    fun validateScript(
        codeReader: CodeReader,
        scriptValidator: ScriptValidator,
    ): ValidateScript = ValidateScript(codeReader, scriptValidator)
}
