package com.example.revehsi;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
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
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.util.Set;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;


public class SelectDeviceFragment extends Fragment {

    private BluetoothAdapter bluetoothAdapter;
    private BroadcastReceiver receiver;
    private ArrayAdapter<String> deviceListAdapter;
    private ListView deviceListView;

    public SelectDeviceFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_select_device, container, false);

        // Initialize ListView
        deviceListView = view.findViewById(R.id.device_list_view);
        SwipeRefreshLayout swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            //Toast.makeText(requireContext(), "Refreshing device list...", Toast.LENGTH_SHORT).show();

            // Refresh Bluetooth devices
            checkPermissionsAndStartDiscovery();

            // Stop refresh after 2 seconds (or when done)
            new Handler().postDelayed(() -> {
                swipeRefreshLayout.setRefreshing(false);
            }, 5000);
        });

        // Menu logic
        ImageView myMenu = view.findViewById(R.id.myMenu);
        myMenu.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(requireContext(), v);
            popupMenu.getMenuInflater().inflate(R.menu.menu_popup, popupMenu.getMenu());

            popupMenu.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.action_view_data) {
                    requireActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, new SummaryFragment())
                            .addToBackStack(null)
                            .commit();
                    return true;
                } else if (id == R.id.action_logout) {
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

        ImageView backIcon = view.findViewById(R.id.myIcon);
        backIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Switch to LoginFragment
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new LoginFragment())
                        .addToBackStack(null)
                        .commit();
            }
        });

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter == null) {
            Toast.makeText(requireContext(), "Bluetooth not supported on this device", Toast.LENGTH_SHORT).show();
        } else {
            checkPermissionsAndStartDiscovery();
        }

        return view;
    }
    private void checkPermissionsAndStartDiscovery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(new String[]{
                        Manifest.permission.BLUETOOTH_SCAN,
                        Manifest.permission.BLUETOOTH_CONNECT,
                        Manifest.permission.ACCESS_FINE_LOCATION
                }, 1001);
                return;
            }
        } else {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION
                }, 1001);
                return;
            }
        }

        startBluetoothDiscovery();
    }
    private void startBluetoothDiscovery() {
        if (!bluetoothAdapter.isEnabled()) {
            Toast.makeText(requireContext(), "Please enable Bluetooth", Toast.LENGTH_SHORT).show();
            return;
        }

        deviceListAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1);
        deviceListView.setAdapter(deviceListAdapter);

        // Show paired devices
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT)
                == PackageManager.PERMISSION_GRANTED) {
            Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
            for (BluetoothDevice device : pairedDevices) {
                if (device.getName() != null) {
                    String info = "Paired: " + device.getName() + "\n" + device.getAddress();
                    deviceListAdapter.add(info);
                }
            }
        }

        // Discover nearby devices
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (BluetoothDevice.ACTION_FOUND.equals(intent.getAction())) {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if (device != null && device.getName() != null) {
                        String info = device.getName() + "\n" + device.getAddress();
                        if (deviceListAdapter.getPosition(info) == -1) {
                            deviceListAdapter.add(info);
                        }
                    }
                }
            }
        };

        requireContext().registerReceiver(receiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        bluetoothAdapter.startDiscovery();

        new Handler().postDelayed(() -> {
            bluetoothAdapter.cancelDiscovery();
            try {
                requireContext().unregisterReceiver(receiver);
            } catch (Exception ignored) {}
        }, 15000);

        deviceListView.setOnItemClickListener((parent, view, position, id) -> {
            String selected = deviceListAdapter.getItem(position);
            Toast.makeText(requireContext(), "Selected: " + selected, Toast.LENGTH_SHORT).show();
            // Optionally save selected MAC
            if (selected != null && selected.length() >= 17) {
                String mac = selected.substring(selected.length() - 17);
                requireContext().getSharedPreferences("bt_pref", Context.MODE_PRIVATE)
                        .edit().putString("last_device_mac", mac).apply();
            }
            // Go to HomeFragment
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .addToBackStack(null)
                    .commit();
        });
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        try {
            requireContext().unregisterReceiver(receiver);
        } catch (Exception ignored) {}
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1001) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            if (allGranted) {
                startBluetoothDiscovery();
            } else {
                Toast.makeText(requireContext(), "Bluetooth permissions are required!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
