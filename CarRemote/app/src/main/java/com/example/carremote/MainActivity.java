package com.example.carremote;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.FileUtils;
import android.telephony.data.TrafficDescriptor;
import android.view.Menu;

import com.google.android.material.navigation.NavigationView;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.example.carremote.databinding.ActivityMainBinding;

import org.tensorflow.lite.support.common.FileUtil;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    public BluetoothConnect bluetoothConnect;
    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;

    private static ArrayList<String> permissionList = new ArrayList<>(){{
        add(Manifest.permission.WRITE_SETTINGS);
        add(android.Manifest.permission.NEARBY_WIFI_DEVICES);
        add(android.Manifest.permission.ACCESS_FINE_LOCATION);
        add(android.Manifest.permission.ACCESS_COARSE_LOCATION);
        add(android.Manifest.permission.BLUETOOTH);
        add(android.Manifest.permission.BLUETOOTH_SCAN);
        add(android.Manifest.permission.BLUETOOTH_CONNECT);
        add(android.Manifest.permission.BLUETOOTH_ADVERTISE);
        add("android.hardware.camera.any");
        add(android.Manifest.permission.CAMERA);
        add(android.Manifest.permission.RECORD_AUDIO);
        add(android.Manifest.permission.CHANGE_WIFI_STATE);
        add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        // Grant permissions
        for (String permission : permissionList)
            if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED)
                requestPermissions(new String[]{permission}, 0);


        //
        // Initialize
        //
        this.init();

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarMain.toolbar);
        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
    }


    private void init() {
        // Bluetooth
        this.bluetoothConnect = new BluetoothConnect(MainActivity.this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}