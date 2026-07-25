package com.brk.chessrunner.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;

public class Toast {

    public static void show(Stage stage, String message, Skin skin) {
        show(stage, message, skin, new Color(0, 0, 0, 0.8f), null);
    }

    public static void show(Stage stage, String message, Skin skin, Color bgColor) {
        show(stage, message, skin, bgColor, null);
    }

    public static void show(Stage stage, String message, Skin skin, Color bgColor, Color accentColor) {
        Table root = new Table();
        root.setFillParent(true);
        root.bottom().padBottom(60);

        Table toastBox = new Table();
        toastBox.setBackground(skin.newDrawable("default-round", bgColor));

        if (accentColor != null) {
            Image accentBar = new Image(skin.newDrawable("white", accentColor));
            toastBox.addActor(accentBar);
            accentBar.setFillParent(true);
            accentBar.setHeight(4f);
            accentBar.setAlign(Align.top);
        }

        String styleName = "hud";
        if (accentColor != null) {
            Label label = new Label(message, skin, styleName);
            label.setAlignment(Align.center);
            label.setWrap(true);
            label.setColor(Color.WHITE);
            toastBox.add(label).width(300).pad(14);
        } else {
            Label label = new Label(message, skin, styleName);
            label.setAlignment(Align.center);
            label.setWrap(true);
            toastBox.add(label).width(300).pad(14);
        }

        root.add(toastBox);
        stage.addActor(root);

        root.getColor().a = 0;
        root.addAction(Actions.sequence(
            Actions.fadeIn(0.3f),
            Actions.delay(2.5f),
            Actions.fadeOut(0.5f),
            Actions.removeActor()
        ));
    }
}