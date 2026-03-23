// Responsibility: Atomic data structure for printer entity
package com.shadow.flashfoto

enum class ConnectionType { IP, WIFI_DIRECT }

data class PrinterModel(
    val name: String,
    val ip: String,
    val type: ConnectionType = ConnectionType.IP,
    var isActive: Boolean = false
)
