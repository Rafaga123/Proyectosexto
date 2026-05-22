package com.brk.chessrunner;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.utils.ObjectMap;

public class MainGame extends ApplicationAdapter {

    SpriteBatch batch;
    OrthographicCamera camera;
    Viewport viewport;
    Vector3 touchPoint; // Objeto para reutilizar en cada toque

    // --- ESTADOS DEL JUEGO PARA DETERMINAR LA SITUACIÓN DEL JUGADOR ---
    public enum EstadoJuego {
        JUGANDO,
        GAME_OVER
    }
    EstadoJuego estadoActual = EstadoJuego.JUGANDO;

    // Tablero
    Texture texturaTablero;
    Texture SombraA;
    Texture SombraB;

    // variables de Scroll y Progresión
    float scrollY = 0f;
    float targetScrollY = 0f; // Y objetivo al que debe llegar la cámara/tablero
    float scrollSpeed = 10f;  // Velocidad de interpolación (lerp)
    int filaLogicaJugador = 0; // Fila real en el tablero
    GestorEnemigos gestorEnemigos;

    // Pieza
    TextureRegion piezaRey;

    // --- CONFIGURACIÓN DE COLOR ---
    // 1 = Negro (Default), 2 = Blanco
    int configColorEnemigo = 1;

    // Texturas originales
    Texture texturaPiezasNegras;
    Texture texturaPiezasBlancas;

    // Mapa para las regiones que se están usando actualmente
    ObjectMap<TipoPieza, TextureRegion> regionesEnemigos;

    // Mundo virtual
    static final float WORLD_WIDTH  = 480f;
    static final float WORLD_HEIGHT = 800f;

    // Jugador
    int jugadorCol = 2;
    static final int COLS = 5;
    int filaMaximaAlcanzada = 0;
    static final float CELL_W = WORLD_WIDTH / COLS;
    static final float CELL_H = CELL_W;
    static final int JUGADOR_FILA_VIS = 1;
    boolean isDragging = false;
    float dragX = 0f;
    float dragY = 0f;

    @Override
    public void create() {
        batch = new SpriteBatch();
        touchPoint = new Vector3();

        // Cargar tablero puro y sombras
        texturaTablero = new Texture(Gdx.files.internal("tablero.png"));
        texturaTablero.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        SombraA = new Texture(Gdx.files.internal("sombra_a.png"));
        SombraB = new Texture(Gdx.files.internal("sombra_b.png"));

        // --- CARGA DE PIEZAS ENEMIGAS ---
        texturaPiezasNegras = new Texture(Gdx.files.internal("piezas_negras.png"));
        texturaPiezasBlancas = new Texture(Gdx.files.internal("piezas_blancas.png"));
        regionesEnemigos = new ObjectMap<>();
        asignarSetDePiezas(configColorEnemigo);

        // Inicializar Gestor
        gestorEnemigos = new GestorEnemigos();
        // gestorEnemigos.generarEnemigo(TipoPieza.ALFIL, 2, 5);

        camera = new OrthographicCamera();
        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        camera.position.set(WORLD_WIDTH / 2f, WORLD_HEIGHT / 2f, 0);
    }

