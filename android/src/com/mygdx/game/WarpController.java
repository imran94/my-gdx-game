package com.mygdx.game;

import android.net.ConnectivityManager;
import android.content.Context;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.health.PackageHealthStats;
import android.util.Log;

import com.shephertz.app42.gaming.multiplayer.client.WarpClient;
import com.shephertz.app42.gaming.multiplayer.client.command.WarpResponseResultCode;
import com.shephertz.app42.gaming.multiplayer.client.events.RoomEvent;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.ByteOrder;
import java.nio.charset.MalformedInputException;
import java.util.Formatter;

/**
 * Created by Administrator on 04-Nov-16.
 */
public class WarpController implements MultiplayerController {
    private static WarpController instance;

    private final String apiKey = "0e7f21e9c0b9250383e717a51243a1de0f19d979d3c0995fb307f4cc0b73eb3d";
    private final String secretKey = "2a9f407142da7442b958120b42d771ffa5f230faa9e998eeb91e3d7f267e3ef7";

    private WarpClient warpClient;
    private WarpListener warpListener;

    boolean isConnected = false;
    boolean isUDPEnabled = false;

    String roomId;
    String localUser;

    int STATE;

    public static final int WAITING = 1;
    public static final int STARTED = 2;
    public static final int COMPLETED = 3;
    public static final int FINISHED = 4;

    Context context;

    public WarpController(Context context) {
        this.context = context;
    }

    public String getIpAddress() {
        WifiManager wifiMan =
                (WifiManager) context.getSystemService(context.WIFI_SERVICE);
        int ip = wifiMan.getConnectionInfo().getIpAddress();

        if (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)) {
            ip = Integer.reverseBytes(ip);
        }

        byte[] ipByteArray = BigInteger.valueOf(ip).toByteArray();

        String ipAddress;
        try {
            ipAddress = InetAddress.getByAddress(ipByteArray).getHostAddress();
        } catch (UnknownHostException e) {
            ipAddress = "Unable to get host address";
        }

        return ipAddress;
    }

    public boolean isReachable(String ip) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();

        if (info !=null && info.isConnected()) {
            try {
                URL url = new URL("http://" + ip);
                HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
                urlc.setConnectTimeout(10 * 1000);
                urlc.connect();
                if (urlc.getResponseCode() == 200) {
                    return true;
                } else {
                    Log.d("mygdxgame", "Response code: " + urlc.getResponseCode());
                    return false;
                }
            } catch (MalformedInputException e) {
                Log.d("mygdxgame", "MalformedInputException: " + e.getMessage());
                return false;
            } catch (IOException e) {
                Log.d("mygdxgame", "IOException: " + e.getMessage());
                return false;
            }
        }

        return false;
    }

    public void initialize() {
        try {
            MyGdxGame.showText("Initializing");
            WarpClient.initialize(apiKey, secretKey);
            warpClient = WarpClient.getInstance();
            warpClient.connectWithUserName(Build.MODEL);
            MyGdxGame.showText("Successfully connected");
        } catch (Exception e) {
            MyGdxGame.showText("Unable to initialize: " + e.toString());
            e.printStackTrace();
        }
    }

    public void stopApp() {
//        if (isConnected) {
//            warpClient.unsubscribeRoom(roomId);
//            warpClient.leaveRoom(roomId);
//        }

        warpClient.disconnect();
    }

    public void onConnectDone(boolean status){
        MyGdxGame.showText("onConnectDone: "+status);
        if(status){
            warpClient.initUDP();
            warpClient.joinRoomInRange(1, 1, false);
        }else{
            isConnected = false;
        }
    }

    public void onDisconnectDone(boolean status) {

    }

    public void setListener(WarpListener listener) {
        this.warpListener = listener;
    }

    public void sendGameUpdate(String msg) {
        if (isConnected) {
            if (isUDPEnabled) {
                warpClient.sendUDPUpdatePeers(msg.getBytes());
            } else {
                warpClient.sendUpdatePeers(msg.getBytes());
            }
        }
    }

    public void updateResult(int code, String msg) {}

    public void onRoomCreated(String roomId) {
        if (roomId != null) {
            warpClient.joinRoom(roomId);
            MyGdxGame.showText("Joined Room successfully");
        } else {
            MyGdxGame.showText("Unable to join room");
        }
    }

    public void onJoinRoomDone(RoomEvent event) {
        MyGdxGame.showText("onJoinRoomDone: " + event.getResult());
        if (event.getResult() == WarpResponseResultCode.SUCCESS) {
            this.roomId = event.getData().getId();
            warpClient.subscribeRoom(roomId);
        }
    }

    public void onRoomSubscribed(String roomId) {
        MyGdxGame.showText("onSubscribeRoomDone: " + roomId);
        if (roomId != null) {
            isConnected = true;
            warpClient.getLiveRoomInfo(roomId);
        }
    }

    public void onGetLiveRoomInfo(String[] liveUsers) {
        MyGdxGame.showText("onGetLiveRoomInfo: " + liveUsers.length + "users");
    }

    public void onUserJoinedRoom(String roomId, String userName){
        if (!localUser.equals(userName)) {
            startGame();
        }
    }

    public void onSendChatDone(boolean status) {}

    public void onGameUpdateReceived(String message) {
        warpListener.onGameUpdateReceived(message);
    }

    public void onResultUpdateReceived(String userName, int code) {

    }

    public void onUserLeftRoom(String roomId, String userName) {}
    public int getState() { return this.STATE; }
    private void startGame() {}
    private void waitForOtherUser() {}
    private void handleError() {}
    private void disconnect() {}
}
