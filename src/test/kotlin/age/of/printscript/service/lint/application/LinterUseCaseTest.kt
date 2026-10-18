package age.of.printscript.service.lint.application

import age.of.printscript.service.lint.domain.CodeLinter
import age.of.printscript.service.lint.domain.Warning
import age.of.printscript.service.shared.application.CodeReader
import age.of.printscript.service.shared.domain.Position
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class LinterUseCaseTest {
    @Test
    fun `reads the requested code and lints it with the requested options`() {
        val reader = RecordingCodeReader("let name: string = \"Lucia\";")
        val warnings = listOf(Warning("Unused variable", Position(1, 5)))
        val linter = RecordingCodeLinter(warnings)
        val useCase = LinterUseCase(reader, linter)
        val config = "{\"rules\": {\"unused-variable\": \"warning\"}}"

        val result = useCase.execute(
            codeUrl = "s3://scripts/example.ps",
            version = "1.1",
            config = config,
        )

        assertEquals(warnings, result)
        assertEquals("s3://scripts/example.ps", reader.readUrl)
        assertEquals("let name: string = \"Lucia\";", linter.source)
        assertEquals("1.1", linter.version)
        assertEquals(config, linter.config)
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
