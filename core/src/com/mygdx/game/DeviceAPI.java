package com.mygdx.game;

/**
 * Created by Administrator on 04-Nov-16.
 */
public interface DeviceAPI {

    String TAG = "mygdxgame";

    String getIpAddress();
    boolean isConnectedToLocalNetwork();

    void showNotification(String message);
    void startRecording();
    void setCallback(GameClientInterface callback);
    void transmit(byte[] message, int bufferSize);

    int getBufferSize();
}
