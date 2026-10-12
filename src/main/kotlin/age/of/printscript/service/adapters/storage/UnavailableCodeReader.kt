package age.of.printscript.service.adapters.storage

import age.of.printscript.service.shared.CodeReader
import age.of.printscript.service.shared.CodeReaderUnavailableException

class UnavailableCodeReader : CodeReader {
    override fun read(codeUrl: String): String = throw CodeReaderUnavailableException()
}
