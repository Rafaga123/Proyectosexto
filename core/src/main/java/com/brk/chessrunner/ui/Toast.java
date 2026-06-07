package com.brk.chessrunner.ui;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;

public class Toast {
    public static void show(Stage stage, String message, Skin skin) {
        final Table root = new Table();
        root.setFillParent(true);
        root.bottom().padBottom(50);
        
        // Creamos una tablita pequeña para el contenido que sí tendrá fondo
        Table toastBox = new Table();
        toastBox.setBackground(skin.newDrawable("white", 0, 0, 0, 0.7f));
        
        Label.LabelStyle style = skin.get(Label.LabelStyle.class);
        Label label = new Label(message, style);
        label.setAlignment(Align.center);
        label.setWrap(true);
        
        toastBox.add(label).width(300).pad(10);
        root.add(toastBox);
        
        stage.addActor(root);
        
        // Animación: Aparecer -> Esperar -> Desaparecer -> Eliminar
        root.getColor().a = 0;
        root.addAction(Actions.sequence(
            Actions.fadeIn(0.3f),
            Actions.delay(2.5f),
            Actions.fadeOut(0.5f),
            Actions.removeActor()
        ));
    }
}