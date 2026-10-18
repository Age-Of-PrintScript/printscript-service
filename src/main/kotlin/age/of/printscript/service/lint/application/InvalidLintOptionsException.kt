package age.of.printscript.service.lint.application

class InvalidLintOptionsException(
    message: String,
    cause: Throwable,
) : RuntimeException(message, cause)
