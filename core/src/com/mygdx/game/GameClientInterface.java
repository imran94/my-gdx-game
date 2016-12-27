package com.mygdx.game;

import java.net.Socket;

/**
 * Created by Administrator on 10-Nov-16.
 */
public interface GameClientInterface extends Runnable {
    int port = 45351;
    int datagramPort = 45354;

    boolean isConnected();
    void onConnected();
    void disconnect();
    void onDisconnected();
    void setListener(GameListener callback);
    void sendMessage(String message);
    void sendVoiceMessage(byte[] message);
    int getPlayerNumber();
    void cancel();

    @Override
    void run();
}
