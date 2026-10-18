package age.of.printscript.service.validation.infrastructure.http

import age.of.printscript.service.shared.application.CodeReader
import age.of.printscript.service.validation.application.ValidateSourceUseCase
import age.of.printscript.service.validation.application.ValidateUseCase
import age.of.printscript.service.validation.domain.CodeValidator
import age.of.printscript.service.validation.domain.ValidationError
import age.of.printscript.service.validation.domain.ValidationResult
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders

class ValidationControllerTest {
    @Test
    fun `returns no content when the script is valid`() {
        mockMvcFor(ValidationResult.Valid)
            .perform(validationRequest())
            .andExpectAll(
                status().isNoContent,
                content().string(""),
            )
    }

    @Test
    fun `returns validation errors with success status when the script is invalid`() {
        mockMvcFor(ValidationResult.Invalid(listOf(ValidationError("Unexpected token"))))
            .perform(validationRequest())
            .andExpectAll(
                status().isOk,
                content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON),
                jsonPath("$.errors[0].message").value("Unexpected token"),
            )
    }

    @Test
    fun `returns validation errors when validating a new source`() {
        mockMvcFor(ValidationResult.Invalid(listOf(ValidationError("Unexpected token"))))
            .perform(
                post("/validate/new")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"code":"let greeting: string = ;","version":"1.1"}"""),
            ).andExpectAll(
                status().isOk,
                content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON),
                jsonPath("$.errors[0].message").value("Unexpected token"),
            )
    }

    @Test
    fun `rejects an unsupported version before validating a new source`() {
        mockMvcFor(ValidationResult.Valid)
            .perform(
                post("/validate/new")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"code":"let greeting: string = \"Hola\";","version":"2.0"}"""),
            ).andExpect(status().isBadRequest)
    }

    private fun mockMvcFor(result: ValidationResult): MockMvc {
        val validator =
            object : CodeValidator {
                override fun validate(
                    source: String,
                    version: String,
                ): ValidationResult = result
            }

        return MockMvcBuilders
            .standaloneSetup(
                ValidationController(
                    validate =
                        ValidateUseCase(
                            codeReader =
                                object : CodeReader {
                                    override fun read(codeUrl: String): String = "let greeting: string = \"Hola\";"
                                },
                            validator = validator,
                        ),
                    validateSource = ValidateSourceUseCase(validator),
                ),
            ).build()
    }

    private fun validationRequest() =
        post("/validate")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"codeUrl":"fake/1","version":"1.1"}""")
}
