package com.shadow.flashfoto

import android.content.Context
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object Logger {
    private const val LOG_FILE = "error_log.txt"

    fun log(context: Context, message: String, e: Throwable? = null) {
        val timeStamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val logEntry = "[$timeStamp] ERROR: $message | ${e?.localizedMessage}\n"
        
        try {
            val file = File(context.getExternalFilesDir(null), LOG_FILE)
            file.appendText(logEntry)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }
}
