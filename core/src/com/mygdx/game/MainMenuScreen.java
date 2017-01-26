package com.mygdx.game;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Timer;

/**
 * Created by Administrator on 07-Nov-16.
 */
public class MainMenuScreen implements Screen, GameListener {

    private Game game;

    private Rectangle createBounds, joinBounds, cancelBounds;
    private TextButton createButton, joinButton, cancelButton;

    private OrthographicCamera guiCam;
    private SpriteBatch batch;
    private Vector3 touchpoint;

    private BitmapFont font;
    public static String debugText = "";

    private Skin skin;
    private Stage stage, cancelStage;
    private boolean hideButtons;

    private GameClientInterface gameClient;
    private DeviceAPI mController;

    public MainMenuScreen(Game game, DeviceAPI mController) {
        this.game = game;
        batch = new SpriteBatch();

        guiCam = new OrthographicCamera(GAME_WIDTH, GAME_HEIGHT);
        guiCam.setToOrtho(false);
        guiCam.position.set(0, 0, 0);
        batch.setProjectionMatrix(guiCam.combined);

        createBasicSkin();

        float scaleX = 0.5f * Gdx.graphics.getWidth() / GAME_WIDTH;
        float scaleY = 0.5f * Gdx.graphics.getHeight() / GAME_HEIGHT;

        createButton = new TextButton("Create Game", skin);
        createButton.setPosition(Gdx.graphics.getWidth() / 2 - createButton.getWidth() / 2,
                Gdx.graphics.getHeight() / 2 - createButton.getHeight());
        createButton.getLabel().setFontScale(scaleX, scaleY);

        joinButton = new TextButton("Join Game", skin);
        joinButton.setPosition(Gdx.graphics.getWidth() / 2 - joinButton.getWidth() / 2,
                Gdx.graphics.getHeight() / 2 + joinButton.getHeight());
        joinButton.getLabel().setFontScale(scaleX, scaleY);

        createBounds = new Rectangle(Gdx.graphics.getWidth() / 2 - createButton.getWidth() / 2,
                Gdx.graphics.getHeight() / 2 - createButton.getHeight(),
                createButton.getWidth(), createButton.getHeight());
        joinBounds = new Rectangle(Gdx.graphics.getWidth() / 2 - joinButton.getWidth() / 2,
                Gdx.graphics.getHeight() / 2 + joinButton.getHeight(),
                joinButton.getWidth(), joinButton.getWidth());

        stage = new Stage();
        stage.addActor(createButton);
        stage.addActor(joinButton);

        cancelButton = new TextButton("Cancel", skin);
        cancelButton.setPosition(Gdx.graphics.getWidth() / 2 - cancelButton.getWidth() / 2,
                Gdx.graphics.getHeight() / 2);
        cancelButton.getLabel().setFontScale(scaleX, scaleY);

        cancelBounds = new Rectangle(Gdx.graphics.getWidth() / 2 - cancelButton.getWidth() / 2,
                Gdx.graphics.getHeight() / 2,
                cancelButton.getWidth(), cancelButton.getHeight());

        cancelStage = new Stage();
        cancelStage.addActor(cancelButton);

        touchpoint = new Vector3();
        font = new BitmapFont();

        hideButtons = false;

        this.mController = mController;
        debugText = mController.getIpAddress();
    }

    private void update() {
        if (Gdx.input.isKeyPressed(Input.Keys.BACK)) {
            Gdx.app.exit();
        }

        if (Gdx.input.justTouched()) {
            guiCam.unproject(touchpoint.set(Gdx.input.getX(), Gdx.input.getY(), 0));

            if (!hideButtons) {
                if (createBounds.contains(touchpoint.x, touchpoint.y)) {
                    if (mController.isConnectedToLocalNetwork()) {
                        hideButtons = true;
                        createServer();
                    } else {
                        //                    mController.showNotification("Not connected to a network.");
                        Gdx.app.log("mygdxgame", "Not connected to a network");
                        debugText = "Not connected to a network";
                    }
                }

                if (joinBounds.contains(touchpoint.x, touchpoint.y)) {
                    if (mController.isConnectedToLocalNetwork()) {
                        hideButtons = true;
                        runClient();
                    } else {
                        //                    mController.showNotification("Not connected to a network.");
                        Gdx.app.log("mygdxgame", "Not connected to a network");
                        debugText = "Not connected to a network";
                    }
                }
            } else {
                if (cancelBounds.contains(touchpoint.x, touchpoint.y)) {
                    gameClient.cancel();
                    hideButtons = false;
                }
            }
        }
    }

    private void draw() {
        Gdx.gl.glClearColor(1, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (!hideButtons) {
            stage.act();
            stage.draw();
        } else {
            cancelStage.act();
            cancelStage.draw();
        }

        batch.begin();
            font.draw(batch, debugText, 0, guiCam.viewportHeight - 10);
        batch.end();
    }

    @Override
    public void render(float delta) {
        draw();
        update();
    }

    private void createServer() {
        gameClient = new MyServer(this, mController.getIpAddress());
        Thread t = new Thread(gameClient);
        t.start();
    }

    private void runClient() {
        debugText = "Starting up client thread";
        gameClient = new MyClient(this, mController.getIpAddress());
        Thread t = new Thread(gameClient);
        t.start();
    }

    private void createBasicSkin(){
        //Create a font
        BitmapFont font = new BitmapFont();
        skin = new Skin();
        skin.add("default", font);

        //Create a texture
        Pixmap pixmap = new Pixmap((int)Gdx.graphics.getWidth()/4,(int)Gdx.graphics.getHeight()/10, Pixmap.Format.RGB888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        skin.add("background",new Texture(pixmap));

        //Create a button style
        TextButton.TextButtonStyle textButtonStyle = new TextButton.TextButtonStyle();
        textButtonStyle.up = skin.newDrawable("background", Color.GRAY);
        textButtonStyle.down = skin.newDrawable("background", Color.DARK_GRAY);
        textButtonStyle.checked = skin.newDrawable("background", Color.DARK_GRAY);
        textButtonStyle.over = skin.newDrawable("background", Color.LIGHT_GRAY);
        textButtonStyle.font = skin.getFont("default");
        skin.add("default", textButtonStyle);
    }

    @Override
    public void onConnected() {
        debugText = "Connected to socket";
        mController.setCallback(gameClient);

        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                Gdx.app.log(mController.TAG, "Timer started");
                game.setScreen(new GameScreen(game, mController, gameClient));
                Gdx.app.log(mController.TAG, "GameScreen set");
            }
        }, 2.0f);
    }

    @Override
    public void onMessageReceived(String message) {
    }

    @Override
    public void onDisconnected() {
        debugText = "Disconnected";
        mController.log("Disconnected");
//        mController.showNotification("Lost connection with other player");

        hideButtons = false;
    }

    @Override
    public void onConnectionFailed() {
        debugText = "Failed to find a connection";
        Gdx.app.log(mController.TAG, "Failed to find a connection");
        mController.log("Failed to find a connection");
        hideButtons = false;
//        mController.showNotification("Failed to find a game. Try again.");
    }

    public void appendText(String text) { debugText += "\n" + text; }

    public DeviceAPI getDeviceAPI() { return mController; }

    @Override public void show() {}

    @Override public void resize(int width, int height) {}

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override public void dispose() {}
}
