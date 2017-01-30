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
import android.util.Log;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteOrder;

/**
 * Created by Administrator on 04-Nov-16.
 */
public class WarpController implements DeviceAPI {

    private Context context;
    private MyCallback androidCallback;
    private GameClientInterface callback;

    public WarpController(Context context) {
        this.context = context;
        androidCallback = (MyCallback) context;
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

    public void showNotification(String message) {
        androidCallback.showNotification(message);
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

    @Override
    public void setCallback(GameClientInterface callback) {
        this.callback = callback;
        Log.d(TAG, "Callback set");
    }

    @Override
    public void transmit(byte[] message, int bufferSize) {
        if (speaker != null) {
            speaker.flush();
            speaker.play();
            speaker.write(message, 0, bufferSize);
            speaker.stop();
//            speaker.flush();
//            recorder.startRecording();
//            Thread t = new Thread(new SpeakerThread(message, bufferSize));
//            t.start();
        }
    }

    private final int maxBufferSize = 4096;
    private final int sampleRate = 8000;
    private final int audioFormat = AudioFormat.ENCODING_PCM_16BIT;

    private AudioTrack speaker;
    private AudioRecord recorder;
    private int speakerChannelConfig = AudioFormat.CHANNEL_OUT_MONO;
    private int recordChannelConfig = AudioFormat.CHANNEL_IN_MONO;

    private int minBufferSize = 0;
    private int recorderBufSize = 0;

    @Override
    public int getBufferSize() {
        return Math.max(minBufferSize, recorderBufSize);
    }

    public void getValidSampleRates() {
        for (int rate : new int[] {8000, 11025, 16000, 22050, 44100}) {  // add the rates you wish to check against
            int bufferSize = AudioRecord.getMinBufferSize(rate, recordChannelConfig, audioFormat);
            if (bufferSize > 0) {
                Log.d(TAG, rate  + " is supported");
            }
        }
    }

    private class StreamThread implements Runnable {

        byte[] buffer;

        public void run() {
            minBufferSize = AudioRecord.getMinBufferSize(sampleRate, recordChannelConfig, audioFormat);
            recorderBufSize = Math.max(minBufferSize, maxBufferSize);
            buffer = new byte[recorderBufSize];

            try {
                speaker = new AudioTrack(AudioManager.STREAM_MUSIC,
                        sampleRate,
                        speakerChannelConfig,
                        audioFormat,
                        recorderBufSize,
                        AudioTrack.MODE_STREAM);

                recorder = new AudioRecord(MediaRecorder.AudioSource.VOICE_RECOGNITION,
                        sampleRate,
                        recordChannelConfig,
                        audioFormat,
                        recorderBufSize);

                Log.d(TAG, "Recorder created");

                recorder.startRecording();
                Log.d(TAG, "Started recording");

                speaker.play();
//                Log.d(TAG, "Speaker playing");

                while (callback.isConnected()) {
                    if (recorder.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
                        recorderBufSize = recorder.read(buffer, 0, buffer.length);
                        callback.sendVoiceMessage(buffer);
                    }
                }
            } catch (Throwable t) {
                Log.d(TAG, "Failed to start recording: " + t);
            } finally {
                if (recorder != null) {
                    recorder.release();
                    Log.d(TAG, "Recorder released");
                }

                if (speaker != null) {
                    try {
                        speaker.stop();
                    } catch (Exception e) {}

                    speaker.flush();
                    speaker.release();
                    Log.d(TAG, "Speaker released");
                }
            }
        }
    }
}
