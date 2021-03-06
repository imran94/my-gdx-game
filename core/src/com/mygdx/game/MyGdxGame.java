package com.mygdx.game;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Game;

public class MyGdxGame extends Game implements ApplicationListener {

	private final DeviceAPI mController;

	public MyGdxGame(DeviceAPI mController) {
		this.mController = mController;
	}

	@Override
	public void create () {
		setScreen(new MainMenuScreen(this, mController));
//		setScreen(new GameScreen(this, mController));
	}

	@Override
	public void render () {
		super.render();
	}

	@Override
	public void dispose () {}
}
