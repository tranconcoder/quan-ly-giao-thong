package com.example.carremote;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLSurfaceView;
import android.util.Log;

import com.example.carremote.ui.home.CustomRenderer;
import com.example.carremote.Constants;
import com.example.carremote.Detector;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class WebsocketServer extends  WebSocketServer {
    private Map<String, WebSocket> clients = new ConcurrentHashMap<>();
    private CustomRenderer customRenderer;
    private GLSurfaceView glSurfaceView;
    private Detector detector;
    private Context ctx;
    private boolean isDetecting = false;

    public WebsocketServer(
            Context ctx,
            Detector.DetectorListener detectorListener,
            int port,
            CustomRenderer customRenderer,
            GLSurfaceView glSurfaceView
    ) {
        super(new InetSocketAddress(port));

        this.customRenderer = customRenderer;
        this.glSurfaceView = glSurfaceView;
        this.ctx = ctx;
        this.detector = new Detector(ctx,Constants.MODEL_PATH, Constants.LABELS_PATH, detectorListener);
    }
    public WebsocketServer(InetSocketAddress address) {
        super(address);
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        String uuid = UUID.randomUUID().toString();
        clients.put(uuid, conn);

        conn.send(uuid);
        conn.send("Welcome to the server!");

        Log.d(Global.TAG.toString(), "onOpen: " + conn.getRemoteSocketAddress());
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        Log.d(Global.TAG.toString(), "onClose: " + conn.getRemoteSocketAddress());
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        Log.d(Global.TAG.toString(), "onMessage: " + message);
    }

    @Override
    public void onMessage(WebSocket conn, ByteBuffer message) {
        super.onMessage(conn, message);

        Bitmap bitmap = decodeCompressedImage(message);
        customRenderer.updateFrame(bitmap);
        glSurfaceView.requestRender();


        new Thread(() -> {
            if (!isDetecting) {
                isDetecting = true;
                // Rotate bitmap 90 degree

                detector.detect(bitmap);
                isDetecting = false;
            }
        }).start();

//        Log.i(Global.TAG.toString(), "onMessage byteBuffer: " + message);
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        Log.e(Global.TAG.toString(), "onError: " + ex.getMessage(), ex);
    }

    @Override
    public void onStart() {
        Log.d(Global.TAG.toString(), "Server started successfully");
        setConnectionLostTimeout(100);
    }


    private Bitmap decodeCompressedImage(ByteBuffer buffer) {
        byte[] byteArray = new byte[buffer.remaining()];
        buffer.get(byteArray); // Chuyển dữ liệu ByteBuffer thành byte array
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
    }
}

