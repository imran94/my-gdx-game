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
import android.widget.Toast;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteOrder;

/**
 * Created by Administrator on 04-Nov-16.
 */
public class WarpController implements MultiplayerController {

    Context context;
    MyCallback androidCallback;
    GameClientInterface callback;


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

    final int maxBufferSize = 4096;
    final int sampleRate = 8000;
    final int audioFormat = AudioFormat.ENCODING_PCM_16BIT;

    AudioTrack speaker;
    AudioRecord recorder;
    int speakerChannelConfig = AudioFormat.CHANNEL_OUT_MONO;
    int recordChannelConfig = AudioFormat.CHANNEL_IN_MONO;

    int minBufferSize = 0;
    int recorderBufSize = 0;

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
                    speaker.stop();
                    speaker.flush();
                    speaker.release();
                    Log.d(TAG, "Speaker released");
                }
            }
        }
    }

    boolean playing = false;

    private class SpeakerThread implements Runnable {

        byte[] buffer;
        int bufferSize;
//        AudioTrack speaker;

        public SpeakerThread(byte[] buffer, int bufferSize) {
            this.buffer = buffer;
            this.bufferSize = bufferSize;
        }

        public void run() {
//            if (speaker.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) return;

//            int minBufSize = AudioTrack.getMinBufferSize(sampleRate, speakerChannelConfig, audioFormat);
//            speaker.play();
//            speaker.write(buffer, 0, bufferSize);
//            recorder.startRecording();
//            speaker.stop();
//            if (playing) return;

//            playing = true;

//            try {
//                speaker = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate, speakerChannelConfig, audioFormat, minBufSize, AudioTrack.MODE_STREAM);
//                Log.d(TAG, "Speaker initialised");
//                speaker.play();
                speaker.write(buffer, 0, bufferSize);
                Log.d(TAG, "Writing to speaker");
//            } catch (Throwable t) {
//                Log.d(TAG, "Error: " + t.getMessage());
//            } finally {
//                speaker.release();
//                Log.d(TAG, "Released speaker");
//            }
//            playing = false;
        }
    }
}
