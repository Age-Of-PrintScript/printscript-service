package age.of.printscript.service.format.infrastructure.printscript

import age.of.printscript.service.format.application.InvalidFormatException
import formatter.Formatter
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class EngineCodeFormatterTest {
    private val codeFormatter = EngineCodeFormatter(Formatter.create())

    @Test
    fun `formats snippet with custom json configuration`() {
        val source = "let   name:string=\"Lucia\";"
        val config =
            """
            {
              "enforce-spacing-around-equals": true,
              "enforce-spacing-after-colon-in-declaration": true
            }
            """.trimIndent()

        val formatted = codeFormatter.format(source, "1.1", config)

        assertEquals("let name: string = \"Lucia\";", formatted.trim())
    }

    @Test
    fun `formats snippet with default configuration when config is null`() {
        val source = "let x: number = 1;"

        val formatted = codeFormatter.format(source, "1.0", null)

        assertTrue(formatted.contains("x"))
    }

    @Test
    fun `throws InvalidFormatException when version is unsupported`() {
        val exception =
            assertThrows(InvalidFormatException::class.java) {
                codeFormatter.format("let x: number = 1;", "2.0", null)
            }

        assertEquals("Unknown version", exception.message)
    }

    @Test
    fun `throws InvalidFormatException when config json is invalid`() {
        val exception =
            assertThrows(InvalidFormatException::class.java) {
                codeFormatter.format("let x: number = 1;", "1.1", "{ invalid json }")
            }

        assertEquals("Formatting rules are invalid", exception.message)
    }

    @Test
    fun `throws InvalidFormatException when conflicting rules are enabled`() {
        val config =
            """
            {
              "enforce-spacing-around-equals": true,
              "enforce-no-spacing-around-equals": true
            }
            """.trimIndent()

        val exception =
            assertThrows(InvalidFormatException::class.java) {
                codeFormatter.format("let x: number = 1;", "1.1", config)
            }

        assertTrue(exception.message!!.contains("cannot both be enabled"))
    }

    @Test
    fun `throws InvalidFormatException when source has syntax error`() {
        assertThrows(InvalidFormatException::class.java) {
            codeFormatter.format("let x: = ;", "1.1", null)
        }
    }
}
