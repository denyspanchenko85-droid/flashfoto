// Responsibility: Wi-Fi P2P Discovery UI and Permissions
package com.shadow.flashfoto

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.net.wifi.p2p.WifiP2pDevice

class WifiDiscoveryHandler(private val context: Context, private val printerManager: PrinterManager) {
    private val wdManager = WifiDirectManager(context)

    fun start() {
        val activity = context as? Activity ?: return
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.NEARBY_WIFI_DEVICES, Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        if (permissions.any { ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED }) {
            ActivityCompat.requestPermissions(activity, permissions, 1001)
            return
        }
        wdManager.discoverPeers()
    }

    fun showPeerDialog(devices: List<WifiP2pDevice>, onSuccess: () -> Unit) {
        val names = devices.map { it.deviceName }.toTypedArray()
        AlertDialog.Builder(context)
            .setTitle("Виберіть принтер")
            .setItems(names) { _, i ->
                wdManager.connect(devices[i]) {
                    wdManager.requestInfo { info ->
                        if (info.groupFormed) {
                            val ip = info.groupOwnerAddress?.hostAddress ?: ""
                            printerManager.addPrinter(devices[i].deviceName, ip, ConnectionType.WIFI_DIRECT)
                            printerManager.setActive(ip)
                            onSuccess()
                        }
                    }
                }
            }.show()
    }
}
