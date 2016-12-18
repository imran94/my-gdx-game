package com.mygdx.game;

/**
 * Created by Administrator on 04-Nov-16.
 */
public interface MultiplayerController {

    String TAG = "mygdxgame";

    String getIpAddress();
    boolean isConnectedToLocalNetwork();

    void log(String message);
    void showNotification(String message);
    void startRecording();
    void setCallback(GameClientInterface callback);
    void transmit(byte[] message, int bufferSize);
}
