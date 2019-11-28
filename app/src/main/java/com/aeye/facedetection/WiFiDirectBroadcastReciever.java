package com.aeye.facedetection;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.aeye.facedetection.WifiActivity.TAG;

/**
 * Önemli WiFi olaylarını yayınlayan sınıf
 * https://developer.android.com/guide/topics/connectivity/wifip2p.html#create-br
 */
public class WiFiDirectBroadcastReciever extends BroadcastReceiver implements WifiP2pManager.PeerListListener {

    private static final String DEVICE_PATTERN = "YEDHRAB-PC";

    WifiP2pManager manager;
    Channel channel;
    WifiActivity wifiActivity;

    /**
     * Eşleşilen cihazların bilgileri
     */
    private List<WifiP2pDevice> peers = new ArrayList<>();

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
                // Wi-Fi P2P'nin aktif olup olmadığını anlama
                case WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION:
                    Log.d(TAG, "onReceive: Wi-Fi P2P durumu değişti");

                    int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
                    if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                        Log.d(TAG, "onReceive: Wi-Fi P2P aktif");

                    } else {
                        Log.d(TAG, "onReceive: Wi-Fi P2P aktif değil");
                    }
                    break;
                // Call WifiP2pManager.requestPeers() to get a list of current peers
                case WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION:
                    Log.d(TAG, "onReceive: Wi-Fi P2P Eşlenebilir cihazların listesi değişti");

                    if (manager != null) {
                        Log.d(TAG, "onReceive: Eşleşme isteğinde bulunuldu");
                        manager.requestPeers(channel, this);
                    }
                    break;
                // Respond to new connection or disconnections
                case WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION:
                    Log.d(TAG, "onReceive: WiFi P2P bağlantısının durumu değişti");
                    break;
                // Respond to this device's wifi state changing
                case WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION:
                    Log.d(TAG, "onReceive: Wi-Fi P2P cihaz eylemleri değişti");
                    break;
            }
        }
    }

    @Override
    public void onPeersAvailable(WifiP2pDeviceList peerList) {
        Collection<WifiP2pDevice> refreshedPeers = peerList.getDeviceList();
        if (!refreshedPeers.equals(peers)) {
            peers.clear();
            peers.addAll(refreshedPeers);
        }

        // TODO: Buraya seçim arayüzü eklenmeli
        // ((WiFiPeerListAdapter) getListAdapter()).notifyDataSetChanged();

        if (peers.isEmpty()) {
            Log.d(TAG, "onPeersAvailable: Eşleşebilecek cihaz bulunamadı");
        } else {
            WifiP2pDevice device = null;
            for (WifiP2pDevice peer : peers) {
                Log.d(TAG, "onPeersAvailable: Cihaz adı: " + peer.deviceName);
                if (peer.deviceName.contains(DEVICE_PATTERN)) {
                    device = peer;
                }
            }

            if (device != null) {
                WifiP2pConfig config = new WifiP2pConfig();
                config.deviceAddress = device.deviceAddress;
                config.wps.setup = WpsInfo.PBC;

                // İlk cihaza bağlanma
                manager.connect(channel, config, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "onSuccess: Wi-Fi P2P bağlantısı başarılı");
                    }

                    @Override
                    public void onFailure(int reason) {
                        String reasonMsg = "";
                        switch (reason) {
                            case WifiP2pManager.P2P_UNSUPPORTED:
                                reasonMsg = "P2P desteklenmiyor";
                                break;
                            case WifiP2pManager.ERROR:
                                reasonMsg = "hata oluştu";
                                break;
                            case WifiP2pManager.BUSY:
                                reasonMsg = "cihaz başka bir bağlantı ile meşgul";
                                break;
                        };

                        Log.e(TAG, "onFailure: Wi-Fi P2P bağlantısı başarısız, " + reasonMsg);
                    }
                });
            }
        }

    }
}
