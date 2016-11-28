package com.mygdx.game;

/**
 * Created by Administrator on 04-Nov-16.
 */
public interface MultiplayerController {
    void initialize();
    String getIpAddress();
    boolean isReachable(String ip);
}
