package age.of.printscript.service.test.application

data class TestContent(
    val inputs: List<String>,
    val outputs: List<String>,
)

interface TestContentReader {
    fun read(url: String): TestContent
}
