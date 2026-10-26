package age.of.printscript.service.jobs.infrastructure.configuration

import age.of.printscript.jobs.QueueNames
import age.of.printscript.service.jobs.infrastructure.redis.JobStreamListener
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.data.redis.RedisSystemException
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.stream.MapRecord
import org.springframework.data.redis.connection.stream.ReadOffset
import org.springframework.data.redis.core.StreamOperations
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.stream.StreamMessageListenerContainer
import org.springframework.data.redis.stream.Subscription

class RedisConsumerConfigurationTest {
    private val config = RedisConsumerConfiguration()

    @Test
    fun `creates jobStreamMessageListenerContainer`() {
        val connectionFactory = mock(RedisConnectionFactory::class.java)

        val container = config.jobStreamMessageListenerContainer(connectionFactory)

        assertNotNull(container)
    }

    @Suppress("UNCHECKED_CAST")
    @Test
    fun `initializes consumer group and starts subscription`() {
        val container =
            mock(StreamMessageListenerContainer::class.java)
                as StreamMessageListenerContainer<String, MapRecord<String, String, String>>
        val subscription = mock(Subscription::class.java)
        val stringRedisTemplate = mock(StringRedisTemplate::class.java)
        val streamOperations = mock(StreamOperations::class.java) as StreamOperations<String, String, String>
        val jobStreamListener = mock(JobStreamListener::class.java)

        `when`(stringRedisTemplate.opsForStream<String, String>()).thenReturn(streamOperations)
        `when`(container.receive(any(), any(), any())).thenReturn(subscription)

        val result = config.jobSubscription(container, stringRedisTemplate, jobStreamListener)

        assertNotNull(result)
        verify(streamOperations).createGroup(
            QueueNames.REQUESTS_STREAM,
            ReadOffset.latest(),
            QueueNames.PRINTSCRIPT_WORKER_GROUP,
        )
        verify(container).receive(any(), any(), any())
        verify(container).start()
    }

    @Suppress("UNCHECKED_CAST")
    @Test
    fun `subscribes successfully even if consumer group already exists`() {
        val container =
            mock(StreamMessageListenerContainer::class.java)
                as StreamMessageListenerContainer<String, MapRecord<String, String, String>>
        val subscription = mock(Subscription::class.java)
        val stringRedisTemplate = mock(StringRedisTemplate::class.java)
        val streamOperations = mock(StreamOperations::class.java) as StreamOperations<String, String, String>
        val jobStreamListener = mock(JobStreamListener::class.java)

        `when`(stringRedisTemplate.opsForStream<String, String>()).thenReturn(streamOperations)
        `when`(
            streamOperations.createGroup(
                QueueNames.REQUESTS_STREAM,
                ReadOffset.latest(),
                QueueNames.PRINTSCRIPT_WORKER_GROUP,
            ),
        ).thenThrow(RedisSystemException("BUSYGROUP Consumer Group name already exists", RuntimeException()))
        `when`(container.receive(any(), any(), any())).thenReturn(subscription)

        val result = config.jobSubscription(container, stringRedisTemplate, jobStreamListener)

        assertNotNull(result)
        verify(container).receive(any(), any(), any())
        verify(container).start()
    }
}