    @Override
    public void render() {
        // Condicional para determinar si el jugador esta vivo
        if (estadoActual == EstadoJuego.JUGANDO) {
            handleInput();

            // Lógica de Scroll (Movimiento suave) (Hay que modificar eventualmente)
            float delta = Gdx.graphics.getDeltaTime();
            scrollY += (targetScrollY - scrollY) * scrollSpeed * delta;
        } else if (estadoActual == EstadoJuego.GAME_OVER) {
            // Si tocamos la pantalla estando muertos, reiniciamos el juego (Hay que modificar eventualmente)
            if (Gdx.input.justTouched()) {
                reiniciarJuego();
            }
        }

        // --- EL DIBUJADO (BATCH) SIGUE IGUAL ---
        camera.update();
        batch.setProjectionMatrix(camera.combined);
        ScreenUtils.clear(0f, 0f, 0f, 1f);

        batch.begin();

        // --- DIBUJO DEL TABLERO INFINITO ---
        float scale = WORLD_WIDTH / texturaTablero.getWidth();
        float scaledHeight = texturaTablero.getHeight() * scale;
        float offsetY = scrollY % scaledHeight;

        float drawY = -offsetY;
        while (drawY < WORLD_HEIGHT) {
            batch.draw(texturaTablero, 0, drawY, WORLD_WIDTH, scaledHeight);
            drawY += scaledHeight;
        }

        // --- DIBUJO DE SOMBRA EN LA BASE ---
        // Calcular qué fila lógica está actualmente cruzando la coordenada Y=0
        int filaEnBase = (int) (scrollY / CELL_H);

        Texture sombraActual;
        // Si es una fila par, la base es la fila de "Abajo" -> Sombra B
        // Si es una fila impar (1, 3, 5...), la base es la fila de "Arriba" -> Sombra A
        if (filaEnBase % 2 == 0) {
            sombraActual = SombraB;
        } else {
            sombraActual = SombraA;
        }

        // Escalar la sombra para que cubra tod el ancho de la pantalla
        float scaleSombra = WORLD_WIDTH / sombraActual.getWidth();
        float altoSombraEscalada = sombraActual.getHeight() * scaleSombra;

        // Dibujarla estática en la parte inferior (Y=0)
        batch.draw(sombraActual, 0, 0, WORLD_WIDTH, altoSombraEscalada);

        // --- DIBUJO DE ENEMIGOS ---
        for (Enemigo e : gestorEnemigos.activos) {
            float px = e.colLogica * CELL_W;
            // Calcular la posicion vertical en base al scroll
            float py = (e.filLogica * CELL_H) - scrollY + (JUGADOR_FILA_VIS * CELL_H);

            if (py > -CELL_H && py < WORLD_HEIGHT + CELL_H) {
                TextureRegion region = regionesEnemigos.get(e.tipo);
                if (region != null) {
                    batch.draw(region, px, py, CELL_W, CELL_H);
                }
            }
        }

        // --- DIBUJAR JUGADOR ---
        if (isDragging) {
            // Dibuja la pieza flotando en la posición del dedo
            batch.draw(piezaRey, dragX, dragY, CELL_W, CELL_H);
        } else {
            // Dibuja la pieza encajada en su cuadrícula
            float px = jugadorCol * CELL_W;
            float py = JUGADOR_FILA_VIS * CELL_H;
            batch.draw(piezaRey, px, py, CELL_W, CELL_H);
        }

        batch.end();
    }

