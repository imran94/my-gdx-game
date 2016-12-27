package com.mygdx.game;

import com.badlogic.gdx.Gdx;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;

/**
 * Created by Administrator on 29-Nov-16.
 */
abstract public class GameClient implements GameClientInterface {

    static Socket socket;
    static DatagramSocket dgSocket;

    GameListener callback;
    String localAddress;

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
    public void sendVoiceMessage(byte[] message) {
        DatagramPacket packet = new DatagramPacket(message, message.length, socket.getInetAddress(),
                port);
        try {
            dgSocket.send(packet);
//            Gdx.app.log(MultiplayerController.TAG, "Packet sent to: " + packet.getAddress());

        } catch (Exception e) {
            Gdx.app.log(MultiplayerController.TAG, "Failed to send packet: " + e.toString());
        }
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

    protected class VoiceReceiveThread implements  Runnable {
        public void run() {
            Gdx.app.log(MultiplayerController.TAG, "voiceReceiveThread started");

            while (isConnected()) {
                try {
                    Gdx.app.log(MultiplayerController.TAG, "voiceReceiveThread isConnected");

                    byte buf[] = new byte[1];
                    DatagramPacket packet = new DatagramPacket(buf, buf.length);
                    dgSocket.receive(packet);

                    String s = new String(packet.getData());
                    Gdx.app.log(MultiplayerController.TAG, "Packet received: " + s);
                } catch (IOException io) {
                    Gdx.app.log(MultiplayerController.TAG, "voiceReceiveThread error: " + io.toString());
                }
            }
        }
    }
}
