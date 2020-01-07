package com.aeye.android;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Önemli WiFi olaylarını yayınlayan sınıf
 * https://developer.android.com/guide/topics/connectivity/wifip2p.html#create-br
 */
public class WiFiDirectBroadcastReciever extends BroadcastReceiver implements WifiP2pManager.PeerListListener {

    public static final String TAG = WiFiDirectBroadcastReciever.class.getSimpleName();
    public static final int reconnect = 1;
    private static final String DEVICE_PATTERN = "HUAWEI P20 lite";

    WifiP2pManager manager;
    Channel channel;
    WiFiDirectActivity wifiDirectActivity;

    /**
     * Eşleşilen cihazların bilgileri
     */
    private List<WifiP2pDevice> peers = new ArrayList<>();

    public WiFiDirectBroadcastReciever(WifiP2pManager manager, Channel channel, WiFiDirectActivity wifiDirectActivity) {
        super();

        this.manager = manager;
        this.channel = channel;
        this.wifiDirectActivity = wifiDirectActivity;
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        // TODO
        if (action != null) {
            switch (action) {
                // Wi-Fi P2P'nin aktif olup olmadığını anlama
                case WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION:
                    Log.v(TAG, "onReceive: Wi-Fi P2P durumu değişti");

                    int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
                    if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                        Log.i(TAG, "onReceive: Wi-Fi P2P aktif");

                    } else {
                        Log.i(TAG, "onReceive: Wi-Fi P2P aktif değil");
                    }
                    break;
                // Call WifiP2pManager.requestPeers() to get a list of current peers
                case WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION:
                    Log.d(TAG, "onReceive: Wi-Fi P2P Eşlenebilir cihazların listesi değişti.");

                    if (manager != null) {
                        Log.d(TAG, "onReceive: Eşleşebilir cihazlar alınıyor...");
                        manager.requestPeers(channel, this);
                    }
                    break;
                // Respond to new connection or disconnections
                case WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION:
                    Log.d(TAG, "onReceive: WiFi P2P bağlantısının durumu değişti");

                    if (manager == null) {
                        return;
                    }

                    // Grup bilgilerini ekrana basma
                    // https://developer.android.com/reference/android/net/wifi/p2p/WifiP2pInfo.html#fields
                    NetworkInfo networkInfo = Objects.requireNonNull(
                            intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO)
                    );
                    if (networkInfo.isConnected()) {
                        manager.requestConnectionInfo(channel, (info) -> {
                            Log.d(TAG, "onReceive: P2P bağlantı verisi: " + info);
                        });
                    }

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
            Log.w(TAG, "onPeersAvailable: Eşleşebilecek cihaz bulunamadı");
        } else {
            // Get the index of the device that we need to connect
            int deviceIndex = -1;
            for (int i = 0; i < peers.size(); i++) {
                WifiP2pDevice peer = peers.get(i);

                Log.d(TAG, "onPeersAvailable: Eşleşilebilecek cihazın adı: " + peer.deviceName);
                if (peer.deviceName.contains(DEVICE_PATTERN)) {
                    deviceIndex = i;
                }
            }

            // Connect the device if it's found
            if (deviceIndex != -1) {
                final WifiP2pDevice device = peers.get(deviceIndex);

                WifiP2pConfig config = new WifiP2pConfig();
                config.deviceAddress = device.deviceAddress;
                config.wps.setup = WpsInfo.PBC;

                Log.d(TAG, "onPeersAvailable: " + device.deviceName + " cihazına bağlanılmaya çalışılıyor...");

                // İlk cihaza bağlanma
                manager.connect(channel, config, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Log.i(TAG, "onSuccess: " + device.deviceName + " cihazına bağlanıldı.");
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
                        }

                        Log.e(TAG, "onFailure: " + device.deviceName + " cihazına bağlanılamadı.");
                    }
                });
            }
        }

    }
}
