package com.mygdx.game;

/**
 * Created by Administrator on 04-Nov-16.
 */
public interface MultiplayerController {

    String TAG = "mygdxgame";

    String getIpAddress();
    boolean isReachable(String ip);
    boolean isConnectedToLocalNetwork();

    void log(String message);
    void showNotification(String message);
}
