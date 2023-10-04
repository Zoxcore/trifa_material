package com.zoffcc.applications.trifa

import java.io.PrintWriter
import java.io.StringWriter
import java.net.UnknownHostException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Mock Log implementation for testing on non android host.
 */
object Log {
    /**
     * Priority constant for the println method; use Log.v.
     */
    const val VERBOSE = 2

    /**
     * Priority constant for the println method; use Log.d.
     */
    const val DEBUG = 3

    /**
     * Priority constant for the println method; use Log.i.
     */
    const val INFO = 4

    /**
     * Priority constant for the println method; use Log.w.
     */
    const val WARN = 5

    /**
     * Priority constant for the println method; use Log.e.
     */
    const val ERROR = 6

    /**
     * Priority constant for the println method.
     */
    const val ASSERT = 7

    /**
     * Dummy, does nothing
     *
     * @param tag
     * @param msg
     * @return
     */
    fun D(tag: String?, msg: String?): Int {
        return 0
    }

    /**
     * Send a [.VERBOSE] log message.
     *
     * @param tag Used to identify the source of a log message.  It usually identifies
     * the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    fun v(tag: String, msg: String): Int {
        return println(LOG_ID_MAIN, VERBOSE, tag, msg)
    }

    /**
     * Send a [.VERBOSE] log message and log the exception.
     *
     * @param tag Used to identify the source of a log message.  It usually identifies
     * the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr  An exception to log
     */
    fun v(tag: String, msg: String, tr: Throwable?): Int {
        return println(
            LOG_ID_MAIN, VERBOSE, tag, """
     $msg
     ${getStackTraceString(tr)}
     """.trimIndent()
        )
    }

    /**
     * Send a [.DEBUG] log message.
     *
     * @param tag Used to identify the source of a log message.  It usually identifies
     * the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    fun d(tag: String, msg: String): Int {
        return println(LOG_ID_MAIN, DEBUG, tag, msg)
    }

    /**
     * Send a [.DEBUG] log message and log the exception.
     *
     * @param tag Used to identify the source of a log message.  It usually identifies
     * the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr  An exception to log
     */
    fun d(tag: String, msg: String, tr: Throwable?): Int {
        return println(
            LOG_ID_MAIN, DEBUG, tag, """
     $msg
     ${getStackTraceString(tr)}
     """.trimIndent()
        )
    }

    /**
     * Send an [.INFO] log message.
     *
     * @param tag Used to identify the source of a log message.  It usually identifies
     * the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    @JvmStatic
    fun i(tag: String, msg: String): Int {
        return println(LOG_ID_MAIN, INFO, tag, msg)
    }

    /**
     * Send a [.INFO] log message and log the exception.
     *
     * @param tag Used to identify the source of a log message.  It usually identifies
     * the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr  An exception to log
     */
    fun i(tag: String, msg: String, tr: Throwable?): Int {
        return println(
            LOG_ID_MAIN, INFO, tag, """
     $msg
     ${getStackTraceString(tr)}
     """.trimIndent()
        )
    }

    /**
     * Send a [.WARN] log message.
     *
     * @param tag Used to identify the source of a log message.  It usually identifies
     * the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    fun w(tag: String, msg: String): Int {
        return println(LOG_ID_MAIN, WARN, tag, msg)
    }

    /**
     * Send a [.WARN] log message and log the exception.
     *
     * @param tag Used to identify the source of a log message.  It usually identifies
     * the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr  An exception to log
     */
    fun w(tag: String, msg: String, tr: Throwable?): Int {
        return println(
            LOG_ID_MAIN, WARN, tag, """
     $msg
     ${getStackTraceString(tr)}
     """.trimIndent()
        )
    }

    /*
     * Send a {@link #WARN} log message and log the exception.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param tr An exception to log
     */
    fun w(tag: String, tr: Throwable?): Int {
        return println(LOG_ID_MAIN, WARN, tag, getStackTraceString(tr))
    }

    /**
     * Send an [.ERROR] log message.
     *
     * @param tag Used to identify the source of a log message.  It usually identifies
     * the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    fun e(tag: String, msg: String): Int {
        return println(LOG_ID_MAIN, ERROR, tag, msg)
    }

    /**
     * Send a [.ERROR] log message and log the exception.
     *
     * @param tag Used to identify the source of a log message.  It usually identifies
     * the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr  An exception to log
     */
    fun e(tag: String, msg: String, tr: Throwable?): Int {
        return println(
            LOG_ID_MAIN, ERROR, tag, """
     $msg
     ${getStackTraceString(tr)}
     """.trimIndent()
        )
    }

    /**
     * Handy function to get a loggable stack trace from a Throwable
     *
     * @param tr An exception to log
     */
    fun getStackTraceString(tr: Throwable?): String {
        if (tr == null) {
            return ""
        }

        // This is to reduce the amount of log spew that apps do in the non-error
        // condition of the network being unavailable.
        var t = tr
        while (t != null) {
            if (t is UnknownHostException) {
                return ""
            }
            t = t.cause
        }
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        tr.printStackTrace(pw)
        pw.flush()
        return sw.toString()
    }

    /**
     * Low-level logging call.
     *
     * @param priority The priority/type of this log message
     * @param tag      Used to identify the source of a log message.  It usually identifies
     * the class or activity where the log call occurs.
     * @param msg      The message you would like logged.
     * @return The number of bytes written.
     */
    fun println(priority: Int, tag: String, msg: String): Int {
        return println(LOG_ID_MAIN, priority, tag, msg)
    }

    /**
     * @hide
     */
    const val LOG_ID_MAIN = 0

    /**
     * @hide
     */
    const val LOG_ID_RADIO = 1

    /**
     * @hide
     */
    const val LOG_ID_EVENTS = 2

    /**
     * @hide
     */
    const val LOG_ID_SYSTEM = 3

    /**
     * @hide
     */
    const val LOG_ID_CRASH = 4
    fun println(bufID: Int, priority: Int, tag: String, msg: String): Int {
        val threadid = Thread.currentThread().id
        val sdf = SimpleDateFormat("yyyy-MM-dd_HH:mm:ss.SSS")
        val now = Date()
        val datetime = sdf.format(now)
        println("$datetime:$threadid:$priority:$tag:$msg")
        return 0
    }
}
