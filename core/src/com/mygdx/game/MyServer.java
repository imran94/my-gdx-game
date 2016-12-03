package com.mygdx.game;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.SocketAddress;

/**
 * Created by Administrator on 10-Nov-16.
 */
public class MyServer extends GameClient {

    static ServerSocket serverSocket;
    boolean keepAlive;

    public MyServer(GameListener callback, String localAddress) {
        super(callback, localAddress);
    }

    @Override
    public void run() {

        keepAlive = true;
        while (keepAlive) {
            try {
                MainMenuScreen.debugText = "Creating server on port no. " + GameClient.port + "\n";

                serverSocket = new ServerSocket(GameClient.port);
                MainMenuScreen.debugText = "Created server at " + localAddress + " on port no. " + GameClient.port + "\n";
                socket = serverSocket.accept();
                serverSocket.close();
                MainMenuScreen.debugText = "Connected to socket " + socket.getInetAddress();

                keepAlive = false;
                Thread t = new Thread(new ReceiveThread());
                t.start();
                callback.onConnected();

            } catch (IOException io) {
                MainMenuScreen.debugText = "Failed to create a server:\n " + io.getMessage();
                callback.onConnectionFailed();
            }
        }
    }

    @Override
    public void disconnect() {
        try {
            serverSocket.close();
        } catch(IOException io) {
            callback.getDeviceAPI().log("Unable to close server. " + io.getMessage());
        }

        onDisconnected();
    }

    @Override
    public int getPlayerNumber() {
        return GameListener.PLAYER1;
    }
}
