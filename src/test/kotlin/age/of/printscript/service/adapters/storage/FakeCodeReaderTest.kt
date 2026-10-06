package age.of.printscript.service.adapters.storage

import age.of.printscript.service.validation.CodeNotFoundException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class FakeCodeReaderTest {
    private val reader = FakeCodeReader()

    @Test
    fun `returns the first fake snippet`() {
        assertEquals("let greeting: string = \"Hola\";", reader.read("fake/1"))
    }

    @Test
    fun `returns the second fake snippet`() {
        assertEquals("let greeting: string = ;", reader.read("fake/2"))
    }

    @Test
    fun `reports an unknown snippet`() {
        assertThrows(CodeNotFoundException::class.java) {
            reader.read("fake/99")
        }
    }
}
