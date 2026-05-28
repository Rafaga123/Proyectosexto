package com.brk.chessrunner.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.brk.chessrunner.MainGame;
import com.brk.chessrunner.database.LocalDatabase;
import com.brk.chessrunner.database.UsuarioLocal;

public class MainMenuScreen implements Screen {

    private final MainGame juego;
    private final LocalDatabase db;
    private Stage stage;
    private Skin skin;

    public MainMenuScreen(MainGame juego, LocalDatabase db) {
        this.juego = juego;
        this.db = db;
    }

    @Override
    public void show() {
        // El Stage es el "teatro" donde pondremos los botones
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage); // Permite que los botones reciban clics

        // Cargamos el diseño de los botones
        skin = new Skin(Gdx.files.internal("ui/uiskin.json"));

        // Table ayuda a centrar y ordenar los botones fácilmente
        Table tabla = new Table();
        tabla.setFillParent(true);
        stage.addActor(tabla);

        // Consultamos quién es el jugador actual (Invitado o Registrado)
        UsuarioLocal usuario = db.obtenerUsuarioActual();
        String aliasMostrar = (usuario != null) ? usuario.getAlias() : "Desconocido";

        // Creamos los elementos visuales
        Label tituloLabel = new Label("CHESS RUNNER", skin);
        Label userLabel = new Label("Jugador: " + aliasMostrar, skin);

        TextButton botonJugar = new TextButton("Jugar", skin);
        TextButton botonSincronizar = new TextButton("Sincronizar / Login", skin);

        // Los añadimos a la tabla (pantalla)
        tabla.add(tituloLabel).padBottom(20).row();
        tabla.add(userLabel).padBottom(40).row();
        tabla.add(botonJugar).width(200).height(50).padBottom(15).row();
        tabla.add(botonSincronizar).width(200).height(50).row();

        // Más adelante le daremos función a los botones
    }

    @Override
    public void render(float delta) {
        // Limpiamos la pantalla (Gris oscuro)
        Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Dibujamos la UI
        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        stage.draw();
    }

    @Override
    public void resize(int width, int height) { stage.getViewport().update(width, height, true); }
    @Override
    public void pause() {}
    @Override
    public void resume() {}
    @Override
    public void hide() {}

    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
    }
}
