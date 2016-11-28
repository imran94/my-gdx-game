package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.esotericsoftware.kryonet.Client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Administrator on 10-Nov-16.
 */
public class MyClient implements GameClientInterface {
    Client client;
    Character character, otherCharacter;

    static Socket socket;
    int address;

    GameListener callback;
    List<InetAddress> ipAddresses;
    String localAddress;

    public MyClient(GameListener callback, String localAddress) {
        this.callback = callback;
        this.localAddress = localAddress;

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
        ipAddresses = new ArrayList<InetAddress>();
        int bitCount = 254;

//        String ip = "192.168.0.114";
//        try {
//            Socket s = new Socket(ip, GameClient.port);
//            socket = s;
//            Thread t = new Thread(new ReceiveThread());
//            t.start();
//            MainMenuScreen.debugText = "Successfully connected to " + ip;
//        } catch(IOException io) {
//            MainMenuScreen.debugText = "Failed to connect\n" + io.getMessage();
//            Gdx.app.log("mygdxgame", io.getMessage());
//        }

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

//        for (InetAddress ip : ipAddresses) {
//            try {
//                MainMenuScreen.debugText += "\nTrying " + ip;
//                Socket s = new Socket(ip, GameClient.port);
//                socket = s;
//                MainMenuScreen.debugText += "\nSuccessfully connected to " + ip;
//                callback.onConnected();
//                Thread t = new Thread(new ReceiveThread());
//                t.start();
//
//                break;
//            } catch (IOException io) {
//                MainMenuScreen.debugText += "\nUnable to connect to " + ip;
//            }
//        }
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

    boolean sending = false;

    private class MessageThread implements Runnable {
        String message;

        public MessageThread(String message) {
            this.message = message;
        }

        @Override
        public void run() {
            if (!isConnected() || sending) return;

            sending = true;
            try {
//                OutputStream out = socket.getOutputStream();
//                Writer writer = new OutputStreamWriter(out, "UTF-8");
//                writer.write(message);

                ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                oos.writeUTF(message);
                oos.flush();

                MainMenuScreen.debugText = "Send message: " + message;
            } catch (IOException io) {
                MainMenuScreen.debugText = "Unable to send message. " + io.getMessage();
            }
            sending = false;
        }
    }

    private class DiscoverThread implements Runnable {
        String subnet;
        final int timeout = 1000;

        public DiscoverThread(String subnet) {
            this.subnet = subnet;
        }

        public void run() {
            try {
                InetAddress ip = InetAddress.getByName(subnet);
//
                if (ip.isReachable(timeout)) {
                    MainMenuScreen.debugText += "\n" + ip + " isReachable.";
                    ipAddresses.add(ip);
                }

//                if (callback.getDeviceAPI().isReachable(subnet)) {
//                    ipAddresses.add(ip);
//                    MainMenuScreen.debugText += "\n" + subnet + "isReachable";
//                } else {
//                    MainMenuScreen.debugText += "\n" + subnet + "is Unreachable";
//
//                }

//                SocketAddress sockAddr = new InetSocketAddress(subnet, GameClient.port);
//                Socket s = new Socket();
//                s.connect(sockAddr, 5000);
//                socket = s;
//                MainMenuScreen.debugText += "\n" + subnet + " isReachable";

            } catch (IOException io) {

            }
        }
    }

    private class ReceiveThread implements Runnable {

        public void run() {
            MainMenuScreen.debugText = "ReceiveThread started";
            while (isConnected()) {
                try {
                    MainMenuScreen.debugText += "\nReading message";

                    ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                    String message = ois.readUTF();

                    MainMenuScreen.debugText += "\nMessage received: " + message;
                    callback.onMessageReceived(message);
                } catch (IOException io) {
                    MainMenuScreen.debugText += "\n" + io.getMessage();
                    Gdx.app.log("mygdxgame", io.getMessage());
                }
            }

            MainMenuScreen.debugText = "Exited ReceiveThread";
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

    public InetAddress[] getLocalHosts() {
        MainMenuScreen.debugText += "\ngetLocalHosts()";

        String subnet = "192.168.0.";
        int timeout = 5000;
        for (int i = 0; i < 255; i++) {
            try {
                InetAddress ip = InetAddress.getByName(subnet + i);

                if (ip.isReachable(timeout)) {
                    ipAddresses.add(ip);
                    MainMenuScreen.debugText += "\n" + ip + " isReachable";
                }
            } catch (IOException io) {}
        }

        return (InetAddress[]) ipAddresses.toArray();
    }

    public String getLocalSubnet() {
        String[] bytes = localAddress.split("\\.");

        StringBuilder subnet = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            subnet.append(bytes[i] + ".");
        }

        return subnet.toString();
    }
}
