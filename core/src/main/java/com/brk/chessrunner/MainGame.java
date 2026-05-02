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

public class MainGame extends ApplicationAdapter {

    SpriteBatch        batch;
    OrthographicCamera camera;
    Viewport           viewport;
    Vector3            touchPoint; // Objeto para reutilizar en cada toque

    // Tablero
    Texture       texturaTablero;

    // variables de Scroll y Progresión
    float scrollY = 0f;
    float targetScrollY = 0f; // Y objetivo al que debe llegar la cámara/tablero
    float scrollSpeed = 10f;  // Velocidad de interpolación (lerp)
    int filaLogicaJugador = 0; // Fila real en el tablero

    // Pieza
    Texture       atlasTextura;
    TextureRegion piezaRey;

    // Mundo virtual
    static final float WORLD_WIDTH  = 480f;
    static final float WORLD_HEIGHT = 800f;


    // Jugador
    int jugadorCol = 2;
    static final int   COLS              = 5;
    static final float CELL_W            = WORLD_WIDTH / COLS;
    static final float CELL_H            = CELL_W;
    static final int   JUGADOR_FILA_VIS  = 1;

    @Override
    public void create() {
        batch = new SpriteBatch();
        touchPoint = new Vector3(); // Inicializamos el vector

        // Configuración de textura del tablero con Repeat para scroll infinito
        texturaTablero = new Texture(Gdx.files.internal("tablero.png"), true);
        texturaTablero.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        texturaTablero.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        // Pieza del jugador (Rey)
        atlasTextura = new Texture("test.png");
        piezaRey     = new TextureRegion(atlasTextura, 64, 73);

        camera   = new OrthographicCamera();
        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        camera.position.set(WORLD_WIDTH / 2f, WORLD_HEIGHT / 2f, 0);
    }

    @Override
    public void render() {
        // 1. Manejar Input
        handleInput();

        // 2. Lógica de Scroll (Movimiento suave)
        float delta = Gdx.graphics.getDeltaTime();
        // Lerp: acerca suavemente scrollY hacia targetScrollY
        scrollY += (targetScrollY - scrollY) * scrollSpeed * delta;

        // 3. Dibujado
        camera.update();
        batch.setProjectionMatrix(camera.combined);
        ScreenUtils.clear(0f, 0f, 0f, 1f);

        batch.begin();

        // Calcular la escala para que la textura ocupe exactamente los 480 de ancho
        float scale = WORLD_WIDTH / texturaTablero.getWidth();
        float scaledHeight = texturaTablero.getHeight() * scale;

        // Determinar el desfase actual basado en el scroll
        float offsetY = scrollY % scaledHeight;

        // Dibujar el tablero en bucle para cubrir toda la altura de la pantalla
        // Esto crea el efecto de scroll infinito sin problemas de TextureWrap
        float drawY = -offsetY;
        while (drawY < WORLD_HEIGHT) {
            batch.draw(texturaTablero, 0, drawY, WORLD_WIDTH, scaledHeight);
            drawY += scaledHeight;
        }

        // Dibujar Jugador (se mantiene en su posición visual fija)
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

                // El jugador avanza una fila
                filaLogicaJugador++;

                // Actualizamos el objetivo de scroll para que el tablero baje
                targetScrollY = filaLogicaJugador * CELL_H;
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

    @Override
    public void dispose() {
        batch.dispose();
        texturaTablero.dispose();
        atlasTextura.dispose();
    }
}

// Falta mejorar y determinar el movimiento de la pieza principal, se puede mover en todas las direcciones
