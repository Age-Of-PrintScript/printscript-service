package age.of.printscript.service.test.infrastructure.storage

import age.of.printscript.service.test.application.TestContent
import age.of.printscript.service.test.application.TestContentReader
import org.springframework.web.client.RestClient
import org.springframework.web.client.body

class StorageTestContentReader(
    private val assetRestClient: RestClient,
) : TestContentReader {
    override fun read(url: String): TestContent =
        assetRestClient
            .get()
            .uri(url)
            .retrieve()
            .body<TestContent>() ?: error("Test asset is empty")
}
