package age.of.printscript.service.test.domain

interface CodeExecutor {
    fun execute(
        source: String,
        version: String,
        inputs: List<String>,
    ): ExecutionResult
}
