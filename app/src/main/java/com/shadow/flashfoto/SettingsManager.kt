package com.shadow.flashfoto

import android.content.Context

class SettingsManager(context: Context) {
    private val prefs = context.getSharedPreferences("flash_settings", Context.MODE_PRIVATE)

    var isAutoPrintEnabled: Boolean
        get() = prefs.getBoolean("auto_print", true)
        set(value) = prefs.edit().putBoolean("auto_print", value).apply()

    var isColorEnabled: Boolean
        get() = prefs.getBoolean("print_color", true)
        set(value) = prefs.edit().putBoolean("print_color", value).apply()

    var printerIp: String?
        get() = prefs.getString("printer_ip", "")
        set(value) = prefs.edit().putString("printer_ip", value).apply()
}
