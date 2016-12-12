package com.mygdx.game;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.net.ConnectivityManager;
import android.content.Context;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.health.PackageHealthStats;
import android.util.Log;
import android.widget.Toast;

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
import java.util.stream.Stream;

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

    public void showNotification(String message) {

    }

    public boolean isConnectedToLocalNetwork() {
        ConnectivityManager connManager =
                (ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connManager.getActiveNetworkInfo();
        return info != null && info.isConnected();
    }

    public void startRecording() {
        Thread t = new Thread(new StreamThread());
        t.start();
    }

    final int maxBufferSize = 4096;
    final int sampleRate = 8000;
    final int audioFormat = AudioFormat.ENCODING_PCM_16BIT;

    private class StreamThread implements Runnable {

        byte[] buffer;
        AudioRecord recorder;

        private int recordChannelConfig = AudioFormat.CHANNEL_IN_MONO;

        public void run() {
            int minBufferSize = AudioRecord.getMinBufferSize(sampleRate, recordChannelConfig, audioFormat);
            int recorderBufSize = Math.max(minBufferSize, maxBufferSize);
            Log.d(TAG, "Buffer size: " + recorderBufSize);
            buffer = new byte[recorderBufSize];

            try {
                recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                        sampleRate,
                        recordChannelConfig,
                        audioFormat,
                        recorderBufSize);

                recorder.startRecording();
                Log.d(TAG, "Started recording");

                while (true) {
                    recorderBufSize = recorder.read(buffer, 0, buffer.length);

                    Thread t = new Thread(new SpeakerThread(buffer));
                    t.start();
                }
            } catch (Throwable t) {
                Log.d(TAG, t.getMessage());
            } finally {
                recorder.release();
            }
        }
    }

    boolean playing = false;

    private class SpeakerThread implements Runnable {

        byte[] buffer;
        AudioTrack speaker;

        private int speakerChannelConfig = AudioFormat.CHANNEL_OUT_MONO;


        public SpeakerThread(byte[] buffer) {
            this.buffer = buffer;
        }

        public void run() {
            if (playing) return;

            playing = true;

            try {
                int minBufSize = AudioTrack.getMinBufferSize(sampleRate, speakerChannelConfig, audioFormat);

                speaker = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate, speakerChannelConfig, audioFormat, minBufSize, AudioTrack.MODE_STREAM);
                Log.d(TAG, "Speaker initialised");
                speaker.play();
                speaker.write(buffer, 0, minBufSize);
                Log.d(TAG, "Writing to speaker");
            } catch (Throwable t) {
                Log.d(TAG, "Error: " + t.getMessage());
            } finally {
                speaker.release();
                Log.d(TAG, "Released speaker");
            }
            playing = false;
        }
    }
}
