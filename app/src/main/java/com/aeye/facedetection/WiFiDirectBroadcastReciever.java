package com.aeye.facedetection;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;

/**
 * Önemli WiFi olaylarını yayınlayan sınıf
 * https://developer.android.com/guide/topics/connectivity/wifip2p.html#create-br
 */
public class WiFiDirectBroadcastReciever extends BroadcastReceiver {
    WifiP2pManager manager;
    Channel channel;
    WifiActivity wifiActivity;

    public WiFiDirectBroadcastReciever(WifiP2pManager manager, Channel channel, WifiActivity wifiActivity) {
        super();

        this.manager = manager;
        this.channel = channel;
        this.wifiActivity = wifiActivity;
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        // TODO
        if (action != null) {
            switch (action) {
                case WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION:
                    break;
                case WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION:
                    break;
                case WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION:
                    break;
                case WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION:
                    break;
            }
        }

    }
}
