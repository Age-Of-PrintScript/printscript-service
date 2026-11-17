package age.of.printscript.service.shared.infrastructure.storage

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpMethod
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.content
import org.springframework.test.web.client.match.MockRestRequestMatchers.method
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess
import org.springframework.web.client.RestClient

class StorageCodeWriterTest {
    private val baseUrl = "http://localhost:8083"
    private lateinit var mockServer: MockRestServiceServer
    private lateinit var writer: StorageCodeWriter

    @BeforeEach
    fun setUp() {
        val builder = RestClient.builder().baseUrl(baseUrl)
        mockServer = MockRestServiceServer.bindTo(builder).build()
        writer = StorageCodeWriter(builder.build())
    }

    @Test
    fun `write sends PUT request to url with formatted code`() {
        val url = "$baseUrl/v1/asset/snippets/snippet-1"
        val formattedCode = "let greeting: string = \"Hola\";"

        mockServer
            .expect(requestTo(url))
            .andExpect(method(HttpMethod.PUT))
            .andExpect(content().string(formattedCode))
            .andRespond(withSuccess())

        writer.write(url, formattedCode)

        mockServer.verify()
    }
}
