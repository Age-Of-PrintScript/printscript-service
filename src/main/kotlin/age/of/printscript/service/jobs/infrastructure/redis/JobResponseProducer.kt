package age.of.printscript.service.jobs.infrastructure.redis

import age.of.printscript.jobs.ExecuteJobResponse
import age.of.printscript.jobs.FormatJobResponse
import age.of.printscript.jobs.JobResponse
import age.of.printscript.jobs.JobSerializer
import age.of.printscript.jobs.LintJobResponse
import age.of.printscript.jobs.PAYLOAD_FIELD
import age.of.printscript.jobs.QueueNames
import age.of.printscript.jobs.SystemErrorJobResponse
import age.of.printscript.jobs.TestJobResponse
import age.of.printscript.jobs.ValidateJobResponse
import org.slf4j.LoggerFactory
import org.springframework.data.redis.connection.stream.MapRecord
import org.springframework.data.redis.connection.stream.RecordId
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component

@Component
class JobResponseProducer(
    private val redisTemplate: StringRedisTemplate,
) {
    private val logger = LoggerFactory.getLogger(JobResponseProducer::class.java)

    fun send(response: JobResponse): RecordId? {
        val tracedResponse = withTraceHeaders(response)
        val payload = JobSerializer.serializeResponse(tracedResponse)
        val record =
            MapRecord.create(
                QueueNames.RESPONSES_STREAM,
                mapOf(PAYLOAD_FIELD to payload),
            )

        val recordId = redisTemplate.opsForStream<String, String>().add(record)
        logger.info("Sent response for job {} to stream {}", response.jobId, QueueNames.RESPONSES_STREAM)
        return recordId
    }

    private fun withTraceHeaders(response: JobResponse): JobResponse {
        val headers = RedisTraceHeaders.capture()
        if (headers.isEmpty()) return response
        return when (response) {
            is ValidateJobResponse -> response.copy(traceHeaders = headers)
            is ExecuteJobResponse -> response.copy(traceHeaders = headers)
            is TestJobResponse -> response.copy(traceHeaders = headers)
            is LintJobResponse -> response.copy(traceHeaders = headers)
            is FormatJobResponse -> response.copy(traceHeaders = headers)
            is SystemErrorJobResponse -> response.copy(traceHeaders = headers)
        }
    }
}
