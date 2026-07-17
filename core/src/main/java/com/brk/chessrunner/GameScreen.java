package com.brk.chessrunner;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack;
import com.brk.chessrunner.database.PartidaLocal;
import com.brk.chessrunner.database.UsuarioLocal;
import com.brk.chessrunner.ui.PauseWidget;

import java.util.UUID;

public class GameScreen implements Screen {

    private final MainGame juego;

    OrthographicCamera camera;
    Viewport viewport;
    Vector3 touchPoint;
    com.badlogic.gdx.graphics.g2d.BitmapFont font;

    public enum EstadoJuego {
        JUGANDO,
        GAME_OVER,
        VICTORIA
    }
    EstadoJuego estadoActual = EstadoJuego.JUGANDO;

    Texture texturaTablero;
    Texture SombraA;
    Texture SombraB;

    // --- VARIABLES DE TABLERO ADAPTATIVO ---
    float altoTablero = 768f;
    float offsetYTablero = 0f;

    float scrollY = 0f;
    float targetScrollY = 0f;
    float scrollSpeed = 10f;
    int filaLogicaJugador = 0;
    GestorEnemigos gestorEnemigos;
    public ModoJuego modoActual;
    public int nivelActual;
    public int filaMeta = -2; // -2 = Infinito (No hay meta)
    public float tiempoRestante;
    public String mensajeGameOver = "";

    TextureRegion piezaRey;
    int configColorEnemigo = 1;
    Texture texturaPiezasNegras;
    Texture texturaPiezasBlancas;
    ObjectMap<TipoPieza, TextureRegion> regionesEnemigos;
    ObjectMap<TipoPieza, TextureRegion> regionesJugador;
    float camaraAutoY = 0f;

    static final float WORLD_WIDTH  = 480f;
    static final float WORLD_HEIGHT = 800f;

    int jugadorCol = 2;
    static final int COLS = 5;
    int filaMaximaAlcanzada = 0;
    static final float CELL_W = WORLD_WIDTH / COLS;
    static final float CELL_H = CELL_W;
    static final int JUGADOR_FILA_VIS = 1;
    boolean isDragging = false;
    float dragX = 0f;
    float dragY = 0f;

    // --- VARIABLES DE POWER-UPS ---
    public boolean tieneEscudo = false;
    public float tiempoEscudo = 0f;

    public int movimientosCambio = 0;
    public TipoPieza piezaTransformada;

    public int movimientosIA = 0;
    public float temporizadorIA = 0f;
    public int ultimaFilaGenerada = 5;

    public float tiempoReloj = 0f;
    private Texture texturaPixelBlanco;

    // --- VARIABLES DE INTERFAZ DE PAUSA ---
    private com.badlogic.gdx.scenes.scene2d.ui.Table hudTable;
    private Stage uiStage;
    private Skin uiSkin;
    private PauseWidget pauseWidget;

    private boolean juegoPausado = false;
    private float tiempoJugado = 0f;

    // --- VARIABLES DEL SHADER Y FONDO ---
    private ShaderProgram shaderFondo;
    private float tiempoGlobal = 0f;
    private Texture texturaBlanca;
    float fondoR = 0.5f;
    float fondoG = 0.85f;
    float fondoB = 0.7f;


    // El constructor recibe el juego principal
    public GameScreen(MainGame juego, ModoJuego modoActual, int nivel) {
        this.juego = juego;
        this.modoActual = modoActual;
        this.nivelActual = nivel;

        if (modoActual == ModoJuego.TUTORIAL) {
            if (nivel == 1) filaMeta = 15;
            else if (nivel == 2) filaMeta = 25;
            else if (nivel == 3) filaMeta = 40;
        }  else if (modoActual == ModoJuego.CONTRARRELOJ) {
            tiempoRestante = 60f;
        }
    }


