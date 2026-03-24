// Responsibility: Managed Wi-Fi Direct operations with public access for LifecycleHelper
package com.shadow.flashfoto

import android.annotation.SuppressLint
import android.content.Context
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import android.widget.Toast

class WifiDirectManager(private val context: Context) {
    // Змінено на публічні val, щоб прибрати помилку в LifecycleHelper
    val manager: WifiP2pManager? = context.getSystemService(Context.WIFI_P2P_SERVICE) as? WifiP2pManager
    val channel: WifiP2pManager.Channel? = manager?.initialize(context, context.mainLooper, null)

    @SuppressLint("MissingPermission")
    fun discoverPeers(listener: WifiP2pManager.PeerListListener? = null) {
        manager?.discoverPeers(channel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                // Якщо передали лісенер — запитуємо список відразу
                listener?.let { manager?.requestPeers(channel, it) }
            }
            override fun onFailure(reason: Int) {
                Logger.log(context, "Discovery Failed: $reason")
            }
        })
    }

    @SuppressLint("MissingPermission")
    fun connect(device: WifiP2pDevice, onSuccess: () -> Unit) {
        val config = WifiP2pConfig().apply { deviceAddress = device.deviceAddress }
        manager?.connect(channel, config, object : WifiP2pManager.ActionListener {
            override fun onSuccess() = onSuccess()
            override fun onFailure(reason: Int) {
                Toast.makeText(context, "Помилка з'єднання: $reason", Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun requestInfo(listener: WifiP2pManager.ConnectionInfoListener) {
        manager?.requestConnectionInfo(channel, listener)
    }
}
