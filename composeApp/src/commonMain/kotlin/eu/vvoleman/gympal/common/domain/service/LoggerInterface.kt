package eu.vvoleman.gympal.common.domain.service

interface LoggerInterface {
    fun debug(message: String, context: Map<String, String?>? = null)
    fun info(message: String, context: Map<String, String?>? = null)
    fun warn(message: String, context: Map<String, String?>? = null)
    fun error(message: String, context: Map<String, String?>? = null)
    fun performance(
        message: String,
        context: Map<String, String?>? = null
    )

    fun log(
        level: LogLevel,
        message: String,
        context: Map<String, String?>? = null
    )
}

enum class LogLevel {
    DEBUG,
    INFO,
    WARN,
    ERROR,
    PERFORMANCE
}