    private void handleInput() {
        // Conficional para determinar si el dedo está tocando la pantalla
        if (Gdx.input.isTouched()) {
            touchPoint.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            viewport.unproject(touchPoint);

            // Verificar si se esta tocando la pantalla
            if (Gdx.input.justTouched()) {
                float px = jugadorCol * CELL_W;
                float py = JUGADOR_FILA_VIS * CELL_H;

                // Verificar si el toque fue dentro de los límites del Rey
                if (touchPoint.x >= px && touchPoint.x <= px + CELL_W &&
                    touchPoint.y >= py && touchPoint.y <= py + CELL_H) {
                    isDragging = true;
                }
            }

            // Mientras se arrastra, se actualiza las coordenadas del dibujo
            if (isDragging) {
                // Centramos la pieza en el dedo
                dragX = touchPoint.x - (CELL_W / 2);
                dragY = touchPoint.y - (CELL_H / 2);
            }

        } else {
            // Verificar si se ha soltado la pieza
            if (isDragging) {
                isDragging = false;

                int targetCol = (int) (touchPoint.x / CELL_W);
                int targetVisualRow = (int) (touchPoint.y / CELL_H);

                int diffCol = targetCol - jugadorCol;
                int diffRow = targetVisualRow - JUGADOR_FILA_VIS;

                // Calculamos a que fila del mundo real esta intentando ir
                int nuevaFilaLogica = filaLogicaJugador + diffRow;

                // REGLA DE RETROCESO: No puede bajar mas de 1 casilla de su record maximo, ni bajar de 0
                int limiteInferior = Math.max(0, filaMaximaAlcanzada - 1);
                boolean retrocesoValido = nuevaFilaLogica >= limiteInferior;

                // REGLAS DEL REY: Maximo 1 casilla en cualquier direccion
                if (targetCol >= 0 && targetCol < COLS &&
                    Math.abs(diffCol) <= 1 && Math.abs(diffRow) <= 1 &&
                    (diffCol != 0 || diffRow != 0) && retrocesoValido) {

                    jugadorCol = targetCol;
                    filaLogicaJugador = nuevaFilaLogica;

                    // Actualizamos nuestro record de altura
                    if (filaLogicaJugador > filaMaximaAlcanzada) {
                        filaMaximaAlcanzada = filaLogicaJugador;
                    }

                    targetScrollY = filaLogicaJugador * CELL_H;

                    // Intentamos capturar
                    // Eliminamos cualquier pieza que esté en la casilla destino
                    gestorEnemigos.intentarCapturar(jugadorCol, filaLogicaJugador);

                    // Supervivencia
                    // Evaluamos la amenaza DESPUÉS de la posible captura
                    if (gestorEnemigos.estaCasillaAmenazada(jugadorCol, filaLogicaJugador)) {
                        System.out.println("¡JAQUE MATE! Game Over.");
                        estadoActual = EstadoJuego.GAME_OVER;
                    } else {
                        System.out.println("Avanzaste a una zona segura.");
                    }

                    // Generacion
                    // Generamos piezas solo si avanzamos, pasándole también la columna actual
                    if (diffRow > 0) {
                        gestorEnemigos.intentarGenerarEnemigos(filaLogicaJugador, jugadorCol);
                    }

                    int filaBase = (int) (scrollY / CELL_H);
                    gestorEnemigos.limpiarEnemigosPasados(filaBase);
                }
            }
        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    public int getFilaLogica() {
        return (int)(scrollY / CELL_H) + JUGADOR_FILA_VIS;
    }

    private void reiniciarJuego() {
        scrollY = 0f;
        targetScrollY = 0f;
        filaLogicaJugador = 0;
        filaMaximaAlcanzada = 0;
        jugadorCol = 2; // Volvemos al centro

        // Limpiamos la memoria de enemigos
        gestorEnemigos.activos.clear();

        // Volvemos a la vida
        estadoActual = EstadoJuego.JUGANDO;
    }
    /**
     * Recorta y asigna las regiones de textura según el color elegido.
     * @param color 1 para negro, 2 para blanco.
     */
    public void asignarSetDePiezas(int color) {
        // Textura para los enemigos
        Texture texturaFuente = (color == 1) ? texturaPiezasNegras : texturaPiezasBlancas;
        // Textura para el jugador (siempre el color opuesto)
        Texture texturaJugador = (color == 1) ? texturaPiezasBlancas : texturaPiezasNegras;

        // Medida exacta de las piezas
        int anchoPieza = 320;
        int altoPieza = 320;

        // libGDX corta ambas imágenes automáticamente usando esa medida
        TextureRegion[][] matrizEnemigos = TextureRegion.split(texturaFuente, anchoPieza, altoPieza);
        TextureRegion[][] matrizJugador = TextureRegion.split(texturaJugador, anchoPieza, altoPieza);

        // Asignamos los enemigos usando la matriz de enemigos
        regionesEnemigos.put(TipoPieza.PEON, matrizEnemigos[1][2]);
        regionesEnemigos.put(TipoPieza.TORRE, matrizEnemigos[1][0]);
        regionesEnemigos.put(TipoPieza.CABALLO, matrizEnemigos[0][1]);
        regionesEnemigos.put(TipoPieza.ALFIL, matrizEnemigos[1][1]);
        regionesEnemigos.put(TipoPieza.REINA, matrizEnemigos[0][2]);

        // Asignamos la pieza del jugador (Rey) usando la matriz del jugador
        piezaRey = matrizJugador[0][0];
    }

    // Alternar color en config:
    public void alternarColorEnemigo() {
        configColorEnemigo = (configColorEnemigo == 1) ? 2 : 1;
        asignarSetDePiezas(configColorEnemigo);
    }

    @Override
    public void dispose() {
        batch.dispose();
        texturaTablero.dispose();
        SombraA.dispose();
        SombraB.dispose();
        texturaPiezasNegras.dispose();
        texturaPiezasBlancas.dispose();
    }
}

// Falta mejorar y determinar el movimiento de la pieza principal, se puede mover en todas las direcciones