    @Override
    public void show() {

        texturaBlanca = new Texture("ui/default.png");

        ShaderProgram.pedantic = false;
        shaderFondo = new ShaderProgram(
            Gdx.files.internal("shaders/fondo.vert"),
            Gdx.files.internal("shaders/fondo.frag")
        );

        if (!shaderFondo.isCompiled()) {
            Gdx.app.error("Shader Error", shaderFondo.getLog());
        }

        touchPoint = new Vector3();
        font = new com.badlogic.gdx.graphics.g2d.BitmapFont();
        font.getData().setScale(2f);

        Gdx.input.setCatchKey(Input.Keys.BACK, true);

        // --- Lectura de tablero---
        com.badlogic.gdx.Preferences prefs = Gdx.app.getPreferences("ChessRunnerSettings");

        // Leemos como Integer. Si no existe, usamos 1 por defecto.
        int estiloTablero = prefs.getInteger("estiloTablero", 1);
        String rutaTablero = "tablero.png";

        // Adaptamos el switch para que evalúe el número directamente
        switch (estiloTablero) {
            case 1: // Clásico
                fondoR = 0.5f; fondoG = 0.85f; fondoB = 0.7f; // Verde menta
                rutaTablero = "tablero.png";
                break;
            case 2: // Madera
                fondoR = 0.75f; fondoG = 0.45f; fondoB = 0.25f; // Marrón
                rutaTablero = "tablero_madera.png";
                break;
            case 3: // Neón
                fondoR = 0.8f; fondoG = 0.2f; fondoB = 0.8f; // Morado
                rutaTablero = "tablero_neon.png";
                break;
            case 4: // Océano
                fondoR = 0.2f; fondoG = 0.5f; fondoB = 0.9f;
                rutaTablero = "tablero_oceano.png";
                break;
            case 5: // Volcán
                fondoR = 0.8f; fondoG = 0.1f; fondoB = 0.1f;
                rutaTablero = "tablero_volcan.png";
                break;
            case 6: // Desierto
                fondoR = 0.9f; fondoG = 0.7f; fondoB = 0.1f;
                rutaTablero = "tablero_desierto.png";
                break;
            case 7: // Tóxico
                fondoR = 0.3f; fondoG = 0.9f; fondoB = 0.2f;
                rutaTablero = "tablero_toxico.png";
                break;
            case 8: // Hielo
                fondoR = 0.6f; fondoG = 0.9f; fondoB = 0.9f;
                rutaTablero = "tablero_hielo.png";
                break;
            default:
                fondoR = 0.5f; fondoG = 0.85f; fondoB = 0.7f;
                rutaTablero = "tablero.png";
                break;
        }

        // --- FallBack para evitar errores al cargar una imagen que no existe ---
        if (!Gdx.files.internal(rutaTablero).exists()) {
            System.out.println("Aviso: Falta la imagen '" + rutaTablero + "'. Usando tablero clásico.");
            rutaTablero = "tablero.png";
        }

        texturaTablero = new Texture(Gdx.files.internal(rutaTablero));
        texturaTablero.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        SombraA = new Texture(Gdx.files.internal("sombra_a.png"));
        SombraB = new Texture(Gdx.files.internal("sombra_b.png"));

        // 2. Preferencias del Color de Piezas
        texturaPiezasNegras = new Texture(Gdx.files.internal("piezas_negras.png"));
        texturaPiezasBlancas = new Texture(Gdx.files.internal("piezas_blancas.png"));
        regionesEnemigos = new ObjectMap<>();

        String colorElegido = prefs.getString("estiloPiezas", "blancas");
        if (colorElegido.equals("negras")) {
            configColorEnemigo = 2;
        } else {
            configColorEnemigo = 1;
        }
        asignarSetDePiezas(configColorEnemigo);

        // --- RESTO DE LA INICIALIZACIÓN ---
        gestorEnemigos = new GestorEnemigos();
        com.badlogic.gdx.graphics.Pixmap pixmap = new com.badlogic.gdx.graphics.Pixmap(1, 1, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        pixmap.setColor(com.badlogic.gdx.graphics.Color.WHITE);
        pixmap.fill();
        texturaPixelBlanco = new Texture(pixmap);
        pixmap.dispose();

        camera = new OrthographicCamera();
        viewport = new ExtendViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        camera.position.set(WORLD_WIDTH / 2f, WORLD_HEIGHT / 2f, 0);

        // UI de Pausa
        uiStage = new Stage(new ExtendViewport(WORLD_WIDTH, WORLD_HEIGHT));

        try {
            uiSkin = new Skin(Gdx.files.internal("ui/uiskin.json"));
        } catch (Exception e) {
            Gdx.app.error("UI", "Error cargando uiskin: " + e.getMessage());
        }

        hudTable = new com.badlogic.gdx.scenes.scene2d.ui.Table();
        hudTable.setFillParent(true);

        com.badlogic.gdx.scenes.scene2d.ui.TextButton btnPausaHUD = new com.badlogic.gdx.scenes.scene2d.ui.TextButton("||", uiSkin);
        btnPausaHUD.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ClickListener() {
            @Override
            public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                if (!juegoPausado && estadoActual == EstadoJuego.JUGANDO) {
                    activarPausa();
                }
            }
        });

