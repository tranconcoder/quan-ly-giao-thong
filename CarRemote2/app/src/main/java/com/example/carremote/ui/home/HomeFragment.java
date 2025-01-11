package com.example.carremote.ui.home;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.carremote.BluetoothConnect;
import com.example.carremote.MainActivity;
import com.example.carremote.databinding.FragmentHomeBinding;

import java.io.IOException;
import java.io.OutputStream;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
//        HomeViewModel homeViewModel =
//                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();


        try {
            MainActivity mainActivity = (MainActivity) getActivity();
            BluetoothConnect bluetoothConnect = mainActivity.bluetoothConnect;
            BluetoothSocket bluetoothSocket = bluetoothConnect.connect();
            OutputStream outputStream = bluetoothSocket.getOutputStream();

            if (bluetoothSocket == null || outputStream == null)
                throw new IOException("Bluetooth connection failed");

            binding.btnUp.setOnTouchListener(new View.OnTouchListener() {
                @SuppressLint("ClickableViewAccessibility")
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    try {
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                                outputStream.write("UP-START".getBytes());
                                break;
                            case MotionEvent.ACTION_UP:
                                outputStream.write("UP-END".getBytes());
                                break;
                        }
                    } catch (IOException e) {
                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                    return false;
                }
            });

            binding.btnDown.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    try {
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                                outputStream.write("DOWN-START".getBytes());
                                break;
                            case MotionEvent.ACTION_UP:
                                outputStream.write("DOWN-END".getBytes());
                                break;
                        }
                    } catch (IOException e) {
                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                    return false;
                }
            });

            binding.btnLeft.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    try {
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                                outputStream.write("LEFT-START".getBytes());
                                break;
                            case MotionEvent.ACTION_UP:
                                outputStream.write("LEFT-END".getBytes());
                                break;
                        }
                    } catch (IOException e) {
                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                    return false;
                }
            });

            binding.btnRight.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    try {
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                                outputStream.write("RIGHT-START".getBytes());
                                break;
                            case MotionEvent.ACTION_UP:
                                outputStream.write("RIGHT-END".getBytes());
                                break;
                        }
                    } catch (IOException e) {
                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                    return false;
                }
            });
        } catch (Exception e) {
            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}