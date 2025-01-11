package com.example.carremote;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class BluetoothConnect {
    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket bluetoothSocket;
    private BluetoothLeScanner bluetoothLeScanner;
    private Context context;

    private String esp32mac = "c8:2e:18:25:e0:82".toUpperCase();


    @SuppressLint({"MissingPermission", "NewApi"})
    public BluetoothConnect(Activity context) {
        this.context = context;

        // Initialize Bluetooth adapter
        bluetoothManager = (BluetoothManager) context.getSystemService(context.BLUETOOTH_SERVICE)  ;
        bluetoothAdapter = bluetoothManager.getAdapter();

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            context.startActivityForResult(enableBtIntent, 0);
        }

    }

    @SuppressLint("MissingPermission")
    public BluetoothSocket connect() {
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        BluetoothDevice esp32 = pairedDevices
                .stream()
                .filter(device -> device.getAddress().equals(esp32mac))
                .findFirst()
                .orElse(null);

        BluetoothSocket bluetoothSocket = null;

        try {
            if (esp32 == null) throw new RuntimeException("ESP32 not found!");

            bluetoothSocket = esp32.createRfcommSocketToServiceRecord(new UUID(0x0000110100001000L, 0x800000805f9b34fbL));
            bluetoothSocket.connect();

            Toast.makeText(context, "Connect bluetooth success!", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(context, "Connect bluetooth failed!", Toast.LENGTH_SHORT).show();
        } catch (RuntimeException e) {
            Toast.makeText(context, "Please connect to esp32 with bluetooth first!", Toast.LENGTH_SHORT).show();
        } finally {
            return bluetoothSocket;
        }
    }
}
