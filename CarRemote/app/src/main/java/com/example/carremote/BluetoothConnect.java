package com.example.carremote;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothSocket;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.security.Provider;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class BluetoothConnect extends Service {
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
        bluetoothManager = (BluetoothManager) context.getSystemService(context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            context.startActivityForResult(enableBtIntent, 0);
        }
    }

    @SuppressLint("MissingPermission")
    public BluetoothGatt connectGatt() {
        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(context.BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        BluetoothDevice esp32s3 = bluetoothAdapter
                .getBondedDevices()
                .stream()
                .filter(device -> device.getAddress().equals(esp32s3mac))
                .findFirst()
                .orElse(null);
        BluetoothGatt gatt = null;

        try {
            if (esp32s3 == null) throw new RuntimeException("ESP32 not found!");

            gatt = esp32s3.connectGatt(this, true, bluetoothGattCallback);
        } catch (RuntimeException e) {
            Log.e("tag", e.getMessage(), e);
            Toast.makeText(context, "Please connect to esp32s3 with bluetooth first!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(context, "Connect GATT failed!", Toast.LENGTH_SHORT).show();
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
        @SuppressLint("MissingPermission")
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                // successfully connected to the GATT Server
                Toast.makeText(context, "Connect GATT success!", Toast.LENGTH_SHORT).show();

                // discover services

                // get services
                List<BluetoothGattService> services = gatt.getServices();
                for (BluetoothGattService service : services) {
                    Log.i("tag", service.getUuid().toString());
                }
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                // disconnected from the GATT Server
                Toast.makeText(context, "Disconnected gatt success!", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                // successfully discovered services
                Toast.makeText(context, "Discovered services success!", Toast.LENGTH_SHORT).show();
            } else {
                // failed to discover services
                Toast.makeText(context, "Discovered services failed!", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onServiceChanged(@NonNull BluetoothGatt gatt) {
            Toast.makeText(context, "Service changed!", Toast.LENGTH_SHORT).show();
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static UUID bytesToUUID(byte[] bytes) {
        long mostSigBits = 0;
        long leastSigBits = 0;

        for (int i = 0; i < 8; i++) {
            mostSigBits = (mostSigBits << 8) | (bytes[i] & 0xFF);
        }

        for (int i = 8; i < 16; i++) {
            leastSigBits = (leastSigBits << 8) | (bytes[i] & 0xFF);
        }

        return new UUID(mostSigBits, leastSigBits);
    }
}
