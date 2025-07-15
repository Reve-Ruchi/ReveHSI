package com.example.revehsi;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;

public class SelectDeviceFragment extends Fragment {

    private ArrayList<String> deviceNames = new ArrayList<>();
    private ArrayAdapter<String> deviceAdapter;
    private ListView deviceListView;
    private BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    private final BroadcastReceiver bleReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if ("com.example.revehsi.devices".equals(action)) {
                String newDevice = intent.getStringExtra("sensorDevices");
                if (newDevice != null && !deviceNames.contains(newDevice)) {
                    deviceNames.add(newDevice);
                    deviceAdapter.notifyDataSetChanged();
                }

            } else if ("com.example.revehsi.cStatus".equals(action)) {
                boolean connected = intent.getBooleanExtra("connectionStatus", false);
                if (connected) {
                    Toast.makeText(requireContext(), "Connected to device!", Toast.LENGTH_SHORT).show();
                }
            }
        }
    };

    public SelectDeviceFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_select_device, container, false);

        // Handle Menu
        ImageView menuBtn = view.findViewById(R.id.myMenu);
        menuBtn.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(requireContext(), v);
            popupMenu.getMenuInflater().inflate(R.menu.menu_popup, popupMenu.getMenu());

            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.action_view_data) {
                    requireActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, new SummaryFragment())
                            .addToBackStack(null)
                            .commit();
                    return true;
                } else if (item.getItemId() == R.id.action_logout) {
                    requireActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, new LoginFragment())
                            .commit();
                    return true;
                }
                return false;
            });
            popupMenu.show();
        });

        // Back Button
        ImageView backBtn = view.findViewById(R.id.myIcon);
        backBtn.setOnClickListener(v -> requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new LoginFragment())
                .addToBackStack(null)
                .commit());

        // Setup ListView
        deviceListView = view.findViewById(R.id.device_list_view);
        deviceAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, deviceNames);
        deviceListView.setAdapter(deviceAdapter);

        // Swipe to refresh
        SwipeRefreshLayout swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            deviceNames.clear();
            deviceAdapter.notifyDataSetChanged();
            restartBleScan();
            swipeRefreshLayout.setRefreshing(false);
        });

        // On device selection
        deviceListView.setOnItemClickListener((parent, v, position, id) -> {
            String selected = deviceNames.get(position);
            Toast.makeText(requireContext(), "Selected: " + selected, Toast.LENGTH_SHORT).show();

            // Save selected device
            requireContext().getSharedPreferences("bt_pref", Context.MODE_PRIVATE)
                    .edit().putString("last_device_mac", selected).apply();

            // Pass to BleService
            BleService.DeviceName = selected;

            // Go to HomeFragment
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .addToBackStack(null)
                    .commit();
        });

        return view;
    }

    private void restartBleScan() {
        Intent intent = new Intent(requireContext(), BleService.class);
        requireContext().startService(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        registerBleReceiver();
        restartBleScan();  // Optional auto start
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(bleReceiver);
        } catch (Exception ignored) {}
    }

    private void registerBleReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.example.revehsi.devices");
        filter.addAction("com.example.revehsi.cStatus");
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(bleReceiver, filter);
    }
}
