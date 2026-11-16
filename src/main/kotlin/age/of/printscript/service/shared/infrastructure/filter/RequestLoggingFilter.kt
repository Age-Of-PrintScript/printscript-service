package age.of.printscript.service.shared.infrastructure.filter

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.UUID

/**
 * Filtro perimetral que intercepta todas las peticiones HTTP entrantes a printscript-service.
 *
 * Responsabilidades:
 * 1. Lee o genera el correlation ID (header "X-Correlation-Id") para trazabilidad distribuida.
 * 2. Carga correlation-id, service y user-id (si viene en header X-User-Id) en MDC.
 * 3. Inyecta el header X-Correlation-Id en la respuesta HTTP.
 * 4. Mide la duración total en milisegundos y registra el resultado (INFO para 2xx/3xx, WARN para 4xx, ERROR para 5xx).
 * 5. Limpia el MDC en el bloque finally para evitar contaminación entre hilos de Tomcat.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class RequestLoggingFilter : OncePerRequestFilter() {
    private val log = LoggerFactory.getLogger(RequestLoggingFilter::class.java)

    companion object {
        const val CORRELATION_ID_HEADER = "X-Correlation-Id"
        const val USER_ID_HEADER = "X-User-Id"
        const val CORRELATION_ID_KEY = "correlation-id"
        const val USER_ID_KEY = "user-id"
        const val SERVICE_KEY = "service"
        const val SERVICE_NAME = "printscript-service"

        private const val HTTP_SERVER_ERROR_THRESHOLD = 500
        private const val HTTP_CLIENT_ERROR_THRESHOLD = 400
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val correlationId =
            request.getHeader(CORRELATION_ID_HEADER)?.takeIf { it.isNotBlank() }
                ?: UUID.randomUUID().toString()

        MDC.put(CORRELATION_ID_KEY, correlationId)
        MDC.put(SERVICE_KEY, SERVICE_NAME)

        request.getHeader(USER_ID_HEADER)?.takeIf { it.isNotBlank() }?.let { userId ->
            MDC.put(USER_ID_KEY, userId)
        }

        response.setHeader(CORRELATION_ID_HEADER, correlationId)

        val start = System.currentTimeMillis()
        try {
            filterChain.doFilter(request, response)
        } finally {
            val duration = System.currentTimeMillis() - start
            val status = response.status
            val method = request.method
            val uri = request.requestURI
            val message = "HTTP $method $uri - $status (${duration}ms)"

            when {
                status >= HTTP_SERVER_ERROR_THRESHOLD -> log.error(message)
                status >= HTTP_CLIENT_ERROR_THRESHOLD -> log.warn(message)
                else -> log.info(message)
            }

            MDC.clear()
        }
    }
}
