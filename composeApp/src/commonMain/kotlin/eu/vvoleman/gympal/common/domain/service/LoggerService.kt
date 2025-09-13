package eu.vvoleman.gympal.common.domain.service

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import kotlinx.serialization.json.Json

class LoggerService : LoggerInterface {

    override fun debug(message: String, context: Map<String, String?>?) {
        this.log(level = LogLevel.DEBUG, message = message, context = context)
    }

    override fun info(message: String, context: Map<String, String?>?) {
        this.log(level = LogLevel.INFO, message = message, context = context)
    }

    override fun warn(message: String, context: Map<String, String?>?) {
        this.log(level = LogLevel.WARN, message = message, context = context)
    }

    override fun error(message: String, context: Map<String, String?>?) {
        this.log(level = LogLevel.ERROR, message = message, context = context)
    }

    override fun performance(message: String, context: Map<String, String?>?) {
        this.log(level = LogLevel.PERFORMANCE, message = message, context = context)
    }

    override fun log(
        level: LogLevel,
        message: String,
        context: Map<String, String?>?,
    ) {
        val severity = when (level) {
            LogLevel.DEBUG -> Severity.Debug
            LogLevel.INFO -> Severity.Info
            LogLevel.WARN -> Severity.Warn
            LogLevel.ERROR -> Severity.Error
            LogLevel.PERFORMANCE -> Severity.Info
        }


        val fullMessage = if (context != null) {
            "$message | context: ${formatContext(context)}"
        } else {
            message
        }

        Logger.log(
            severity = severity,
            tag = TAG,
            message = fullMessage,
            throwable = null,
        )
    }

    private fun formatContext(context: Map<String, String?>): String {
        return Json.encodeToString(context)
    }

    companion object {
        const val TAG = "GymPal"
    }
}