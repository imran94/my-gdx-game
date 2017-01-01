package com.mygdx.game;

import com.badlogic.gdx.Gdx;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Administrator on 10-Nov-16.
 */
public class MyClient extends GameClient {

    ExecutorService pool;

    public MyClient(GameListener callback, String localAddress) {
        super (callback, localAddress);
    }

    @Override
    public void run() {
        int bitCount = 254;

        pool = Executors.newFixedThreadPool(bitCount);

        String subnet = getLocalSubnet();
        for (int i = 0; i <= bitCount; i++) {
            Runnable task = new ConnectThread(subnet + i);
            pool.submit(task);
        }
        MainMenuScreen.debugText = "Looking for servers...";
        pool.shutdown();

        while (!isConnected() && !pool.isTerminated()) {}

        if (isConnected()) {
            if (!pool.isTerminated()) pool.shutdownNow();

            MainMenuScreen.debugText = "Successfully connected to " + socket.getInetAddress();

            Thread receiveThread = new Thread(new ReceiveThread());
            receiveThread.start();

            Thread voiceReceiveThread = new Thread(new VoiceReceiveThread());
            voiceReceiveThread.start();

            callback.onConnected();
        } else {
            callback.onConnectionFailed();
        }
    }

    @Override
    public int getPlayerNumber() {
        return GameListener.PLAYER2;
    }

    @Override
    public void cancel() {
        if (isConnected()) {
            try {
                socket.close();
            } catch (IOException io) {
                Gdx.app.log(MultiplayerController.TAG, "Failed to close socket: " + io.toString());
            }
        }

        if (pool != null && pool.isShutdown() && !pool.isTerminated()) pool.shutdownNow();
    }

    private class ConnectThread implements Runnable {

        String address;

        public ConnectThread(String address) {
            this.address = address;
        }

        public void run() {
            try {
                Socket s = new Socket(address, port);

                voiceSocket = new Socket(s.getLocalAddress(), voicePort);
                if (!voiceSocket.getReuseAddress()) voiceSocket.setReuseAddress(true);
                if (!voiceSocket.getTcpNoDelay()) voiceSocket.setTcpNoDelay(true);

                socket = s;
                if (!socket.getReuseAddress()) socket.setReuseAddress(true);
                if (!socket.getTcpNoDelay()) socket.setTcpNoDelay(true);

                Gdx.app.log("mygdxgame", "Successfully connected to " + address);
                MainMenuScreen.debugText = "Successfully connected to " + address;
            } catch (IOException io) {
                MainMenuScreen.debugText += "\nConnection to " + address + " failed";
            }
        }
    }
}
