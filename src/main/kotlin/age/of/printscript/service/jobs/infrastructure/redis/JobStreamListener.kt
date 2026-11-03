package age.of.printscript.service.jobs.infrastructure.redis

import age.of.printscript.jobs.JobSerializer
import age.of.printscript.jobs.PAYLOAD_FIELD
import age.of.printscript.jobs.QueueNames
import age.of.printscript.service.jobs.application.JobProcessor
import org.slf4j.LoggerFactory
import org.springframework.data.redis.connection.stream.MapRecord
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.stream.StreamListener
import org.springframework.stereotype.Component

@Component
class JobStreamListener(
    private val redisTemplate: StringRedisTemplate,
    private val jobProcessor: JobProcessor,
    private val jobResponseProducer: JobResponseProducer,
) : StreamListener<String, MapRecord<String, String, String>> {
    private val logger = LoggerFactory.getLogger(JobStreamListener::class.java)

    @Suppress("TooGenericExceptionCaught")
    override fun onMessage(record: MapRecord<String, String, String>) {
        val payload =
            record.value[PAYLOAD_FIELD] ?: run {
                logger.warn("Received message without '{}' field: recordId={}", PAYLOAD_FIELD, record.id)
                acknowledge(record)
                return
            }

        try {
            val request = JobSerializer.deserializeRequest(payload)
            logger.info("Processing job request: jobId={}, type={}", request.jobId, request::class.simpleName)

            val response = jobProcessor.process(request)
            jobResponseProducer.send(response)
        } catch (exception: Exception) {
            logger.error("Failed to process message from stream: recordId={}", record.id, exception)
        } finally {
            acknowledge(record)
        }
    }

    private fun acknowledge(record: MapRecord<String, String, String>) {
        redisTemplate.opsForStream<String, String>().acknowledge(
            QueueNames.REQUESTS_STREAM,
            QueueNames.PRINTSCRIPT_WORKER_GROUP,
            record.id,
        )
    }
}
