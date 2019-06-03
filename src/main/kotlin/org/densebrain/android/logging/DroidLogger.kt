@file:Suppress("unused", "NOTHING_TO_INLINE", "NAME_SHADOWING", "FunctionName", "TestFunctionName")
package org.densebrain.android.logging

import android.util.Log

fun emitLogRecord(
  level: DroidLogRecord.LogLevel,
  tag:String,
  message: String,
  stackTrace: String? = null
) {
    DroidLogRecord.emit(DroidLogRecord(level, tag, message, stackTrace))
}

interface DroidLogger {
    /**
     * The logger tag used in extension functions for the [DroidLogger].
     * Note that the tag length should not be more than 23 symbols.
     */
    val loggerTag: String
        get() = getTag(javaClass)
}

fun DroidLogger(clazz: Class<*>): DroidLogger = object : DroidLogger {
    override val loggerTag = getTag(clazz)
}

fun DroidLogger(tag: String): DroidLogger = object : DroidLogger {
    init {
        assert(tag.length <= 23) { "The maximum tag length is 23, got $tag" }
    }
    override val loggerTag = tag
}

inline fun <reified T: Any> DroidLogger(): DroidLogger = DroidLogger(T::class.java)

/**
 * Send a log message with the [Log.VERBOSE] severity.
 * Note that the log message will not be written if the current log level is above [Log.VERBOSE].
 * The default log level is [Log.INFO].
 *
 * @param message the message text to log. `null` value will be represent as "null", for any other value
 *   the [Any.toString] will be invoked.
 * @param thr an exception to log (optional).
 *
 * @see [Log.v].
 */
fun DroidLogger.verbose(message: Any?, thr: Throwable? = null) {
    if (Log.isLoggable(loggerTag, Log.VERBOSE)) {
        log(this, message, thr, Log.VERBOSE,
          { tag, msg -> Log.v(tag, msg) },
          { tag, msg, thr -> Log.v(tag, msg, thr) })
    }
}

/**
 * Send a log message with the [Log.DEBUG] severity.
 * Note that the log message will not be written if the current log level is above [Log.DEBUG].
 * The default log level is [Log.INFO].
 *
 * @param message the message text to log. `null` value will be represent as "null", for any other value
 *   the [Any.toString] will be invoked.
 * @param thr an exception to log (optional).
 *
 * @see [Log.d].
 */
fun DroidLogger.debug(message: Any?, thr: Throwable? = null) {
    if (Log.isLoggable(loggerTag, Log.DEBUG)) {
        log(this, message, thr, Log.DEBUG,
          { tag, msg -> Log.d(tag, msg) },
          { tag, msg, thr -> Log.d(tag, msg, thr) })
    }
}

/**
 * Send a log message with the [Log.INFO] severity.
 * Note that the log message will not be written if the current log level is above [Log.INFO]
 *   (it is the default level).
 *
 * @param message the message text to log. `null` value will be represent as "null", for any other value
 *   the [Any.toString] will be invoked.
 * @param thr an exception to log (optional).
 *
 * @see [Log.i].
 */
fun DroidLogger.info(message: Any?, thr: Throwable? = null) {
    log(this, message, thr, Log.INFO,
      { tag, msg -> Log.i(tag, msg) },
      { tag, msg, thr -> Log.i(tag, msg, thr) })
}

/**
 * Send a log message with the [Log.WARN] severity.
 * Note that the log message will not be written if the current log level is above [Log.WARN].
 * The default log level is [Log.INFO].
 *
 * @param message the message text to log. `null` value will be represent as "null", for any other value
 *   the [Any.toString] will be invoked.
 * @param thr an exception to log (optional).
 *
 * @see [Log.w].
 */
fun DroidLogger.warn(message: Any?, thr: Throwable? = null) {
    log(this, message, thr, Log.WARN,
      { tag, msg -> Log.w(tag, msg) },
      { tag, msg, thr -> Log.w(tag, msg, thr) })
}

/**
 * Send a log message with the [Log.ERROR] severity.
 * Note that the log message will not be written if the current log level is above [Log.ERROR].
 * The default log level is [Log.INFO].
 *
 * @param message the message text to log. `null` value will be represent as "null", for any other value
 *   the [Any.toString] will be invoked.
 * @param thr an exception to log (optional).
 *
 * @see [Log.e].
 */
fun DroidLogger.error(message: Any?, thr: Throwable? = null) {
    log(this, message, thr, Log.ERROR,
      { tag, msg -> Log.e(tag, msg) },
      { tag, msg, thr -> Log.e(tag, msg, thr) })
}

/**
 * Send a log message with the "What a Terrible Failure" severity.
 * Report an exception that should never happen.
 *
 * @param message the message text to log. `null` value will be represent as "null", for any other value
 *   the [Any.toString] will be invoked.
 * @param thr an exception to log (optional).
 *
 * @see [Log.wtf].
 */
