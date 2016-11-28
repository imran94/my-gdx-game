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
public class MyServer implements GameClient {

    ServerSocket serverSocket;
    static Socket socket;
    GameListener callback;
    String localAddress;

    public MyServer(GameListener callback, String localAddress) {
        this.callback = callback;
        this.localAddress = localAddress;
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

    @Override
    public void onConnected(Socket s) {
        callback.onConnected();
    }

    @Override
    public void setListener(GameListener callback) {
        this.callback = callback;
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }

    public String getLocalSubnet() {
        String[] bytes = localAddress.split("\\.");

        StringBuilder subnet = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            subnet.append(bytes[i] + ".");
        }

        return subnet.toString();
    }

    boolean sending = false;

    @Override
    public void sendMessage(String message) {
//        Thread t = new Thread(new MessageThread(message));
//        t.start();

        if (!isConnected() || sending) return;

        sending = true;
        try {
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            oos.writeUTF(message);
            oos.flush();

            MainMenuScreen.debugText = "Send message " + message;
        } catch (IOException io) {
            MainMenuScreen.debugText = "Unable to send message. " + io.getMessage();
        }
        sending = false;
    }

    private class MessageThread implements Runnable {

        String message;

        public MessageThread(String message) {
            this.message = message;
        }

        @Override
        public void run() {
            if (!isConnected()) return;

            try {
                ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                oos.writeUTF(message);
                oos.flush();

                MainMenuScreen.debugText = "Send message: " + message;
            } catch (IOException io) {
                MainMenuScreen.debugText = "Unable to send message. " + io.getMessage();
            }
        }
    }

    private class ReceiveThread implements Runnable {
        public void run() {
            MainMenuScreen.debugText = "ReceiveThread running";
            while (isConnected()) {
                MainMenuScreen.debugText += "\nisConnected";
                try {
                    ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                    String message = ois.readUTF();

                    MainMenuScreen.debugText += "\nMessage received: " + message;
                    callback.onMessageReceived(message);
                } catch (IOException io) {
                    MainMenuScreen.debugText += "\n" + io.getMessage();
                    Gdx.app.log("mygdxgame", io.getMessage());
                }
            }
        }
    }
}
