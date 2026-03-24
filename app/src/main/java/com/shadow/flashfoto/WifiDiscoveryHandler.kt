// Responsibility: Universal Wi-Fi P2P discovery handler (Fixed start() signature)
package com.shadow.flashfoto

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.net.wifi.p2p.WifiP2pDevice

class WifiDiscoveryHandler(private val context: Context, private val printerManager: PrinterManager) {
    private val wdManager = WifiDirectManager(context)

    // Додано параметр за замовчуванням, щоб виправити помилку в PrinterDialogHandler
    fun start(onSuccess: (() -> Unit)? = null) {
        val activity = context as? Activity ?: return
        val permissions = mutableListOf<String>()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.NEARBY_WIFI_DEVICES)
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        val missing = permissions.filter {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missing.isNotEmpty()) {
            ActivityCompat.requestPermissions(activity, missing.toTypedArray(), 1001)
            return
        }

        // Викликаємо без лісенера, бо список прийде в Receiver
        wdManager.discoverPeers(null)
    }

    fun showPeerDialog(devices: List<WifiP2pDevice>, onSuccess: () -> Unit) {
        if (devices.isEmpty()) return
        val deviceNames = devices.map { "${it.deviceName}\n${it.deviceAddress}" }.toTypedArray()

        AlertDialog.Builder(context)
            .setTitle("Виберіть принтер")
            .setItems(deviceNames) { _, which ->
                val selected = devices[which]
                wdManager.connect(selected) {
                    wdManager.requestInfo { info ->
                        if (info.groupFormed) {
                            val ip = info.groupOwnerAddress?.hostAddress ?: ""
                            printerManager.addPrinter(selected.deviceName, ip, ConnectionType.WIFI_DIRECT)
                            printerManager.setActive(ip)
                            onSuccess()
                        }
                    }
                }
            }.show()
    }
}