fun DroidLogger.wtf(message: Any?, thr: Throwable? = null) {
    val message = message?.toString() ?:"null"
    emitLogRecord(DroidLogRecord.LogLevel.WTF,loggerTag,message, thr?.getStackTraceString())
    if (thr != null) {
        Log.wtf(loggerTag, message, thr)
    } else {
        Log.wtf(loggerTag, message)
    }
}

/**
 * Send a log message with the [Log.VERBOSE] severity.
 * Note that the log message will not be written if the current log level is above [Log.VERBOSE].
 * The default log level is [Log.INFO].
 *
 * @param message the function that returns message text to log.
 *   `null` value will be represent as "null", for any other value the [Any.toString] will be invoked.
 *
 * @see [Log.v].
 */
inline fun DroidLogger.verbose(message: () -> Any?) {
    val tag = loggerTag
    if (Log.isLoggable(tag, Log.VERBOSE)) {
        val message = message()?.toString() ?: "null"
        emitLogRecord(DroidLogRecord.LogLevel.VERBOSE,loggerTag,message)
        Log.v(tag, message)
    }
}

/**
 * Send a log message with the [Log.DEBUG] severity.
 * Note that the log message will not be written if the current log level is above [Log.DEBUG].
 * The default log level is [Log.INFO].
 *
 * @param message the function that returns message text to log.
 *   `null` value will be represent as "null", for any other value the [Any.toString] will be invoked.
 *
 * @see [Log.d].
 */
inline fun DroidLogger.debug(message: () -> Any?) {
    val tag = loggerTag
    if (Log.isLoggable(tag, Log.DEBUG)) {
        val message = message()?.toString() ?: "null"
        emitLogRecord(DroidLogRecord.LogLevel.DEBUG,loggerTag,message)
        Log.d(tag, message)
    }
}

/**
 * Send a log message with the [Log.INFO] severity.
 * Note that the log message will not be written if the current log level is above [Log.INFO].
 * The default log level is [Log.INFO].
 *
 * @param message the function that returns message text to log.
 *   `null` value will be represent as "null", for any other value the [Any.toString] will be invoked.
 *
 * @see [Log.i].
 */
inline fun DroidLogger.info(message: () -> Any?) {
    val tag = loggerTag
    val message = message()?.toString() ?: "null"


    if (Log.isLoggable(tag, Log.INFO)) {
        emitLogRecord(DroidLogRecord.LogLevel.INFO,loggerTag,message)
        Log.i(tag, message)
    }
}

/**
 * Send a log message with the [Log.WARN] severity.
 * Note that the log message will not be written if the current log level is above [Log.WARN].
 * The default log level is [Log.INFO].
 *
 * @param message the function that returns message text to log.
 *   `null` value will be represent as "null", for any other value the [Any.toString] will be invoked.
 *
 * @see [Log.w].
 */
inline fun DroidLogger.warn(message: () -> Any?) {
    val tag = loggerTag
    val message = message()?.toString() ?: "null"
    emitLogRecord(DroidLogRecord.LogLevel.WARN,loggerTag,message)
    if (Log.isLoggable(tag, Log.WARN)) {
        Log.w(tag, message)
    }
}

/**
 * Send a log message with the [Log.ERROR] severity.
 * Note that the log message will not be written if the current log level is above [Log.ERROR].
 * The default log level is [Log.INFO].
 *
 * @param message the function that returns message text to log.
 *   `null` value will be represent as "null", for any other value the [Any.toString] will be invoked.
 *
 * @see [Log.e].
 */
inline fun DroidLogger.error(message: () -> Any?) {
    val tag = loggerTag
    val message = message()?.toString() ?: "null"
    emitLogRecord(DroidLogRecord.LogLevel.ERROR,loggerTag,message)
    if (Log.isLoggable(tag, Log.ERROR)) {
        Log.e(tag, message)
    }
}

/**
 * Return the stack trace [String] of a throwable.
 */
inline fun Throwable.getStackTraceString(): String = Log.getStackTraceString(this)

private inline fun log(
  logger: DroidLogger,
  message: Any?,
  thr: Throwable?,
  level: Int,
  f: (String, String) -> Unit,
  fThrowable: (String, String, Throwable) -> Unit) {
    val tag = logger.loggerTag
    val message = message?.toString() ?: "null"
    try {
        if (Log.isLoggable(tag,level) || level >= Log.INFO) {
            emitLogRecord(
              DroidLogRecord.LogLevel.values().find { it.android == level } ?: DroidLogRecord.LogLevel.WTF,
              tag,
              message,
              thr?.getStackTraceString()
            )
        }
    } catch (ex:Exception) {
        Log.e("DroidLogging","Emit log record failed", ex)
    }

    if (Log.isLoggable(tag, level)) {
        try {
            if (thr != null) {
                fThrowable(tag, message, thr)
            } else {
                f(tag, message)
            }
        } catch (ex: Exception) {
            Log.e(tag, "Regular logging failed", ex)
        }
    }
}

private fun getTag(clazz: Class<*>): String {
    val tag = clazz.simpleName
    return if (tag.length <= 23) {
        tag
    } else {
        tag.substring(0, 23)
    }
}
