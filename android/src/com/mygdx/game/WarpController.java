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

    public void log(String message) { Log.d(TAG, message); }
}
