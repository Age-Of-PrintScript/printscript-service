package age.of.printscript.service.jobs.infrastructure.redis

import age.of.printscript.jobs.JobSerializer
import age.of.printscript.jobs.PAYLOAD_FIELD
import age.of.printscript.jobs.QueueNames
import age.of.printscript.jobs.ValidateJobRequest
import age.of.printscript.jobs.ValidateJobResponse
import age.of.printscript.service.jobs.application.JobProcessor
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.Mockito.`when`
import org.springframework.data.redis.connection.stream.MapRecord
import org.springframework.data.redis.connection.stream.RecordId
import org.springframework.data.redis.core.StreamOperations
import org.springframework.data.redis.core.StringRedisTemplate

class JobStreamListenerTest {
    @Suppress("UNCHECKED_CAST")
    private val streamOperations = mock(StreamOperations::class.java) as StreamOperations<String, String, String>
    private val redisTemplate =
        mock(StringRedisTemplate::class.java).apply {
            `when`(opsForStream<String, String>()).thenReturn(streamOperations)
        }
    private val jobProcessor = mock(JobProcessor::class.java)
    private val jobResponseProducer = mock(JobResponseProducer::class.java)

    private val listener = JobStreamListener(redisTemplate, jobProcessor, jobResponseProducer)

    @Test
    fun `processes message successfully and acknowledges it`() {
        val recordId = RecordId.of("1700000000000-0")
        val request =
            ValidateJobRequest(
                jobId = "job-1",
                snippetId = "snip-1",
                snippetUrl = "snippet/url",
                languageVersion = "1.1",
            )
        val response = ValidateJobResponse(jobId = "job-1", isValid = true)

        `when`(jobProcessor.process(request)).thenReturn(response)

        val json = JobSerializer.serializeRequest(request)
        val record =
            MapRecord
                .create(
                    QueueNames.REQUESTS_STREAM,
                    mapOf(PAYLOAD_FIELD to json),
                ).withId(recordId)

        listener.onMessage(record)

        verify(jobProcessor).process(request)
        verify(jobResponseProducer).send(response)
        verify(streamOperations).acknowledge(
            QueueNames.REQUESTS_STREAM,
            QueueNames.PRINTSCRIPT_WORKER_GROUP,
            recordId,
        )
    }

    @Test
    fun `acknowledges and returns early when payload field is missing`() {
        val recordId = RecordId.of("1700000000000-1")
        val record =
            MapRecord
                .create(
                    QueueNames.REQUESTS_STREAM,
                    mapOf("other-field" to "some-value"),
                ).withId(recordId)

        listener.onMessage(record)

        verifyNoInteractions(jobProcessor, jobResponseProducer)
        verify(streamOperations).acknowledge(
            QueueNames.REQUESTS_STREAM,
            QueueNames.PRINTSCRIPT_WORKER_GROUP,
            recordId,
        )
    }

    @Test
    fun `acknowledges record even if processing throws exception`() {
        val recordId = RecordId.of("1700000000000-2")
        val record =
            MapRecord
                .create(
                    QueueNames.REQUESTS_STREAM,
                    mapOf(PAYLOAD_FIELD to "{ invalid json }"),
                ).withId(recordId)

        listener.onMessage(record)

        verify(streamOperations).acknowledge(
            QueueNames.REQUESTS_STREAM,
            QueueNames.PRINTSCRIPT_WORKER_GROUP,
            recordId,
        )
    }
}
