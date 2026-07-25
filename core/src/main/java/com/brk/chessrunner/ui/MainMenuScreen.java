package com.brk.chessrunner.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.brk.chessrunner.GameScreen;
import com.brk.chessrunner.MainGame;
import com.brk.chessrunner.ModoJuego;
import com.brk.chessrunner.database.LocalDatabase;
import com.brk.chessrunner.database.UsuarioLocal;

public class MainMenuScreen implements Screen {

    private final MainGame game;
    private final LocalDatabase db;
    private Stage stage;
    private Skin skin;

    private Array<InfoModo> modosDisponibles;
    private int indiceModoActual=0;

    private Image imgPiezaAjedrez;
    private TextButton botonJugar;
    private Table mainTable;

    private static class InfoModo{
        String nombrePantalla;
        String regionTexturaPieza;
        ModoJuego modo;
        int nivel;

        public InfoModo(String nombrePantalla, String regionTexturaPieza, ModoJuego modo, int nivel){
            this.nombrePantalla= nombrePantalla;
            this.regionTexturaPieza = regionTexturaPieza;
            this.modo= modo;
            this.nivel= nivel;
        }
    }
    public MainMenuScreen(MainGame game, LocalDatabase db) {
        this.game = game;
        this.db = db;
        inicializarModosJuego();
    }

    private void inicializarModosJuego(){
        modosDisponibles= new Array<>();
        modosDisponibles.add(new InfoModo("Clasico\n(Sin presión)","default-rect", ModoJuego.CLASICO,0));
        modosDisponibles.add(new InfoModo("Supervivencia\n(Infinito)","default-rect", ModoJuego.INFINITO,0));
        modosDisponibles.add(new InfoModo("Contrarreloj","default-rect", ModoJuego.CONTRARRELOJ,0));
        modosDisponibles.add(new InfoModo("Tutorial: Nivel 1","default-rect", ModoJuego.TUTORIAL,1));
        modosDisponibles.add(new InfoModo("Tutorial: Nivel 2","default-rect", ModoJuego.TUTORIAL,2));
        modosDisponibles.add(new InfoModo("Tutorial: Nivel 3","default-rect", ModoJuego.TUTORIAL,3));
    }

    @Override
    public void show() {
        stage = new Stage(new FitViewport(480, 800));
        Gdx.input.setInputProcessor(stage);

        try{
            skin = new Skin(Gdx.files.internal("ui/uiskin.json"));
            skin.get("titulo", Label.LabelStyle.class).font = game.fontTitulo;
            skin.get("hud", Label.LabelStyle.class).font = game.fontHUD;
            skin.get("normal", Label.LabelStyle.class).font = game.fontNormal;
            skin.get(Label.LabelStyle.class).font = game.fontNormal;
            skin.get("gameover", Label.LabelStyle.class).font = game.fontHUD;
            skin.get(TextButton.TextButtonStyle.class).font = game.fontHUD;
            skin.get(TextField.TextFieldStyle.class).font = game.fontHUD;
            skin.get(Window.WindowStyle.class).titleFont = game.fontTitulo;
            skin.get("dialog", Window.WindowStyle.class).titleFont = game.fontTitulo;
        } catch(Exception e){
            Gdx.app.error("UI_ERROR","Error cargando el uiskin.json"+e.getMessage());
        }

        UsuarioLocal usuario = db.obtenerUsuarioActual();
        String aliasMostrar = (usuario != null) ? usuario.getAlias() : "Desconocido";

        mainTable = new Table();
        mainTable.setFillParent(true);

        Table topTable= new Table();
        TextButton btnConfiguracion= new TextButton("Opciones",skin);
        TextButton btnUsuario= new TextButton(aliasMostrar,skin);

        Label lblTituloJuego= new Label("KING DASH", skin, "titulo");

        topTable.add(btnConfiguracion).left().pad(15).size(60,60);
        topTable.add(lblTituloJuego).expandX().center();
        topTable.add(btnUsuario).right().pad(15).size(60,60);

        Table carouselTable= new Table();
        TextButton btnFlechaIzq= new TextButton("<",skin);
        TextButton btnFlechaDer= new TextButton(">",skin);

        imgPiezaAjedrez= new Image(skin,"default-rect");

        carouselTable.add(btnFlechaIzq).left().pad(20).size(60,60);
        carouselTable.add(imgPiezaAjedrez).expandX().center().size(180,280);
        carouselTable.add(btnFlechaDer).right().pad(20).size(60,60);

        botonJugar= new TextButton("MODO DE JUEGO",skin);
        botonJugar.getLabel().setFontScale(1.35f);

        mainTable.add(topTable).fillX().row();
        mainTable.add(carouselTable).expand().fill().row();
        mainTable.add(botonJugar).padBottom(60).width(240).height(55).row();

        stage.addActor(mainTable);

        animarEntrada();

        actualizarInterfazModo();

        btnFlechaIzq.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                moverCarrusel(-1);
            }
        });

        btnFlechaDer.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
               moverCarrusel(1);
            }
        });

        botonJugar.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                InfoModo seleccionado= modosDisponibles.get(indiceModoActual);
                game.switchScreen(new GameScreen(game, seleccionado.modo, seleccionado.nivel));
            }
        });

        btnConfiguracion.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                ConfigMenu config= new ConfigMenu("Configuracion",skin);
                config.show(stage);
            }
        });

        btnUsuario.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (usuario != null && usuario.getId().startsWith("guest")) {
                    LoginDialog dialog = new LoginDialog("Iniciar Sesion", skin, db, game);
                    dialog.show(stage);
                } else {
                    SyncDialog dialog = new SyncDialog("Sincronizando", skin, db);
                    dialog.show(stage);
                }
            }
        });

        InputMultiplexer multiplexer= new InputMultiplexer();
        multiplexer.addProcessor(stage);

        GestureDetector gestureDetector = new GestureDetector(new GestureDetector.GestureAdapter() {
            @Override
            public boolean fling(float velocityX, float velocityY, int button) {
                if (Math.abs(velocityX) > Math.abs(velocityY)) {
                    if (velocityX > 150) {
                        moverCarrusel(-1);
                        return true;
                    } else if (velocityX < -150) {
                        moverCarrusel(1);
                        return true;
                    }
                }
                return false;
            }
        });

        multiplexer.addProcessor(gestureDetector);
        Gdx.input.setInputProcessor(multiplexer);

        com.brk.chessrunner.network.SyncManager.syncSilently(db, stage, skin);
    }

    private void animarEntrada() {
        mainTable.getColor().a = 0f;
        mainTable.addAction(Actions.sequence(
            Actions.alpha(0f),
            Actions.fadeIn(0.5f, Interpolation.sineOut)
        ));
    }

    private void actualizarInterfazModo(){
        InfoModo modoActual= modosDisponibles.get(indiceModoActual);
        imgPiezaAjedrez.setDrawable(skin.getDrawable(modoActual.regionTexturaPieza));
        botonJugar.setText(modoActual.nombrePantalla);
    }

    private void moverCarrusel(int direccion){
        indiceModoActual += direccion;
        if(indiceModoActual<0){
            indiceModoActual= modosDisponibles.size-1;
        }else if(indiceModoActual>= modosDisponibles.size){
            indiceModoActual=0;
        }
        actualizarInterfazModo();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.15f, 0.15f, 0.2f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
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
