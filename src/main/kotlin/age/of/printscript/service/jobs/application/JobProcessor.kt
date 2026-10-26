package age.of.printscript.service.jobs.application

import age.of.printscript.jobs.ExecuteJobRequest
import age.of.printscript.jobs.FormatJobRequest
import age.of.printscript.jobs.FormatJobResponse
import age.of.printscript.jobs.JobRequest
import age.of.printscript.jobs.JobResponse
import age.of.printscript.jobs.LintJobRequest
import age.of.printscript.jobs.LintJobResponse
import age.of.printscript.jobs.LintWarningDto
import age.of.printscript.jobs.PositionDto
import age.of.printscript.jobs.SystemErrorJobResponse
import age.of.printscript.jobs.TestJobRequest
import age.of.printscript.jobs.ValidateJobRequest
import age.of.printscript.jobs.ValidateJobResponse
import age.of.printscript.jobs.ValidationErrorDto
import age.of.printscript.service.lint.application.LinterUseCase
import age.of.printscript.service.lint.domain.Warning
import age.of.printscript.service.shared.application.CodeReader
import age.of.printscript.service.validation.application.ValidateUseCase
import age.of.printscript.service.validation.domain.ValidationResult
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class JobProcessor(
    private val validateUseCase: ValidateUseCase,
    private val linterUseCase: LinterUseCase,
    private val codeReader: CodeReader,
) {
    private val logger = LoggerFactory.getLogger(JobProcessor::class.java)

    @Suppress("TooGenericExceptionCaught")
    fun process(request: JobRequest): JobResponse =
        try {
            when (request) {
                is ValidateJobRequest -> processValidate(request)
                is LintJobRequest -> processLint(request)
                is FormatJobRequest -> processFormat(request)
                is ExecuteJobRequest -> SystemErrorJobResponse(request.jobId, EXECUTE_NOT_IMPLEMENTED)
                is TestJobRequest -> SystemErrorJobResponse(request.jobId, TEST_NOT_IMPLEMENTED)
            }
        } catch (exception: Exception) {
            logger.error("Failed to process job ${request.jobId}", exception)
            SystemErrorJobResponse(request.jobId, exception.message ?: DEFAULT_ERROR_MESSAGE)
        }

    private fun processValidate(request: ValidateJobRequest): JobResponse =
        when (val result = validateUseCase.execute(request.snippetUrl, request.languageVersion)) {
            ValidationResult.Valid ->
                ValidateJobResponse(
                    jobId = request.jobId,
                    isValid = true,
                    errors = emptyList(),
                )
            is ValidationResult.Invalid ->
                ValidateJobResponse(
                    jobId = request.jobId,
                    isValid = false,
                    errors = result.errors.map { ValidationErrorDto(it.message) },
                )
        }

    private fun processLint(request: LintJobRequest): JobResponse {
        val config = buildLintConfig(request.rules)
        val warnings =
            linterUseCase.execute(
                codeUrl = request.snippetUrl,
                version = request.languageVersion,
                config = config,
            )

        return LintJobResponse(
            jobId = request.jobId,
            warnings = warnings.map(::toLintWarningDto),
        )
    }

    private fun processFormat(request: FormatJobRequest): JobResponse {
        codeReader.read(request.snippetUrl)
        return FormatJobResponse(jobId = request.jobId)
    }

    private fun toLintWarningDto(warning: Warning): LintWarningDto =
        LintWarningDto(
            message = warning.message,
            positionDto = PositionDto(warning.position.line, warning.position.charPosition),
        )

    private fun buildLintConfig(rules: Map<String, String?>): String? {
        if (rules.isEmpty()) return null
        val entries =
            rules.entries.joinToString(",") { (name, value) ->
                if (value != null) {
                    val paramKey = if (name == IDENTIFIER_FORMAT_RULE) CONVENTION_PARAM else VALUE_PARAM
                    """{"name":"$name","params":{"$paramKey":"$value"}}"""
                } else {
                    """{"name":"$name"}"""
                }
            }
        return """{"rules":[$entries]}"""
    }

    private companion object {
        const val EXECUTE_NOT_IMPLEMENTED = "EXECUTE is not implemented yet"
        const val TEST_NOT_IMPLEMENTED = "TEST is not implemented yet"
        const val DEFAULT_ERROR_MESSAGE = "Failed to process job"
        const val IDENTIFIER_FORMAT_RULE = "identifier-format"
        const val CONVENTION_PARAM = "convention"
        const val VALUE_PARAM = "value"
    }
}
