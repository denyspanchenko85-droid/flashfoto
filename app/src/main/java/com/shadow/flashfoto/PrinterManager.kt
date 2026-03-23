package com.shadow.flashfoto

import android.content.Context

class PrinterManager(context: Context) {
    private val prefs = context.getSharedPreferences("printer_prefs", Context.MODE_PRIVATE)

    fun getPrinters(): MutableList<PrinterModel> {
        val rawSet = prefs.getStringSet("printer_list", emptySet()) ?: emptySet()
        val activeIp = prefs.getString("active_ip", "")
        
        return rawSet.map {
            val parts = it.split("|")
            PrinterModel(parts[0], parts[1], parts[1] == activeIp)
        }.toMutableList()
    }

    fun addPrinter(name: String, ip: String) {
        val printers = getPrinters().map { "${it.name}|${it.ip}" }.toMutableSet()
        printers.add("$name|$ip")
        prefs.edit().putStringSet("printer_list", printers).apply()
    }

    fun deletePrinter(printer: PrinterModel) {
        val printers = getPrinters()
        printers.removeAll { it.ip == printer.ip }
        val rawSet = printers.map { "${it.name}|${it.ip}" }.toSet()
        prefs.edit().putStringSet("printer_list", rawSet).apply()
    }

    fun setActive(ip: String) {
        prefs.edit().putString("active_ip", ip).apply()
    }

    fun getActiveIp(): String = prefs.getString("active_ip", "") ?: ""
}
