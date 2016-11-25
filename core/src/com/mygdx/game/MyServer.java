package com.mygdx.game;

import com.badlogic.gdx.Net;
import com.badlogic.gdx.graphics.Cursor;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Administrator on 10-Nov-16.
 */
public class MyServer extends Thread implements GameClient {
    Server server;
    ServerSocket serverSocket;
    Character character, otherCharacter;

    final int port = 45351;

    public MyServer() {
//        server = new Server();
//        character = new Character();
//        otherCharacter = new Character();
//
//        Network.register(server);
//
//        server.addListener(new Listener() {
//
//            @Override
//            public void connected(Connection connection) {
//                MainMenuScreen.debugText = "Connected: " + connection.toString();
//            }
//
//            @Override
//            public void disconnected(Connection connection) {
//                MainMenuScreen.debugText = "Disconnected: " + connection.toString();
//            }
//
//            @Override
//            public void received(Connection connection, Object object) {
//                otherCharacter = (Character) object;
//                MainMenuScreen.debugText = "Updated";
//            }
//
//            @Override
//            public void idle(Connection connection) {
//                MainMenuScreen.debugText = "Idle: " + connection.toString();
//            }
//        });
//
////        try {
//            server.bind(Network.port);
////        } catch(Exception e) {
////            MainMenuScreen.debugText = "Fuck you bitch nigga";
////        }
////        bind(Network.port);
//        server.start();
//        System.out.println("Server opened: " + server.toString());
//        MainMenuScreen.debugText = "Server opened: " + server.toString();
    }

    @Override
    public void run() {
        try {
            MainMenuScreen.debugText = "Creating server on port no. " + port + "\n";

            ServerSocket server = new ServerSocket(port);
            MainMenuScreen.debugText = "Created server on port no. " + port + "\n";
            Socket socket = server.accept();
            MainMenuScreen.debugText += "Connected to socket " + socket.getInetAddress();
        } catch (IOException io) {
            MainMenuScreen.debugText = "Failed to create a server:\n " + io.getMessage();
        }
    }

    public void bind(int port) {
        try {
            server.bind(port);
//            MainMenuScreen.debugText = "Successfully bound to port no. " + server.getKryo().;
        } catch (Exception e) {
            MainMenuScreen.debugText = "Retrying with port no. " + (port + 1);
            bind(++port);
        }
    }

    public void send(float x, float y) {
        character.x = (int) x;
        character.y = (int) y;
        server.sendToAllTCP(character);
    }

    public Character getCharacter() {
        return otherCharacter;
    }

    public boolean isConnected() { return server.getConnections().length > 0; }
}
