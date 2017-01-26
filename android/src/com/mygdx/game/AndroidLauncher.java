package com.mygdx.game;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.mygdx.game.MyGdxGame;

public class AndroidLauncher extends AndroidApplication implements MyCallback {
	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();

		initialize(new MyGdxGame(new WarpController(this)), config);
	}

	@Override
	public void showNotification(String message) {
		Toast.makeText(this, message, Toast.LENGTH_SHORT);
	}
}
