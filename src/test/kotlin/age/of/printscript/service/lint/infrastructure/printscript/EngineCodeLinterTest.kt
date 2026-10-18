package age.of.printscript.service.lint.infrastructure.printscript

import age.of.printscript.service.lint.application.InvalidLintOptionsException
import linter.Linter
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class EngineCodeLinterTest {
    private val codeLinter = EngineCodeLinter(Linter.create())

    @Test
    fun `translates an unsupported version into invalid lint options`() {
        val exception =
            assertThrows(InvalidLintOptionsException::class.java) {
                codeLinter.lint("let name: string = \"Lucia\";", "2.0", null)
            }

        assertEquals("Unsupported version: 2.0", exception.message)
    }

    @Test
    fun `translates malformed config into invalid lint options`() {
        val exception =
            assertThrows(InvalidLintOptionsException::class.java) {
                codeLinter.lint("let name: string = \"Lucia\";", "1.1", "{ invalid json }")
            }

        assertEquals("The lint configuration is invalid.", exception.message)
    }

    @Test
    fun `translates unsupported rules into invalid lint options`() {
        val config =
            """
            {
              "rules": [
                { "name": "unknown-rule" }
              ]
            }
            """.trimIndent()

        val exception =
            assertThrows(InvalidLintOptionsException::class.java) {
                codeLinter.lint("let name: string = \"Lucia\";", "1.1", config)
            }

        assertEquals("Rule 'unknown-rule' is not supported in version 1.1", exception.message)
    }
}
