package com.shadow.flashfoto

import android.annotation.SuppressLint
import android.content.Context
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager

class WifiDirectManager(private val context: Context) {
    val manager: WifiP2pManager? = context.getSystemService(Context.WIFI_P2P_SERVICE) as? WifiP2pManager
    val channel: WifiP2pManager.Channel? = manager?.initialize(context, context.mainLooper, null)

    @SuppressLint("MissingPermission")
    fun discoverPeers(onPeersReady: (List<WifiP2pDevice>) -> Unit) {
        manager?.discoverPeers(channel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                manager?.requestPeers(channel) { peers -> 
                    onPeersReady(peers.deviceList.toList()) 
                }
            }
            override fun onFailure(reason: Int) {
                Logger.log(context, "Discovery Failed: $reason")
            }
        })
    }

    @SuppressLint("MissingPermission")
    fun connect(device: WifiP2pDevice, onSuccess: () -> Unit) {
        // Логіка підключення до пристрою
    }

    fun requestInfo(listener: WifiP2pManager.ConnectionInfoListener) {
        manager?.requestConnectionInfo(channel, listener)
    }
}
