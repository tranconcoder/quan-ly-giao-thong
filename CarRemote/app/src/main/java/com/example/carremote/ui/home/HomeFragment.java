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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;

import com.example.carremote.BLE;
import com.example.carremote.BluetoothCommand;
import com.example.carremote.BluetoothConnect;
import com.example.carremote.MainActivity;
import com.example.carremote.R;
import com.example.carremote.databinding.FragmentHomeBinding;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;

    private PreviewView previewView;
    private BluetoothSocket bluetoothSocket;
    private OutputStream outputStream;
    private InputStream inputStream;
    private float dX, dY;

    @SuppressLint("ClickableViewAccessibility")
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
//        HomeViewModel homeViewModel =
//                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder()
                .build();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                .build();

        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        /* Camera camera = */ cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, preview);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        try {
            MainActivity mainActivity = (MainActivity) getActivity();
            BluetoothConnect bluetoothConnect = mainActivity.bluetoothConnect;

            bluetoothSocket = bluetoothConnect.connect();
            outputStream = bluetoothSocket.getOutputStream();
            inputStream = bluetoothSocket.getInputStream();

            // Setup camera preview
            this.cameraProviderFuture = ProcessCameraProvider.getInstance(getContext());
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

            // Handle touch up down event
            List<TouchUpDownEvent> touchEvent = new ArrayList<>() {{
                add(new TouchUpDownEvent(binding.btnUp, BluetoothCommand.UP_START.toString(), BluetoothCommand.UP_STOP.toString()));
                add(new TouchUpDownEvent(binding.btnDown, BluetoothCommand.DOWN_START.toString(), BluetoothCommand.DOWN_STOP.toString()));
                add(new TouchUpDownEvent(binding.btnLeft, BluetoothCommand.LEFT_START.toString(), BluetoothCommand.LEFT_STOP.toString()));
                add(new TouchUpDownEvent(binding.btnRight, BluetoothCommand.RIGHT_START.toString(), BluetoothCommand.RIGHT_STOP.toString()));
            }};
            touchEvent.forEach(item -> {
                item.getElement().setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        try {
                            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                                outputStream.write(item.getStartCommand().getBytes());
                            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                                outputStream.write(item.getEndCommand().getBytes());
                            }
                        } catch (IOException e) {
                            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                        return false;
                    }
                });
            });

            // Handle toggle switch
            List<SwitchEvent> switchEvents = new ArrayList<>() {{
                add(new SwitchEvent(binding.switchAdas, BluetoothCommand.ADAS_ON.toString(), BluetoothCommand.ADAS_OFF.toString()));
                add(new SwitchEvent(binding.switchSleepDetect, BluetoothCommand.SLEEP_DETECT_ON.toString(), BluetoothCommand.SLEEP_DETECT_OFF.toString()));
                add(new SwitchEvent(binding.switchBlindSpot, BluetoothCommand.BLIND_SPOT_ON.toString(), BluetoothCommand.BLIND_SPOT_OFF.toString()));
                add(new SwitchEvent(binding.switchLaneKeeping, BluetoothCommand.LANE_KEEPING_ON.toString(), BluetoothCommand.LANE_KEEPING_OFF.toString()));
            }};
            switchEvents.forEach(item -> {
                item.getSwitchCompat().setOnCheckedChangeListener((buttonView, isChecked) -> {
                    try {
                        if (isChecked) {
                            outputStream.write(item.getEnableCommand().getBytes());
                        } else {
                            outputStream.write(item.getDisableCommand().getBytes());
                        }
                    } catch (IOException e) {
                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
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
                            float x = Math.min(Math.max(event.getRawX() + dX, 20), getView().getWidth() - binding.previewView.getWidth() - 20);
                            float y = Math.min(Math.max(event.getRawY() + dY, 20), getView().getHeight() - binding.previewView.getHeight() - 20);

                            binding.previewView
                                    .animate()
                                    .x(x).y(y)
                                    .setDuration(0)
                                    .start();
                            break;

                        case MotionEvent.ACTION_UP:
                            // Move to edge
                            float previewCenterX = binding.previewView.getX() + (binding.previewView.getWidth() / 2);
                            float previewCenterY = binding.previewView.getY() + (binding.previewView.getHeight() / 2);

                            if (previewCenterX < getView().getWidth() / 2) {
                                binding.previewView
                                        .animate()
                                        .x(20)
                                        .setDuration(0)
                                        .start();
                            } else {
                                binding.previewView
                                        .animate()
                                        .x(getView().getWidth() - binding.previewView.getWidth() - 20)
                                        .setDuration(0)
                                        .start();
                            }

                            break;
                    }

                    return true;
                }
            });

            // Handle reconnect button
            binding.btnReconnect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        bluetoothSocket = bluetoothConnect.connect();
                        outputStream = bluetoothSocket.getOutputStream();
                        inputStream = bluetoothSocket.getInputStream();
                    } catch (IOException e) {
                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            });
        } catch (Exception e) {
            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e("tag", e.getMessage(), e);
        }
    }
}