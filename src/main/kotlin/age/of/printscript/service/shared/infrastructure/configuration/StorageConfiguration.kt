package age.of.printscript.service.shared.infrastructure.configuration

import age.of.printscript.service.shared.application.CodeReader
import age.of.printscript.service.shared.application.CodeWriter
import age.of.printscript.service.shared.infrastructure.storage.StorageCodeReader
import age.of.printscript.service.shared.infrastructure.storage.StorageCodeWriter
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient

@Configuration
class StorageConfiguration {
    @Bean
    fun assetRestClient(
        @Value("\${services.asset.url}") baseUrl: String,
        builder: RestClient.Builder,
    ): RestClient =
        builder
            .baseUrl(baseUrl)
            .build()

    @Bean
    fun storageCodeReader(assetRestClient: RestClient): CodeReader = StorageCodeReader(assetRestClient)

    @Bean
    fun storageCodeWriter(assetRestClient: RestClient): CodeWriter = StorageCodeWriter(assetRestClient)
}
