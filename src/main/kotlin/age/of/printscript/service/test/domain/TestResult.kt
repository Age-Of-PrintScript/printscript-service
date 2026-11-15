package age.of.printscript.service.test.domain

sealed interface TestResult {
    data object Passed : TestResult

    data class Failed(
        val message: String,
    ) : TestResult
}
