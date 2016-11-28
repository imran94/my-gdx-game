package com.mygdx.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.StringBuilder;
import com.esotericsoftware.kryonet.Server;
import com.sun.org.apache.xpath.internal.operations.Mult;

import org.w3c.dom.Text;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

/**
 * Created by Administrator on 07-Nov-16.
 */
public class MainMenuScreen implements Screen, GameListener {

    Game game;

    Rectangle createBounds;
    TextButton createButton;
    Rectangle joinBounds;
    TextButton joinButton;
    ShapeRenderer shapeRenderer;

    OrthographicCamera guiCam;
    SpriteBatch batch;
    Vector3 touchpoint;

    BitmapFont font;
    public static String debugText = "";

    Skin skin;
    Stage stage;
    boolean hideButtons;

    GameClientInterface gameClient;

    MultiplayerController mController;

    public MainMenuScreen(Game game, MultiplayerController mController) {
        this.game = game;
        shapeRenderer = new ShapeRenderer();
        batch = new SpriteBatch();

        guiCam = new OrthographicCamera(320, 480);
        guiCam.setToOrtho(false);
        guiCam.position.set(0, 0, 0);
        batch.setProjectionMatrix(guiCam.combined);

        createBounds = new Rectangle(160 - 150, 0, Gdx.graphics.getWidth() / 4, Gdx.graphics.getHeight() / 10);
        joinBounds = new Rectangle(160 - 150, 200 + 18, Gdx.graphics.getWidth() / 4, Gdx.graphics.getHeight() / 10);

        createBasicSkin();

        createButton = new TextButton("Create Game", skin);
        createButton.setPosition(160 - 150, 0);
//        createButton.setText("(" + createButton.getX() + ", " + createButton.getY() + ")");
        joinButton = new TextButton("Join Game", skin);
        joinButton.setPosition(160 - 150, 200 + 18);
//        joinButton.setText("(" + joinButton.getX() + ", " + joinButton.getY() + ")");

        stage = new Stage();
        stage.addActor(createButton);
        stage.addActor(joinButton);

        touchpoint = new Vector3();
        font = new BitmapFont();

        hideButtons = false;

        this.mController = mController;
        debugText = mController.getIpAddress();
    }

    @Override
    public void show() {

    }

    boolean justTouched = false;

    public void update() {
        if (Gdx.input.isTouched()) {
            guiCam.unproject(touchpoint.set(Gdx.input.getX(), Gdx.input.getY(), 0));

            x1 = (int)touchpoint.x;
            y1 = (int)touchpoint.y;

            if (gameClient != null && gameClient.isConnected()) {
                debugText += "\nSending message";
                gameClient.sendMessage(x1 + "," + y1);
            }

//            touchpoint.x = guiCam.position.x - guiCam.viewportWidth + touchpoint.x;
//            touchpoint.y = guiCam.position.y - guiCam.viewportHeight + touchpoint.y;
        }

        if (Gdx.input.justTouched() && !justTouched) {
            if (createBounds.contains(touchpoint.x, touchpoint.y)) {
                justTouched = true;
                createServer();
            }

            if (joinBounds.contains(touchpoint.x, touchpoint.y)) {
                justTouched = true;
                runClient();
            }
        }
    }

    int x1 = 0, y1 = 0;
    int x2 = 0, y2 = 0;

    public void draw() {
        Gdx.gl.glClearColor(1, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (!hideButtons) {
            stage.act();
            stage.draw();
        }

        drawCharacter(x1, y1);
        drawCharacter(x2, y2);

        batch.begin();
            font.draw(batch, debugText, 0, guiCam.viewportHeight - 10);
        batch.end();
    }

    @Override
    public void render(float delta) {
        draw();
        update();
    }

    void drawCharacter(int x, int y) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.BLACK);
        shapeRenderer.rect(x, y, 100, 100);
        shapeRenderer.end();
    }

    void createServer() {
        gameClient = new MyServer(this, mController.getIpAddress());
        Thread t = new Thread(gameClient);
        t.start();
    }

    void runClient() {
        debugText = "Starting up client thread";
        gameClient = new MyClient(this, mController.getIpAddress());
        Thread t = new Thread(gameClient);
        t.start();
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {

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
    }

    @Override
    public void onMessageReceived(String message) {
        debugText = "onMessageReceived: " + message;
    }

    @Override
    public void onDisconnected() {
        debugText = "Disconnected";
    }

    public void appendText(String text) { debugText += "\n" + text; }

    public MultiplayerController getDeviceAPI() { return mController; }
}
