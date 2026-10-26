package age.of.printscript.service.jobs.infrastructure.redis

import age.of.printscript.jobs.JobSerializer
import age.of.printscript.jobs.PAYLOAD_FIELD
import age.of.printscript.jobs.QueueNames
import age.of.printscript.jobs.SystemErrorJobResponse
import age.of.printscript.jobs.ValidateJobResponse
import age.of.printscript.jobs.ValidationErrorDto
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.data.redis.connection.stream.MapRecord
import org.springframework.data.redis.connection.stream.RecordId
import org.springframework.data.redis.core.StreamOperations
import org.springframework.data.redis.core.StringRedisTemplate

class JobResponseProducerTest {
    @Suppress("UNCHECKED_CAST")
    private val streamOperations = mock(StreamOperations::class.java) as StreamOperations<String, String, String>
    private val redisTemplate =
        mock(StringRedisTemplate::class.java).apply {
            `when`(opsForStream<String, String>()).thenReturn(streamOperations)
        }

    private val producer = JobResponseProducer(redisTemplate)

    @Test
    fun `serializes and sends ValidateJobResponse to responses stream`() {
        val expectedRecordId = RecordId.of("1700000000000-0")
        val captor = ArgumentCaptor.forClass(MapRecord::class.java) as ArgumentCaptor<MapRecord<String, String, String>>

        `when`(streamOperations.add(captor.capture())).thenReturn(expectedRecordId)

        val response =
            ValidateJobResponse(
                jobId = "job-123",
                isValid = false,
                errors = listOf(ValidationErrorDto("Syntax error")),
            )

        val actualRecordId = producer.send(response)

        assertEquals(expectedRecordId, actualRecordId)
        verify(streamOperations).add(captor.capture())

        val capturedRecord = captor.value
        assertEquals(QueueNames.RESPONSES_STREAM, capturedRecord.stream)

        val payload = capturedRecord.value[PAYLOAD_FIELD]
        val deserialized = JobSerializer.deserializeResponse(payload!!) as ValidateJobResponse

        assertEquals(response.jobId, deserialized.jobId)
        assertEquals(response.isValid, deserialized.isValid)
        assertEquals(response.errors.first().message, deserialized.errors.first().message)
    }

    @Test
    fun `serializes and sends SystemErrorJobResponse to responses stream`() {
        val expectedRecordId = RecordId.of("1700000000000-1")
        val captor = ArgumentCaptor.forClass(MapRecord::class.java) as ArgumentCaptor<MapRecord<String, String, String>>

        `when`(streamOperations.add(captor.capture())).thenReturn(expectedRecordId)

        val response = SystemErrorJobResponse(jobId = "job-err-999", reason = "Storage down")
        val actualRecordId = producer.send(response)

        assertEquals(expectedRecordId, actualRecordId)
        verify(streamOperations).add(captor.capture())

        val capturedRecord = captor.value
        assertEquals(QueueNames.RESPONSES_STREAM, capturedRecord.stream)

        val payload = capturedRecord.value[PAYLOAD_FIELD]
        val deserialized = JobSerializer.deserializeResponse(payload!!) as SystemErrorJobResponse

        assertEquals(response.jobId, deserialized.jobId)
        assertEquals(response.reason, deserialized.reason)
    }
}
