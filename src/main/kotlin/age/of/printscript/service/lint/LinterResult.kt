package age.of.printscript.service.lint

import age.of.printscript.service.shared.Position
import linter.Warning as PrintScriptWarning

data class Warning(val message: String, val position: Position)

fun PrintScriptWarning.toServiceWarning(): Warning =
    Warning(
        message,
        Position(
            position.line,
            position.charPosition
        ),
    )
