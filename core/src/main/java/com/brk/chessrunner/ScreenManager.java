package com.brk.chessrunner;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class ScreenManager {
    private final MainGame game;
    private final Stage stage;
    private final Image fadeImage;
    private boolean transitioning = false;
    private static final float FADE_DURATION = 0.35f;

    public ScreenManager(MainGame game) {
        this.game = game;
        stage = new Stage(new ScreenViewport());

        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.BLACK);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();

        fadeImage = new Image(texture);
        fadeImage.setScaling(Scaling.fill);

        Table root = new Table();
        root.setFillParent(true);
        root.add(fadeImage).grow();
        stage.addActor(root);

        fadeImage.getColor().a = 0f;
    }

    public void setScreen(final Screen screen) {
        if (transitioning) return;
        transitioning = true;

        fadeImage.getColor().a = 0f;
        fadeImage.clearActions();

        fadeImage.addAction(Actions.sequence(
            Actions.fadeIn(FADE_DURATION),
            Actions.run(() -> {
                game.setScreen(screen);
                fadeImage.addAction(Actions.sequence(
                    Actions.fadeOut(FADE_DURATION),
                    Actions.run(() -> transitioning = false)
                ));
            })
        ));
    }

    public void setScreen(final Screen screen, float duration) {
        if (transitioning) return;
        transitioning = true;

        fadeImage.getColor().a = 0f;
        fadeImage.clearActions();

        fadeImage.addAction(Actions.sequence(
            Actions.fadeIn(duration),
            Actions.run(() -> {
                game.setScreen(screen);
                fadeImage.addAction(Actions.sequence(
                    Actions.fadeOut(duration),
                    Actions.run(() -> transitioning = false)
                ));
            })
        ));
    }

    public void render(float delta) {
        if (transitioning) {
            stage.act(delta);
            stage.draw();
        }
    }
}
