package com.aeye.android;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.ListFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Eşleşilebilir cihazları listeleyen fragment
 *
 * @see <a href="https://android.googlesource.com/platform/development/+/master/samples/WiFiDirectDemo/src/com/example/android/wifidirect/DeviceListFragment.java?autodive=0%2F%2F%2F">WifiDirect Demo</a>
 */
public class DeviceListFragment extends ListFragment implements WifiP2pManager.PeerListListener {

    public static final String TAG = DeviceListFragment.class.getName();
    private final List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();

    private AlertDialog alertDialog;
    private View contentView;

    private static String getDeviceStatus(int deviceStatus) {
        Log.d(WiFiDirectActivity.TAG, "Peer status :" + deviceStatus);
        switch (deviceStatus) {
            case WifiP2pDevice.AVAILABLE:
                return "Available";
            case WifiP2pDevice.INVITED:
                return "Invited";
            case WifiP2pDevice.CONNECTED:
                return "Connected";
            case WifiP2pDevice.FAILED:
                return "Failed";
            case WifiP2pDevice.UNAVAILABLE:
                return "Unavailable";
            default:
                return "Unknown";
        }
    }

    @SuppressLint("InflateParams")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        contentView = inflater.inflate(R.layout.fragment_device_list, null);
        return contentView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.setListAdapter(new WifiPeerListAdapter(Objects.requireNonNull(getActivity()), R.layout.layout_row_devices, peers));
    }

    @Override
    public void onPeersAvailable(WifiP2pDeviceList peerList) {
        // TODO: https://www.one-tab.com/page/aFZ1mdprQx27jPXerh8ZKg
        if (alertDialog != null && alertDialog.isShowing()) {
            alertDialog.dismiss();
        }

        peers.clear();
        peers.addAll(peerList.getDeviceList());
        ((WifiPeerListAdapter) Objects.requireNonNull(getListAdapter())).notifyDataSetChanged();
        if (peers.size() == 0) {
            Log.d(TAG, "onPeersAvailable: Cihaz bulunamadı");
        }
    }

    private class WifiPeerListAdapter extends ArrayAdapter<WifiP2pDevice> {

        private List<WifiP2pDevice> peers;

        WifiPeerListAdapter(@NonNull Context context, int resource, List<WifiP2pDevice> peers) {
            super(context, resource, peers);
            this.peers = peers;
        }

        @SuppressLint("InflateParams")
        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater viewInflater = (LayoutInflater) Objects.requireNonNull(getActivity())
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                Objects.requireNonNull(viewInflater);
                convertView = viewInflater.inflate(R.layout.layout_row_devices, null);
            }

            WifiP2pDevice device = peers.get(position);
            if (device != null) {
                TextView tvDeviceName = convertView.findViewById(R.id.tv_device_name);
                TextView tvDeviceDetails = convertView.findViewById(R.id.tv_device_details);

                if (tvDeviceName != null) {
                    tvDeviceName.setText(device.deviceName);
                }
                if (tvDeviceDetails != null) {
                    tvDeviceDetails.setText(getDeviceStatus(device.status));
                }

            }

            return convertView;
        }
    }
}
