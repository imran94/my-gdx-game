package com.mygdx.game;

import com.badlogic.gdx.Gdx;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Administrator on 10-Nov-16.
 */
public class MyServer extends GameClient {

    private static ServerSocket serverSocket, voiceServer;

    public MyServer(GameListener callback, String localAddress) {
        super(callback, localAddress);
    }

    @Override
    public void run() {
        try {
            MainMenuScreen.debugText = "Creating server on port no. " + GameClient.port + "\n";

            try {
                socket.close();
            } catch (Exception e) {}
            try {
                voiceSocket.close();
            } catch (Exception e) {}
            try {
                serverSocket.close();
            } catch (Exception e) {}
            try {
                voiceServer.close();
            } catch (Exception e) {}

            serverSocket = new ServerSocket(port);
            voiceServer = new ServerSocket(voicePort);
            MainMenuScreen.debugText = "Created server at " + localAddress + " on port no. " + GameClient.port + "\n";

            Thread t1 = new Thread(new NormalServerThread());
            t1.start();

            Thread t2 = new Thread(new VoiceServerThread());
            t2.start();
        } catch (IOException io) {
            Gdx.app.log(DeviceAPI.TAG, "Failed to create a server: " + io.toString());
            MainMenuScreen.debugText = "Failed to create a server:\n " + io.getMessage();
            callback.onConnectionFailed();
        }
    }

    private class NormalServerThread implements Runnable {
        @Override
        public void run() {
            try {
                socket = serverSocket.accept();
                if (!socket.getReuseAddress()) socket.setReuseAddress(true);
                if (!socket.getTcpNoDelay()) socket.setTcpNoDelay(true);

                Thread t = new Thread(new ReceiveThread());
                t.start();
                callback.onConnected();
            } catch (IOException io) {
                MainMenuScreen.debugText = "Failed to create a server:\n " + io.getMessage();
                callback.onConnectionFailed();
            }
        }
    }

    private class VoiceServerThread implements Runnable {
        @Override
        public void run() {
            try {
                voiceSocket = voiceServer.accept();
                if (!voiceSocket.getReuseAddress()) voiceSocket.setReuseAddress(true);
                if (!voiceSocket.getTcpNoDelay()) voiceSocket.setTcpNoDelay(true);

                Thread t = new Thread(new VoiceReceiveThread());
                t.start();

                voiceServer.close();
            } catch (Exception io) {
                Gdx.app.log(DeviceAPI.TAG, "VoiceServer exception: " + io.toString());
                try {
                    voiceServer.close();
                } catch (Exception e) {}
            }
        }
    }

    @Override
    public void cancel() {
        try {
            serverSocket.close();
            voiceServer.close();
        } catch (IOException io) {
            Gdx.app.log(DeviceAPI.TAG, "Failed to close server: " + io.toString());
        }
    }

    @Override
    public int getPlayerNumber() {
        return GameListener.PLAYER1;
    }
}
