package age.of.printscript.service.shared.infrastructure.storage

import age.of.printscript.service.shared.application.CodeNotFoundException
import age.of.printscript.service.shared.application.CodeReader
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestClient
import org.springframework.web.client.body

class StorageCodeReader(
    private val assetRestClient: RestClient,
) : CodeReader {
    override fun read(codeUrl: String): String =
        try {
            assetRestClient
                .get()
                .uri(codeUrl)
                .retrieve()
                .body<String>() ?: ""
        } catch (exception: HttpClientErrorException.NotFound) {
            throw CodeNotFoundException(codeUrl, exception)
        }
}
