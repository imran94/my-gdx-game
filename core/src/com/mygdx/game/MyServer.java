package com.mygdx.game;

import com.badlogic.gdx.Gdx;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Administrator on 10-Nov-16.
 */
public class MyServer extends GameClient {

    static ServerSocket serverSocket;

    public MyServer(GameListener callback, String localAddress) {
        super(callback, localAddress);
    }

    @Override
    public void run() {
        try {
            MainMenuScreen.debugText = "Creating server on port no. " + GameClient.port + "\n";

            serverSocket = new ServerSocket(GameClient.port);
            MainMenuScreen.debugText = "Created server at " + localAddress + " on port no. " + GameClient.port + "\n";

            socket = serverSocket.accept();
            if (!socket.getReuseAddress()) socket.setReuseAddress(true);
            if (!socket.getTcpNoDelay()) socket.setTcpNoDelay(true);

            MainMenuScreen.debugText = "Connected to socket " + socket.getInetAddress();

            voiceSocket = new Socket(socket.getLocalAddress(), voicePort);
            if (!voiceSocket.getReuseAddress()) voiceSocket.setReuseAddress(true);
            if (!voiceSocket.getTcpNoDelay()) voiceSocket.setTcpNoDelay(true);

            Thread receiveThread = new Thread(new ReceiveThread());
            receiveThread.start();

            Thread voiceReceiveThread = new Thread(new VoiceReceiveThread());
            voiceReceiveThread.start();

            callback.onConnected();
            serverSocket.close();

        } catch (IOException io) {
            Gdx.app.log(MultiplayerController.TAG, "Failed to create a server: " + io.toString());
            MainMenuScreen.debugText = "Failed to create a server:\n " + io.getMessage();
            callback.onConnectionFailed();
        }
    }

    @Override
    public void cancel() {
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
