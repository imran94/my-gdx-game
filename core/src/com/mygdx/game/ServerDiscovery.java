package com.mygdx.game;

import java.io.IOException;
import java.net.Socket;

/**
 * Created by Administrator on 26-Nov-16.
 */
public class ServerDiscovery implements Runnable {

    String ipAddress;
    MyClient callback;

    public ServerDiscovery(int i, MyClient callback) {
        ipAddress = "192.168.0." + i;
        this.callback = callback;
    }

    @Override
    public void run() {
        try {
            Socket s = new Socket(ipAddress, GameClient.port);
            callback.onConnected(s);
        } catch (IOException io) {

        }
    }
}
