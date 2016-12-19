package com.mygdx.game;

import com.badlogic.gdx.Gdx;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Created by Administrator on 29-Nov-16.
 */
abstract public class GameClient implements GameClientInterface {

    static Socket socket;

    GameListener callback;
    String localAddress;
    Thread receiveThread;

    public GameClient(GameListener callback, String localAddress) {
        this.callback = callback;
        this.localAddress = localAddress;
    }

    @Override abstract public void run();
    @Override abstract public int getPlayerNumber();
    @Override abstract public void cancel();

    public String getLocalSubnet() {
        String[] bytes = localAddress.split("\\.");
        return bytes[0] + "." + bytes[1] + "." + bytes[2] + ".";
    }

    @Override
    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }

    @Override
    public void onConnected() {
        callback.onConnected();
    }

    @Override
    public void onDisconnected() {
        try {
            socket.close();
        } catch(IOException io) {}

        callback.onDisconnected();
    }

    @Override
    public void setListener(GameListener callback) {
        this.callback = callback;
    }

    @Override
    public void sendMessage(String message) {
        Thread t = new Thread(new MessageThread(message));
        t.start();
    }

    @Override
    public void sendMessage(byte[] message) {
        Thread t = new Thread(new MessageThread(message));
        t.start();
    }

    protected class MessageThread implements Runnable {
        byte[] message;

        public MessageThread(String message) {
            this.message = message.getBytes();
        }

        public MessageThread(byte[] message) {
            this.message = message;
        }

        @Override
        public void run() {
            try {
                ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                oos.write(message);
                oos.flush();
                Gdx.app.log(MultiplayerController.TAG, "Message sent");
            } catch (IOException io) {
                Gdx.app.log(MultiplayerController.TAG, "Failed to send message");
            }
        }
    }

    protected class ReceiveThread implements Runnable {
        public void run() {
            MainMenuScreen.debugText = "ReceiveThread running";
            while (isConnected()) {
                MainMenuScreen.debugText = "isConnected";
                byte[] message = new byte[4096];
                try {
                    ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                    Gdx.app.log("mygdxgame", "ois Created");
                    ois.readFully(message);
                    Gdx.app.log("mygdxgame", "Message read: " + new String(message));
                    String s = new String(message);

                    Gdx.app.log(MultiplayerController.TAG, "Message Received: " + s.length());
                    if (s.length() <= 10) {
                        callback.onMessageReceived(s);
                        Gdx.app.log("mygdxgame", "onMessageReceived");
                    } else {
                        Gdx.app.log("mygdxgame", "Message length greater than 10");
                        callback.getDeviceAPI().transmit(message, 4096);
                        Gdx.app.log("mygdxgame", "Transmitting");
                    }
                } catch (Exception io) {
                    Gdx.app.log("mygdxgame", io.toString());
                }
            }

            onDisconnected();
        }
    }
}
