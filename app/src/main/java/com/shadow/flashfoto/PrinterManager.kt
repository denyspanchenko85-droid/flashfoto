// Responsibility: Persistent storage and management of printer list
package com.shadow.flashfoto

import android.content.Context

class PrinterManager(context: Context) {
    private val prefs = context.getSharedPreferences("printer_prefs", Context.MODE_PRIVATE)

    fun getPrinters(): MutableList<PrinterModel> {
        val rawSet = prefs.getStringSet("printer_list", emptySet()) ?: emptySet()
        val activeIp = prefs.getString("active_ip", "")
        
        return rawSet.map {
            val parts = it.split("|")
            // parts: 0=Name, 1=IP, 2=Type
            val type = if (parts.size > 2) ConnectionType.valueOf(parts[2]) else ConnectionType.IP
            PrinterModel(parts[0], parts[1], type, parts[1] == activeIp)
        }.toMutableList()
    }

    fun addPrinter(name: String, ip: String, type: ConnectionType = ConnectionType.IP) {
        val printers = getPrinters().map { "${it.name}|${it.ip}|${it.type}" }.toMutableSet()
        printers.add("$name|$ip|$type")
        prefs.edit().putStringSet("printer_list", printers).apply()
    }

    fun deletePrinter(printer: PrinterModel) {
        val printers = getPrinters()
        printers.removeAll { it.ip == printer.ip }
        val rawSet = printers.map { "${it.name}|${it.ip}|${it.type}" }.toSet()
        prefs.edit().putStringSet("printer_list", rawSet).apply()
    }

    fun setActive(ip: String) {
        prefs.edit().putString("active_ip", ip).apply()
    }

    fun getActiveIp(): String = prefs.getString("active_ip", "") ?: ""
}
