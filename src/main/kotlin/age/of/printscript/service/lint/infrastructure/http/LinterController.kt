package age.of.printscript.service.lint.infrastructure.http

import age.of.printscript.service.lint.application.LinterUseCase
import age.of.printscript.service.lint.domain.Warning
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/lint")
class LinterController(
    private val linterUseCase: LinterUseCase,
) {
    @PostMapping
    fun lint(
        @Valid @RequestBody request: ValidateLinterRequest,
    ): ResponseEntity<List<Warning>> =
        ResponseEntity.ok(
            linterUseCase.execute(
                request.codeUrl,
                request.version,
                request.config,
            ),
        )
}

data class ValidateLinterRequest(
    @field:NotBlank val codeUrl: String,
    @field:NotBlank val version: String,
    val config: String? = null,
)
