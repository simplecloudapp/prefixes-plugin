package app.simplecloud.prefixes.shared.platform

/**
 * A basic logger interface to allow for platform logger implementations.
 */
interface PrefixesLogger {

    /**
     * Logs an info message.
     *
     * @param msg the message to log
     */
    fun info(msg: String)

    /**
     * Logs a warning message.
     *
     * @param msg the message to log
     */
    fun warn(msg: String)

    /**
     * Logs an error message.
     *
     * @param msg the message to log
     * @param cause the throwable that caused the error
     */
    fun error(msg: String, cause: Throwable)

}