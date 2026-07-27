package com.brk.chessrunner.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.ImageTextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.brk.chessrunner.GameScreen;
import com.brk.chessrunner.MainGame;
import com.brk.chessrunner.ModoJuego;
import com.brk.chessrunner.database.LocalDatabase;
import com.brk.chessrunner.database.UsuarioLocal;

public class MainMenuScreen implements Screen {

    private Stage stage;
    private SpriteBatch batch;

    private TextureAtlas uiAtlas;
    private TextureRegion recuadroTexto;
    private TextureRegion iconoConfig;
    private TextureRegion iconoPersona;

    private final MainGame game;
    private final LocalDatabase db;
    private Skin skin;

    private Array<InfoModo> modosDisponibles;
    private int indiceModoActual = 0;

    private Image imgPiezaAjedrez;
    private TextButton botonJugar;
    private Table mainTable;

    private static class InfoModo {
        String nombrePantalla;
        String regionTexturaPieza;
        ModoJuego modo;
        int nivel;

        public InfoModo(String nombrePantalla, String regionTexturaPieza, ModoJuego modo, int nivel) {
            this.nombrePantalla = nombrePantalla;
            this.regionTexturaPieza = regionTexturaPieza;
            this.modo = modo;
            this.nivel = nivel;
        }
    }

    public MainMenuScreen(MainGame game, LocalDatabase db) {
        this.game = game;
        this.db = db;
        batch = new SpriteBatch();
        inicializarModosJuego();
    }

    private void inicializarModosJuego() {
        modosDisponibles = new Array<>();
        modosDisponibles.add(new InfoModo("Clasico\n(Sin presión)", "default-rect", ModoJuego.CLASICO, 0));
        modosDisponibles.add(new InfoModo("Supervivencia\n(Infinito)", "default-rect", ModoJuego.INFINITO, 0));
        modosDisponibles.add(new InfoModo("Contrarreloj", "default-rect", ModoJuego.CONTRARRELOJ, 0));
        modosDisponibles.add(new InfoModo("Tutorial: Nivel 1", "default-rect", ModoJuego.TUTORIAL, 1));
        modosDisponibles.add(new InfoModo("Tutorial: Nivel 2", "default-rect", ModoJuego.TUTORIAL, 2));
        modosDisponibles.add(new InfoModo("Tutorial: Nivel 3", "default-rect", ModoJuego.TUTORIAL, 3));
    }

