package age.of.printscript.service.validation.infrastructure.configuration

import age.of.printscript.service.shared.application.CodeReader
import age.of.printscript.service.validation.application.ValidateSourceUseCase
import age.of.printscript.service.validation.application.ValidateUseCase
import age.of.printscript.service.validation.domain.CodeValidator
import age.of.printscript.service.validation.infrastructure.printscript.EngineCodeValidator
import engine.Engine
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

// esta clase declara objetos que Spring debe gestionar
@Configuration
class ValidationConfiguration {
    @Bean
    fun validator(): CodeValidator = EngineCodeValidator(Engine())

    @Bean
    fun validateUseCase(
        codeReader: CodeReader,
        codeValidator: CodeValidator,
    ): ValidateUseCase = ValidateUseCase(codeReader, codeValidator)

    @Bean
    fun validateSourceUseCase(validator: CodeValidator): ValidateSourceUseCase = ValidateSourceUseCase(validator)
}
