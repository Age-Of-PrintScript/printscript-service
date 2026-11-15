package age.of.printscript.service.test.application

import age.of.printscript.service.shared.application.CodeReader
import age.of.printscript.service.test.domain.CodeExecutor
import age.of.printscript.service.test.domain.ExecutionResult
import age.of.printscript.service.test.domain.TestResult
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TestUseCaseTest {
    @Test
    fun `returns Passed when outputs match expectedOutputs`() {
        val reader = RecordingCodeReader("println(\"hi\");")
        val executor = RecordingCodeExecutor(ExecutionResult.Success(listOf("hi")))
        val useCase = TestUseCase(reader, executor)

        val result =
            useCase.execute(
                codeUrl = "snippet/url",
                version = "1.1",
                inputs = listOf("in-1"),
                expectedOutputs = listOf("hi"),
            )

        assertEquals(TestResult.Passed, result)
        assertEquals("snippet/url", reader.readUrl)
        assertEquals("println(\"hi\");", executor.source)
        assertEquals("1.1", executor.version)
        assertEquals(listOf("in-1"), executor.inputs)
    }

    @Test
    fun `returns Failed when outputs do not match expectedOutputs`() {
        val reader = RecordingCodeReader("println(\"actual\");")
        val executor = RecordingCodeExecutor(ExecutionResult.Success(listOf("actual")))
        val useCase = TestUseCase(reader, executor)

        val result =
            useCase.execute(
                codeUrl = "snippet/url",
                version = "1.1",
                inputs = emptyList(),
                expectedOutputs = listOf("expected"),
            )

        assertEquals(
            TestResult.Failed("Expected outputs [expected] but got [actual]"),
            result,
        )
    }

    @Test
    fun `returns Failed with execution errors when execution fails`() {
        val reader = RecordingCodeReader("let x: number = \"bad\";")
        val executor = RecordingCodeExecutor(ExecutionResult.Failure(listOf("Type mismatch")))
        val useCase = TestUseCase(reader, executor)

        val result =
            useCase.execute(
                codeUrl = "snippet/url",
                version = "1.1",
                inputs = emptyList(),
                expectedOutputs = emptyList(),
            )

        assertEquals(TestResult.Failed("Type mismatch"), result)
    }

    @Test
    fun `returns default message when execution fails without error details`() {
        val reader = RecordingCodeReader("invalid")
        val executor = RecordingCodeExecutor(ExecutionResult.Failure(emptyList()))
        val useCase = TestUseCase(reader, executor)

        val result =
            useCase.execute(
                codeUrl = "snippet/url",
                version = "1.1",
                inputs = emptyList(),
                expectedOutputs = emptyList(),
            )

        assertEquals(TestResult.Failed("Execution failed"), result)
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

    private class RecordingCodeExecutor(
        private val result: ExecutionResult,
    ) : CodeExecutor {
        var source: String? = null
        var version: String? = null
        var inputs: List<String>? = null

        override fun execute(
            source: String,
            version: String,
            inputs: List<String>,
        ): ExecutionResult {
            this.source = source
            this.version = version
            this.inputs = inputs
            return result
        }
    }
}
