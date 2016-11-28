package com.mygdx.game;

import java.net.Socket;

/**
 * Created by Administrator on 25-Nov-16.
 */
public interface GameListener {
    void onConnected();
    void onDisconnected();
    void onMessageReceived(String message);

    MultiplayerController getDeviceAPI();
}
