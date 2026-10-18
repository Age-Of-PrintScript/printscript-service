package age.of.printscript.service.shared.infrastructure.configuration

import age.of.printscript.service.shared.application.CodeReader
import age.of.printscript.service.shared.infrastructure.storage.FakeCodeReader
import age.of.printscript.service.shared.infrastructure.storage.UnavailableCodeReader
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
