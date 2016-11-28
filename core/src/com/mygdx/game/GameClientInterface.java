package com.mygdx.game;

import java.net.Socket;

/**
 * Created by Administrator on 10-Nov-16.
 */
public interface GameClientInterface extends Runnable {
    int port = 45351;

    boolean isConnected();
    void onConnected(Socket s);
    void setListener(GameListener callback);
    void sendMessage(String message);

    @Override
    void run();
}
