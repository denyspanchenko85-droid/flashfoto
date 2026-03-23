package com.shadow.flashfoto

import android.app.Activity
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File

object ShareManager {
    fun share(activity: Activity, file: File?) {
        if (file == null || !file.exists()) return

        val uri = FileProvider.getUriForFile(
            activity,
            "${activity.packageName}.provider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/jpeg"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        activity.startActivity(Intent.createChooser(intent, "Поділитися фото"))
    }
}
