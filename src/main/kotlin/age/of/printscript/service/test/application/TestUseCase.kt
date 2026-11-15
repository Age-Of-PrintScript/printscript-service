package age.of.printscript.service.test.application

import age.of.printscript.service.shared.application.CodeReader
import age.of.printscript.service.test.domain.CodeExecutor
import age.of.printscript.service.test.domain.ExecutionResult
import age.of.printscript.service.test.domain.TestResult

class TestUseCase(
    private val codeReader: CodeReader,
    private val testContentReader: TestContentReader,
    private val codeExecutor: CodeExecutor,
) {
    fun execute(
        codeUrl: String,
        version: String,
        testUrl: String,
    ): TestResult {
        val test = testContentReader.read(testUrl)
        val source = codeReader.read(codeUrl)
        return when (val result = codeExecutor.execute(source = source, version = version, inputs = test.inputs)) {
            is ExecutionResult.Success ->
                if (result.outputs == test.outputs) {
                    TestResult.Passed
                } else {
                    TestResult.Failed("Expected outputs ${test.outputs} but got ${result.outputs}")
                }
            is ExecutionResult.Failure ->
                TestResult.Failed(result.errors.joinToString("\n").ifBlank { DEFAULT_EXECUTION_ERROR })
        }
    }

    private companion object {
        const val DEFAULT_EXECUTION_ERROR = "Execution failed"
    }
}
