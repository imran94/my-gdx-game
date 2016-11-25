package com.mygdx.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
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

/**
 * Created by Administrator on 15-Nov-16.
 */
public class GameScreen implements Screen {

    Game game;

    OrthographicCamera guiCam;
    SpriteBatch batch;
    Vector3 touchPoint;

    Texture img;
    TextureRegion background;
    Sprite backgroundSprite;

    ShapeRenderer shapeRenderer;

    public GameScreen(Game game) {
        this.game = game;

        shapeRenderer = new ShapeRenderer();

        guiCam = new OrthographicCamera(320, 480);
        guiCam.setToOrtho(false);
        guiCam.position.set(0, 0, 0);

        batch = new SpriteBatch();
        batch.setProjectionMatrix(guiCam.combined);

        img = new Texture("Board2.png");
        background = new TextureRegion(img, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        backgroundSprite = new Sprite(img);
//        backgroundSprite.setSize(1f, backgroundSprite.getHeight() / backgroundSprite.getWidth());
        backgroundSprite.setSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        touchPoint = new Vector3();
    }

    float x1 = Gdx.graphics.getWidth() / 2;
    float x2 = Gdx.graphics.getWidth() / 2;

    float y1 = 150;
    float y2 = Gdx.graphics.getHeight() - 150;

    float x3 = Gdx.graphics.getWidth() / 2;
    float y3 = Gdx.graphics.getHeight() / 2;

    int paddleRadius = 60;
    int puckRadius = 40;

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
        batch.end();

        drawPaddle1();
        drawPaddle2();
        drawPuck();
    }

    public void draw() {
        backgroundSprite.draw(batch);

    }

    public void drawPaddle1() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.circle(x1, y1, paddleRadius);
        shapeRenderer.end();
    }

    public void drawPaddle2() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.circle(x2, y2, paddleRadius);
        shapeRenderer.end();
    }

    public void drawPuck() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.BLACK);
        shapeRenderer.circle(x3, y3, puckRadius);
        shapeRenderer.end();
    }

    public void update() {
        x3 += puckSpeedX;
        y3 += puckSpeedY;

        if (x3 <= 0 || x3 >= Gdx.graphics.getWidth()) {
            puckSpeedX *= -1;
        }

        if (y3 <= 0 || y3 >= Gdx.graphics.getHeight()) {
            if (x3 > 100 && x3 < Gdx.graphics.getWidth() - 100) {
                resetPuck();
            } else {
                puckSpeedY *= -1;
            }
        }

        for (int i = 0; i < 2; i++) {
            if (Gdx.input.isTouched(i)) {
                guiCam.unproject(touchPoint.set(Gdx.input.getX(i), Gdx.input.getY(i), 0));

                if (touchPoint.y <= Gdx.graphics.getHeight() / 2) {
                    x1 = touchPoint.x;
                    y1 = touchPoint.y;
                } else {
                    x2 = touchPoint.x;
                    y2 = touchPoint.y;
                }
            }
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

            float deltaX = x3 - (x1 - puckRadius + puckRadius / 2);
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

            float deltaX = x3 - (x2 - puckRadius + puckRadius / 2);
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
    public void show() {

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
}
