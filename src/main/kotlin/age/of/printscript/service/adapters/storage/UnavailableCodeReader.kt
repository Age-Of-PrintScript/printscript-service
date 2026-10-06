package age.of.printscript.service.adapters.storage

import age.of.printscript.service.validation.CodeReader
import age.of.printscript.service.validation.CodeReaderUnavailableException

class UnavailableCodeReader : CodeReader {
    override fun read(codeUrl: String): String = throw CodeReaderUnavailableException()
}