    @Override
    public void show() {
        stage = new Stage(new FitViewport(480, 800));
        Gdx.input.setInputProcessor(stage);

        // --- 1. CARGAR EL ATLAS Y LOS DRAWABLES ---
        uiAtlas = new TextureAtlas(Gdx.files.internal("ui/menu_ui.atlas"));

        TextureRegionDrawable drawRecuadro = new TextureRegionDrawable(uiAtlas.findRegion("Recuadro_Texto"));
        TextureRegionDrawable drawConfig = new TextureRegionDrawable(uiAtlas.findRegion("Img_Configuracion_transparente"));
        TextureRegionDrawable drawUser = new TextureRegionDrawable(uiAtlas.findRegion("Img_Usuario_transparente"));
        TextureRegionDrawable drawFlechaIzq = new TextureRegionDrawable(uiAtlas.findRegion("Flecha_izq_transparente"));
        TextureRegionDrawable drawFlechaDer = new TextureRegionDrawable(uiAtlas.findRegion("Flecha_der_transparente"));

        try {
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
        } catch (Exception e) {
            Gdx.app.error("UI_ERROR", "Error cargando el uiskin.json: " + e.getMessage());
        }

        UsuarioLocal usuario = db.obtenerUsuarioActual();
        String aliasMostrar = (usuario != null) ? usuario.getAlias() : "Desconocido";

        mainTable = new Table();
        mainTable.setFillParent(true);

        Table topTable = new Table();

        // ACTUALIZAR BOTONES SUPERIORES
        Button.ButtonStyle configStyle = new Button.ButtonStyle();
        configStyle.up = drawConfig;
        configStyle.down = drawConfig;
        Button btnConfiguracion = new Button(configStyle);


        Button btnUsuario = new Button(new Button.ButtonStyle());
        btnUsuario.add(new Image(drawUser)).size(40, 40).center().row();
        btnUsuario.add(new Label(aliasMostrar, skin, "normal")).padTop(5).center();

        Label lblTituloJuego = new Label("KING DASH", skin, "titulo");

        topTable.add(btnConfiguracion).left().pad(15).size(60, 60);
        topTable.add(lblTituloJuego).expandX().center();
        topTable.add(btnUsuario).right().pad(15).height(60);

        Table carouselTable = new Table();

        // --- BOTONES DEL CARRUSEL ---
        Button.ButtonStyle flechaIzqStyle = new Button.ButtonStyle();
        flechaIzqStyle.up = drawFlechaIzq;
        flechaIzqStyle.down = drawFlechaIzq;
        Button btnFlechaIzq = new Button(flechaIzqStyle);

        Button.ButtonStyle flechaDerStyle = new Button.ButtonStyle();
        flechaDerStyle.up = drawFlechaDer;
        flechaDerStyle.down = drawFlechaDer;
        Button btnFlechaDer = new Button(flechaDerStyle);

        imgPiezaAjedrez = new Image(skin, "default-rect");

        carouselTable.add(btnFlechaIzq).left().pad(20).size(60, 60);
        carouselTable.add(imgPiezaAjedrez).expandX().center().size(180, 280);
        carouselTable.add(btnFlechaDer).right().pad(20).size(60, 60);

        // --- BOTÓN JUGAR ---
        TextButton.TextButtonStyle playBtnStyle = new TextButton.TextButtonStyle(skin.get(TextButton.TextButtonStyle.class));
        playBtnStyle.up = drawRecuadro;
        playBtnStyle.down = drawRecuadro;
        playBtnStyle.fontColor = Color.BLACK;

        botonJugar = new TextButton("MODO DE JUEGO", playBtnStyle);
        botonJugar.getLabel().setFontScale(1.35f);

        mainTable.add(topTable).fillX().row();
        mainTable.add(carouselTable).expand().fill().row();
        mainTable.add(botonJugar).padBottom(60).width(240).height(70).row();

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
                InfoModo seleccionado = modosDisponibles.get(indiceModoActual);
                game.switchScreen(new GameScreen(game, seleccionado.modo, seleccionado.nivel));
            }
        });

        btnConfiguracion.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                ConfigMenu config = new ConfigMenu("Configuracion", skin);
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

        InputMultiplexer multiplexer = new InputMultiplexer();
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

    private void actualizarInterfazModo() {
        InfoModo modoActual = modosDisponibles.get(indiceModoActual);
        imgPiezaAjedrez.setDrawable(skin.getDrawable(modoActual.regionTexturaPieza));
        botonJugar.setText(modoActual.nombrePantalla);
    }

    private void moverCarrusel(int direccion) {
        indiceModoActual += direccion;
        if (indiceModoActual < 0) {
            indiceModoActual = modosDisponibles.size - 1;
        } else if (indiceModoActual >= modosDisponibles.size) {
            indiceModoActual = 0;
        }
        actualizarInterfazModo();
    }

    @Override
    public void render(float delta) {
        // Fondo negro por defecto o el color que prefieras
        Gdx.gl.glClearColor(0.12f,0.15f , 0.18f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {}
    @Override
    public void resume() {}
    @Override
    public void hide() {}

    @Override
    public void dispose() {
        if (stage != null) stage.dispose();
        if (skin != null) skin.dispose();
        if (uiAtlas != null) uiAtlas.dispose();
        if (batch != null) batch.dispose();
    }
}
