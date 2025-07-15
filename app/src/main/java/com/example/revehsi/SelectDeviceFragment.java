package com.example.revehsi;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
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
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;

public class SelectDeviceFragment extends Fragment {

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bleScanner;
    private ScanCallback scanCallback;

    private ArrayList<String> deviceNames = new ArrayList<>();
    private ArrayAdapter<String> deviceAdapter;
    private ListView deviceListView;

    private static final int REQUEST_BLE_PERMISSIONS = 101;

    public SelectDeviceFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_select_device, container, false);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // Menu
        view.findViewById(R.id.myMenu).setOnClickListener(v -> {
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
        view.findViewById(R.id.myIcon).setOnClickListener(v -> requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new LoginFragment())
                .addToBackStack(null)
                .commit());

        // List
        deviceListView = view.findViewById(R.id.device_list_view);
        deviceAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, deviceNames);
        deviceListView.setAdapter(deviceAdapter);

        deviceListView.setOnItemClickListener((parent, v, position, id) -> {
            String selected = deviceNames.get(position);
            Toast.makeText(requireContext(), "Selected: " + selected, Toast.LENGTH_SHORT).show();

            requireContext().getSharedPreferences("bt_pref", Context.MODE_PRIVATE)
                    .edit().putString("last_device_mac", selected).apply();

            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .addToBackStack(null)
                    .commit();
        });

        // Swipe to refresh
        SwipeRefreshLayout swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            deviceNames.clear();
            deviceAdapter.notifyDataSetChanged();
            startBleScan();
            swipeRefreshLayout.setRefreshing(false);
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (checkPermissions() && isLocationEnabled()) {
            startBleScan();
        } else {
            requestPermissions();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        stopBleScan();
    }

    private void startBleScan() {
        stopBleScan();

        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Toast.makeText(requireContext(), "Bluetooth not enabled", Toast.LENGTH_SHORT).show();
            return;
        }

        bleScanner = bluetoothAdapter.getBluetoothLeScanner();
        if (bleScanner == null) {
            Toast.makeText(requireContext(), "BLE scanner not available", Toast.LENGTH_SHORT).show();
            return;
        }

        scanCallback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                BluetoothDevice device = result.getDevice();
                String deviceName = device.getName();
                Log.d("BLE_SCAN", "Found: " + deviceName);

                if (deviceName != null && deviceName.startsWith("ReveSensor") && !deviceNames.contains(deviceName)) {
                    deviceNames.add(deviceName);
                    deviceAdapter.notifyDataSetChanged();
                }
            }
        };

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        bleScanner.startScan(scanCallback);
    }

    private void stopBleScan() {
        if (bleScanner != null && scanCallback != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                    ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            bleScanner.stopScan(scanCallback);
        }
    }

    private boolean checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED;
        } else {
            return ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        }
    }

    private boolean isLocationEnabled() {
        LocationManager lm = (LocationManager) requireContext().getSystemService(Context.LOCATION_SERVICE);
        return lm != null && lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requestPermissions(new String[]{
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT
            }, REQUEST_BLE_PERMISSIONS);
        } else {
            requestPermissions(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, REQUEST_BLE_PERMISSIONS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean granted = true;
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                granted = false;
                break;
            }
        }

        if (granted && isLocationEnabled()) {
            startBleScan();
        } else {
            Toast.makeText(requireContext(), "BLE permissions and GPS required", Toast.LENGTH_LONG).show();
        }
    }
}
