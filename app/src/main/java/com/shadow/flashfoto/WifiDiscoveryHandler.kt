// Responsibility: Universal Wi-Fi P2P discovery handler (Android 10 to 14+)
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

        // 1. Формуємо список дозволів залежно від версії
        val permissions = mutableListOf<String>()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13+
            permissions.add(Manifest.permission.NEARBY_WIFI_DEVICES)
            // На нових Android локація потрібна тільки якщо ми хочемо знати координати, 
            // але для стабільності P2P краще запитати і її.
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            // Android 10, 11, 12
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        // 2. Перевірка відсутніх дозволів
        val missing = permissions.filter {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missing.isNotEmpty()) {
            ActivityCompat.requestPermissions(activity, missing.toTypedArray(), 1001)
            Toast.makeText(context, "Надайте дозволи для роботи з Wi-Fi", Toast.LENGTH_SHORT).show()
            return
        }

        // 3. Запуск сканування
        Toast.makeText(context, "Шукаю пристрої...", Toast.LENGTH_SHORT).show()
        
        val wdManager = WifiDirectManager(context)
        wdManager.discoverPeers { peerList ->
            val devices = peerList.deviceList.toMutableList()
            
            if (devices.isEmpty()) {
                val msg = if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.R) {
                    "Пристроїв не знайдено. Перевірте, чи увімкнено GPS та Wi-Fi Direct!"
                } else {
                    "Пристроїв не знайдено. Перевірте налаштування принтера."
                }
                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                return@discoverPeers
            }

            val deviceNames = devices.map { "${it.deviceName}\n${it.deviceAddress}" }.toTypedArray()

            AlertDialog.Builder(context)
                .setTitle("Виберіть принтер")
                .setItems(deviceNames) { _, which ->
                    val selected = devices[which]
                    Toast.makeText(context, "З'єднуюсь з ${selected.deviceName}...", Toast.LENGTH_SHORT).show()
                    
                    wdManager.connect(selected) {
                        wdManager.requestInfo { info ->
                            if (info.groupFormed) {
                                val ip = info.groupOwnerAddress.hostAddress
                                manager.addPrinter(selected.deviceName, ip, ConnectionType.WIFI_DIRECT)
                                manager.setActive(ip)
                                Toast.makeText(context, "Готово! IP: $ip", Toast.LENGTH_LONG).show()
                                onSuccess()
                            }
                        }
                    }
                }.show()
        }
    }
}
