package age.of.printscript.service.shared.application

class CodeNotFoundException(
    codeUrl: String,
    cause: Throwable? = null,
) : RuntimeException("Code was not found at '$codeUrl'.", cause)

class CodeReaderUnavailableException : RuntimeException("No code storage has been configured.")
