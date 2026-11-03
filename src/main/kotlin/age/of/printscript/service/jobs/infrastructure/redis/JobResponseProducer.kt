package age.of.printscript.service.jobs.infrastructure.redis

import age.of.printscript.jobs.JobResponse
import age.of.printscript.jobs.JobSerializer
import age.of.printscript.jobs.PAYLOAD_FIELD
import age.of.printscript.jobs.QueueNames
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
        val payload = JobSerializer.serializeResponse(response)
        val record =
            MapRecord.create(
                QueueNames.RESPONSES_STREAM,
                mapOf(PAYLOAD_FIELD to payload),
            )

        val recordId = redisTemplate.opsForStream<String, String>().add(record)
        logger.info("Sent response for job {} to stream {}", response.jobId, QueueNames.RESPONSES_STREAM)
        return recordId
    }
}
