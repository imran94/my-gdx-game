package com.mygdx.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 15-Nov-16.
 */
public class GameScreen implements Screen, GameListener {

    private Game game;
    private MultiplayerController mController;
    private GameClientInterface gameClient;

    private OrthographicCamera guiCam;
    private SpriteBatch batch;
    private Vector3 touchPoint;

    private Texture img;
    private TextureRegion background;
    private Sprite backgroundSprite;

    private Puck puck;
   	private Player player1, player2;
    private Player[] players;

    private Viewport gamePort;

    private final int GAME_WIDTH = 320;
    private final int GAME_HEIGHT = 480;

    private int SCREEN_WIDTH, SCREEN_HEIGHT;

    private float offset = 70;

    Map<Integer, Sprite> spriteMap;

    public GameScreen(Game game, MultiplayerController mController) {
        this.game = game;
        this.mController = mController;
        this.gameClient = new LameClient();

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
        guiCam = new OrthographicCamera();
        gamePort = new StretchViewport(GAME_WIDTH, GAME_HEIGHT, guiCam);
        guiCam.setToOrtho(false);
        guiCam.position.set(0, 0, 0);

        batch = new SpriteBatch();
        batch.setProjectionMatrix(guiCam.combined);

        SCREEN_WIDTH = Gdx.graphics.getWidth();
        SCREEN_HEIGHT = Gdx.graphics.getHeight();

        spriteMap = new HashMap<Integer, Sprite>();
        for (int i = 0; i < 10; i++) {
            spriteMap.put(i, new Sprite(new Texture(i + ".png")));
        }

        img = new Texture("Board2.png");
        background = new TextureRegion(img, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        backgroundSprite = new Sprite(img);
        backgroundSprite.setSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        puck = new Puck();
        player1 = new Player();
        player2 = new Player();

        offset = offset * SCREEN_HEIGHT / GAME_HEIGHT;

        player1.setPosition(SCREEN_WIDTH / 2, offset);
        player2.setPosition(SCREEN_WIDTH / 2, SCREEN_HEIGHT - offset);

//        player1.score = 10;
//        player1.updateScore();
//        player2.score = 20;
//        player2.updateScore();
//        setPlayer1ScorePosition();
//        setPlayer2ScorePosition();

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
            draw();
            drawScores();
            drawPaddle1();
            drawPaddle2();
            drawPuck();
        batch.end();
    }

    public void draw() {
        backgroundSprite.draw(batch);
    }

    public void drawScores() {
        player2.score1.setPosition(scoreHorizontalOffset,
                SCREEN_HEIGHT / 2 + scoreVerticalOffset);
        player2.score1.draw(batch);

        player2.score2.setPosition(scoreHorizontalOffset,
                SCREEN_HEIGHT / 2 + scoreVerticalOffset + player2.score1.getHeight());
        player2.score2.draw(batch);

        float score2Offset = SCREEN_HEIGHT / 2 - scoreVerticalOffset - player1.score2.getHeight();
        player1.score1.setPosition(scoreHorizontalOffset,
                score2Offset - player1.score1.getHeight());
        player1.score1.draw(batch);

        player1.score2.setPosition(scoreHorizontalOffset,
                score2Offset);
        player1.score2.draw(batch);
    }

    void setPlayer1ScorePosition() {
        float score2Offset = SCREEN_HEIGHT / 2 - scoreVerticalOffset - player1.score2.getHeight();
        player1.score1.setPosition(scoreHorizontalOffset,
                score2Offset - player1.score1.getHeight());
        player1.score2.setPosition(scoreHorizontalOffset,
                score2Offset);
    }

    void setPlayer2ScorePosition() {
        player2.score1.setPosition(scoreHorizontalOffset,
                SCREEN_HEIGHT / 2 + scoreVerticalOffset);
        player2.score2.setPosition(scoreHorizontalOffset,
                SCREEN_HEIGHT / 2 + scoreVerticalOffset + player2.score1.getHeight());
    }

    public void drawPaddle1() {
        player1.draw();
    }

    public void drawPaddle2() {
        player2.draw();
    }

    public void drawPuck() {
        puck.draw();
    }

    public void update() {
        if (Gdx.input.isKeyPressed(Input.Keys.BACK)) {
            gameClient.disconnect();
        }

        puck.update();

        if (Gdx.input.isTouched()) {
            guiCam.unproject(touchPoint.set(Gdx.input.getX(), Gdx.input.getY(), 0));

//            Gdx.app.log(MultiplayerController.TAG, "isTouched: " + touchPoint.x + ", " + touchPoint.y);

            switch (gameClient.getPlayerNumber()) {
                case PLAYER1:
                    if (touchPoint.y >= SCREEN_HEIGHT / 2)
                        touchPoint.y = SCREEN_HEIGHT / 2;

                    player1.update(touchPoint.x, touchPoint.y);
                    break;
                case PLAYER2:
                    if (touchPoint.y <= SCREEN_HEIGHT / 2)
                        touchPoint.y = SCREEN_HEIGHT / 2;

                    player2.update(touchPoint.x, touchPoint.y);
                    gameClient.sendMessage(touchPoint.x * GAME_WIDTH / SCREEN_WIDTH + "," +
                            touchPoint.y * GAME_HEIGHT / SCREEN_HEIGHT);
                    break;
            }

            String s = touchPoint.x + ", " + touchPoint.y;
            gameClient.sendVoiceMessage(s.getBytes());
        }

        if (gameClient.getPlayerNumber() == PLAYER1) {
            gameClient.sendMessage(player1.x * GAME_WIDTH / SCREEN_WIDTH
                    + "," + player1.y * GAME_HEIGHT / SCREEN_HEIGHT
                    + "," + puck.x * GAME_WIDTH / SCREEN_WIDTH
                    + "," + puck.y * GAME_HEIGHT / SCREEN_HEIGHT
                    + "," + puck.velocity.i + "," + puck.velocity.j
            );
        }

        checkCollision(player1, puck);
        checkCollision(player2, puck);
    }

    public void checkCollision(Player player, Puck puck) {
        puck.checkCollision(player);

        double distance = Math.sqrt(Math.pow(puck.x-player.x, 2)+Math.pow(puck.y-player.y, 2));
        if(distance<Math.sqrt(Math.pow(puck.radius+player.radius, 2)))
        {
            puck.x = (puck.x-player.x)*(puck.radius+player.radius)/distance+player.x;
            puck.y = (puck.y-player.y)*(puck.radius+player.radius)/distance+player.y;
        }

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
//        Gdx.app.log(MultiplayerController.TAG, "onMessageReceived: " + message);

        String[] coords = message.split(",");

        float otherX = Float.parseFloat(coords[0]) * SCREEN_WIDTH / GAME_WIDTH;
        float otherY = Float.parseFloat(coords[1]) * SCREEN_HEIGHT / GAME_HEIGHT;

//        Gdx.app.log(MultiplayerController.TAG, "Calibration after receiving: " + otherX + ", " + otherY);

        switch(gameClient.getPlayerNumber()) {
            case PLAYER1:
                player2.update(otherX, otherY);
                break;
            case PLAYER2:
                player1.update(otherX, otherY);
                puck.update(
                        Double.parseDouble(coords[2]) * SCREEN_WIDTH / GAME_WIDTH,
                        Double.parseDouble(coords[3]) * SCREEN_HEIGHT / GAME_HEIGHT,
                        Double.parseDouble(coords[4]),
                        Double.parseDouble(coords[5])
                );
                break;
        }
    }

    @Override
    public MultiplayerController getDeviceAPI() {
        return mController;
    }

    float scoreVerticalOffset = 40;
    float scoreHorizontalOffset = 40;

    private class Puck {
        public Vector2d velocity = new Vector2d(0, 0);
        public double x, y;
        public int radius = 30/2;

        int leftBound = 100;
        int rightBound = 220;

        Sprite puckSprite;

        public Puck() {
            radius = radius * SCREEN_WIDTH / GAME_WIDTH;
            x = SCREEN_WIDTH / 2;
            y = SCREEN_HEIGHT / 2;

            puckSprite = new Sprite(new Texture("Puck.png"));
            puckSprite.setSize(radius * 2, radius * 2);

            velocity.i = 0;
            velocity.j = 0;

            leftBound = leftBound * SCREEN_WIDTH / GAME_WIDTH;
            rightBound = rightBound * SCREEN_WIDTH / GAME_WIDTH;
        }

        public float getWidth() {return puckSprite.getWidth();}
        public float getHeight() {return puckSprite.getHeight();}

        public void setPosition(double x, double y) {
            this.x = x;
            this.y = y;
        }

        public void draw() {
            puckSprite.setPosition((float)x - getWidth() / 2, (float)y - getHeight() / 2);
            puckSprite.draw(batch);
        }

        public void update() {
            x += velocity.i *= .98;
            y += velocity.j *= .98;

            // bounce off left wall
            if(x <= radius){
                velocity.i = Math.abs(velocity.i); //bounce
                x = radius;
            }

            // bounce off right wall
            if(x >= SCREEN_WIDTH - radius){
                velocity.i = -Math.abs(velocity.i);
                x = SCREEN_WIDTH - radius;
            }

            // bounce off bottom
            if(y <= radius){

                // goal
                if (x >= leftBound && x <= rightBound) {
                    reset();

                    player2.score++;
                    player2.updateScore();
//                    setPlayer2ScorePosition();
                } else {
                    velocity.j = Math.abs(velocity.j);
                    y = radius;
                }
            }

            // bounce off top
            if(y >= SCREEN_HEIGHT - radius){

                //goal
                if (x >= leftBound && x <= rightBound) {
                    reset();

                    player1.score++;
                    player1.updateScore();
//                    setPlayer1ScorePosition();
                } else {
                    velocity.j = -Math.abs(velocity.j);
                    y = SCREEN_HEIGHT - radius;
                }
            }
        }

        void update(double newX, double newY, double i, double j) {
            velocity.i = i;
            velocity.j = j;

            x = newX;
            y = newY;
        }

        void reset() {
            velocity.i = 0;
            velocity.j = 0;

            x = SCREEN_WIDTH / 2;
            y = SCREEN_HEIGHT / 2;
        }

        public boolean checkCollision(Player p){
            if(Math.pow(x - p.x, 2) + Math.pow(y - p.y, 2) <= Math.pow(p.radius + radius, 2)){
                Vector2d collisionDirection = new Vector2d(x-p.x, y-p.y);
                velocity = p.velocity.proj(collisionDirection).plus(velocity.proj(collisionDirection).times(-1)
                        .plus(velocity.proj(new Vector2d(collisionDirection.j, -collisionDirection.i)))).times(0.9);
                return true;
            }
            return false;
        }
    }

    private class Player {
        public int score = 0;

        Sprite score1, score2;

        public Vector2d velocity = new Vector2d(0,0);
        int radius = 70/2;
        public double x = 0;
        public double y = 0;

        Texture img;
        Sprite playerSprite;

        public Player() {
            img = new Texture("Paddle.png");
            playerSprite = new Sprite(img);

            radius = radius * SCREEN_WIDTH / GAME_WIDTH;

            playerSprite.setSize(radius * 2,
                    radius * 2);

            score1 = spriteMap.get(0);
            score2 = spriteMap.get(0);
        }

        public float getWidth() {return playerSprite.getWidth();}
        public float getHeight() {return playerSprite.getHeight();}

        public void setPosition(float x, float y) {
            this.x = x;
            this.y = y;
        }

        public void draw() {
            playerSprite.setPosition((float)x - getWidth() / 2, (float)y - getHeight() / 2);
            playerSprite.draw(batch);
        }

        public void update(float x, float y) {
            velocity.i = x - this.x;
            velocity.j = y - this.y;
            this.x = x;
            this.y = y;
        }

        public void updateScore() {
            score1 = spriteMap.get(score / 10);
            score2 = spriteMap.get(score % 10);
        }
    }


    @Override public void show() {}
    @Override public void resize(int width, int height) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override public void dispose() {}
}