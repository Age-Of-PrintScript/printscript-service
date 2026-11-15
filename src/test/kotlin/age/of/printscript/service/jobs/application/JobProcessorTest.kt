package age.of.printscript.service.jobs.application

import age.of.printscript.jobs.ExecuteJobRequest
import age.of.printscript.jobs.FormatJobRequest
import age.of.printscript.jobs.FormatJobResponse
import age.of.printscript.jobs.LintJobRequest
import age.of.printscript.jobs.LintJobResponse
import age.of.printscript.jobs.SystemErrorJobResponse
import age.of.printscript.jobs.TestJobRequest
import age.of.printscript.jobs.TestJobResponse
import age.of.printscript.jobs.ValidateJobRequest
import age.of.printscript.jobs.ValidateJobResponse
import age.of.printscript.service.format.application.FormatUseCase
import age.of.printscript.service.lint.application.LinterUseCase
import age.of.printscript.service.lint.domain.Warning
import age.of.printscript.service.shared.application.CodeNotFoundException
import age.of.printscript.service.shared.domain.Position
import age.of.printscript.service.test.application.TestUseCase
import age.of.printscript.service.test.domain.TestResult
import age.of.printscript.service.validation.application.ValidateUseCase
import age.of.printscript.service.validation.domain.ValidationError
import age.of.printscript.service.validation.domain.ValidationResult
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

class JobProcessorTest {
    private val validateUseCase = mock(ValidateUseCase::class.java)
    private val linterUseCase = mock(LinterUseCase::class.java)
    private val formatUseCase = mock(FormatUseCase::class.java)
    private val testUseCase = mock(TestUseCase::class.java)

    private val processor = JobProcessor(validateUseCase, linterUseCase, formatUseCase, testUseCase)

    @Test
    fun `processes ValidateJobRequest successfully when valid`() {
        `when`(validateUseCase.execute("snippet/url", "1.1")).thenReturn(ValidationResult.Valid)

        val request =
            ValidateJobRequest(
                jobId = "job-1",
                snippetId = "snip-1",
                snippetUrl = "snippet/url",
                languageVersion = "1.1",
            )

        val response = processor.process(request) as ValidateJobResponse

        assertEquals("job-1", response.jobId)
        assertTrue(response.isValid)
        assertTrue(response.errors.isEmpty())
    }

    @Test
    fun `processes ValidateJobRequest successfully when invalid`() {
        val errors = listOf(ValidationError("Syntax error at line 3"))
        `when`(validateUseCase.execute("snippet/url", "1.1")).thenReturn(ValidationResult.Invalid(errors))

        val request =
            ValidateJobRequest(
                jobId = "job-2",
                snippetId = "snip-2",
                snippetUrl = "snippet/url",
                languageVersion = "1.1",
            )

        val response = processor.process(request) as ValidateJobResponse

        assertEquals("job-2", response.jobId)
        assertFalse(response.isValid)
        assertEquals(1, response.errors.size)
        assertEquals("Syntax error at line 3", response.errors.first().message)
    }

    @Test
    fun `processes LintJobRequest with empty rules`() {
        val warnings =
            listOf(
                Warning(
                    message = "CamelCase required",
                    position = Position(line = 2, charPosition = 5),
                ),
            )
        `when`(linterUseCase.execute("snippet/url", "1.1", null)).thenReturn(warnings)

        val request =
            LintJobRequest(
                jobId = "job-3",
                snippetId = "snip-3",
                snippetUrl = "snippet/url",
                languageVersion = "1.1",
                rules = emptyMap(),
            )

        val response = processor.process(request) as LintJobResponse

        assertEquals("job-3", response.jobId)
        assertEquals(1, response.warnings.size)
        assertEquals("CamelCase required", response.warnings.first().message)
        assertEquals(
            2,
            response.warnings
                .first()
                .positionDto.line,
        )
        assertEquals(
            5,
            response.warnings
                .first()
                .positionDto.charPosition,
        )
    }

    @Test
    fun `processes LintJobRequest with specific rules`() {
        val expectedConfig = """{"rules":[{"name":"identifier-format","params":{"convention":"camelCase"}},{"name":"println-no-expression"}]}"""
        val warnings = emptyList<Warning>()
        `when`(linterUseCase.execute("snippet/url", "1.1", expectedConfig)).thenReturn(warnings)

        val request =
            LintJobRequest(
                jobId = "job-4",
                snippetId = "snip-4",
                snippetUrl = "snippet/url",
                languageVersion = "1.1",
                rules =
                    linkedMapOf(
                        "identifier-format" to "camelCase",
                        "println-no-expression" to null,
                    ),
            )

        val response = processor.process(request) as LintJobResponse

        assertEquals("job-4", response.jobId)
        assertTrue(response.warnings.isEmpty())
    }

