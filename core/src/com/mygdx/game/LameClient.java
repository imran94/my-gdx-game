package com.mygdx.game;

/**
 * Created by Administrator on 20-Dec-16.
 */


// For testing purposes
public class LameClient implements GameClientInterface {

    public LameClient() {}

    @Override
    public boolean isConnected() {
        return false;
    }

    @Override
    public void onConnected() {

    }

    @Override
    public void disconnect() {

    }

    @Override
    public void onDisconnected() {

    }

    @Override
    public void setListener(GameListener callback) {

    }

    @Override
    public void sendMessage(String message) {

    }

    @Override
    public void sendMessage(byte[] message) {

    }

    @Override
    public int getPlayerNumber() {
        return GameListener.PLAYER1;
    }

    @Override
    public void cancel() {

    }

    @Override
    public void run() {

    }
}
