package age.of.printscript.service.jobs.infrastructure.configuration

import age.of.printscript.jobs.QueueNames
import age.of.printscript.service.jobs.infrastructure.redis.JobStreamListener
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.stream.Consumer
import org.springframework.data.redis.connection.stream.MapRecord
import org.springframework.data.redis.connection.stream.ReadOffset
import org.springframework.data.redis.connection.stream.StreamOffset
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.stream.StreamMessageListenerContainer
import org.springframework.data.redis.stream.StreamMessageListenerContainer.StreamMessageListenerContainerOptions
import org.springframework.data.redis.stream.Subscription
import java.net.InetAddress
import java.net.UnknownHostException
import java.time.Duration
import java.util.UUID
import java.util.concurrent.Executors

// Configura el contenedor de escucha de Redis Streams y la suscripcion en background para jobs.
@Configuration
class RedisConsumerConfiguration {
    private val logger = LoggerFactory.getLogger(RedisConsumerConfiguration::class.java)

    // Configura el contenedor con lectura bloqueante (2s) y un pool de 4 hilos para evitar saturacion.
    @Bean
    fun jobStreamMessageListenerContainer(connectionFactory: RedisConnectionFactory): StreamMessageListenerContainer<String, MapRecord<String, String, String>> {
        val options =
            StreamMessageListenerContainerOptions
                .builder()
                .pollTimeout(Duration.ofSeconds(POLL_TIMEOUT_SECONDS))
                .executor(Executors.newFixedThreadPool(THREAD_POOL_SIZE))
                .build()

        return StreamMessageListenerContainer.create(connectionFactory, options)
    }

    // Vincula el listener al stream jobs:requests, inicializa el grupo y arranca el contenedor.
    @Bean
    fun jobSubscription(
        container: StreamMessageListenerContainer<String, MapRecord<String, String, String>>,
        stringRedisTemplate: StringRedisTemplate,
        jobStreamListener: JobStreamListener,
    ): Subscription {
        // Asegura que el stream y el consumer group existan en Redis antes de intentar consumir.
        initConsumerGroup(stringRedisTemplate)

        val consumerName = resolveConsumerName()

        // Registra el listener para recibir los mensajes pendientes y nuevos dirigidos al grupo.
        val subscription =
            container.receive(
                Consumer.from(QueueNames.PRINTSCRIPT_WORKER_GROUP, consumerName),
                StreamOffset.create(QueueNames.REQUESTS_STREAM, ReadOffset.lastConsumed()),
                jobStreamListener,
            )

        container.start()
        logger.info(
            "Subscribed consumer '{}' in group '{}' to stream '{}'",
            consumerName,
            QueueNames.PRINTSCRIPT_WORKER_GROUP,
            QueueNames.REQUESTS_STREAM,
        )
        return subscription
    }

    @Suppress("TooGenericExceptionCaught")
    private fun initConsumerGroup(redisTemplate: StringRedisTemplate) {
        try {
            redisTemplate.opsForStream<String, String>().createGroup(
                QueueNames.REQUESTS_STREAM,
                ReadOffset.latest(),
                QueueNames.PRINTSCRIPT_WORKER_GROUP,
            )
            logger.info(
                "Created consumer group '{}' on stream '{}'",
                QueueNames.PRINTSCRIPT_WORKER_GROUP,
                QueueNames.REQUESTS_STREAM,
            )
        } catch (exception: Exception) {
            logger.info(
                "Consumer group '{}' on stream '{}' already initialized or ready",
                QueueNames.PRINTSCRIPT_WORKER_GROUP,
                QueueNames.REQUESTS_STREAM,
                exception,
            )
        }
    }

    // Combina el hostname con un sufijo aleatorio para identificar a este worker de forma unica en Docker.
    private fun resolveConsumerName(): String =
        try {
            val suffix = UUID.randomUUID().toString().take(CONSUMER_NAME_SUFFIX_LENGTH)
            "${InetAddress.getLocalHost().hostName}-$suffix"
        } catch (exception: UnknownHostException) {
            logger.debug("Could not resolve local host name, using fallback identifier", exception)
            val fallbackSuffix = UUID.randomUUID().toString().take(CONSUMER_NAME_SUFFIX_LENGTH)
            "worker-$fallbackSuffix"
        }

    private companion object {
        const val POLL_TIMEOUT_SECONDS = 2L
        const val THREAD_POOL_SIZE = 4
        const val CONSUMER_NAME_SUFFIX_LENGTH = 6
    }
}
