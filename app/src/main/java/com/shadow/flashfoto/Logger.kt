package com.shadow.flashfoto

import android.content.Context
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object Logger {
    private const val LOG_FILE = "flash_log.txt" // Змінимо назву на загальну

    fun log(context: Context, message: String, e: Throwable? = null) {
        val timeStamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val type = if (e == null) "INFO" else "ERROR"
        val logEntry = "[$timeStamp] $type: $message ${e?.message ?: ""}\n"
        
        try {
            val file = File(context.getExternalFilesDir(null), LOG_FILE)
            file.appendText(logEntry)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }
}
