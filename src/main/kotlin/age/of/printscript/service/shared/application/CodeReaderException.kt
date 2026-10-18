package age.of.printscript.service.shared.application

class CodeNotFoundException(
    codeUrl: String,
) : RuntimeException("Code was not found at '$codeUrl'.")

class CodeReaderUnavailableException : RuntimeException("No code storage has been configured.")
