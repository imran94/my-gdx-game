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
        try {
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            oos.writeUTF(message);
            oos.flush();
        } catch (IOException io) {
            Gdx.app.log(MultiplayerController.TAG, "Failed to send message");
        }
    }

    @Override
    public void sendMessage(byte[] message) {
//        Thread t = new Thread(new MessageThread(message));
//        t.start();
    }

    protected class MessageThread implements Runnable {
        String message;

        public MessageThread(String message) {
            this.message = message;
        }

        @Override
        public void run() {
            try {
                ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                oos.writeUTF(message);
                oos.flush();
                Gdx.app.log(MultiplayerController.TAG, "Message sent");
            } catch (IOException io) {
                Gdx.app.log(MultiplayerController.TAG, "Failed to send message");
            }
        }
    }

    protected class ReceiveThread implements Runnable {
        public void run() {
            while (isConnected()) {
                try {
                    ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                    String message = ois.readUTF();

                    callback.onMessageReceived(message);
                } catch (Exception io) {
                    MainMenuScreen.debugText += "\n" + io.getMessage();
                    Gdx.app.log("mygdxgame", io.getMessage());
                }
            }

            onDisconnected();
        }
    }
}
