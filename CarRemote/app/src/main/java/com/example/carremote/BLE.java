package com.example.carremote;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Looper;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.logging.LogRecord;

public class BLE {
    private Context ctx;
    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothLeScanner;
    private boolean scanning;
    private ArrayList<BluetoothDevice> leDeviceListAdapter = new ArrayList<>();
    private Handler handler = new Handler();

    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;

    public BLE(Context ctx) {
        this.ctx = ctx;
        this.bluetoothManager = (BluetoothManager) ctx.getSystemService(ctx.BLUETOOTH_SERVICE);
        this.bluetoothAdapter = bluetoothManager.getAdapter();
        this.bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
    }


    @SuppressLint("MissingPermission")
    public void scanLeDevice() {
        if (!scanning) {
            // Stops scanning after a predefined scan period.
            this.handler.postDelayed(new Runnable() {
                @SuppressLint("MissingPermission")
                @Override
                public void run() {
                    scanning = false;
                    bluetoothLeScanner.stopScan(leScanCallback);
                }
            }, SCAN_PERIOD);

            scanning = true;
            bluetoothLeScanner.startScan(leScanCallback);
        } else {
            scanning = false;
            bluetoothLeScanner.stopScan(leScanCallback);
        }
    }

    // Device scan callback.
    private ScanCallback leScanCallback =
            new ScanCallback() {
                @SuppressLint("MissingPermission")
                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    super.onScanResult(callbackType, result);
                    leDeviceListAdapter.add(result.getDevice());

                    Log.i("tag", result.getDevice().toString());

                    Toast.makeText(ctx, result.getDevice().getAddress(), Toast.LENGTH_SHORT).show();
                }
            };
}
