package com.example.carremote;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLSurfaceView;
import android.util.Log;

import org.java_websocket.WebSocket;
import org.java_websocket.WebSocketListener;
import org.java_websocket.drafts.Draft;
import org.java_websocket.exceptions.InvalidDataException;
import org.java_websocket.framing.Framedata;
import org.java_websocket.framing.PingFrame;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.handshake.Handshakedata;
import org.java_websocket.handshake.ServerHandshake;
import org.java_websocket.handshake.ServerHandshakeBuilder;
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

    public WebsocketServer(int port, CustomRenderer customRenderer, GLSurfaceView glSurfaceView) {
        super(new InetSocketAddress(port));

        this.customRenderer = customRenderer;
        this.glSurfaceView = glSurfaceView;
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

        Log.i(Global.TAG.toString(), "onMessage byteBuffer: " + message);
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

