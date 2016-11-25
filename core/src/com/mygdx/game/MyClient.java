package com.mygdx.game;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Listener.ThreadedListener;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;


/**
 * Created by Administrator on 10-Nov-16.
 */
public class MyClient extends Thread implements GameClient {
    Client client;
    Character character, otherCharacter;

    Socket socket;
    int address;
    final int port = 45351;

    public MyClient(int i) {
        address = i;
//        client = new Client();
//        character = new Character();
//        otherCharacter = new Character();
//
//        client.start();
//
//        Network.register(client);
//
//        client.addListener(new ThreadedListener(new Listener() {
//            @Override
//            public void connected(Connection connection) {
//                MainMenuScreen.debugText = "Client connected: " + connection.toString();
//            }
//
//            @Override
//            public void received(Connection connection, Object object) {
//                otherCharacter = (Character) object;
//            }
//
//            @Override
//            public void disconnected(Connection connection) {
//                MainMenuScreen.debugText = "Client disconnected: " + connection.toString();
//            }
//        }));
//
//        MainMenuScreen.debugText = "Discovering a host";
//        InetAddress address = client.discoverHost(Network.port, 5000);
//
//        if (address == null) {
//            MainMenuScreen.debugText = "Did not find a host";
//        } else {
//            MainMenuScreen.debugText = "Host found. Connecting to " + address;
//            client.connect(5000, address, Network.port);
//        }
//        connect(Network.port);
    }

    @Override
    public void run() {
        try {
            String ipAddress = "192.168.0." + address;
            socket = new Socket(ipAddress, port);
            MainMenuScreen.debugText = "Connected to server at ";
        } catch (IOException io) {
            MainMenuScreen.debugText += "Failed: " + address + "\n";
        }
    }

    public void connect(int port) throws IOException {
        if (port > 60000) {
            MainMenuScreen.debugText = "Did not find a host";
            return;
        }

        InetAddress address = client.discoverHost(port, 5000);

        if (address == null) {
            connect(++port);
        } else {
            MainMenuScreen.debugText = "Host found.\n Connecting to address " + address + "\nPort no.: " + port;
            client.connect(5000, address, port);
        }
    }

    public void send(float x, float y) {
        character.x = (int) x;
        character.y = (int) y;
        client.sendTCP(character);
    }

    public Character getCharacter() {
        return otherCharacter;
    }

    public boolean isConnected() {
        return client.isConnected();
    }
}
