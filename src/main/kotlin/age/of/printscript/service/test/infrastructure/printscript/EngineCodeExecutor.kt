package age.of.printscript.service.test.infrastructure.printscript

import age.of.printscript.service.test.domain.CodeExecutor
import age.of.printscript.service.test.domain.ExecutionResult
import engine.Engine
import engine.EngineIO
import engine.ExitCode
import engine.Logger

class EngineCodeExecutor(
    private val engine: Engine,
) : CodeExecutor {
    override fun execute(
        source: String,
        version: String,
        inputs: List<String>,
    ): ExecutionResult {
        val outputs = mutableListOf<String>()
        val inputIterator = inputs.iterator()
        val io =
            EngineIO(
                emitter = { message -> outputs.add(message) },
                provider = { if (inputIterator.hasNext()) inputIterator.next() else "" },
            )

        val messages = mutableListOf<String>()
        val logger =
            object : Logger {
                override fun log(string: String) {
                    messages.add(string)
                }
            }

        val result = engine.execute(source = source, io = io, logger = logger, version = version)
        return when (result.exitCode) {
            ExitCode.SUCCESS -> ExecutionResult.Success(outputs)
            ExitCode.FAILURE ->
                ExecutionResult.Failure(
                    errors = messages.filterNot { it == BUILD_FAILED_MESSAGE },
                )
        }
    }

    private companion object {
        const val BUILD_FAILED_MESSAGE = "Build Failed"
    }
}
