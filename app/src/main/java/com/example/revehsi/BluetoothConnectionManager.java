package com.example.revehsi;

import android.bluetooth.BluetoothSocket;

public class BluetoothConnectionManager {

    private static BluetoothConnectionManager instance;
    private BluetoothSocket socket;

    private BluetoothConnectionManager() {}

    public static synchronized BluetoothConnectionManager getInstance() {
        if (instance == null) {
            instance = new BluetoothConnectionManager();
        }
        return instance;
    }

    public void setSocket(BluetoothSocket socket) {
        this.socket = socket;
    }

    public BluetoothSocket getSocket() {
        return socket;
    }
}

