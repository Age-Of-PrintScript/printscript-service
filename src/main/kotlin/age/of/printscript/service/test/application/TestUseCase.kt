package age.of.printscript.service.test.application

import age.of.printscript.service.shared.application.CodeReader
import age.of.printscript.service.test.domain.CodeExecutor
import age.of.printscript.service.test.domain.ExecutionResult
import age.of.printscript.service.test.domain.TestResult

class TestUseCase(
    private val codeReader: CodeReader,
    private val codeExecutor: CodeExecutor,
) {
    fun execute(
        codeUrl: String,
        version: String,
        inputs: List<String>,
        expectedOutputs: List<String>,
    ): TestResult {
        val source = codeReader.read(codeUrl)
        return when (val result = codeExecutor.execute(source = source, version = version, inputs = inputs)) {
            is ExecutionResult.Success ->
                if (result.outputs == expectedOutputs) {
                    TestResult.Passed
                } else {
                    TestResult.Failed("Expected outputs $expectedOutputs but got ${result.outputs}")
                }
            is ExecutionResult.Failure ->
                TestResult.Failed(result.errors.joinToString("\n").ifBlank { DEFAULT_EXECUTION_ERROR })
        }
    }

    private companion object {
        const val DEFAULT_EXECUTION_ERROR = "Execution failed"
    }
}
