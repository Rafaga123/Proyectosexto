package com.brk.chessrunner.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
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

    private Array<InfoModo> modosDisponibles;
    private int indiceModoActual=0;

    private Label lblTituloModo;
    private Image imgPiezaAjedrez;
    private TextButton botonJugar;

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
        // El Stage es el "teatro" donde pondremos los botones
        stage = new Stage(new FitViewport(480, 800));
        Gdx.input.setInputProcessor(stage); // Permite que los botones reciban clics

        try{
            // Cargamos el diseño de los botones
            skin = new Skin(Gdx.files.internal("ui/uiskin.json"));

        } catch(Exception e){
            Gdx.app.error("UI_ERROR","Error cargando el uiskin.json"+e.getMessage());
        }

        // Consultamos quién es el jugador actual (Invitado o Registrado)
        UsuarioLocal usuario = db.obtenerUsuarioActual();
        String aliasMostrar = (usuario != null) ? usuario.getAlias() : "Desconocido";

        //Configuración del Layout Principal (Seran tablas anidadas)
        Table mainTable = new Table();
        mainTable.setFillParent(true);

        // Fila Superior (botones para la configuracion y el usuario)
        Table topTable= new Table();
        TextButton btnConfiguracion= new TextButton("Opciones",skin);
        TextButton btnUsuario= new TextButton(aliasMostrar,skin);

        //Titulo principal del juego
        Label lblTituloJuego= new Label("KING DASH",skin);
        lblTituloJuego.setFontScale(1.8f);

        //Distribución de lso botones de usuario y configuracion
        topTable.add(btnConfiguracion).left().pad(15).size(60,60);
        topTable.add(lblTituloJuego).expandX().center();
        topTable.add(btnUsuario).right().pad(15).size(60,60);

        //Fila Central (carrusel de modos e imagen principal)
        Table carouselTable= new Table();
        TextButton btnFlechaIzq= new TextButton("<",skin);
        TextButton btnFlechaDer= new TextButton(">",skin);

        imgPiezaAjedrez= new Image(skin,"default-rect");

        carouselTable.add(btnFlechaIzq).left().pad(20).size(60,60);
        carouselTable.add(imgPiezaAjedrez).expandX().center().size(180,280);
        carouselTable.add(btnFlechaDer).right().pad(20).size(60,60);

        botonJugar= new TextButton("MODO DE JUEGO",skin);

        mainTable.add(topTable).fillX().row();
        mainTable.add(carouselTable).expand().fill().row();
        mainTable.add(botonJugar).padBottom(60).width(240).height(55).row();

        stage.addActor(mainTable);

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
                game.setScreen(new GameScreen(game, seleccionado.modo, seleccionado.nivel));
            }
        });

        btnConfiguracion.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                // Cambia la vista al tablero de juego
                ConfigMenu config= new ConfigMenu("Configuracion",skin);
                config.show(stage);
            }
        });


        // Asegúrate de importar com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
        // y com.badlogic.gdx.scenes.scene2d.Actor;

        btnUsuario.addListener(new ChangeListener() {
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

        com.badlogic.gdx.InputMultiplexer multiplexer= new InputMultiplexer();
        multiplexer.addProcessor(stage);

        com.badlogic.gdx.input.GestureDetector gestureDetector = new com.badlogic.gdx.input.GestureDetector(new com.badlogic.gdx.input.GestureDetector.GestureAdapter() {
            @Override
            public boolean fling(float velocityX, float velocityY, int button) {
                // Evaluamos si el movimiento fue más horizontal que vertical
                if (Math.abs(velocityX) > Math.abs(velocityY)) {

                    // Comprobamos la velocidad para ignorar toques accidentales lentos
                    if (velocityX > 150) {
                        // Deslizamiento hacia la DERECHA -> Ver el modo ANTERIOR
                        moverCarrusel(-1);
                        return true;
                    } else if (velocityX < -150) {
                        // Deslizamiento hacia la IZQUIERDA -> Ver el modo SIGUIENTE
                        moverCarrusel(1);
                        return true;
                    }
                }
                return false; // Retorna false si no fue un deslizamiento válido
            }
        });

        multiplexer.addProcessor(gestureDetector);
        Gdx.input.setInputProcessor(multiplexer);

        // --- SINCRONIZACIÓN AUTOMÁTICA AL ENTRAR ---
        com.brk.chessrunner.network.SyncManager.syncSilently(db, stage, skin);

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
