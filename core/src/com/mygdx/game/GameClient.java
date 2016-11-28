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

    public GameClient(GameListener callback, String localAddress) {
        this.callback = callback;
        this.localAddress = localAddress;
    }

    @Override
    public abstract void run();

    public String getLocalSubnet() {
        String[] bytes = localAddress.split("\\.");

        StringBuilder subnet = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            subnet.append(bytes[i] + ".");
        }

        return subnet.toString();
    }

    @Override
    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }

    @Override
    public void onConnected(Socket s) {
        socket = s;
        callback.onConnected();
    }

    @Override
    public void setListener(GameListener callback) {
        this.callback = callback;
    }

    @Override
    public void sendMessage(String message) {
        Thread t = new Thread(new MessageThread(message));
        t.start();

//        if (!isConnected()) return;
//
//        try {
//            OutputStream out = socket.getOutputStream();
//            OutputStreamWriter writer = new OutputStreamWriter(out);
//            BufferedWriter bw = new BufferedWriter(writer);
//            bw.write(message);
//            bw.flush();
//            MainMenuScreen.debugText = "Send message " + message;
//        } catch (IOException io) {
//            MainMenuScreen.debugText = "Unable to send message. " + io.getMessage();
//        }
    }

    protected class MessageThread implements Runnable {
        String message;
        boolean sending;

        public MessageThread(String message) {
            this.message = message;
            sending = false;
        }

        @Override
        public void run() {
//            if (!isConnected() || sending) return;
//
//            sending = true;
            try {
                ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                oos.writeUTF(message);
                oos.flush();

                MainMenuScreen.debugText = "Send message: " + message;
            } catch (IOException io) {
                MainMenuScreen.debugText = "Unable to send message. " + io.getMessage();
            }
//            sending = false;
        }
    }

    protected class ReceiveThread implements Runnable {
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
