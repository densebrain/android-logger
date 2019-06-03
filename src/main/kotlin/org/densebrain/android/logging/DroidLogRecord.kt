package org.densebrain.android.logging

import android.util.Log
import java.text.SimpleDateFormat
import java.util.*


data class DroidLogRecord(
  val level:LogLevel,
  val tag:String,
  val message: String,
  val stackTrace: String? = null,
  val timestamp: Long = System.currentTimeMillis(),
  val date: String = formatter.format(Date(timestamp))
) {

  enum class LogLevel(val android: Int) {
    VERBOSE(Log.VERBOSE),
    DEBUG(Log.DEBUG),
    INFO(Log.INFO),
    WARN(Log.WARN),
    ERROR(Log.ERROR),
    WTF(-1)
  }


  //fun toJson() = gson.toJson(this)!!

  companion object : Event<DroidLogRecord>() {
    private val formatter = SimpleDateFormat("YYYY-MM-dd HH:mm:ss.SSS",Locale.US).apply {
      timeZone = TimeZone.getTimeZone("UTC")
    }
  }
}