    @Test
    fun `processes FormatJobRequest with empty rules`() {
        `when`(formatUseCase.execute("snippet/url", "1.1", null)).thenReturn("let x: number = 5;")

        val request =
            FormatJobRequest(
                jobId = "job-5",
                snippetId = "snip-5",
                snippetUrl = "snippet/url",
                languageVersion = "1.1",
                rules = emptyMap(),
            )

        val response = processor.process(request) as FormatJobResponse

        assertEquals("job-5", response.jobId)
        verify(formatUseCase).execute("snippet/url", "1.1", null)
    }

    @Test
    fun `processes FormatJobRequest with specific rules`() {
        val expectedConfig =
            """{"enforce-spacing-around-equals":true,"indent-inside-if":4,"if-brace-same-line":false,"custom-rule":"text"}"""
        `when`(formatUseCase.execute("snippet/url", "1.1", expectedConfig)).thenReturn("let x: number = 5;")

        val request =
            FormatJobRequest(
                jobId = "job-5b",
                snippetId = "snip-5",
                snippetUrl = "snippet/url",
                languageVersion = "1.1",
                rules =
                    linkedMapOf(
                        "enforce-spacing-around-equals" to null,
                        "indent-inside-if" to "4",
                        "if-brace-same-line" to "false",
                        "custom-rule" to "text",
                    ),
            )

        val response = processor.process(request) as FormatJobResponse

        assertEquals("job-5b", response.jobId)
        verify(formatUseCase).execute("snippet/url", "1.1", expectedConfig)
    }

    @Test
    fun `returns SystemErrorJobResponse for ExecuteJobRequest`() {
        val request =
            ExecuteJobRequest(
                jobId = "job-6",
                snippetId = "snip-6",
                snippetUrl = "snippet/url",
                languageVersion = "1.1",
            )

        val response = processor.process(request) as SystemErrorJobResponse

        assertEquals("job-6", response.jobId)
        assertEquals("EXECUTE is not implemented yet", response.reason)
    }

    @Test
    fun `processes TestJobRequest successfully when test passes`() {
        `when`(
            testUseCase.execute(
                codeUrl = "snippet/url",
                version = "1.1",
                testUrl = "test/url",
            ),
        ).thenReturn(TestResult.Passed)

        val request =
            TestJobRequest(
                jobId = "job-7",
                snippetId = "snip-7",
                snippetUrl = "snippet/url",
                languageVersion = "1.1",
                testUrl = "test/url",
            )

        val response = processor.process(request) as TestJobResponse

        assertEquals("job-7", response.jobId)
        assertTrue(response.success)
        assertNull(response.message)
        verify(testUseCase).execute("snippet/url", "1.1", "test/url")
    }

    @Test
    fun `processes TestJobRequest when test fails`() {
        `when`(
            testUseCase.execute(
                codeUrl = "snippet/url",
                version = "1.1",
                testUrl = "test/url",
            ),
        ).thenReturn(TestResult.Failed("Expected outputs [Hello Alice] but got [Hello Bob]"))

        val request =
            TestJobRequest(
                jobId = "job-7b",
                snippetId = "snip-7",
                snippetUrl = "snippet/url",
                languageVersion = "1.1",
                testUrl = "test/url",
            )

        val response = processor.process(request) as TestJobResponse

        assertEquals("job-7b", response.jobId)
        assertFalse(response.success)
        assertEquals("Expected outputs [Hello Alice] but got [Hello Bob]", response.message)
    }

    @Test
    fun `returns SystemErrorJobResponse when an unhandled exception occurs`() {
        `when`(formatUseCase.execute("snippet/url", "1.1", null)).thenThrow(CodeNotFoundException("snippet/url"))

        val request =
            FormatJobRequest(
                jobId = "job-8",
                snippetId = "snip-8",
                snippetUrl = "snippet/url",
                languageVersion = "1.1",
            )

        val response = processor.process(request) as SystemErrorJobResponse

        assertEquals("job-8", response.jobId)
        assertEquals("Code was not found at 'snippet/url'.", response.reason)
    }
}
