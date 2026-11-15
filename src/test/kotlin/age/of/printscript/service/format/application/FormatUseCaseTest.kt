package age.of.printscript.service.format.application

import age.of.printscript.service.format.domain.CodeFormatter
import age.of.printscript.service.shared.application.CodeReader
import age.of.printscript.service.shared.application.CodeWriter
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class FormatUseCaseTest {
    @Test
    fun `reads code, formats it with requested config, writes result back, and returns formatted code`() {
        val reader = RecordingCodeReader("let x:number=5;")
        val writer = RecordingCodeWriter()
        val formatter = RecordingCodeFormatter("let x: number = 5;")
        val useCase = FormatUseCase(reader, writer, formatter)
        val config = """{"enforce-spacing-around-equals":true}"""

        val result =
            useCase.execute(
                codeUrl = "http://asset-service:8080/v1/asset/snippets/snip-1",
                version = "1.1",
                config = config,
            )

        assertEquals("let x: number = 5;", result)
        assertEquals("http://asset-service:8080/v1/asset/snippets/snip-1", reader.readUrl)
        assertEquals("let x:number=5;", formatter.source)
        assertEquals("1.1", formatter.version)
        assertEquals(config, formatter.config)
        assertEquals("http://asset-service:8080/v1/asset/snippets/snip-1", writer.writtenUrl)
        assertEquals("let x: number = 5;", writer.writtenContent)
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

    private class RecordingCodeWriter : CodeWriter {
        var writtenUrl: String? = null
        var writtenContent: String? = null

        override fun write(
            codeUrl: String,
            content: String,
        ) {
            writtenUrl = codeUrl
            writtenContent = content
        }
    }

    private class RecordingCodeFormatter(
        private val formatted: String,
    ) : CodeFormatter {
        var source: String? = null
        var version: String? = null
        var config: String? = null

        override fun format(
            source: String,
            version: String,
            config: String?,
        ): String {
            this.source = source
            this.version = version
            this.config = config
            return formatted
        }
    }
}
