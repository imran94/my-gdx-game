package com.mygdx.game;

/**
 * Created by Administrator on 10-Nov-16.
 */
public interface GameClient {
    public void send(float x, float y);
    public Character getCharacter();
    public boolean isConnected();
}
