package com.brk.chessrunner.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.brk.chessrunner.GameScreen;
import com.brk.chessrunner.MainGame;
import com.brk.chessrunner.ModoJuego;
import com.brk.chessrunner.database.LocalDatabase;
import com.brk.chessrunner.database.UsuarioLocal;

import org.w3c.dom.Text;

public class MainMenuScreen implements Screen {

    private final MainGame game;
    private final LocalDatabase db;
    private Stage stage;
    private Skin skin;

    public MainMenuScreen(MainGame game, LocalDatabase db) {
        this.game = game;
        this.db = db;
    }

    @Override
    public void show() {
        // El Stage es el "teatro" donde pondremos los botones
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage); // Permite que los botones reciban clics

        try{
            // Cargamos el diseño de los botones
            skin = new Skin(Gdx.files.internal("ui/uiskin.json"));

        } catch(Exception e){
            Gdx.app.error("UI_ERROR","Error cargando el uiskin.json"+e.getMessage());
        }



        // Table ayuda a centrar y ordenar los botones fácilmente
        Table tabla = new Table();
        tabla.setFillParent(true);
        stage.addActor(tabla);

        // Consultamos quién es el jugador actual (Invitado o Registrado)
        UsuarioLocal usuario = db.obtenerUsuarioActual();
        String aliasMostrar = (usuario != null) ? usuario.getAlias() : "Desconocido";

        // Creamos los elementos visuales
        Label tituloLabel = new Label("CHESS RUNNER", skin);
        tituloLabel.setFontScale(2.0f);
        Label userLabel = new Label("Jugador: " + aliasMostrar, skin);

        TextButton botonJugar = new TextButton("Jugar", skin);
        TextButton botonConfiguracion= new TextButton("Opciones",skin);
        TextButton botonSincronizar = new TextButton("Sincronizar / Login", skin);
        TextButton botonSalida= new TextButton("Salir",skin);


        // Los añadimos a la tabla (pantalla)
        tabla.add(tituloLabel).padBottom(50).colspan(2).row();

        tabla.add(botonJugar).width(200).height(50).padBottom(15).row();
        tabla.add(botonConfiguracion).width(200).height(50).padBottom(15).row();
        tabla.add(botonSincronizar).width(200).height(50).padBottom(15).row();
        tabla.add(botonSalida).width(200).height(50).padBottom(15).row();
        tabla.add(userLabel).padBottom(20).row();

        // --- SINCRONIZACIÓN AUTOMÁTICA AL ENTRAR ---
        com.brk.chessrunner.network.SyncManager.syncSilently(db, stage, skin);


        botonJugar.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                // Cambia la vista al tablero de juego
                SelectModeDialog selectmode= new SelectModeDialog("Seleccionar Modo", skin, game);
                selectmode.show(stage);
            }
        });

        botonConfiguracion.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                // Cambia la vista al tablero de juego
                ConfigMenu config= new ConfigMenu("Configuracion",skin);
                config.show(stage);
            }
        });


        // Asegúrate de importar com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
        // y com.badlogic.gdx.scenes.scene2d.Actor;

        botonSincronizar.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                // Si el ID empieza con "guest", no está logueado en la nube
                if (usuario != null && usuario.getId().startsWith("guest")) {
                    LoginDialog dialog = new LoginDialog("Iniciar Sesion", skin, db, game);
                    dialog.show(stage);
                } else {
                    // Si ya es un usuario real, abrimos el SyncDialog
                    SyncDialog dialog = new SyncDialog("Sincronizando", skin, db);
                    dialog.show(stage);
                }
            }
        });

        botonSalida.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                // Cambia la vista al tablero de juego
                Gdx.app.exit();
            }
        });
    }

    @Override
    public void render(float delta) {
        // Limpiamos la pantalla (Gris oscuro)
        Gdx.gl.glClearColor(0.15f, 0.15f, 0.2f, 1f);
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
