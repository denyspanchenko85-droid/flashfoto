package com.shadow.flashfoto

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.wifi.p2p.WifiP2pManager

class WifiDirectReceiver(
    private val manager: WifiP2pManager?,
    private val channel: WifiP2pManager.Channel?,
    private val onPeersChanged: () -> Unit,
    private val onConnectionChanged: () -> Unit
) : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> onPeersChanged()
            WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> onConnectionChanged()
            WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION -> {
                val state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1)
                if (state != WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                    // Wi-Fi Direct вимкнено
                }
            }
        }
    }
}
