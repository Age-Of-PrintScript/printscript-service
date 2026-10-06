package age.of.printscript.service.adapters.printscript

import age.of.printscript.service.validation.ValidationResult
import engine.Engine
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class EngineScriptValidatorTest {
    private val validator = EngineScriptValidator(Engine())

    @Test
    fun `returns valid for syntactically valid source`() {
        val result = validator.validate("let name: string = \"Lucia\";", "1.1")

        assertEquals(ValidationResult.Valid, result)
    }

    @Test
    fun `returns validation messages for invalid source`() {
        val result = validator.validate("let name: string = ;", "1.1")

        assertTrue(result is ValidationResult.Invalid)
        assertTrue((result as ValidationResult.Invalid).errors.isNotEmpty())
    }
}
