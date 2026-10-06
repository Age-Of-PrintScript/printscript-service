package age.of.printscript.service.adapters.storage

import age.of.printscript.service.validation.CodeNotFoundException
import age.of.printscript.service.validation.CodeReader

class FakeCodeReader : CodeReader {
    override fun read(codeUrl: String): String =
        snippets[codeUrl] ?: throw CodeNotFoundException(codeUrl)

    private companion object {
        val snippets =
            mapOf(
                "fake/1" to "let greeting: string = \"Hola\";",
                "fake/2" to "let greeting: string = ;",
            )
    }
}