        hudTable.top().right();
        hudTable.add(btnPausaHUD).size(60f, 60f).padTop(15f).padRight(15f);

        uiStage.addActor(hudTable);

        pauseWidget = new PauseWidget(juego, uiSkin, uiStage, new PauseWidget.IPauseListener() {
            @Override
            public void onResume() {
                quitarPausa();
            }
        });

        Gdx.input.setInputProcessor(uiStage);
    }

    private void activarPausa() {
        juegoPausado = true;
        hudTable.setVisible(false);
        uiStage.addActor(pauseWidget);
        Gdx.input.setInputProcessor(uiStage);
    }

    private void quitarPausa() {
        juegoPausado = false;
        pauseWidget.remove();
        hudTable.setVisible(true);
        Gdx.input.setInputProcessor(uiStage);
    }

    @Override
    public void render(float delta) {

        tiempoGlobal += delta;

        // CÁLCULO DINÁMICO ADAPTATIVO: Mantiene el tablero en el centro vertical exacto
        offsetYTablero = (viewport.getWorldHeight() - altoTablero) / 2f;

        // Limpiamos pantalla
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // --- 1. DETECCIÓN DE BOTÓN DE PAUSA ---
        if ((Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) || Gdx.input.isKeyJustPressed(Input.Keys.BACK))
            && estadoActual == EstadoJuego.JUGANDO) {

            if (!juegoPausado) {
                activarPausa();
            } else {
                quitarPausa();
            }
        }

        // --- 2. LÓGICA DEL JUEGO ---
        if (!juegoPausado) {
            if (estadoActual == EstadoJuego.JUGANDO) {

                if (tiempoEscudo > 0) {
                    tiempoEscudo -= delta;
                    if (tiempoEscudo <= 0) tieneEscudo = false;
                }

                if (movimientosIA > 0) {
                    temporizadorIA -= delta;
                    if (temporizadorIA <= 0) {
                        ejecutarMovimientoIA();
                        movimientosIA--;
                        temporizadorIA = 0.3f;
                    }
                } else {
                    handleInput();
                }

                if (tiempoReloj > 0) {
                    tiempoReloj -= delta;
                    scrollSpeed = (10f + (filaMaximaAlcanzada * 0.25f)) * 0.4f;
                } else {
                    scrollSpeed = 10f + (filaMaximaAlcanzada * 0.25f);
                    if (modoActual == ModoJuego.CONTRARRELOJ) {
                        tiempoRestante -= delta;
                        if (tiempoRestante <= 0) {
                            tiempoRestante = 0;
                            dispararGameOver("¡TIEMPO AGOTADO!");
                        }
                    }
                }

                if (modoActual == ModoJuego.INFINITO) {
                    if (filaMaximaAlcanzada > 0) {
                        float presionSpeed = 25f + (filaMaximaAlcanzada * 0.8f);
                        if (tiempoReloj > 0) presionSpeed *= 0.4f;
                        camaraAutoY += presionSpeed * delta;
                    }

                    if (targetScrollY > camaraAutoY) {
                        camaraAutoY += (targetScrollY - camaraAutoY) * scrollSpeed * delta;
                    }
                    scrollY = camaraAutoY;

                    float pyJugadorCalculado = (filaLogicaJugador * CELL_H) - scrollY + (JUGADOR_FILA_VIS * CELL_H);
                    if (pyJugadorCalculado < -CELL_H && estadoActual == EstadoJuego.JUGANDO) {
                        dispararGameOver("¡TE ALCANZÓ EL TABLERO!");
                    }

                } else {
                    scrollY += (targetScrollY - scrollY) * scrollSpeed * delta;
                    camaraAutoY = scrollY;
                }

                tiempoJugado += delta;
            } else if (estadoActual == EstadoJuego.GAME_OVER) {
                if (Gdx.input.justTouched()) {
                    reiniciarJuego();
                }
            } else if (estadoActual == EstadoJuego.VICTORIA) {
                if (Gdx.input.justTouched()) {
                    juego.setScreen(new com.brk.chessrunner.ui.MainMenuScreen(juego, juego.db));
                }
            }
        }

        // --- 3. DIBUJADO DEL FONDO Y MUNDO ---
        camera.update();
        juego.batch.setProjectionMatrix(camera.combined);
        ScreenUtils.clear(0f, 0f, 0f, 1f);

        juego.batch.begin();

        // FONDO SHADER
        juego.batch.setShader(shaderFondo);
        shaderFondo.setUniformf("u_time", tiempoGlobal);
        shaderFondo.setUniformf("u_colorBase", fondoR, fondoG, fondoB);

        juego.batch.draw(texturaBlanca, -200, -200, viewport.getWorldWidth() + 400, viewport.getWorldHeight() + 400);
        juego.batch.setShader(null);

        juego.batch.flush();
        Rectangle boundsTablero = new Rectangle(0, offsetYTablero, WORLD_WIDTH, altoTablero);
        Rectangle scissors = new Rectangle();
        ScissorStack.calculateScissors(camera, viewport.getScreenX(), viewport.getScreenY(), viewport.getScreenWidth(), viewport.getScreenHeight(), juego.batch.getTransformMatrix(), boundsTablero, scissors);
        ScissorStack.pushScissors(scissors);

        // TABLERO
        float scale = WORLD_WIDTH / texturaTablero.getWidth();
        float scaledHeight = texturaTablero.getHeight() * scale;
        float scrollOffset = scrollY % scaledHeight;

        float drawY = offsetYTablero - scrollOffset;
        if (drawY > offsetYTablero) drawY -= scaledHeight;

        while (drawY < offsetYTablero + altoTablero) {
            juego.batch.draw(texturaTablero, 0, drawY, WORLD_WIDTH, scaledHeight);
            drawY += scaledHeight;
        }

        // SOMBRAS
        int filaEnBase = (int) (scrollY / CELL_H);
        Texture sombraActual = (filaEnBase % 2 == 0) ? SombraA : SombraB;
        float scaleSombra = WORLD_WIDTH / sombraActual.getWidth();
        float altoSombraEscalada = sombraActual.getHeight() * scaleSombra;
        juego.batch.draw(sombraActual, 0, offsetYTablero, WORLD_WIDTH, altoSombraEscalada);

        // ENEMIGOS
        for (Enemigo e : gestorEnemigos.activos) {
            float px = e.colLogica * CELL_W;
            float py = (e.filLogica * CELL_H) - scrollY + (JUGADOR_FILA_VIS * CELL_H) + offsetYTablero;

            if (py > offsetYTablero - CELL_H && py < offsetYTablero + altoTablero + CELL_H) {
                TextureRegion region = regionesEnemigos.get(e.tipo);
                if (region != null) {
                    juego.batch.draw(region, px, py, CELL_W, CELL_H);
                }
            }
        }

        // POWER-UPS
        for (PowerUp p : gestorEnemigos.powerUpsActivos) {
            float px = p.colLogica * CELL_W;
            float py = (p.filLogica * CELL_H) - scrollY + (JUGADOR_FILA_VIS * CELL_H) + offsetYTablero;

            if (py > offsetYTablero - CELL_H && py < offsetYTablero + altoTablero + CELL_H) {
                if (p.tipo == TipoPowerUp.CAMBIO) juego.batch.setColor(com.badlogic.gdx.graphics.Color.MAGENTA);
                else if (p.tipo == TipoPowerUp.ESCUDO) juego.batch.setColor(com.badlogic.gdx.graphics.Color.BLUE);
                else if (p.tipo == TipoPowerUp.SACUDIR_MESA) juego.batch.setColor(com.badlogic.gdx.graphics.Color.ORANGE);
                else if (p.tipo == TipoPowerUp.TUMBAR_MESA) juego.batch.setColor(com.badlogic.gdx.graphics.Color.RED);
                else if (p.tipo == TipoPowerUp.IA) juego.batch.setColor(com.badlogic.gdx.graphics.Color.GREEN);
                else if (p.tipo == TipoPowerUp.RELOJ) juego.batch.setColor(com.badlogic.gdx.graphics.Color.CYAN);

                juego.batch.draw(texturaPixelBlanco, px + 15, py + 15, CELL_W - 30, CELL_H - 30);
                juego.batch.setColor(com.badlogic.gdx.graphics.Color.WHITE);
            }
        }

        // JUGADOR
        TextureRegion regionDibujo = (movimientosCambio > 0 && piezaTransformada != null)
            ? regionesJugador.get(piezaTransformada)
            : piezaRey;

        float pxJugador = isDragging ? dragX : (jugadorCol * CELL_W);
        float pyJugador = isDragging ? dragY : ((filaLogicaJugador * CELL_H) - scrollY + (JUGADOR_FILA_VIS * CELL_H) + offsetYTablero);

        juego.batch.draw(regionDibujo, pxJugador, pyJugador, CELL_W, CELL_H);

        // AURA DE ESCUDO
        if (tieneEscudo) {
            juego.batch.setColor(0, 0, 1, 0.4f);
            juego.batch.draw(texturaPixelBlanco, jugadorCol * CELL_W, (JUGADOR_FILA_VIS * CELL_H) + offsetYTablero, CELL_W, CELL_H);
            juego.batch.setColor(com.badlogic.gdx.graphics.Color.WHITE);
        }

        // --- FIN DE RECORTE (SCISSOR) ---
        juego.batch.flush();
        ScissorStack.popScissors();

        // TEXTOS DE INTERFAZ
        if (estadoActual == EstadoJuego.JUGANDO) {
            if (modoActual == ModoJuego.TUTORIAL) {
                font.draw(juego.batch, "Tutorial " + nivelActual + " - Meta: " + filaMeta, 20, viewport.getWorldHeight() - 20);
            } else {
                font.draw(juego.batch, "Puntos: " + (filaMaximaAlcanzada * 10), 20, viewport.getWorldHeight() - 20);

                if (modoActual == ModoJuego.CONTRARRELOJ) {
                    font.draw(juego.batch, "Tiempo: " + (int) tiempoRestante + "s", 20, viewport.getWorldHeight() - 60);
                }
            }
        } else if (estadoActual == EstadoJuego.GAME_OVER) {
            font.getData().setScale(3f);
            font.draw(juego.batch, "GAME OVER", viewport.getWorldWidth() / 2f - 110, viewport.getWorldHeight() / 2f + 80);
            font.getData().setScale(1.5f);
            font.draw(juego.batch, mensajeGameOver, viewport.getWorldWidth() / 2f - 90, viewport.getWorldHeight() / 2f + 30);
            font.getData().setScale(2f);
            font.draw(juego.batch, "Puntos: " + (filaMaximaAlcanzada * 10), viewport.getWorldWidth() / 2f - 70, viewport.getWorldHeight() / 2f - 20);
            font.getData().setScale(1.2f);
            font.draw(juego.batch, "Toca para reiniciar", viewport.getWorldWidth() / 2f - 90, viewport.getWorldHeight() / 2f - 70);
            font.getData().setScale(2f);
        } else if (estadoActual == EstadoJuego.VICTORIA) {
            font.getData().setScale(3f);
            font.draw(juego.batch, "¡VICTORIA!", viewport.getWorldWidth() / 2f - 110, viewport.getWorldHeight() / 2f + 50);
            font.getData().setScale(1.5f);
            font.draw(juego.batch, "Tutorial completado", viewport.getWorldWidth() / 2f - 100, viewport.getWorldHeight() / 2f - 10);
            font.getData().setScale(1.2f);
            font.draw(juego.batch, "Toca para continuar", viewport.getWorldWidth() / 2f - 90, viewport.getWorldHeight() / 2f - 60);
            font.getData().setScale(2f);
        }

        juego.batch.end();

        // --- 4. DIBUJADO DE LAS INTERFACES DE UI ---
        uiStage.act(delta);
        uiStage.draw();

        // 5. SISTEMA DE GENERACIÓN CONTINUA E INFINITA
        int filaSuperiorPantalla = (int) ((scrollY + viewport.getWorldHeight()) / CELL_H);
        int filaObjetivo = Math.max(filaLogicaJugador + 8, filaSuperiorPantalla + 2);

        while (ultimaFilaGenerada < filaObjetivo) {
            ultimaFilaGenerada++;
            TipoPieza piezaActual = (movimientosCambio > 0) ? piezaTransformada : null;

            gestorEnemigos.generarFilaDeEnemigos(ultimaFilaGenerada, jugadorCol, filaLogicaJugador, modoActual, nivelActual, piezaActual);
            gestorEnemigos.intentarGenerarPowerUp(ultimaFilaGenerada);
        }
    }

    private void handleInput() {
        if (Gdx.input.isTouched()) {
            touchPoint.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            viewport.unproject(touchPoint);

            if (Gdx.input.justTouched()) {
                float px = jugadorCol * CELL_W;
                float py = (filaLogicaJugador * CELL_H) - scrollY + (JUGADOR_FILA_VIS * CELL_H) + offsetYTablero;

                if (touchPoint.x >= px && touchPoint.x <= px + CELL_W &&
                    touchPoint.y >= py && touchPoint.y <= py + CELL_H) {
                    isDragging = true;
                }
            }

            if (isDragging) {
                dragX = touchPoint.x - (CELL_W / 2);
                dragY = touchPoint.y - (CELL_H / 2);
            }

        } else {
            if (isDragging) {
                isDragging = false;

                int targetCol = (int) (touchPoint.x / CELL_W);

                float yRealTablero = touchPoint.y - offsetYTablero + scrollY - (JUGADOR_FILA_VIS * CELL_H);
                int nuevaFilaLogica = (int) (yRealTablero / CELL_H);
                if (yRealTablero < 0) nuevaFilaLogica -= 1;

                int diffCol = targetCol - jugadorCol;
                int diffRow = nuevaFilaLogica - filaLogicaJugador;

                int limiteInferior = Math.max(0, filaMaximaAlcanzada - 1);
                boolean retrocesoValido = nuevaFilaLogica >= limiteInferior;

                boolean movimientoValido = false;
                boolean caminoLibreJugador = true;

                if (movimientosCambio > 0) {
                    movimientoValido = switch (piezaTransformada) {
                        case TORRE -> (diffCol == 0 || diffRow == 0) && retrocesoValido;
                        case ALFIL -> (Math.abs(diffCol) == Math.abs(diffRow)) && retrocesoValido;
                        case CABALLO ->
                            ((Math.abs(diffCol) == 1 && Math.abs(diffRow) == 2) || (Math.abs(diffCol) == 2 && Math.abs(diffRow) == 1)) && retrocesoValido;
                        case REINA ->
                            (diffCol == 0 || diffRow == 0 || Math.abs(diffCol) == Math.abs(diffRow)) && retrocesoValido;
                        default ->
                            Math.abs(diffCol) <= 1 && Math.abs(diffRow) <= 1 && retrocesoValido;
                    };
                } else {
                    movimientoValido = Math.abs(diffCol) <= 1 && Math.abs(diffRow) <= 1 && retrocesoValido;
                }

                if (movimientoValido && movimientosCambio > 0 && piezaTransformada != TipoPieza.CABALLO && (diffCol != 0 || diffRow != 0)) {
                    if (caminoBloqueadoJugador(piezaTransformada, jugadorCol, filaLogicaJugador, targetCol, nuevaFilaLogica)) {
                        caminoLibreJugador = false;
                    }
                }

                if (targetCol >= 0 && targetCol < COLS && (diffCol != 0 || diffRow != 0) && movimientoValido && caminoLibreJugador) {
                    if (movimientosCambio > 0) movimientosCambio--;

                    jugadorCol = targetCol;
                    filaLogicaJugador = nuevaFilaLogica;

                    if (filaLogicaJugador > filaMaximaAlcanzada) {
                        filaMaximaAlcanzada = filaLogicaJugador;
                    }

                    targetScrollY = filaMaximaAlcanzada * CELL_H;

                    gestorEnemigos.intentarCapturar(jugadorCol, filaLogicaJugador);
                    recolectarPowerUpsLocal();

                    if (gestorEnemigos.estaCasillaAmenazada(jugadorCol, filaLogicaJugador)) {
                        if (tieneEscudo) {
                            tieneEscudo = false;
                            tiempoEscudo = 0f;
                        } else {
                            dispararGameOver("¡JAQUE MATE!");
                        }
                    }

                    gestorEnemigos.limpiarObjetosPasados((int) (scrollY / CELL_H));
                    if (modoActual == ModoJuego.TUTORIAL && filaLogicaJugador >= filaMeta) estadoActual = EstadoJuego.VICTORIA;
                }
            }
        }
    }

    public boolean caminoBloqueadoJugador(TipoPieza pieza, int c1, int f1, int c2, int f2) {
        if (pieza == TipoPieza.CABALLO || pieza == null) return false;

        int dCol = Integer.signum(c2 - c1);
        int dRow = Integer.signum(f2 - f1);
        int cc = c1 + dCol;
        int rr = f1 + dRow;

        while (cc != c2 || rr != f2) {
            if (gestorEnemigos.hayEnemigoEnCasilla(cc, rr)) {
                return true;
            }
            cc += dCol;
            rr += dRow;
        }
        return false;
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        if (uiStage != null) {
            uiStage.getViewport().update(width, height, true);
        }
    }

    private void reiniciarJuego() {
        scrollY = 0f;
        targetScrollY = 0f;
        camaraAutoY = 0f;
        filaLogicaJugador = 0;
        filaMaximaAlcanzada = 0;
        jugadorCol = 2;
        tiempoJugado = 0f;
        tieneEscudo = false;
        tiempoEscudo = 0f;
        movimientosCambio = 0;
        piezaTransformada = null;
        movimientosIA = 0;
        temporizadorIA = 0f;
        tiempoReloj = 0f;
        isDragging = false;
        ultimaFilaGenerada = 5;

        if (modoActual == ModoJuego.CONTRARRELOJ) {
            tiempoRestante = 60f;
        }

        gestorEnemigos.vaciar();
        estadoActual = EstadoJuego.JUGANDO;
    }

    private void activarPowerUp(TipoPowerUp tipo) {
        System.out.println("Power-up recolectado: " + tipo);
        switch (tipo) {
            case CAMBIO:
                movimientosCambio = com.badlogic.gdx.math.MathUtils.random(5, 10);
                TipoPieza[] piezas = {TipoPieza.TORRE, TipoPieza.CABALLO, TipoPieza.ALFIL, TipoPieza.REINA};
                piezaTransformada = piezas[com.badlogic.gdx.math.MathUtils.random(0, piezas.length - 1)];
                break;
            case ESCUDO:
                tieneEscudo = true;
                tiempoEscudo = 15f;
                break;
            case SACUDIR_MESA:
                int eliminar = com.badlogic.gdx.math.MathUtils.random(2, 4);
                for (int i = 0; i < eliminar; i++) {
                    if (gestorEnemigos.activos.size > 0) {
                        int r = com.badlogic.gdx.math.MathUtils.random(0, gestorEnemigos.activos.size - 1);
                        gestorEnemigos.activos.removeIndex(r);
                    }
                }
                break;
            case TUMBAR_MESA:
                gestorEnemigos.activos.clear();
                break;
            case IA:
                movimientosIA = com.badlogic.gdx.math.MathUtils.random(8, 10);
                temporizadorIA = 0f;
                break;
            case RELOJ:
                tiempoReloj = 5f;
                break;
        }
    }

    private void recolectarPowerUpsLocal() {
        for (int i = gestorEnemigos.powerUpsActivos.size - 1; i >= 0; i--) {
            PowerUp p = gestorEnemigos.powerUpsActivos.get(i);
            if (p.colLogica == jugadorCol && p.filLogica == filaLogicaJugador) {
                activarPowerUp(p.tipo);
                gestorEnemigos.powerUpsActivos.removeIndex(i);
            }
        }
    }

    private void ejecutarMovimientoIA() {
        int mejorCol = jugadorCol;
        int mejorFila = filaLogicaJugador;
        boolean movio = false;

        int nuevaFila = filaLogicaJugador + 1;
        int[] columnasPosibles = {jugadorCol, jugadorCol - 1, jugadorCol + 1, jugadorCol - 2, jugadorCol + 2};

        for (int col : columnasPosibles) {
            if (col >= 0 && col < COLS) {
                if (!gestorEnemigos.estaCasillaAmenazada(col, nuevaFila) && !gestorEnemigos.estaCasillaDefendida(col, nuevaFila)) {
                    mejorCol = col;
                    mejorFila = nuevaFila;
                    movio = true;
                    break;
                }
            }
        }

        if (!movio) {
            for (int col : columnasPosibles) {
                if (col >= 0 && col < COLS && col != jugadorCol) {
                    if (!gestorEnemigos.estaCasillaAmenazada(col, filaLogicaJugador) && !gestorEnemigos.estaCasillaDefendida(col, filaLogicaJugador)) {
                        mejorCol = col;
                        mejorFila = filaLogicaJugador;
                        movio = true;
                        break;
                    }
                }
            }
        }

        if (!movio) mejorFila = filaLogicaJugador + 1;

        jugadorCol = mejorCol;
        filaLogicaJugador = mejorFila;

        if (filaLogicaJugador > filaMaximaAlcanzada) filaMaximaAlcanzada = filaLogicaJugador;
        targetScrollY = filaMaximaAlcanzada * CELL_H;

        gestorEnemigos.intentarCapturar(jugadorCol, filaLogicaJugador);
        recolectarPowerUpsLocal();

        if (gestorEnemigos.estaCasillaAmenazada(jugadorCol, filaLogicaJugador)) {
            if (tieneEscudo) {
                tieneEscudo = false;
                tiempoEscudo = 0f;
            } else {
                dispararGameOver("¡JAQUE MATE!");
                movimientosIA = 0;
            }
        }

        gestorEnemigos.limpiarObjetosPasados((int) (scrollY / CELL_H));
        if (modoActual == ModoJuego.TUTORIAL && filaLogicaJugador >= filaMeta) estadoActual = EstadoJuego.VICTORIA;
    }

    private void dispararGameOver(String razon) {
        System.out.println(razon + " Game Over.");
        mensajeGameOver = razon;
        estadoActual = EstadoJuego.GAME_OVER;

        String piezaAsesina = "Desconocida";
        for (Enemigo e : gestorEnemigos.activos) {
            if (e.atacaCasilla(jugadorCol, filaLogicaJugador)) {
                piezaAsesina = e.tipo.name();
                break;
            }
        }

        if (juego.db != null) {
            com.brk.chessrunner.database.UsuarioLocal jugadorActual = juego.db.obtenerUsuarioActual();

            if (jugadorActual != null) {
                String idPartida = java.util.UUID.randomUUID().toString();
                int puntuacion = filaMaximaAlcanzada * 10;
                int tiempoSobrevivido = (modoActual == ModoJuego.CONTRARRELOJ) ? (int)(60f - tiempoRestante) : (int) tiempoJugado;
                String fechaIso = java.time.LocalDateTime.now().toString();

                com.brk.chessrunner.database.PartidaLocal nuevaPartida = new com.brk.chessrunner.database.PartidaLocal(
                    idPartida, jugadorActual.getId(), puntuacion, filaMaximaAlcanzada,
                    piezaAsesina, tiempoSobrevivido, fechaIso, false
                );
                juego.db.guardarPartida(nuevaPartida);

                com.brk.chessrunner.network.SyncManager.syncSilently(juego.db, uiStage, uiSkin);
            } else {
                System.err.println("No se pudo guardar: No hay usuario activo.");
            }
        }
    }

    public void asignarSetDePiezas(int color) {
        Texture texturaFuente = (color == 1) ? texturaPiezasNegras : texturaPiezasBlancas;
        Texture texturaJugador = (color == 1) ? texturaPiezasBlancas : texturaPiezasNegras;

        int anchoPieza = 320;
        int altoPieza = 320;

        TextureRegion[][] matrizEnemigos = TextureRegion.split(texturaFuente, anchoPieza, altoPieza);
        TextureRegion[][] matrizJugador = TextureRegion.split(texturaJugador, anchoPieza, altoPieza);

        regionesEnemigos = new ObjectMap<>();
        regionesJugador = new ObjectMap<>();

        regionesEnemigos.put(TipoPieza.PEON, matrizEnemigos[1][2]);
        regionesEnemigos.put(TipoPieza.TORRE, matrizEnemigos[1][0]);
        regionesEnemigos.put(TipoPieza.CABALLO, matrizEnemigos[0][1]);
        regionesEnemigos.put(TipoPieza.ALFIL, matrizEnemigos[1][1]);
        regionesEnemigos.put(TipoPieza.REINA, matrizEnemigos[0][2]);

        regionesJugador.put(TipoPieza.PEON, matrizJugador[1][2]);
        regionesJugador.put(TipoPieza.TORRE, matrizJugador[1][0]);
        regionesJugador.put(TipoPieza.CABALLO, matrizJugador[0][1]);
        regionesJugador.put(TipoPieza.ALFIL, matrizJugador[1][1]);
        regionesJugador.put(TipoPieza.REINA, matrizJugador[0][2]);

        piezaRey = matrizJugador[0][0];
    }

    public void alternarColorEnemigo() { // Sigue sin uso hasta crear la configuracion - igual que los tableros
        configColorEnemigo = (configColorEnemigo == 1) ? 2 : 1;
        asignarSetDePiezas(configColorEnemigo);
    }

    @Override
    public void pause() {
        if (estadoActual == EstadoJuego.JUGANDO && !juegoPausado) {
            activarPausa();
        }
    }

    @Override
    public void resume() { }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void dispose() {
        texturaTablero.dispose();
        SombraA.dispose();
        SombraB.dispose();
        texturaPiezasNegras.dispose();
        texturaPiezasBlancas.dispose();
        if (texturaPixelBlanco != null) texturaPixelBlanco.dispose();
        if (texturaBlanca != null) texturaBlanca.dispose();
        if (shaderFondo != null) shaderFondo.dispose();

        if (uiStage != null) uiStage.dispose();
        if (uiSkin != null) uiSkin.dispose();
        font.dispose();
    }
}
