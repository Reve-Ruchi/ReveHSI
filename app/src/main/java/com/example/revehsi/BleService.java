package com.example.revehsi;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class BleService extends Service {

    public static String DeviceName = "";

    private Handler handler = new Handler();
    private Runnable fakeScanRunnable;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Simulate BLE scan result every 2 seconds
        fakeScanRunnable = new Runnable() {
            @Override
            public void run() {
                // Simulate one device name (you can generate random names)
                String fakeDevice = "ReveSensor_" + System.currentTimeMillis();

                Intent broadcastIntent = new Intent("com.example.revehsi.devices");
                broadcastIntent.putExtra("sensorDevices", fakeDevice);
                LocalBroadcastManager.getInstance(BleService.this).sendBroadcast(broadcastIntent);

                // Repeat
                handler.postDelayed(this, 2000);
            }
        };

        handler.post(fakeScanRunnable);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacks(fakeScanRunnable);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
