package com.shadow.flashfoto

import android.content.Context
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object Logger {
    fun log(context: Context, message: String, e: Throwable? = null) {
        val timeStamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val logEntry = "[$timeStamp] $message | ${e?.message}\n"
        try {
            val file = File(context.getExternalFilesDir(null), "error_log.txt")
            file.appendText(logEntry)
        } catch (ex: Exception) { ex.printStackTrace() }
    }
}
