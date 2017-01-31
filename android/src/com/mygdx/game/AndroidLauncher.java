package com.mygdx.game;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

public class AndroidLauncher extends AndroidApplication implements DeviceAPI {

	private GameClientInterface callback;

	private AudioTrack speaker;
	private AudioRecord recorder;

	private int minBufferSize = 0;
	private final int maxBufferSize = 4096;

	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();

		Log.d(TAG, "API Level: " + Build.VERSION.SDK_INT);

		initialize(new MyGdxGame(this), config);
	}

	@Override
	public void showNotification(final String message) {
		handler.post(new Runnable()
		{
			@Override
			public void run() {
				Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
			}
		});
	}

	public String getIpAddress() {
		WifiManager wifiMan =
				(WifiManager) getContext().getSystemService(getContext().WIFI_SERVICE);
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

	public boolean isConnectedToLocalNetwork() {
		ConnectivityManager connManager =
				(ConnectivityManager) getContext().getSystemService(getContext().CONNECTIVITY_SERVICE);
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
	public void transmit(byte[] message) {
		this.message = message;
		updated = true;
	}

	@Override
	public int getBufferSize() {
		return Math.max(minBufferSize, maxBufferSize);
	}

//	public List<Integer> getValidSampleRates() {
//		List<Integer> list = new ArrayList<>();
//		for (int rate : new int[] {8000, 11025, 16000, 22050, 44100}) {  // add the rates you wish to check against
//			int bufferSize = AudioRecord.getMinBufferSize(rate, recordChannelConfig, audioFormat);
//			if (bufferSize > 0) {
//				Log.d(TAG, rate  + " is supported");
//				list.add(rate);
//			}
//		}

//		return list;
//	}

	private boolean updated = false;
	private byte[] message;

	private class SpeakerThread implements Runnable {
		@Override
		public void run() {
			speaker.play();

			while (callback.isConnected()) {
				if (updated) {
					speaker.write(message, 0, message.length);
					updated = false;
				}
			}
		}
	}

	private class StreamThread implements Runnable {

		@Override
		public void run() {
			int audioSource;
			final int sampleRate = 8000;
			final int audioFormat = AudioFormat.ENCODING_PCM_16BIT;

			int speakerChannelConfig = AudioFormat.CHANNEL_OUT_MONO;
			int recordChannelConfig = AudioFormat.CHANNEL_IN_MONO;

			minBufferSize = AudioRecord.getMinBufferSize(sampleRate, recordChannelConfig, audioFormat);
			int recorderBufSize = Math.max(minBufferSize, maxBufferSize);
			byte[] buffer = new byte[recorderBufSize];

			if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {
				audioSource = MediaRecorder.AudioSource.VOICE_COMMUNICATION;
			} else {
				audioSource = MediaRecorder.AudioSource.VOICE_RECOGNITION;
			}

			try {
				speaker = new AudioTrack(AudioManager.STREAM_MUSIC,
						sampleRate,
						speakerChannelConfig,
						audioFormat,
						recorderBufSize,
						AudioTrack.MODE_STREAM);

				recorder = new AudioRecord(audioSource,
						sampleRate,
						recordChannelConfig,
						audioFormat,
						recorderBufSize);

				Log.d(TAG, "Recorder created");

				recorder.startRecording();
				Log.d(TAG, "Started recording");

				Thread t = new Thread(new SpeakerThread());
				t.start();
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
