package age.of.printscript.service.jobs.infrastructure.redis

import age.of.printscript.jobs.JobSerializer
import age.of.printscript.jobs.PAYLOAD_FIELD
import age.of.printscript.jobs.QueueNames
import age.of.printscript.service.jobs.application.JobProcessor
import age.of.printscript.service.shared.infrastructure.filter.RequestLoggingFilter
import com.newrelic.api.agent.Trace
import org.slf4j.LoggerFactory
import org.slf4j.MDC
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

    @Trace(dispatcher = true)
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
            RedisTraceHeaders.accept(request.traceHeaders)
            MDC.put(RequestLoggingFilter.CORRELATION_ID_KEY, request.jobId)
            MDC.put(RequestLoggingFilter.SERVICE_KEY, RequestLoggingFilter.SERVICE_NAME)
            logger.info("Processing job request: jobId={}, type={}", request.jobId, request::class.simpleName)

            val response = jobProcessor.process(request)
            jobResponseProducer.send(response)
            logger.info("Completed job request: jobId={}, responseType={}", request.jobId, response::class.simpleName)
        } catch (exception: Exception) {
            logger.error("Failed to process message from stream: recordId={}", record.id, exception)
        } finally {
            MDC.clear()
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
