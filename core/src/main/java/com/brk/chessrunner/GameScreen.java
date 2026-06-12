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

    // --- VARIABLES DE INTERFAZ DE PAUSA ---

    private com.badlogic.gdx.scenes.scene2d.ui.Table hudTable; // Cambiado a mayúscula para seguir el estándar
    private com.badlogic.gdx.scenes.scene2d.ui.TextButton btnPausaHUD;
    private Stage uiStage;
    private Skin uiSkin;
    private PauseWidget pauseWidget;

    private boolean juegoPausado = false;

    public GameScreen(MainGame juego) {
    // El constructor recibe el juego principal
    public GameScreen(MainGame juego, ModoJuego modo, int nivel) {
        this.juego = juego;
        this.modoActual = modo;
        this.nivelActual = nivel;

        // Configurar la meta si es tutorial
        if (modo == ModoJuego.TUTORIAL) {
            if (nivel == 1) filaMeta = 15;
            else if (nivel == 2) filaMeta = 25;
            else if (nivel == 3) filaMeta = 40;
        }  else if (modo == ModoJuego.CONTRARRELOJ) {
        tiempoRestante = 60f; // Hay que ajustar para balancear
        }
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
        // Deteccion de salida, para ESC en PC y Atras de Android
        if ((Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) || Gdx.input.isKeyJustPressed(Input.Keys.BACK))
            && estadoActual == EstadoJuego.JUGANDO) {

            if (!juegoPausado) {
                activarPausa();
            } else {
                quitarPausa();
        if (estadoActual == EstadoJuego.JUGANDO) {
            handleInput();

            // Contrarelloj logica
            if (modoActual == ModoJuego.CONTRARRELOJ) {
                tiempoRestante -= delta; // Restamos los milisegundos que van pasando
                if (tiempoRestante <= 0) {
                    tiempoRestante = 0;
                    dispararGameOver("¡TIEMPO AGOTADO!"); // Muelto
                }
            }

            // La velocidad base es 10f, pero aumenta ligeramente por cada fila que subas.
            scrollSpeed = 10f + (filaMaximaAlcanzada * 0.25f);

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

        // --- Logica del juego para la pausa ---
        if (!juegoPausado) {
            if (estadoActual == EstadoJuego.JUGANDO) {
                handleInput();
                scrollY += (targetScrollY - scrollY) * scrollSpeed * delta;
            } else if (estadoActual == EstadoJuego.GAME_OVER) {
                if (Gdx.input.justTouched()) {
                    reiniciarJuego();
                }
            }
        }
            }

        }
        // ---  Dibujado del fondo ---
        camera.update();
        juego.batch.setProjectionMatrix(camera.combined);
        ScreenUtils.clear(0f, 0f, 0f, 1f);

        juego.batch.begin();

        // --- DIBUJADO DEL MUNDO (Tablero, sombras, enemigos, jugador) ---
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

            // Mostramos si fue Jaque Mate o Tiempo Agotado
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

                if (targetCol >= 0 && targetCol < COLS &&
                    Math.abs(diffCol) <= 1 && Math.abs(diffRow) <= 1 &&
                    (diffCol != 0 || diffRow != 0) && retrocesoValido) {

                    jugadorCol = targetCol;
                    filaLogicaJugador = nuevaFilaLogica;

                    if (filaLogicaJugador > filaMaximaAlcanzada) {
                        filaMaximaAlcanzada = filaLogicaJugador;
                    }

                    targetScrollY = filaLogicaJugador * CELL_H;

                    gestorEnemigos.intentarCapturar(jugadorCol, filaLogicaJugador);

                    if (gestorEnemigos.estaCasillaAmenazada(jugadorCol, filaLogicaJugador)) {
                        System.out.println("¡JAQUE MATE! Game Over.");
                        estadoActual = EstadoJuego.GAME_OVER;

                        // Obtenemos el usuario y guardamos usando el db del gestor "juego"
                        if (juego.db != null) {
                            UsuarioLocal jugadorActual = juego.db.obtenerUsuarioActual();

                            if (jugadorActual != null) {
                                String idPartida = UUID.randomUUID().toString();
                                int puntuacion = filaMaximaAlcanzada * 10;
                                int tiempo = 0;

                                PartidaLocal nuevaPartida = new PartidaLocal(idPartida, jugadorActual.getId(), puntuacion, tiempo, false);
                                juego.db.guardarPartida(nuevaPartida);
                            } else {
                                System.err.println("No se pudo guardar: No hay usuario activo.");
                            }
                        }
                        dispararGameOver("¡JAQUE MATE!");
                    } else {
                        if (diffRow > 0) {
                            gestorEnemigos.intentarGenerarEnemigos(filaLogicaJugador, jugadorCol, modoActual, nivelActual);
                        }
                    }

                    int filaBase = (int) (scrollY / CELL_H);
                    gestorEnemigos.limpiarEnemigosPasados(filaBase);

                    // Condicion de victoria, que solo se vera en el tutorial
                    if (modoActual == ModoJuego.TUTORIAL && filaLogicaJugador >= filaMeta) {
                        System.out.println("¡TUTORIAL " + nivelActual + " COMPLETADO!");
                        estadoActual = EstadoJuego.VICTORIA;
                    }
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

        gestorEnemigos.activos.clear();
        estadoActual = EstadoJuego.JUGANDO;
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

        if (uiStage != null) uiStage.dispose();
        if (uiSkin != null) uiSkin.dispose();
        font.dispose();
    }
}
