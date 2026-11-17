package age.of.printscript.service.test.infrastructure.printscript

import age.of.printscript.service.test.domain.ExecutionResult
import engine.Engine
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class EngineCodeExecutorTest {
    private val executor = EngineCodeExecutor(Engine())

    @Test
    fun `executes v1_1 snippet with inputs and collects outputs`() {
        val source =
            """
            let name: string = readInput("Name: ");
            let age: number = readInput("Age: ");
            println("Hello " + name);
            println(age + 1);
            """.trimIndent()

        val result =
            executor.execute(
                source = source,
                version = "1.1",
                inputs = listOf("Alice", "25"),
            )

        assertEquals(ExecutionResult.Success(listOf("Hello Alice", "26")), result)
    }

    @Test
    fun `returns empty string when inputs are exhausted`() {
        val source =
            """
            let value: string = readInput("Prompt: ");
            println("Got:" + value);
            """.trimIndent()

        val result =
            executor.execute(
                source = source,
                version = "1.1",
                inputs = emptyList(),
            )

        assertEquals(ExecutionResult.Success(listOf("Got:")), result)
    }

    @Test
    fun `returns failure with error messages when execution fails`() {
        val source =
            """
            let age: number = readInput("Age: ");
            println(age);
            """.trimIndent()

        val result =
            executor.execute(
                source = source,
                version = "1.1",
                inputs = listOf("not-a-number"),
            )

        assertTrue(result is ExecutionResult.Failure)
        val errors = (result as ExecutionResult.Failure).errors
        assertTrue(errors.isNotEmpty())
        assertFalse(errors.contains("Build Failed"))
    }
}
