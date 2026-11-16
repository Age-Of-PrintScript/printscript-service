package age.of.printscript.service.jobs.application

import age.of.printscript.jobs.JobSerializer
import age.of.printscript.jobs.TestJobRequest
import age.of.printscript.jobs.TestJobResponse
import age.of.printscript.service.format.application.FormatUseCase
import age.of.printscript.service.lint.application.LinterUseCase
import age.of.printscript.service.shared.infrastructure.storage.StorageCodeReader
import age.of.printscript.service.test.application.TestUseCase
import age.of.printscript.service.test.infrastructure.printscript.EngineCodeExecutor
import age.of.printscript.service.test.infrastructure.storage.StorageTestContentReader
import age.of.printscript.service.validation.application.ValidateUseCase
import engine.Engine
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.springframework.http.MediaType
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess
import org.springframework.web.client.RestClient

class TestJobProcessingIntegrationTest {
    private val baseUrl = "http://localhost:8083"
    private val snippetUrl = "$baseUrl/v1/asset/snippets/snippet-1"
    private val testUrl = "$baseUrl/v1/asset/snippet-tests/test-1"

    @Test
    fun `job reads both assets and passes when output matches`() {
        val (processor, server) = processorWithAssets(listOf("Hello Ana"))

        val response = processor.process(serializedJob()) as TestJobResponse

        server.verify()
        assertTrue(response.success)
        assertEquals("test-job-1", response.jobId)
    }

    @Test
    fun `job fails when output differs from expected output`() {
        val (processor, server) = processorWithAssets(listOf("Other output"))

        val response = processor.process(serializedJob()) as TestJobResponse

        server.verify()
        assertFalse(response.success)
        assertEquals("Expected outputs [Other output] but got [Hello Ana]", response.message)
    }

    private fun processorWithAssets(expectedOutputs: List<String>): Pair<JobProcessor, MockRestServiceServer> {
        val builder = RestClient.builder().baseUrl(baseUrl)
        val server = MockRestServiceServer.bindTo(builder).build()
        val testContent = """{"inputs":["Ana"],"outputs":["${expectedOutputs.single()}"]}"""
        server.expect(requestTo(testUrl)).andRespond(withSuccess(testContent, MediaType.APPLICATION_JSON))
        server
            .expect(requestTo(snippetUrl))
            .andRespond(withSuccess("""let name: string = readInput("Name: "); println("Hello " + name);""", MediaType.TEXT_PLAIN))

        val restClient = builder.build()
        val testUseCase =
            TestUseCase(
                StorageCodeReader(restClient),
                StorageTestContentReader(restClient),
                EngineCodeExecutor(Engine()),
            )
        val processor =
            JobProcessor(
                mock(ValidateUseCase::class.java),
                mock(LinterUseCase::class.java),
                mock(FormatUseCase::class.java),
                testUseCase,
            )
        return processor to server
    }

    private fun serializedJob(): TestJobRequest {
        val job =
            TestJobRequest(
                jobId = "test-job-1",
                snippetId = "snippet-1",
                snippetUrl = snippetUrl,
                languageVersion = "1.1",
                testUrl = testUrl,
            )
        return JobSerializer.deserializeRequest(JobSerializer.serializeRequest(job)) as TestJobRequest
    }
}
