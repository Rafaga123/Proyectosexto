package com.brk.chessrunner;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
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

    public float tiempoReloj = 0f;

    private Texture texturaPixelBlanco;

    // --- VARIABLES DE INTERFAZ DE PAUSA ---

    private com.badlogic.gdx.scenes.scene2d.ui.Table hudTable; // Cambiado a mayúscula para seguir el estándar
    private com.badlogic.gdx.scenes.scene2d.ui.TextButton btnPausaHUD;
    private Stage uiStage;
    private Skin uiSkin;
    private PauseWidget pauseWidget;

    private boolean juegoPausado = false;


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
        touchPoint = new Vector3();
        font = new com.badlogic.gdx.graphics.g2d.BitmapFont();
        font.getData().setScale(2f);

        //Esto seria para el regreso dentro de movil
        Gdx.input.setCatchKey(Input.Keys.BACK, true);

        texturaTablero = new Texture(Gdx.files.internal("tablero.png"));
        texturaTablero.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
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

        // --- INICIALIZACIÓN DE LA UI DE PAUSA ---
        uiStage = new Stage(new ScreenViewport());

        try {
            uiSkin = new Skin(Gdx.files.internal("ui/uiskin.json"));
        } catch (Exception e) {
            Gdx.app.error("UI", "Error cargando uiskin: " + e.getMessage());
        }

        // --- CREACIÓN DEL BOTÓN HUD DE PAUSA ---
        hudTable = new com.badlogic.gdx.scenes.scene2d.ui.Table();
        hudTable.setFillParent(true);

        btnPausaHUD = new com.badlogic.gdx.scenes.scene2d.ui.TextButton("||", uiSkin);
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
                    scrollSpeed = (10f + (filaMaximaAlcanzada * 0.25f)) * 0.4f; // Cámara lenta (60% más lento)
                    // No restamos 'tiempoRestante', por lo que el reloj de la partida se congela
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

                scrollY += (targetScrollY - scrollY) * scrollSpeed * delta;

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
        ScreenUtils.clear(0f, 0f, 0f, 1f);

        juego.batch.begin();

        float scale = WORLD_WIDTH / texturaTablero.getWidth();
        float scaledHeight = texturaTablero.getHeight() * scale;
        float offsetY = scrollY % scaledHeight;

        float drawY = -offsetY;
        while (drawY < WORLD_HEIGHT) {
            juego.batch.draw(texturaTablero, 0, drawY, WORLD_WIDTH, scaledHeight);
            drawY += scaledHeight;
        }

        int filaEnBase = (int) (scrollY / CELL_H);
        Texture sombraActual = (filaEnBase % 2 == 0) ? SombraB : SombraA;
        float scaleSombra = WORLD_WIDTH / sombraActual.getWidth();
        float altoSombraEscalada = sombraActual.getHeight() * scaleSombra;
        juego.batch.draw(sombraActual, 0, 0, WORLD_WIDTH, altoSombraEscalada);

        for (Enemigo e : gestorEnemigos.activos) {
            float px = e.colLogica * CELL_W;
            float py = (e.filLogica * CELL_H) - scrollY + (JUGADOR_FILA_VIS * CELL_H);

            if (py > -CELL_H && py < WORLD_HEIGHT + CELL_H) {
                TextureRegion region = regionesEnemigos.get(e.tipo);
                if (region != null) {
                    juego.batch.draw(region, px, py, CELL_W, CELL_H);
                }
            }
        }

        if (isDragging) {
            juego.batch.draw(piezaRey, dragX, dragY, CELL_W, CELL_H);
        } else {
            float px = jugadorCol * CELL_W;
            float py = JUGADOR_FILA_VIS * CELL_H;
            juego.batch.draw(piezaRey, px, py, CELL_W, CELL_H);
        }

        // --- DIBUJADO DE LA INTERFAZ DE USUARIO (TEXTOS) ---
        if (estadoActual == EstadoJuego.JUGANDO) {
            if (modoActual == ModoJuego.TUTORIAL) {
                font.draw(juego.batch, "Tutorial " + nivelActual + " - Meta: " + filaMeta, 20, WORLD_HEIGHT - 20);
            } else {
                font.draw(juego.batch, "Puntos: " + (filaMaximaAlcanzada * 10), 20, WORLD_HEIGHT - 20);

                // HUD Exclusivo de Contrarreloj
                if (modoActual == ModoJuego.CONTRARRELOJ) {
                    font.draw(juego.batch, "Tiempo: " + (int)tiempoRestante + "s", 20, WORLD_HEIGHT - 60);
                }
            }
        } else if (estadoActual == EstadoJuego.GAME_OVER) {
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
            font.getData().setScale(3f);
            font.draw(juego.batch, "¡VICTORIA!", WORLD_WIDTH / 2f - 110, WORLD_HEIGHT / 2f + 50);

            font.getData().setScale(1.5f);
            font.draw(juego.batch, "Tutorial completado", WORLD_WIDTH / 2f - 100, WORLD_HEIGHT / 2f - 10);

            font.getData().setScale(1.2f);
            font.draw(juego.batch, "Toca para continuar", WORLD_WIDTH / 2f - 90, WORLD_HEIGHT / 2f - 60);

            font.getData().setScale(2f);
        }
        // Dibujar cajas de Power-Ups
        for (PowerUp p : gestorEnemigos.powerUpsActivos) {
            float px = p.colLogica * CELL_W;
            float py = (p.filLogica * CELL_H) - scrollY + (JUGADOR_FILA_VIS * CELL_H);

            if (py > -CELL_H && py < WORLD_HEIGHT + CELL_H) {
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

        // Definir qué textura usar para el jugador (Rey o Transformación)
        TextureRegion regionDibujo = (movimientosCambio > 0 && piezaTransformada != null) ? regionesEnemigos.get(piezaTransformada) : piezaRey;

        float pxJugador = isDragging ? dragX : (jugadorCol * CELL_W);
        float pyJugador = isDragging ? dragY : (JUGADOR_FILA_VIS * CELL_H);

        juego.batch.draw(regionDibujo, pxJugador, pyJugador, CELL_W, CELL_H);

        // Dibujar aura de Escudo si está activo
        if (tieneEscudo) {
            juego.batch.setColor(0, 0, 1, 0.4f);
            juego.batch.draw(texturaPixelBlanco, jugadorCol * CELL_W, JUGADOR_FILA_VIS * CELL_H, CELL_W, CELL_H);
            juego.batch.setColor(com.badlogic.gdx.graphics.Color.WHITE);
        }

        juego.batch.end();

        // --- 4. DIBUJADO DE LAS INTERFACES DE UI ---
        // Siempre se dibuja para mostrar el HUD en juego y el menú cuando se pausa
        uiStage.act(delta);
        uiStage.draw();
    }

    private void handleInput() {
        if (Gdx.input.isTouched()) {
            touchPoint.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            viewport.unproject(touchPoint);

            if (Gdx.input.justTouched()) {
                float px = jugadorCol * CELL_W;
                float py = JUGADOR_FILA_VIS * CELL_H;

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
                int targetVisualRow = (int) (touchPoint.y / CELL_H);

                int diffCol = targetCol - jugadorCol;
                int diffRow = targetVisualRow - JUGADOR_FILA_VIS;
                int nuevaFilaLogica = filaLogicaJugador + diffRow;

                int limiteInferior = Math.max(0, filaMaximaAlcanzada - 1);
                boolean retrocesoValido = nuevaFilaLogica >= limiteInferior;

                boolean movimientoValido = false;

                // Lógica del CAMBIO (Permite teletransportarse al destino si cumple las reglas de la pieza)
                if (movimientosCambio > 0) {
                    switch (piezaTransformada) {
                        case TORRE:
                            movimientoValido = (diffCol == 0 || diffRow == 0) && retrocesoValido;
                            break;
                        case ALFIL:
                            movimientoValido = (Math.abs(diffCol) == Math.abs(diffRow)) && retrocesoValido;
                            break;
                        case CABALLO:
                            movimientoValido = ((Math.abs(diffCol) == 1 && Math.abs(diffRow) == 2) || (Math.abs(diffCol) == 2 && Math.abs(diffRow) == 1)) && retrocesoValido;
                            break;
                        case REINA:
                            movimientoValido = (diffCol == 0 || diffRow == 0 || Math.abs(diffCol) == Math.abs(diffRow)) && retrocesoValido;
                            break;
                        default:
                            movimientoValido = Math.abs(diffCol) <= 1 && Math.abs(diffRow) <= 1 && retrocesoValido;
                    }
                } else {
                    // Reglas base del Rey
                    movimientoValido = Math.abs(diffCol) <= 1 && Math.abs(diffRow) <= 1 && retrocesoValido;
                }

                if (targetCol >= 0 && targetCol < COLS && (diffCol != 0 || diffRow != 0) && movimientoValido) {

                    if (movimientosCambio > 0) movimientosCambio--;

                    jugadorCol = targetCol;
                    filaLogicaJugador = nuevaFilaLogica;

                    if (filaLogicaJugador > filaMaximaAlcanzada) {
                        filaMaximaAlcanzada = filaLogicaJugador;
                    }

                    targetScrollY = filaLogicaJugador * CELL_H;
                    gestorEnemigos.intentarCapturar(jugadorCol, filaLogicaJugador);

                    recolectarPowerUpsLocal();

                    if (gestorEnemigos.estaCasillaAmenazada(jugadorCol, filaLogicaJugador)) {
                        if (tieneEscudo) {
                            tieneEscudo = false;
                            tiempoEscudo = 0f;
                        } else {
                            dispararGameOver("¡JAQUE MATE!");
                        }
                    } else {
                        if (diffRow > 0) {
                            gestorEnemigos.intentarGenerarEnemigos(filaLogicaJugador, jugadorCol, modoActual, nivelActual);
                            gestorEnemigos.intentarGenerarPowerUp(filaLogicaJugador);
                        }
                    }

                    gestorEnemigos.limpiarObjetosPasados((int) (scrollY / CELL_H));
                    if (modoActual == ModoJuego.TUTORIAL && filaLogicaJugador >= filaMeta) estadoActual = EstadoJuego.VICTORIA;

                }
            }
        }
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
        filaLogicaJugador = 0;
        filaMaximaAlcanzada = 0;
        jugadorCol = 2;

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

        int diffRow = mejorFila - filaLogicaJugador;
        jugadorCol = mejorCol;
        filaLogicaJugador = mejorFila;

        if (filaLogicaJugador > filaMaximaAlcanzada) filaMaximaAlcanzada = filaLogicaJugador;
        targetScrollY = filaLogicaJugador * CELL_H;

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
        } else {
            if (diffRow > 0) {
                gestorEnemigos.intentarGenerarEnemigos(filaLogicaJugador, jugadorCol, modoActual, nivelActual);
                gestorEnemigos.intentarGenerarPowerUp(filaLogicaJugador);
            }
        }

        gestorEnemigos.limpiarObjetosPasados((int) (scrollY / CELL_H));
        if (modoActual == ModoJuego.TUTORIAL && filaLogicaJugador >= filaMeta) estadoActual = EstadoJuego.VICTORIA;
    }

    private void dispararGameOver(String razon) {
        System.out.println(razon + " Game Over.");
        mensajeGameOver = razon;
        estadoActual = EstadoJuego.GAME_OVER;

        // Obtenemos el usuario y guardamos en BD local
        com.brk.chessrunner.database.UsuarioLocal jugadorActual = juego.db.obtenerUsuarioActual();

        if (jugadorActual != null) {
            String idPartida = java.util.UUID.randomUUID().toString();
            int puntuacion = filaMaximaAlcanzada * 10;
            // Calculamos el tiempo sobrevivido
            int tiempoSobrevivido = (modoActual == ModoJuego.CONTRARRELOJ) ? (int)(60f - tiempoRestante) : 0;

            com.brk.chessrunner.database.PartidaLocal nuevaPartida = new com.brk.chessrunner.database.PartidaLocal(idPartida, jugadorActual.getId(), puntuacion, tiempoSobrevivido, false);
            juego.db.guardarPartida(nuevaPartida);
        } else {
            System.err.println("No se pudo guardar: No hay usuario activo.");
        }
    }

    public void asignarSetDePiezas(int color) {
        Texture texturaFuente = (color == 1) ? texturaPiezasNegras : texturaPiezasBlancas;
        Texture texturaJugador = (color == 1) ? texturaPiezasBlancas : texturaPiezasNegras;

        int anchoPieza = 320;
        int altoPieza = 320;

        TextureRegion[][] matrizEnemigos = TextureRegion.split(texturaFuente, anchoPieza, altoPieza);
        TextureRegion[][] matrizJugador = TextureRegion.split(texturaJugador, anchoPieza, altoPieza);

        regionesEnemigos.put(TipoPieza.PEON, matrizEnemigos[1][2]);
        regionesEnemigos.put(TipoPieza.TORRE, matrizEnemigos[1][0]);
        regionesEnemigos.put(TipoPieza.CABALLO, matrizEnemigos[0][1]);
        regionesEnemigos.put(TipoPieza.ALFIL, matrizEnemigos[1][1]);
        regionesEnemigos.put(TipoPieza.REINA, matrizEnemigos[0][2]);

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
