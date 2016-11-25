package com.mygdx.game;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by Administrator on 12-Nov-16.
 */
public class BluetoothServer implements GameClient {
    String TAG = "mygdxgame";

    BluetoothAdapter mAdapter;
    BluetoothServerSocket mServerSocket;
    BluetoothSocket BTSocket;

    private static final String NAME = "GdxGame";
    // Unique UUID for this application
    private static final UUID MY_UUID_SECURE =
            UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
    private static final UUID MY_UUID_INSECURE =
            UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");

    private int mState;

    // Constants that indicate the current connection state
    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device

    public BluetoothServer() throws IOException {

        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mServerSocket = mAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID_INSECURE);

    }

    public void start() {
        try {
            BTSocket = mServerSocket.accept();
            mServerSocket.close();
            Log.d(TAG, "Accept success: " + BTSocket.getRemoteDevice());
        } catch (IOException io) {
            Log.d(TAG, "Accept failed");
        }
    }

    @Override
    public void send(float x, float y) {

    }

    @Override
    public Character getCharacter() {
        return null;
    }

    @Override
    public boolean isConnected() {
        if (BTSocket == null)
            return false;

        return BTSocket.getRemoteDevice() != null;
    }
}
