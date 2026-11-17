package age.of.printscript.service.shared.infrastructure.storage

import age.of.printscript.service.shared.application.CodeWriter
import org.springframework.web.client.RestClient

class StorageCodeWriter(
    private val assetRestClient: RestClient,
) : CodeWriter {
    override fun write(
        codeUrl: String,
        content: String,
    ) {
        assetRestClient
            .put()
            .uri(codeUrl)
            .body(content)
            .retrieve()
            .toBodilessEntity()
    }
}
