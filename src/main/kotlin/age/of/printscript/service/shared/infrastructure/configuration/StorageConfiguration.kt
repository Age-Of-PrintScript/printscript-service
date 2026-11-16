package age.of.printscript.service.shared.infrastructure.configuration

import age.of.printscript.service.shared.application.CodeReader
import age.of.printscript.service.shared.application.CodeWriter
import age.of.printscript.service.shared.infrastructure.filter.RequestLoggingFilter
import age.of.printscript.service.shared.infrastructure.storage.StorageCodeReader
import age.of.printscript.service.shared.infrastructure.storage.StorageCodeWriter
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.web.client.RestClient
import java.io.IOException

@Configuration
class StorageConfiguration {
    private val log = LoggerFactory.getLogger(StorageConfiguration::class.java)

    @Bean
    fun assetRestClient(
        @Value("\${services.asset.url}") baseUrl: String,
        builder: RestClient.Builder,
    ): RestClient =
        builder
            .baseUrl(baseUrl)
            .requestInterceptor(loggingAndCorrelationInterceptor())
            .build()

    @Bean
    fun storageCodeReader(assetRestClient: RestClient): CodeReader = StorageCodeReader(assetRestClient)

    @Bean
    fun storageCodeWriter(assetRestClient: RestClient): CodeWriter = StorageCodeWriter(assetRestClient)

    /**
     * Interceptor que propaga el X-Correlation-Id y loguea las llamadas salientes hacia asset-service.
     */
    private fun loggingAndCorrelationInterceptor() =
        ClientHttpRequestInterceptor { request, body, execution ->
            val correlationId = MDC.get(RequestLoggingFilter.CORRELATION_ID_KEY)
            if (!correlationId.isNullOrBlank()) {
                request.headers.set(RequestLoggingFilter.CORRELATION_ID_HEADER, correlationId)
            }

            val start = System.currentTimeMillis()
            try {
                val response = execution.execute(request, body)
                val duration = System.currentTimeMillis() - start
                log.info(
                    "External HTTP {} {} - {} ({}ms)",
                    request.method,
                    request.uri,
                    response.statusCode.value(),
                    duration,
                )
                response
            } catch (e: IOException) {
                val duration = System.currentTimeMillis() - start
                log.error(
                    "External HTTP {} {} failed after {}ms: {}",
                    request.method,
                    request.uri,
                    duration,
                    e.message,
                )
                throw e
            }
        }
}
