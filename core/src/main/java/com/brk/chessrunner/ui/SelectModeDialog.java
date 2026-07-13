package com.brk.chessrunner.ui;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.brk.chessrunner.GameScreen;
import com.brk.chessrunner.MainGame;
import com.brk.chessrunner.ModoJuego;

public class SelectModeDialog extends Dialog {

    public SelectModeDialog(String title, Skin skin, final MainGame game) {
        super(title, skin);

        setMovable(false); // Evita que se pueda arrastrar por error
        setResizable(false);

        Table content = getContentTable();
        content.pad(20);

        // --- 1. CREACIÓN DE LOS BOTONES ---
        TextButton btnClasico = new TextButton("Clasico (Sin presion)", skin);
        TextButton btnSupervivencia = new TextButton("Supervivencia (Infinito)", skin);
        TextButton btnContrarreloj = new TextButton("Contrarreloj", skin);

        Label lblTutorial = new Label("--- TUTORIALES ---", skin);
        TextButton btnTut1 = new TextButton("Nivel 1", skin);
        TextButton btnTut2 = new TextButton("Nivel 2", skin);
        TextButton btnTut3 = new TextButton("Nivel 3", skin);

        TextButton btnCerrar = new TextButton("Volver", skin);

        // --- 2. LÓGICA DE CONEXIÓN CON GAMESCREEN ---
        // Modo Clasico
        btnClasico.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new GameScreen(game, ModoJuego.CLASICO, 0));
                hide();
            }
        });

        // Modo Supervivencia
        btnSupervivencia.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // Mantenemos INFINITO según la nomenclatura que venías usando
                game.setScreen(new GameScreen(game, ModoJuego.INFINITO, 0));
                hide();
            }
        });

        // Modo Contrarreloj
        btnContrarreloj.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new GameScreen(game, ModoJuego.CONTRARRELOJ, 0));
                hide();
            }
        });

        // Modos Tutorial
        btnTut1.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new GameScreen(game, ModoJuego.TUTORIAL, 1));
                hide();
            }
        });

        btnTut2.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new GameScreen(game, ModoJuego.TUTORIAL, 2));
                hide();
            }
        });

        btnTut3.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new GameScreen(game, ModoJuego.TUTORIAL, 3));
                hide();
            }
        });

        // --- LAYOUT de la seleccion ---
        // Ponemos los botones principales arriba
        content.add(btnClasico).width(260).height(45).padBottom(10).colspan(3).row();
        content.add(btnSupervivencia).width(260).height(45).padBottom(10).colspan(3).row();
        content.add(btnContrarreloj).width(260).height(45).padBottom(20).colspan(3).row();

        // Los botones de tutorial en fila horizontal
        content.add(lblTutorial).padBottom(10).colspan(3).row();
        content.add(btnTut1).width(80).padRight(10);
        content.add(btnTut2).width(80).padRight(10);
        content.add(btnTut3).width(80).row();

        // El botón para cerrar o arrepentirse en la parte inferior
        getButtonTable().add(btnCerrar).width(150).padTop(15);
        btnCerrar.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                hide(); // Simplemente esconde el popup y vuelve al menú
            }
        });
    }
}
