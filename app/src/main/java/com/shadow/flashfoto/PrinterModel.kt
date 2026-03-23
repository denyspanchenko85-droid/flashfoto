// Responsibility: Atomic data structure with connection type support
package com.shadow.flashfoto

enum class ConnectionType { IP, WIFI_DIRECT }

data class PrinterModel(
    val name: String,
    val address: String, // IP або MAC-адреса
    val type: ConnectionType = ConnectionType.IP,
    var isActive: Boolean = false
)
