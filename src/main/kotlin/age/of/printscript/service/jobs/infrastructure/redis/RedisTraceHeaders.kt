package age.of.printscript.service.jobs.infrastructure.redis

import com.newrelic.api.agent.HeaderType
import com.newrelic.api.agent.Headers
import com.newrelic.api.agent.NewRelic
import com.newrelic.api.agent.TransportType

internal object RedisTraceHeaders {
    fun capture(): Map<String, String> {
        val captured = mutableMapOf<String, String>()
        NewRelic.getAgent().transaction.insertDistributedTraceHeaders(MapHeaders(captured))
        return captured
    }

    fun accept(traceHeaders: Map<String, String>) {
        if (traceHeaders.isEmpty()) return
        NewRelic.getAgent().transaction.acceptDistributedTraceHeaders(
            TransportType.Other,
            MapHeaders(traceHeaders.toMutableMap()),
        )
    }

    private class MapHeaders(
        private val map: MutableMap<String, String>,
    ) : Headers {
        override fun getHeaderType(): HeaderType = HeaderType.MESSAGE

        override fun getHeader(name: String): String? = map[name]

        override fun getHeaders(name: String): Collection<String> = listOfNotNull(map[name])

        override fun setHeader(
            name: String,
            value: String,
        ) {
            map[name] = value
        }

        override fun addHeader(
            name: String,
            value: String,
        ) {
            map[name] = value
        }

        override fun getHeaderNames(): Collection<String> = map.keys

        override fun containsHeader(name: String): Boolean = map.containsKey(name)
    }
}
