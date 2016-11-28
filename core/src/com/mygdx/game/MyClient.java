package com.mygdx.game;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Administrator on 10-Nov-16.
 */
public class MyClient extends GameClient {

    public MyClient(GameListener callback, String localAddress) {
        super (callback, localAddress);
    }

    private class ConnectThread implements Runnable {

        String subnet;

        public ConnectThread(String subnet) {
            this.subnet = subnet;
        }

        public void run() {
            try {
                Socket s = new Socket();
                s.setReuseAddress(true);

                SocketAddress address = new InetSocketAddress(subnet, GameClient.port);
                s.connect(address);
                socket = s;
                if (!socket.getTcpNoDelay()) socket.setTcpNoDelay(true);

                MainMenuScreen.debugText = "Thread successfully connected to " + subnet;
            } catch (IOException io) {
                MainMenuScreen.debugText += "\nCould not connect to " + subnet;
            }
        }
    }

    @Override
    public void run() {
        int bitCount = 254;

        ExecutorService pool = Executors.newFixedThreadPool(bitCount);

        String subnet = getLocalSubnet();
        for (int i = 0; i <= bitCount; i++) {
            Runnable task = new ConnectThread(subnet + i);
            pool.submit(task);
        }
        pool.shutdown();

        while (!isConnected() && !pool.isTerminated()) {}

        if (isConnected()) {
            if (!pool.isTerminated()) pool.shutdownNow();

            MainMenuScreen.debugText = "Successfully connected to " + socket.getInetAddress();

            Thread t = new Thread(new ReceiveThread());
            t.start();
        } else {
            MainMenuScreen.debugText += "\nCould not find a server";
        }
    }
}
