package age.of.printscript.service.shared.infrastructure.filter

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletResponse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.MDC
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse

class RequestLoggingFilterTest {
    private val filter = RequestLoggingFilter()

    @BeforeEach
    fun setUp() {
        MDC.clear()
    }

    @Test
    fun `genera correlation-id cuando el request no lo envia`() {
        val request = MockHttpServletRequest("GET", "/validate")
        val response = MockHttpServletResponse()
        var correlationIdInChain: String? = null
        var serviceInChain: String? = null

        val chain =
            FilterChain { req, res ->
                correlationIdInChain = MDC.get(RequestLoggingFilter.CORRELATION_ID_KEY)
                serviceInChain = MDC.get(RequestLoggingFilter.SERVICE_KEY)
                (res as HttpServletResponse).status = HttpServletResponse.SC_OK
            }

        filter.doFilter(request, response, chain)

        assertNotNull(correlationIdInChain)
        assertTrue(correlationIdInChain!!.isNotBlank())
        assertEquals("printscript-service", serviceInChain)
        assertEquals(correlationIdInChain, response.getHeader(RequestLoggingFilter.CORRELATION_ID_HEADER))
        assertNull(MDC.get(RequestLoggingFilter.CORRELATION_ID_KEY))
    }

    @Test
    fun `reutiliza correlation-id cuando viene en el header del request`() {
        val incomingId = "incoming-correlation-id-999"
        val request =
            MockHttpServletRequest("POST", "/lint").apply {
                addHeader(RequestLoggingFilter.CORRELATION_ID_HEADER, incomingId)
            }
        val response = MockHttpServletResponse()
        var correlationIdInChain: String? = null

        val chain =
            FilterChain { _, res ->
                correlationIdInChain = MDC.get(RequestLoggingFilter.CORRELATION_ID_KEY)
                (res as HttpServletResponse).status = HttpServletResponse.SC_OK
            }

        filter.doFilter(request, response, chain)

        assertEquals(incomingId, correlationIdInChain)
        assertEquals(incomingId, response.getHeader(RequestLoggingFilter.CORRELATION_ID_HEADER))
        assertNull(MDC.get(RequestLoggingFilter.CORRELATION_ID_KEY))
    }

    @Test
    fun `captura user-id en MDC cuando viene en header X-User-Id`() {
        val userId = "auth0|testuser"
        val request =
            MockHttpServletRequest("POST", "/lint").apply {
                addHeader(RequestLoggingFilter.USER_ID_HEADER, userId)
            }
        val response = MockHttpServletResponse()
        var userIdInChain: String? = null

        val chain =
            FilterChain { _, res ->
                userIdInChain = MDC.get(RequestLoggingFilter.USER_ID_KEY)
                (res as HttpServletResponse).status = HttpServletResponse.SC_OK
            }

        filter.doFilter(request, response, chain)

        assertEquals(userId, userIdInChain)
        assertNull(MDC.get(RequestLoggingFilter.USER_ID_KEY))
    }

    @Test
    fun `limpia el MDC incluso si la cadena de filtros lanza excepcion`() {
        val request = MockHttpServletRequest("GET", "/error")
        val response = MockHttpServletResponse()

        val chain =
            FilterChain { _, _ ->
                throw IllegalStateException("Fallo simulado en downstream")
            }

        runCatching { filter.doFilter(request, response, chain) }

        assertNull(MDC.get(RequestLoggingFilter.CORRELATION_ID_KEY))
        assertNull(MDC.get(RequestLoggingFilter.SERVICE_KEY))
    }
}
