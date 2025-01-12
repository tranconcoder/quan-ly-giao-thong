package com.example.carremote;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothSocket;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
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

    private boolean runInEsp32S3 = false;
    private String esp32mac = "c8:2e:18:25:e0:82".toUpperCase();
    private String esp32s3mac = "24:ec:4a:3a:2b:ee".toUpperCase();


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
    public BluetoothGatt connectGatt() {
        BluetoothGatt gatt = null;
        BluetoothDevice esp32s3 = bluetoothAdapter
                .getBondedDevices()
                .stream()
                .filter(device -> device.getAddress().equals(esp32s3mac))
                .findFirst()
                .orElse(null);

        try {
            if (esp32s3 == null) throw new RuntimeException("ESP32 not found!");

            gatt = esp32s3.connectGatt(context, false, bluetoothGattCallback);
            boolean result = gatt.connect();

            if (!result) throw new RuntimeException("Failed to connect to ESP32S3!");

            Toast.makeText(context, "Connect bluetooth success!", Toast.LENGTH_SHORT).show();
        } catch (RuntimeException e) {
            Toast.makeText(context, "Please connect to esp32 with bluetooth first!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(context, "Connect bluetooth failed!", Toast.LENGTH_SHORT).show();
        } finally {
            return gatt;
        }
    }

    @SuppressLint("MissingPermission")
    public BluetoothSocket connect() {
        BluetoothSocket bluetoothSocket = null;
        BluetoothDevice esp32 = bluetoothAdapter
                .getBondedDevices()
                .stream()
                .filter(device -> device.getAddress().equals(esp32mac))
                .findFirst()
                .orElse(null);

        try {
            if (esp32 == null) throw new RuntimeException("ESP32 not found!");

            bluetoothSocket = esp32.createRfcommSocketToServiceRecord(new UUID(0x0000110100001000L, 0x800000805f9b34fbL));
            bluetoothSocket.connect();

            Toast.makeText(context, "Connect bluetooth success!", Toast.LENGTH_SHORT).show();
        } catch (RuntimeException e) {
            Toast.makeText(context, "Please connect to esp32 with bluetooth first!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(context, "Connect bluetooth failed!", Toast.LENGTH_SHORT).show();
        } finally {
            return bluetoothSocket;
        }
    }


    private final BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                // successfully connected to the GATT Server
                Toast.makeText(context, "Connected gatt success!", Toast.LENGTH_SHORT).show();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                // disconnected from the GATT Server
                Toast.makeText(context, "Disconnected gatt success!", Toast.LENGTH_SHORT).show();
            }
        }
    };
}
