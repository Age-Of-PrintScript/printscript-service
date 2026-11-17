package age.of.printscript.service.shared.application

interface CodeWriter {
    fun write(
        codeUrl: String,
        content: String,
    )
}
