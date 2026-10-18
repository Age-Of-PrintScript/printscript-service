package age.of.printscript.service.shared.infrastructure.storage

import age.of.printscript.service.shared.application.CodeReader
import age.of.printscript.service.shared.application.CodeReaderUnavailableException

class UnavailableCodeReader : CodeReader {
    override fun read(codeUrl: String): String = throw CodeReaderUnavailableException()
}
