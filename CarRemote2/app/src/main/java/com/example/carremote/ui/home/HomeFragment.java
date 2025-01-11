package com.example.carremote.ui.home;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothSocket;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.CameraController;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;

import com.example.carremote.BluetoothConnect;
import com.example.carremote.MainActivity;
import com.example.carremote.R;
import com.example.carremote.databinding.FragmentHomeBinding;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.ExecutionException;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private PreviewView previewView;
    private float dX, dY;

    @SuppressLint("ClickableViewAccessibility")
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

            // Setup camera preview
            this.cameraProviderFuture = ProcessCameraProvider.getInstance(mainActivity);
            previewView = binding.previewView;
            cameraProviderFuture.addListener(() -> {
                try {
                    ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                    bindPreview(cameraProvider);
                } catch (ExecutionException | InterruptedException e) {
                }
            }, ContextCompat.getMainExecutor(mainActivity));

            // Setup cabin camera preview
            Uri uri = Uri.parse(String.format("android.resource://%s/%d", mainActivity.getPackageName(), R.raw.esp32_cam));
            binding.previewView2.setVideoURI(uri);
            binding.previewView2.start();

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


            // Handle move camera preview
            binding.previewView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            dX = binding.previewView.getX() - event.getRawX();
                            dY = binding.previewView.getY() - event.getRawY();
                            break;

                        case MotionEvent.ACTION_MOVE:
                            float x = Math.min(Math.max(event.getRawX() + dX, 20), root.getWidth() - binding.previewView.getWidth() - 20);
                            float y = Math.min(Math.max(event.getRawY() + dY, 20), root.getHeight() - binding.previewView.getHeight() - 20);

                            binding.previewView.animate()
                                    .x(x).y(y)
                                    .setDuration(0)
                                    .start();
                            break;

                        default:
                            return false;
                    }
                    return true;
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

    void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder()
                .build();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                .build();

        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        /* Camera camera = */ cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, preview);
    }
}