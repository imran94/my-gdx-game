package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.esotericsoftware.kryonet.Server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Administrator on 10-Nov-16.
 */
public class MyServer extends GameClient {

    ServerSocket serverSocket;

    public MyServer(GameListener callback, String localAddress) {
        super(callback, localAddress);
    }

    @Override
    public void run() {
        while (true) {
            try {
                MainMenuScreen.debugText = "Creating server on port no. " + GameClient.port + "\n";

                ServerSocket server = new ServerSocket(GameClient.port);
                MainMenuScreen.debugText = "Created server " + server.getLocalSocketAddress() + " on port no. " + GameClient.port + "\n";
                socket = server.accept();
                callback.onConnected();
                MainMenuScreen.debugText = "Connected to socket " + socket.getInetAddress();

                Thread t = new Thread(new ReceiveThread());
                t.start();
            } catch (IOException io) {
                MainMenuScreen.debugText = "Failed to create a server:\n " + io.getMessage();
            }
        }
    }
}
