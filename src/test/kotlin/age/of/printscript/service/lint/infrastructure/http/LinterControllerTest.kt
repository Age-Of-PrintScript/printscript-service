package age.of.printscript.service.lint.infrastructure.http

import age.of.printscript.service.lint.application.InvalidLintOptionsException
import age.of.printscript.service.lint.application.LinterUseCase
import age.of.printscript.service.lint.domain.CodeLinter
import age.of.printscript.service.lint.domain.Warning
import age.of.printscript.service.shared.application.CodeReader
import age.of.printscript.service.shared.domain.Position
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders

class LinterControllerTest {
    private val codeReader = RecordingCodeReader("let greeting: string = \"Hola\";")
    private val warnings = listOf(Warning("Unused variable", Position(2, 4)))
    private val codeLinter = RecordingCodeLinter(warnings)
    private val mockMvc: MockMvc =
        MockMvcBuilders
            .standaloneSetup(LinterController(LinterUseCase(codeReader, codeLinter)))
            .setControllerAdvice(LinterExceptionHandler())
            .build()

    @Test
    fun `returns warnings and forwards an omitted config as null`() {
        mockMvc
            .perform(
                post("/lint")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"codeUrl":"fake/1","version":"1.1"}"""),
            ).andExpectAll(
                status().isOk,
                content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON),
                jsonPath("\$[0].message").value("Unused variable"),
                jsonPath("\$[0].position.line").value(2),
                jsonPath("\$[0].position.charPosition").value(4),
            )

        assertEquals("fake/1", codeReader.readUrl)
        assertEquals("let greeting: string = \"Hola\";", codeLinter.source)
        assertEquals("1.1", codeLinter.version)
        assertNull(codeLinter.config)
    }

    @Test
    fun `returns a bad request when lint options are invalid`() {
        val failingLinter =
            object : CodeLinter {
                override fun lint(
                    source: String,
                    version: String,
                    config: String?,
                ): List<Warning> = throw InvalidLintOptionsException("Unsupported version: $version", IllegalArgumentException())
            }
        val mockMvc =
            MockMvcBuilders
                .standaloneSetup(LinterController(LinterUseCase(codeReader, failingLinter)))
                .setControllerAdvice(LinterExceptionHandler())
                .build()

        mockMvc
            .perform(
                post("/lint")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"codeUrl":"fake/1","version":"2.0"}"""),
            ).andExpectAll(
                status().isBadRequest,
                content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON),
                jsonPath("$.message").value("Unsupported version: 2.0"),
            )
    }

    private class RecordingCodeReader(
        private val source: String,
    ) : CodeReader {
        var readUrl: String? = null

        override fun read(codeUrl: String): String {
            readUrl = codeUrl
            return source
        }
    }

    private class RecordingCodeLinter(
        private val warnings: List<Warning>,
    ) : CodeLinter {
        var source: String? = null
        var version: String? = null
        var config: String? = null

        override fun lint(
            source: String,
            version: String,
            config: String?,
        ): List<Warning> {
            this.source = source
            this.version = version
            this.config = config
            return warnings
        }
    }
}
