package age.of.printscript.service.validation

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ValidateScriptTest {
    @Test
    fun `reads the requested code and validates it with the requested version`() {
        val reader = RecordingCodeReader("let name: string = \"Lucia\";")
        val validator = RecordingValidator(ValidationResult.Valid)
        val useCase = ValidateScript(reader, validator)

        val result = useCase.execute(codeUrl = "s3://scripts/example.ps", version = "1.1")

        assertEquals(ValidationResult.Valid, result)
        assertEquals("s3://scripts/example.ps", reader.readUrl)
        assertEquals("let name: string = \"Lucia\";", validator.source)
        assertEquals("1.1", validator.version)
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

    private class RecordingValidator(
        private val result: ValidationResult,
    ) : ScriptValidator {
        var source: String? = null
        var version: String? = null

        override fun validate(
            source: String,
            version: String,
        ): ValidationResult {
            this.source = source
            this.version = version
            return result
        }
    }
}
