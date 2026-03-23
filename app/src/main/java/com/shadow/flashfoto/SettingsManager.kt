package com.shadow.flashfoto

import android.content.Context

class SettingsManager(context: Context) {
    private val prefs = context.getSharedPreferences("flash_settings", Context.MODE_PRIVATE)

    var isAutoPrintEnabled: Boolean
        get() = prefs.getBoolean("auto_print", true)
        set(value) = prefs.edit().putBoolean("auto_print", value).apply()

    var isKeepOriginalEnabled: Boolean
        get() = prefs.getBoolean("keep_original", true)
        set(value) = prefs.edit().putBoolean("keep_original", value).apply()

    var printerIp: String?
        get() = prefs.getString("printer_ip", "")
        set(value) = prefs.edit().putString("printer_ip", value).apply()

    var customTemplatePath: String?
        get() = prefs.getString("custom_template_path", null)
        set(value) = prefs.edit().putString("custom_template_path", value).apply()

    // НОВИЙ РЕЖИМ: false - Звичайний (Історія), true - Конструктор (2 шари)
    var isDualLayerMode: Boolean
        get() = prefs.getBoolean("dual_layer_mode", false)
        set(value) = prefs.edit().putBoolean("dual_layer_mode", value).apply()
}
