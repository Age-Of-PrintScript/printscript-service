package age.of.printscript.service.configuration

import age.of.printscript.service.adapters.storage.FakeCodeReader
import age.of.printscript.service.adapters.storage.UnavailableCodeReader
import age.of.printscript.service.validation.CodeReader
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class StorageConfiguration {
    @Bean
    @ConditionalOnProperty(name = ["printscript.storage"], havingValue = "fake")
    fun fakeCodeReader(): CodeReader = FakeCodeReader()

    @Bean
    @ConditionalOnProperty(
        name = ["printscript.storage"],
        havingValue = "unavailable",
        matchIfMissing = true,
    )
    fun unavailableCodeReader(): CodeReader = UnavailableCodeReader()
}
