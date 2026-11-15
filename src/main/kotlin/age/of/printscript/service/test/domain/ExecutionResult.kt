package age.of.printscript.service.test.domain

sealed interface ExecutionResult {
    data class Success(
        val outputs: List<String>,
    ) : ExecutionResult

    data class Failure(
        val errors: List<String>,
    ) : ExecutionResult
}
