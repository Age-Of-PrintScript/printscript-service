package age.of.printscript.service.shared.infrastructure.storage

import age.of.printscript.service.shared.application.CodeNotFoundException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.method
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withResourceNotFound
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess
import org.springframework.web.client.RestClient

class StorageCodeReaderTest {
    private val baseUrl = "http://localhost:8083"
    private lateinit var mockServer: MockRestServiceServer
    private lateinit var reader: StorageCodeReader

    @BeforeEach
    fun setUp() {
        val builder = RestClient.builder().baseUrl(baseUrl)
        mockServer = MockRestServiceServer.bindTo(builder).build()
        reader = StorageCodeReader(builder.build())
    }

    @Test
    fun `read sends GET request to url and returns code string`() {
        val url = "$baseUrl/v1/asset/snippets/snippet-1"
        val code = "let greeting: string = \"Hola\";"

        mockServer
            .expect(requestTo(url))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withSuccess(code, MediaType.TEXT_PLAIN))

        val result = reader.read(url)

        mockServer.verify()
        assertEquals(code, result)
    }

    @Test
    fun `read throws CodeNotFoundException when asset service returns 404`() {
        val url = "$baseUrl/v1/asset/snippets/missing-snippet"

        mockServer
            .expect(requestTo(url))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withResourceNotFound())

        assertThrows(CodeNotFoundException::class.java) {
            reader.read(url)
        }
        mockServer.verify()
    }
}
