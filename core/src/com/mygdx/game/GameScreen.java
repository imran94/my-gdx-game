package com.mygdx.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * Created by Administrator on 15-Nov-16.
 */
public class GameScreen implements Screen, GameListener {

    Game game;
    MultiplayerController mController;
    GameClientInterface gameClient;

    OrthographicCamera guiCam;
    SpriteBatch batch;
    Vector3 touchPoint;

    Texture img, puckImg, paddleImg1, paddleImg2;
    TextureRegion background;
    Sprite backgroundSprite, paddleSprite1, paddleSprite2;

    Puck puck;

    ShapeRenderer shapeRenderer;

    Viewport gamePort;

    final int GAME_WIDTH = 320;
    final int GAME_HEIGHT = 480;

    int SCREEN_WIDTH, SCREEN_HEIGHT;

    int paddleRadius = 70;

    float PADDLE_WIDTH,PADDLE_HEIGHT;

    public GameScreen(Game game, MultiplayerController mController) {
        this.game = game;
        this.mController = mController;

        createGame();
    }

    public GameScreen(Game game, MultiplayerController mController, GameClientInterface gameClient) {
        this.game = game;
        this.mController = mController;
        this.gameClient = gameClient;
        this.gameClient.setListener(this);

        createGame();
    }

    public void createGame() {
        shapeRenderer = new ShapeRenderer();

        guiCam = new OrthographicCamera();
        gamePort = new StretchViewport(GAME_WIDTH, GAME_HEIGHT, guiCam);
        guiCam.setToOrtho(false);
        guiCam.position.set(0, 0, 0);

        batch = new SpriteBatch();
        batch.setProjectionMatrix(guiCam.combined);

        SCREEN_WIDTH = Gdx.graphics.getWidth();
        SCREEN_HEIGHT = Gdx.graphics.getHeight();

        img = new Texture("Board2.png");
        background = new TextureRegion(img, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        backgroundSprite = new Sprite(img);
//        backgroundSprite.setSize(1f, backgroundSprite.getHeight() / backgroundSprite.getWidth());
        backgroundSprite.setSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
//        backgroundSprite.setSize(GAME_WIDTH, GAME_HEIGHT);

        puck = new Puck();

        paddleImg1 = new Texture("Paddle.png");
        paddleImg2 = new Texture("Paddle.png");

        paddleSprite1 = new Sprite(paddleImg1);
        paddleSprite2 = new Sprite(paddleImg2);

        paddleSprite1.setSize(paddleRadius * SCREEN_WIDTH / GAME_WIDTH,
                paddleRadius * SCREEN_HEIGHT / GAME_HEIGHT);
        paddleSprite2.setSize(paddleRadius * SCREEN_WIDTH / GAME_WIDTH,
                paddleRadius * SCREEN_HEIGHT / GAME_HEIGHT);

        PADDLE_WIDTH = paddleSprite1.getWidth();
        PADDLE_HEIGHT = paddleSprite2.getHeight();

        paddleRadius = paddleRadius * SCREEN_WIDTH / GAME_WIDTH;

        touchPoint = new Vector3();
    }

    public void setGameClient(GameClientInterface gameClient) {
        this.gameClient = gameClient;
        this.gameClient.setListener(this);
    }

    float x1 = Gdx.graphics.getWidth() / 2;
    float x2 = Gdx.graphics.getWidth() / 2;

    float y1 = 150;
    float y2 = Gdx.graphics.getHeight() - 150;

    float x3 = Gdx.graphics.getWidth() / 2;
    float y3 = Gdx.graphics.getHeight() / 2;

    float puckSpeedX = 0, puckSpeedY = 0;
    int paddleMass = 10, puckMass = 1;

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(1, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        update();

        batch.begin();
//            batch.draw(background, 0,0);
//            backgroundSprite.draw(batch);
            draw();
            drawPaddle1();
            drawPaddle2();
            drawPuck();
        batch.end();
    }

    public void draw() {
        backgroundSprite.draw(batch);
    }

    public void drawPaddle1() {
//        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
//        shapeRenderer.setColor(Color.RED);
//        shapeRenderer.circle(x1, y1, paddleRadius);
//        shapeRenderer.end();

//        batch.draw(paddleImg1, x1 - paddleImg1.getWidth()/2,
//                y1 - paddleImg1.getHeight()/2);

        paddleSprite1.setPosition(x1 - PADDLE_WIDTH / 2, y1 - PADDLE_HEIGHT / 2);
        paddleSprite1.draw(batch);
    }

    public void drawPaddle2() {
//        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
//        shapeRenderer.setColor(Color.RED);
//        shapeRenderer.circle(x2, y2, paddleRadius);
//        shapeRenderer.end();

        paddleSprite2.setPosition(x2 - PADDLE_WIDTH / 2, y2 - PADDLE_HEIGHT / 2);
        paddleSprite2.draw(batch);
    }

    public void drawPuck() {
//        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
//        shapeRenderer.setColor(Color.BLACK);
//        shapeRenderer.circle(x3, y3, puckRadius);
//        shapeRenderer.end();

//        puckSprite.setPosition(x3 - PUCK_WIDTH / 2, y3 - PUCK_HEIGHT / 2);
//        puckSprite.draw(batch);
        puck.draw();
    }

    public void update() {
        if (Gdx.input.isKeyPressed(Input.Keys.BACK)) {
            gameClient.disconnect();
        }

        puck.update();

        if (y3 <= 0 || y3 >= Gdx.graphics.getHeight()) {
            if (x3 > 100 && x3 < Gdx.graphics.getWidth() - 100) {
                resetPuck();
            } else {
                puckSpeedY *= -1;
            }
        }

        if (Gdx.input.isTouched()) {
            guiCam.unproject(touchPoint.set(Gdx.input.getX(), Gdx.input.getY(), 0));

            Gdx.app.log(MultiplayerController.TAG, "isTouched: " + touchPoint.x + ", " + touchPoint.y);

//            switch (gameClient.getPlayerNumber()) {
//                case PLAYER1:
//                    x1 = touchPoint.x;
//                    y1 = touchPoint.y;
//                    break;
//                case PLAYER2:
//                    x2 = touchPoint.x;
//                    y2 = touchPoint.y;
//                    break;
//            }

            x1 = touchPoint.x;
            y1 = touchPoint.y;

            float sendX = touchPoint.x * GAME_WIDTH / Gdx.graphics.getWidth();
            float sendY = touchPoint.y * GAME_HEIGHT / Gdx.graphics.getHeight();
//            gameClient.sendMessage(sendX + "," + sendY);

            Gdx.app.log(MultiplayerController.TAG, "Send message: " + sendX + ", " + sendY);
        }

        if (y1 > Gdx.graphics.getHeight() / 2) {
            y1 = Gdx.graphics.getHeight() / 2;
        }

        if (y2 < Gdx.graphics.getHeight() / 2) {
            y2 = Gdx.graphics.getHeight() / 2;
        }

        // Paddle 1 collision
//        if (x1 + paddleRadius + puckRadius > x3
//                && x1 < x3 + paddleRadius + puckRadius
//                && y1 + paddleRadius + puckRadius > y3
//                && y1 < y3 + paddleRadius + puckRadius) {
//            double dist = Math.sqrt(
//                    ((x1 - x3) * (x1 - x3))
//                    + ((y1 - y3) * (y1 - y3))
//            );
//
//            if (dist < paddleRadius + puckRadius) {
////                puckSpeedX = (puckSpeedX * (puckMass - paddleMass) + (2 * paddleMass)) / (paddleMass + puckMass);
////                puckSpeedY = (puckSpeedY * (puckMass - paddleMass) + (2 * paddleMass)) / (paddleMass + puckMass);
////                Vector2 delta = ()
//            }
//        }

        if (x3 > x1 - paddleRadius && x3 < x1 - paddleRadius + paddleRadius * 2 && y3 <= y1 + paddleRadius) {
            if (puckSpeedY == 0) {
                puckSpeedY = 5;
            } else {
                puckSpeedY *= -1;
            }

            float deltaX = x3 - (x1 - puck.radius + puck.radius / 2);
            puckSpeedX = (float) (deltaX * 0.4);
        }

        // Paddle 2 collision
//        if (x2 + paddleRadius + puckRadius > x3
//                && x2 < x3 + paddleRadius + puckRadius
//                && y2 + paddleRadius + puckRadius > y3
//                && y2 < y3 + paddleRadius + puckRadius) {
//
//        }
        if (x3 > x2 - paddleRadius && x3 < x2 - paddleRadius + paddleRadius * 2 && y3 >= y2 - paddleRadius) {
            if (puckSpeedY == 0) {
                puckSpeedY = -5;
            } else {
                puckSpeedY *= -1;
            }

            float deltaX = x3 - (x2 - puck.radius + puck.radius / 2);
            puckSpeedX = (float) (deltaX * 0.4);
        }

    }

    public void resetPuck() {
        x3 = Gdx.graphics.getWidth() / 2;
        y3 = Gdx.graphics.getHeight() / 2;

        puckSpeedX = 0;
        puckSpeedY = 0;
    }

    @Override
    public void onConnected() {

    }

    @Override
    public void onDisconnected() {
//        mController.showNotification("Disconnected from game");
        game.setScreen(new MainMenuScreen(game, mController));
    }

    @Override
    public void onConnectionFailed() {

    }

    @Override
    public void onMessageReceived(String message) {
        Gdx.app.log(MultiplayerController.TAG, "onMessageReceived: " + message);

        String[] coords = message.split(",");

        float otherX = Float.parseFloat(coords[0]) * SCREEN_WIDTH / GAME_WIDTH;
        float otherY = Float.parseFloat(coords[1]) * SCREEN_HEIGHT / GAME_HEIGHT;

        Gdx.app.log(MultiplayerController.TAG, "Calibration after receiving: " + otherX + ", " + otherY);

        switch(gameClient.getPlayerNumber()) {
            case PLAYER1:
                x2 = otherX;
                y2 = otherY;
                break;
            case PLAYER2:
                x1 = otherX;
                y1 = otherY;
                break;
        }
    }

    @Override
    public MultiplayerController getDeviceAPI() {
        return mController;
    }

    class Puck {
        public Vector2d velocity = new Vector2d(0, 0);
        public float x = 0;
        public float y = 0;
        public int radius = 30;

        Texture puckImg;
        Sprite puckSprite;

        public Puck() {
            radius = radius * SCREEN_WIDTH / GAME_WIDTH;

            puckImg = new Texture("Puck.png");
            puckSprite = new Sprite(puckImg);
            puckSprite.setSize(radius * SCREEN_WIDTH / GAME_WIDTH,
                    radius * SCREEN_HEIGHT / GAME_HEIGHT);

            velocity.i = 0;
            velocity.j = 0;
        }

        public float getWidth() {return puckSprite.getWidth();}
        public float getHeight() {return puckSprite.getHeight();}

        public void draw() {
            puckSprite.setPosition(x - getWidth() / 2, y - getHeight() / 2);
            puckSprite.draw(batch);
        }

        public void move() {
            x += velocity.i *= .98;
            y += velocity.j *= .98;

            // bounce off left wall
            if(x <= radius){
                velocity.i = Math.abs(velocity.i); //bounce
                x = radius;
            }

            // bounce off right wall
            if(x >= Gdx.graphics.getWidth() - radius){
                velocity.i = -Math.abs(velocity.i);
                x = Gdx.graphics.getWidth() - x;
            }

            // bounce off bottom
            if(y <= radius){
                velocity.j = Math.abs(velocity.j);
                y = radius;
            }

            // bounce off top
            if(y >= Gdx.graphics.getHeight() - radius){
                velocity.j = -Math.abs(velocity.j);
                y = Gdx.graphics.getHeight() - radius;
            }
        }
    }

    class Player {
        
    }


    @Override public void show() {}
    @Override public void resize(int width, int height) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override public void dispose() {}
}