package com.mygdx.game;

import com.badlogic.gdx.Gdx;

import java.io.IOException;
import java.net.DatagramSocket;
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
                MainMenuScreen.debugText = "Connected to socket " + socket.getInetAddress();

                dgSocket = new DatagramSocket(datagramPort, socket.getLocalAddress());

                keepAlive = false;
                Thread receiveThread = new Thread(new ReceiveThread());
                receiveThread.start();

                Thread voiceReceiveThread = new Thread(new VoiceReceiveThread());
                voiceReceiveThread.start();

                callback.onConnected();
                serverSocket.close();

            } catch (IOException io) {
                MainMenuScreen.debugText = "Failed to create a server:\n " + io.getMessage();
                callback.onConnectionFailed();
            }
        }
    }

    @Override
    public void disconnect() {
        try {
            socket.close();
            dgSocket.close();
        } catch(IOException io) {
            callback.getDeviceAPI().log("Unable to close socket. " + io.getMessage());
        }

        onDisconnected();
    }

    @Override
    public void cancel() {
        keepAlive = false;
        try {
            serverSocket.close();
        } catch (IOException io) {
            Gdx.app.log(MultiplayerController.TAG, "Failed to close server: " + io.toString());
        }
    }

    @Override
    public int getPlayerNumber() {
        return GameListener.PLAYER1;
    }
}
