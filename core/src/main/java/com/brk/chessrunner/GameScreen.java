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
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
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

    private com.badlogic.gdx.scenes.scene2d.ui.Table hudTable; // Cambiado a mayúscula para seguir el estándar
    private Stage uiStage;
    private Skin uiSkin;
    private PauseWidget pauseWidget;

    private boolean juegoPausado = false;
    private float tiempoJugado = 0f;

    private SpriteBatch batch;
    private ShaderProgram shaderFondo;
    private float tiempoGlobal =0f;
    private Texture texturaBlanca;


    // El constructor recibe el juego principal
    public GameScreen(MainGame juego, ModoJuego modoActual, int nivel) {
        this.juego = juego;
        this.modoActual = modoActual;
        this.nivelActual = nivel;

        // Configurar la meta si es tutorial
        if (modoActual == ModoJuego.TUTORIAL) {
            if (nivel == 1) filaMeta = 15;
            else if (nivel == 2) filaMeta = 25;
            else if (nivel == 3) filaMeta = 40;
        }  else if (modoActual == ModoJuego.CONTRARRELOJ) {
        tiempoRestante = 60f; // Hay que ajustar para balancear
        }
    }


    @Override
    public void show() {

        batch = new SpriteBatch();

        // Creamos una textura blanca genérica para que el shader tenga un lienzo donde pintar
        texturaBlanca = new Texture("ui/default.png"); // Puedes usar la textura por defecto de tu uiskin

        // Cargamos el shader
        ShaderProgram.pedantic = false; // Importante para que no de errores si no usas todas las variables
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

        //Esto seria para el regreso dentro de movil
        Gdx.input.setCatchKey(Input.Keys.BACK, true);

        texturaTablero = new Texture(Gdx.files.internal("tablero.png"));
        texturaTablero.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        SombraA = new Texture(Gdx.files.internal("sombra_a.png"));
        SombraB = new Texture(Gdx.files.internal("sombra_b.png"));

        texturaPiezasNegras = new Texture(Gdx.files.internal("piezas_negras.png"));
        texturaPiezasBlancas = new Texture(Gdx.files.internal("piezas_blancas.png"));
        regionesEnemigos = new ObjectMap<>();
        asignarSetDePiezas(configColorEnemigo);

        gestorEnemigos = new GestorEnemigos();
        com.badlogic.gdx.graphics.Pixmap pixmap = new com.badlogic.gdx.graphics.Pixmap(1, 1, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        pixmap.setColor(com.badlogic.gdx.graphics.Color.WHITE);
        pixmap.fill();
        texturaPixelBlanco = new Texture(pixmap);
        pixmap.dispose(); // Liberamos la memoria del constructor

        camera = new OrthographicCamera();
        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        camera.position.set(WORLD_WIDTH / 2f, WORLD_HEIGHT / 2f, 0);

        //camera.zoom=1.2f;

        // --- INICIALIZACIÓN DE LA UI DE PAUSA ---
        uiStage = new Stage(new FitViewport(WORLD_WIDTH, WORLD_HEIGHT));

        try {
            uiSkin = new Skin(Gdx.files.internal("ui/uiskin.json"));
        } catch (Exception e) {
            Gdx.app.error("UI", "Error cargando uiskin: " + e.getMessage());
        }

        // --- CREACIÓN DEL BOTÓN HUD DE PAUSA ---
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

        // Posicionamos el botón arriba a la derecha
        hudTable.top().right();
        hudTable.add(btnPausaHUD).size(60f, 60f).padTop(15f).padRight(15f);

        uiStage.addActor(hudTable);

        // Instanciamos el widget de pausa con el callback para reanudar
        pauseWidget = new PauseWidget(juego, uiSkin, uiStage, new PauseWidget.IPauseListener() {
            @Override
            public void onResume() {
                quitarPausa();
            }
        });

        // Hacemos que la interfaz procese los toques (para poder presionar el botón)
        Gdx.input.setInputProcessor(uiStage);
    }

    // --- MÉTODOS DE CONTROL DE PAUSA ---
    private void activarPausa() {
        juegoPausado = true;
        hudTable.setVisible(false); // Ocultamos el botón ||
        uiStage.addActor(pauseWidget); // Añade el menú a la pantalla
        Gdx.input.setInputProcessor(uiStage); // Asegura prioridad de clics
    }

    private void quitarPausa() {
        juegoPausado = false;
        pauseWidget.remove(); // Quita el menú de la pantalla
        hudTable.setVisible(true); // Vuelve a mostrar el botón ||
        Gdx.input.setInputProcessor(uiStage); // Mantenemos el stage escuchando para el botón HUD
    }

    @Override
    public void render(float delta) {

        // Acumulamos el tiempo
        tiempoGlobal += delta;

        // Limpiamos pantalla
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // --- DIBUJO DEL FONDO CON SHADER ---
        batch.begin();
        batch.setShader(shaderFondo); // Activamos el shader

        // Le pasamos la variable "u_time" al archivo GLSL
        shaderFondo.setUniformf("u_time", tiempoGlobal);

        // Dibujamos un rectángulo que cubra toda la pantalla
        batch.draw(texturaBlanca, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        batch.setShader(null); // Desactivamos el shader para no afectar el resto del juego
        batch.end();

        // --- 1. DETECCIÓN DE BOTÓN DE PAUSA (ESC o Atras) ---
        if ((Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) || Gdx.input.isKeyJustPressed(Input.Keys.BACK))
            && estadoActual == EstadoJuego.JUGANDO) {

            if (!juegoPausado) {
                activarPausa();
            } else {
                quitarPausa();
            }
        }

        // --- 2. LÓGICA DEL JUEGO (Se congela si está pausado) ---
        if (!juegoPausado) {
            if (estadoActual == EstadoJuego.JUGANDO) {

                // Gestión de Escudo
                if (tiempoEscudo > 0) {
                    tiempoEscudo -= delta;
                    if (tiempoEscudo <= 0) tieneEscudo = false;
                }

                // Control: ¿Juega el humano o juega la IA?
                if (movimientosIA > 0) {
                    temporizadorIA -= delta;
                    if (temporizadorIA <= 0) {
                        ejecutarMovimientoIA();
                        movimientosIA--;
                        temporizadorIA = 0.3f; // 1 movimiento cada 0.3 segundos para ver la animación
                    }
                } else {
                    handleInput();
                }

                // Lógica de Reloj / Contrarreloj / Velocidad
                if (tiempoReloj > 0) {
                    tiempoReloj -= delta;
                    scrollSpeed = (10f + (filaMaximaAlcanzada * 0.25f)) * 0.4f; // Efecto cámara lenta
                } else {
                    scrollSpeed = 10f + (filaMaximaAlcanzada * 0.25f); // Velocidad normal que aumenta al subir
                    if (modoActual == ModoJuego.CONTRARRELOJ) {
                        tiempoRestante -= delta;
                        if (tiempoRestante <= 0) {
                            tiempoRestante = 0;
                            dispararGameOver("¡TIEMPO AGOTADO!");
                        }
                    }
                }

                // --- DIVISIÓN DE MODOS DE CÁMARA ---

                if (modoActual == ModoJuego.INFINITO) {
                    // Infinito: La cámara te empuja y te mata si te quedas atrás
                    if (filaMaximaAlcanzada > 0) {
                        float presionSpeed = 25f + (filaMaximaAlcanzada * 0.8f);
                        if (tiempoReloj > 0) presionSpeed *= 0.4f;
                        camaraAutoY += presionSpeed * delta;
                    }

                    if (targetScrollY > camaraAutoY) {
                        camaraAutoY += (targetScrollY - camaraAutoY) * scrollSpeed * delta;
                    }
                    scrollY = camaraAutoY;

                    // Validar si el jugador fue tragado por la pantalla
                    float pyJugadorCalculado = (filaLogicaJugador * CELL_H) - scrollY + (JUGADOR_FILA_VIS * CELL_H);
                    if (pyJugadorCalculado < -CELL_H && estadoActual == EstadoJuego.JUGANDO) {
                        dispararGameOver("¡TE ALCANZÓ EL TABLERO!");
                    }

                } else {
                    // Todos los demas: Movimiento relajado
                    // La cámara solo sube si tú subes.
                    scrollY += (targetScrollY - scrollY) * scrollSpeed * delta;
                    camaraAutoY = scrollY; // Sincronizamos por precaución
                }

                tiempoJugado += delta;
            } else if (estadoActual == EstadoJuego.GAME_OVER) {
                if (Gdx.input.justTouched()) {
                    reiniciarJuego();
                }
            } else if (estadoActual == EstadoJuego.VICTORIA) {
                // Si ganamos, al tocar la pantalla volvemos al menú principal
                if (Gdx.input.justTouched()) {
                    juego.setScreen(new com.brk.chessrunner.ui.MainMenuScreen(juego, juego.db));
                }
            }
        }

        // --- 3. DIBUJADO DEL FONDO Y MUNDO (Siempre activo) ---
        camera.update();
        juego.batch.setProjectionMatrix(camera.combined);

        // Limpiamos el buffer del stage anterior (importante para evitar artefactos)
        ScreenUtils.clear(0f, 0f, 0f, 1f);

        juego.batch.begin();

        //Fondo Shader
        juego.batch.setShader(shaderFondo);
        shaderFondo.setUniformf("u_time",tiempoGlobal);
        juego.batch.draw(texturaBlanca,-200,-200, WORLD_WIDTH+400,WORLD_HEIGHT+400);
        juego.batch.setShader(null);

        // Variables de centralización de nuestro tablero 480x480
        float altoTablero = 640f;
        float offsetYTablero = 80f; // (800 alto de pantalla - 480 alto de tablero) / 2

        // --- INICIO DE RECORTE (SCISSOR) ---
        juego.batch.flush(); // Obligatorio antes de recortar
        Rectangle boundsTablero = new Rectangle(0, offsetYTablero, WORLD_WIDTH, altoTablero);
        Rectangle scissors = new Rectangle();
        ScissorStack.calculateScissors(camera, viewport.getScreenX(), viewport.getScreenY(), viewport.getScreenWidth(), viewport.getScreenHeight(), juego.batch.getTransformMatrix(), boundsTablero, scissors);
        ScissorStack.pushScissors(scissors);

        // TABLERO
        float scale = WORLD_WIDTH / texturaTablero.getWidth();
        float scaledHeight = texturaTablero.getHeight() * scale;
        float scrollOffset = scrollY % scaledHeight;

        float drawY = offsetYTablero - scrollOffset;
        if (drawY > offsetYTablero) drawY -= scaledHeight; // Evita un hueco si el offset es positivo

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
            // AÑADIDO: + offsetYTablero
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
            // AÑADIDO: + offsetYTablero
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
        // AÑADIDO: + offsetYTablero al cálculo si no se está arrastrando
        float pyJugador = isDragging ? dragY : ((filaLogicaJugador * CELL_H) - scrollY + (JUGADOR_FILA_VIS * CELL_H) + offsetYTablero);

        juego.batch.draw(regionDibujo, pxJugador, pyJugador, CELL_W, CELL_H);

        // AURA DE ESCUDO
        if (tieneEscudo) {
            juego.batch.setColor(0, 0, 1, 0.4f);
            juego.batch.draw(texturaPixelBlanco, jugadorCol * CELL_W, (JUGADOR_FILA_VIS * CELL_H) + offsetYTablero, CELL_W, CELL_H);
            juego.batch.setColor(com.badlogic.gdx.graphics.Color.WHITE);
        }

        // --- FIN DE RECORTE (SCISSOR) ---
        juego.batch.flush(); // Obligatorio antes de quitar el recorte
        ScissorStack.popScissors();

        // TEXTOS DE INTERFAZ (Ahora se dibujan libres por encima del shader)
        if (estadoActual == EstadoJuego.JUGANDO) {
            if (modoActual == ModoJuego.TUTORIAL) {
                font.draw(juego.batch, "Tutorial " + nivelActual + " - Meta: " + filaMeta, 20, WORLD_HEIGHT - 20);
            } else {
                font.draw(juego.batch, "Puntos: " + (filaMaximaAlcanzada * 10), 20, WORLD_HEIGHT - 20);

                if (modoActual == ModoJuego.CONTRARRELOJ) {
                    font.draw(juego.batch, "Tiempo: " + (int)tiempoRestante + "s", 20, WORLD_HEIGHT - 60);
                }
            }
        } else if (estadoActual == EstadoJuego.GAME_OVER) {
            // ... (Mismo código de game over que ya tienes)
            font.getData().setScale(3f);
            font.draw(juego.batch, "GAME OVER", WORLD_WIDTH / 2f - 110, WORLD_HEIGHT / 2f + 80);
            font.getData().setScale(1.5f);
            font.draw(juego.batch, mensajeGameOver, WORLD_WIDTH / 2f - 90, WORLD_HEIGHT / 2f + 30);
            font.getData().setScale(2f);
            font.draw(juego.batch, "Puntos: " + (filaMaximaAlcanzada * 10), WORLD_WIDTH / 2f - 70, WORLD_HEIGHT / 2f - 20);
            font.getData().setScale(1.2f);
            font.draw(juego.batch, "Toca para reiniciar", WORLD_WIDTH / 2f - 90, WORLD_HEIGHT / 2f - 70);
            font.getData().setScale(2f);
        } else if (estadoActual == EstadoJuego.VICTORIA) {
            // ... (Mismo código de victoria que ya tienes)
            font.getData().setScale(3f);
            font.draw(juego.batch, "¡VICTORIA!", WORLD_WIDTH / 2f - 110, WORLD_HEIGHT / 2f + 50);
            font.getData().setScale(1.5f);
            font.draw(juego.batch, "Tutorial completado", WORLD_WIDTH / 2f - 100, WORLD_HEIGHT / 2f - 10);
            font.getData().setScale(1.2f);
            font.draw(juego.batch, "Toca para continuar", WORLD_WIDTH / 2f - 90, WORLD_HEIGHT / 2f - 60);
            font.getData().setScale(2f);
        }

        juego.batch.end();

        // --- 4. DIBUJADO DE LAS INTERFACES DE UI ---
        // Siempre se dibuja para mostrar el HUD en juego y el menú cuando se pausa
        uiStage.act(delta);
        uiStage.draw();

        // 5. SISTEMA DE GENERACIÓN CONTINUA E INFINITA
        // Averiguamos cuál es la fila más alta que está viendo la cámara ahora mismo
        int filaSuperiorPantalla = (int) ((scrollY + WORLD_HEIGHT) / CELL_H);

        // Queremos tener siempre generadas 2 filas por encima de lo que se ve en la pantalla
        // Pero si el jugador va rapidísimo, generamos 8 filas por delante de él.
        int filaObjetivo = Math.max(filaLogicaJugador + 8, filaSuperiorPantalla + 2);

        while (ultimaFilaGenerada < filaObjetivo) {
            ultimaFilaGenerada++;
            TipoPieza piezaActual = (movimientosCambio > 0) ? piezaTransformada : null;

            // Le pedimos al gestor que cree la fila específica
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

                float py = (filaLogicaJugador * CELL_H) - scrollY + (JUGADOR_FILA_VIS * CELL_H) + 80f;

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


                float yRealTablero = touchPoint.y - 80f + scrollY - (JUGADOR_FILA_VIS * CELL_H);
                int nuevaFilaLogica = (int) (yRealTablero / CELL_H);
                if (yRealTablero < 0) nuevaFilaLogica -= 1; // Previene un bug con números negativos

                int diffCol = targetCol - jugadorCol;
                int diffRow = nuevaFilaLogica - filaLogicaJugador;

                // El jugador solo puede volver 1 casilla atrás (Lógica intacta)
                int limiteInferior = Math.max(0, filaMaximaAlcanzada - 1);
                boolean retrocesoValido = nuevaFilaLogica >= limiteInferior;

                boolean movimientoValido = false;
                boolean caminoLibreJugador = true;

                // Lógica del CAMBIO
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
                    } /*else {
                        if (diffRow > 0) {
                            TipoPieza piezaActual = (movimientosCambio > 0) ? piezaTransformada : null;
                            gestorEnemigos.intentarGenerarEnemigos(filaLogicaJugador, jugadorCol, modoActual, nivelActual, piezaActual);
                        }
                    }*/

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
        viewport.update(width, height);
        if (uiStage != null) {
            uiStage.getViewport().update(width, height, true);
        }
    }

//    public int getFilaLogica() {
//        return (int)(scrollY / CELL_H) + JUGADOR_FILA_VIS;
//    }

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
                // Otorga entre 5 y 10 movimientos
                movimientosCambio = com.badlogic.gdx.math.MathUtils.random(5, 10);
                // Elige una pieza al azar (Torre, Alfil, Caballo o Reina)
                TipoPieza[] piezas = {TipoPieza.TORRE, TipoPieza.CABALLO, TipoPieza.ALFIL, TipoPieza.REINA};
                piezaTransformada = piezas[com.badlogic.gdx.math.MathUtils.random(0, piezas.length - 1)];
                break;
            case ESCUDO:
                tieneEscudo = true;
                tiempoEscudo = 15f; // 15 segundos de protección
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
                // Al limpiar la memoria, destruye todo lo visible y lo que se acaba de generar fuera de cámara
                gestorEnemigos.activos.clear();
                break;
            case IA:
                movimientosIA = com.badlogic.gdx.math.MathUtils.random(8, 10);
                temporizadorIA = 0f; // Actúa inmediatamente
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
        // Prioridad: frente, diagonales, esquinas
        int[] columnasPosibles = {jugadorCol, jugadorCol - 1, jugadorCol + 1, jugadorCol - 2, jugadorCol + 2};

        // Intentar avanzar
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

        // Si no puede avanzar, intentar moverse a los lados
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

        // Si está completamente acorralado, avanza de frente para sacrificar el escudo o morir
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
                movimientosIA = 0; // Abortar IA
            }
        } /*else {
            if (diffRow > 0) {
                TipoPieza piezaActual = (movimientosCambio > 0) ? piezaTransformada : null;
                gestorEnemigos.intentarGenerarEnemigos(filaLogicaJugador, jugadorCol, modoActual, nivelActual, piezaActual);
                gestorEnemigos.intentarGenerarPowerUp(filaLogicaJugador);
            }
        }*/

        gestorEnemigos.limpiarObjetosPasados((int) (scrollY / CELL_H));
        if (modoActual == ModoJuego.TUTORIAL && filaLogicaJugador >= filaMeta) estadoActual = EstadoJuego.VICTORIA;
    }

    private void dispararGameOver(String razon) {
        System.out.println(razon + " Game Over.");
        mensajeGameOver = razon;
        estadoActual = EstadoJuego.GAME_OVER;

        // Identificamos qué pieza nos mató
        String piezaAsesina = "Desconocida";
        for (Enemigo e : gestorEnemigos.activos) {
            if (e.atacaCasilla(jugadorCol, filaLogicaJugador)) {
                piezaAsesina = e.tipo.name();
                break;
            }
        }

        // Obtenemos el usuario y guardamos en BD local
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
    public void hide() { }

    @Override
    public void dispose() {
        texturaTablero.dispose();
        SombraA.dispose();
        SombraB.dispose();
        texturaPiezasNegras.dispose();
        texturaPiezasBlancas.dispose();
        if (texturaPixelBlanco != null) texturaPixelBlanco.dispose();

        if (uiStage != null) uiStage.dispose();
        if (uiSkin != null) uiSkin.dispose();
        font.dispose();
    }
}
