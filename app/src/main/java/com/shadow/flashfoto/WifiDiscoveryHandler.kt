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

class WifiDiscoveryHandler(private val context: Context, private val manager: PrinterManager) {

    fun start(onSuccess: () -> Unit) {
        val activity = context as? Activity ?: return

        // 1. Список необхідних дозволів залежно від версії Android
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.NEARBY_WIFI_DEVICES, Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        // 2. Перевірка, чи всі дозволи надані
        val missingPermissions = permissions.filter {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isNotEmpty()) {
            // Запитуємо дозволи
            ActivityCompat.requestPermissions(activity, missingPermissions.toTypedArray(), 1001)
            Toast.makeText(context, "Надайте дозволи і спробуйте знову", Toast.LENGTH_SHORT).show()
            return
        }

        // 3. Якщо дозволи є — починаємо пошук
        Toast.makeText(context, "Пошук принтерів...", Toast.LENGTH_SHORT).show()
        
        val wdManager = WifiDirectManager(context)
        wdManager.discoverPeers { peerList ->
            val devices = peerList.deviceList.toMutableList()
            
            if (devices.isEmpty()) {
                Toast.makeText(context, "Пристроїв не знайдено. Перевірте, чи увімкнено Wi-Fi Direct на принтері", Toast.LENGTH_LONG).show()
                return@discoverPeers
            }

            val deviceNames = devices.map { "${it.deviceName}\n${it.deviceAddress}" }.toTypedArray()

            AlertDialog.Builder(context)
                .setTitle("Знайдені принтери")
                .setItems(deviceNames) { _, which ->
                    val selected = devices[which]
                    Toast.makeText(context, "Підключення до ${selected.deviceName}...", Toast.LENGTH_SHORT).show()
                    
                    wdManager.connect(selected) {
                        wdManager.requestInfo { info ->
                            if (info.groupFormed) {
                                val ip = info.groupOwnerAddress.hostAddress
                                manager.addPrinter(selected.deviceName, ip, ConnectionType.WIFI_DIRECT)
                                manager.setActive(ip)
                                Toast.makeText(context, "Успішно підключено!", Toast.LENGTH_LONG).show()
                                onSuccess()
                            }
                        }
                    }
                }.show()
        }
    }
}
