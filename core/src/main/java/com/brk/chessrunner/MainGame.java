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
    Texture atlasTextura;
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
    static final float CELL_W = WORLD_WIDTH / COLS;
    static final float CELL_H = CELL_W;
    static final int JUGADOR_FILA_VIS = 1;

    @Override
    public void create() {
        batch = new SpriteBatch();
        touchPoint = new Vector3();

        // Cargar tablero puro y sombras
        texturaTablero = new Texture(Gdx.files.internal("tablero.png"));
        texturaTablero.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        SombraA = new Texture(Gdx.files.internal("sombra_a.png"));
        SombraB = new Texture(Gdx.files.internal("sombra_b.png"));

        // Pieza del jugador
        atlasTextura = new Texture("test.png"); // Cambiar por pieza real luego
        piezaRey = new TextureRegion(atlasTextura, 64, 73);

        // --- CARGA DE PIEZAS ENEMIGAS ---
        texturaPiezasNegras = new Texture(Gdx.files.internal("piezas_negras.png"));
        texturaPiezasBlancas = new Texture(Gdx.files.internal("piezas_blancas.png"));
        regionesEnemigos = new ObjectMap<>();
        asignarSetDePiezas(configColorEnemigo);

        // Inicializar Gestor
        gestorEnemigos = new GestorEnemigos();
        gestorEnemigos.generarEnemigo(TipoPieza.ALFIL, 2, 5);

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

        // --- DIBUJO DEL JUGADOR ---
        float px = jugadorCol * CELL_W;
        float py = JUGADOR_FILA_VIS * CELL_H;
        batch.draw(piezaRey, px, py, CELL_W, CELL_H);

        batch.end();
    }

    private void handleInput() {
        if (Gdx.input.justTouched()) {
            touchPoint.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            viewport.unproject(touchPoint);

            int tappedCol = (int) (touchPoint.x / CELL_W);

            if (tappedCol >= 0 && tappedCol < COLS) {
                jugadorCol = tappedCol;
                filaLogicaJugador++; // El jugador avanza
                targetScrollY = filaLogicaJugador * CELL_H;

                // Revisamos si la casilla a la que nos movimos es muerte segura
                if (gestorEnemigos.estaCasillaAmenazada(jugadorCol, filaLogicaJugador)) {
                    System.out.println("¡JAQUEEEE! Game Over.");
                    estadoActual = EstadoJuego.GAME_OVER; // ¡El juego se congela visualmente!
                } else {
                    System.out.println("Avanzaste a una zona segura.");
                }

                // Limpiamos los enemigos que ya dejamos atrás
                int filaBase = (int) (scrollY / CELL_H);
                gestorEnemigos.limpiarEnemigosPasados(filaBase);
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
        Texture texturaFuente = (color == 1) ? texturaPiezasNegras : texturaPiezasBlancas;

        // Medida exacta de las piezas
        int anchoPieza = 320;
        int altoPieza = 320;

        // libGDX corta la imagen automáticamente usando esa medida
        TextureRegion[][] matrizPiezas = TextureRegion.split(texturaFuente, anchoPieza, altoPieza);

        // IMPORTANTE: Asegúrate de que este índice coincida con el orden visual de tu PNG
        // Asumiendo que están en la fila 0 y ordenadas de izquierda a derecha:
        regionesEnemigos.put(TipoPieza.PEON, matrizPiezas[1][2]);
        regionesEnemigos.put(TipoPieza.TORRE, matrizPiezas[1][0]);
        regionesEnemigos.put(TipoPieza.CABALLO, matrizPiezas[0][1]);
        regionesEnemigos.put(TipoPieza.ALFIL, matrizPiezas[1][1]);
        regionesEnemigos.put(TipoPieza.REINA, matrizPiezas[0][2]);
        regionesEnemigos.put(TipoPieza.REY, matrizPiezas[0][0]);
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
        atlasTextura.dispose();
        texturaPiezasNegras.dispose();
        texturaPiezasBlancas.dispose();
    }
}

// Falta mejorar y determinar el movimiento de la pieza principal, se puede mover en todas las direcciones
