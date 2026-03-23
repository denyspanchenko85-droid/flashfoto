package com.shadow.flashfoto

import android.app.AlertDialog
import android.content.Context
import android.widget.Toast

class WifiDiscoveryHandler(private val context: Context, private val manager: PrinterManager) {

    fun start(onSuccess: () -> Unit) {
        val wdManager = WifiDirectManager(context)
        
        wdManager.discoverPeers { peerList ->
            val devices = peerList.deviceList.toMutableList()
            if (devices.isEmpty()) {
                Toast.makeText(context, "Пристроїв не знайдено", Toast.LENGTH_SHORT).show()
                return@discoverPeers
            }

            val deviceNames = devices.map { "${it.deviceName}\n${it.deviceAddress}" }.toTypedArray()

            AlertDialog.Builder(context)
                .setTitle("Виберіть принтер")
                .setItems(deviceNames) { _, which ->
                    val selected = devices[which]
                    wdManager.connect(selected) {
                        wdManager.requestInfo { info ->
                            if (info.groupFormed) {
                                val ip = info.groupOwnerAddress.hostAddress
                                manager.addPrinter(selected.deviceName, ip)
                                manager.setActive(ip)
                                Toast.makeText(context, "Підключено: $ip", Toast.LENGTH_LONG).show()
                                onSuccess()
                            }
                        }
                    }
                }.show()
        }
    }
}
