package com.mygdx.game;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class MyGdxGame extends Game implements ApplicationListener {
	SpriteBatch batch;
	ShapeRenderer shapeRenderer;
	Texture img;
	Application.ApplicationType appType;
	BitmapFont font;
	OrthographicCamera camera;
	static String displayText = "";

	int x2;

	private final MultiplayerController mController;

	String TAG = "Game";

	public MyGdxGame(MultiplayerController mController) {
		this.mController = mController;
	}

	@Override
	public void create () {
		setScreen(new MainMenuScreen(this, mController));
//		setScreen(new GameScreen(this));

//		batch = new SpriteBatch();
//		shapeRenderer = new ShapeRenderer();
//		img = new Texture("badlogic.jpg");
//		appType = Gdx.app.getType();
//		font = new BitmapFont();
//
//		camera = new OrthographicCamera();
//		camera.setToOrtho(false);
//
//		x2 = Gdx.graphics.getWidth() - 100;
//
//		try {
//			Gdx.app.log(TAG, "initializing warpclient");
//
//			Gdx.app.log(TAG, "Warpclient initialized");
//		} catch (Exception e) {
//			Gdx.app.log(TAG, "Unable to initialize");
//			e.printStackTrace();
//		}
	}

	int x = 0;
	int y = 0;

	@Override
	public void render () {
		super.render();
//		Gdx.gl.glClearColor(1, 0, 0, 1);
//		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

//		if (appType == Application.ApplicationType.Android) {
//			if (Gdx.input.isTouched()) {
//				x = Gdx.input.getX();
//				y = Gdx.input.getY();
//			}
//		} else {
//
//		}
//
//		x = Gdx.input.getX(0);
//		y = Gdx.input.getY(0);
//		y = Math.abs(Gdx.graphics.getHeight()-y);
//
//		shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
//		shapeRenderer.setColor(Color.BLACK);
//		shapeRenderer.rect(5, y, 100, 100);
//		shapeRenderer.end();
//
//		shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
//		shapeRenderer.setColor(Color.BLACK);
//		shapeRenderer.rect(x2, y, 100, 100);
//		shapeRenderer.end();
//
//		batch.begin();
//		font.draw(batch, displayText, 5,20);
//		batch.end();
	}

	@Override
	public void dispose () {
//		batch.dispose();
//		img.dispose();
	}

	public static void showText(String text) {
		displayText = text;
	}
}
