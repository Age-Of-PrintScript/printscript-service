package age.of.printscript.service.test.infrastructure.storage

import age.of.printscript.service.test.application.TestContent
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.header
import org.springframework.test.web.client.match.MockRestRequestMatchers.method
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess
import org.springframework.web.client.RestClient

class StorageTestContentReaderTest {
    @Test
    fun `reads inputs and outputs from test asset URL`() {
        val builder = RestClient.builder().baseUrl("http://localhost:8083")
        val server = MockRestServiceServer.bindTo(builder).build()
        val url = "http://localhost:8083/v1/asset/snippet-tests/test-1"
        server
            .expect(requestTo(url))
            .andExpect(method(HttpMethod.GET))
            .andExpect(header("Accept", MediaType.APPLICATION_JSON_VALUE))
            .andRespond(withSuccess("""{"inputs":["A","B"],"outputs":["C","D"]}""", MediaType.APPLICATION_JSON))

        val content = StorageTestContentReader(builder.build()).read(url)

        server.verify()
        assertEquals(TestContent(listOf("A", "B"), listOf("C", "D")), content)
    }
}
