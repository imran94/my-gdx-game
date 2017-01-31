package com.mygdx.game;

import com.badlogic.gdx.Gdx;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Created by Administrator on 29-Nov-16.
 */
abstract public class GameClient implements GameClientInterface {

    static Socket socket, voiceSocket;

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
    public void disconnect() {
        sendMessage("Disconnect");
        try {
            socket.close();
            voiceSocket.close();
        } catch(IOException io) {}
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
            Gdx.app.log(DeviceAPI.TAG, "Failed to send message");
        }
    }

    @Override
    public void sendVoiceMessage(byte[] message) {
        try {
            DataOutputStream dos = new DataOutputStream(voiceSocket.getOutputStream());
            dos.write(message, 0, message.length);
            dos.flush();
//            Gdx.app.log(DeviceAPI.TAG, "Packet sent to: " + packet.getAddress());

        } catch (Exception e) {
            Gdx.app.log(DeviceAPI.TAG, "Failed to send voice: " + e.toString());
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
                Gdx.app.log(DeviceAPI.TAG, "Message sent");
            } catch (IOException io) {
                Gdx.app.log(DeviceAPI.TAG, "Failed to send message");
            }
        }
    }

    protected class ReceiveThread implements Runnable {
        public void run() {
            while (isConnected()) {
                try {
                    ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                    String message = ois.readUTF();

                    if (message.equals("Disconnect")) {
                        callback.onDisconnected();
                        break;
                    } else {
                        callback.onMessageReceived(message);
                    }
                } catch (Exception io) {
//                    Gdx.app.log(DeviceAPI.TAG, "Disconnected");
//                    callback.onDisconnected();
                }
            }

            disconnect();
        }
    }

    protected class VoiceReceiveThread implements  Runnable {
        public void run() {
            Gdx.app.log(DeviceAPI.TAG, "voiceReceiveThread started");

//            callback.getDeviceAPI().startRecording();

            while (isConnected()) {
                try {
                    byte[] message = new byte[callback.getDeviceAPI().getBufferSize()];

                    DataInputStream dis = new DataInputStream(voiceSocket.getInputStream());
                    dis.read(message);
//                    callback.getDeviceAPI().transmit(message);
                } catch (IOException io) {}
            }
        }
    }
}
 