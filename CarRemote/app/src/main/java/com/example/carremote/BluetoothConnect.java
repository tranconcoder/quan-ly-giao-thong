package com.example.carremote;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothSocket;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.icu.text.Edits;
import android.os.Build;
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
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class BluetoothConnect extends Service {
    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket bluetoothSocket;
    private BluetoothLeScanner bluetoothLeScanner;
    private Context context;

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
    public BluetoothGatt connectGatt(String address) {
        BluetoothDevice esp32s3 = bluetoothAdapter
                .getBondedDevices()
                .stream()
                .filter(device -> device.getAddress().equals(address))
                .findFirst()
                .orElse(null);
        BluetoothGatt gatt = null;

        try {
            if (esp32s3 == null) throw new RuntimeException("ESP32 not found!");

            gatt = esp32s3.connectGatt(this, true, bluetoothGattCallback);
        } catch (RuntimeException e) {
            Log.e(Global.TAG.toString(), e.getMessage(), e);
            Toast.makeText(context, "Please connect to esp32s3 with bluetooth first!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(context, "Connect GATT failed!", Toast.LENGTH_SHORT).show();
        } finally {
            return gatt;
        }
    }


    @SuppressLint("MissingPermission")
    public BluetoothSocket connectSPP(String address) {
        BluetoothSocket bluetoothSocket = null;
        BluetoothDevice esp32 = bluetoothAdapter
                .getBondedDevices()
                .stream()
                .filter(device -> device.getAddress().equals(address))
                .findFirst()
                .orElse(null);

        try {
            if (esp32 == null) throw new RuntimeException("ESP32 not found!");

            bluetoothSocket = esp32.createRfcommSocketToServiceRecord(new UUID(0x0000110100001000L, 0x800000805f9b34fbL));
            bluetoothSocket.connect();

//            Toast.makeText(context, "Connect bluetooth success!", Toast.LENGTH_SHORT).show();
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
            Log.d(Global.TAG.toString(), "onConnectionStateChange: status = " + status + ", newState = " + newState);

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                // successfully connected to the GATT Server
                Log.i(Global.TAG.toString(), "Connected gatt success!");

                // discover services
                gatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                // disconnected from the GATT Server
                Toast.makeText(context, "Disconnected gatt success!", Toast.LENGTH_SHORT).show();
            }
        }

        @SuppressLint("MissingPermission")
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                // successfully discovered services
                Log.i(Global.TAG.toString(), "Discovered services success!");

                // get services
                for (BluetoothGattService service : gatt.getServices()) {
                    Log.d(Global.TAG.toString(), "Service: " + service.getUuid().toString());
                    for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                        Log.d(Global.TAG.toString(), "Characteristic: " + characteristic.getUuid().toString());
                    }
                }
                List<BluetoothGattService> services = gatt.getServices();
                BluetoothGattService service = services.get(services.size() - 1);
                BluetoothGattCharacteristic characteristic = service.getCharacteristics().get(2);

                // set characteristic
                byte[] data = new byte[5000];
                for (int i = 0; i < data.length; i++) {
                    data[i] = (byte)(i % 256);
                }
                characteristic.setValue(data);

                // write characteristic
                gatt.beginReliableWrite();
                gatt.writeCharacteristic(characteristic);
                gatt.executeReliableWrite();
            } else {
                // failed to discover services
                Log.e(Global.TAG.toString(), "Discovered services failed!");
            }
        }

        @Override
        public void onServiceChanged(@NonNull BluetoothGatt gatt) {
            Toast.makeText(context, "Service changed!", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);

            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i(Global.TAG.toString(), "Write characteristic success!");
            } else {
                Log.e(Global.TAG.toString(), "Write characteristic failed!");
            }
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
