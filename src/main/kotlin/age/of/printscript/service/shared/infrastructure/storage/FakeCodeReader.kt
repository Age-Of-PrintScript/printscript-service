package age.of.printscript.service.shared.infrastructure.storage

import age.of.printscript.service.shared.application.CodeNotFoundException
import age.of.printscript.service.shared.application.CodeReader

class FakeCodeReader : CodeReader {
    override fun read(codeUrl: String): String = snippets[codeUrl] ?: throw CodeNotFoundException(codeUrl)

    private companion object {
        val snippets =
            mapOf(
                "fake/1" to "let greeting: string = \"Hola\";",
                "fake/2" to "let greeting: string = ;",
            )
    }
}
