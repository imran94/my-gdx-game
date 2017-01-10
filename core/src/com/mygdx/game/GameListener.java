package com.mygdx.game;

import java.net.Socket;

/**
 * Created by Administrator on 25-Nov-16.
 */
public interface GameListener {

    int PLAYER1 = 1;
    int PLAYER2 = 2;

    int GAME_WIDTH = 320;
    int GAME_HEIGHT = 480;

    void onConnected();
    void onDisconnected();

    void onConnectionFailed();

    void onMessageReceived(String message);

    MultiplayerController getDeviceAPI();
}
