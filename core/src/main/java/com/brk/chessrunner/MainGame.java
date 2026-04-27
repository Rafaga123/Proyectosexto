package com.brk.chessrunner;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class MainGame extends ApplicationAdapter {
    SpriteBatch batch;
    Texture atlasTextura;
    TextureRegion piezaRey;

    // --- NUEVAS VARIABLES PARA RESPONSIVE ---
    OrthographicCamera camera;
    Viewport viewport;

    // Definimos nuestra resolución virtual (siempre trabajaremos sobre esto)
    final float WORLD_WIDTH = 480;
    final float WORLD_HEIGHT = 800;
    // -----------------------------------------

    float x, y;

    @Override
    public void create() {
        batch = new SpriteBatch();
        atlasTextura = new Texture("atlas.png");
        piezaRey = new TextureRegion(atlasTextura, 16, 208, 16, 16);

        // 1. Configuramos la cámara
        camera = new OrthographicCamera();

        // 2. Configuramos el Viewport con el tamaño virtual
        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);

        // 3. Centramos la cámara al inicio
        camera.position.set(WORLD_WIDTH / 2, WORLD_HEIGHT / 2, 0);

        // Ahora nuestras coordenadas son respecto a 480x800, no píxeles reales
        x = WORLD_WIDTH / 2 - 32;
        y = 50;
    }

    @Override
    public void render() {
        // Actualizamos la cámara
        camera.update();

        // Le decimos al Batch que use la vista de la cámara
        batch.setProjectionMatrix(camera.combined);

        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);

        batch.begin();
        batch.draw(piezaRey, x, y, 64, 64);
        batch.end();
    }

    // --- ESTE METODO ES VITAL PARA EL RESPONSIVE ---
    @Override
    public void resize(int width, int height) {
        // Cuando la ventana cambia de tamaño, el viewport ajusta el mundo virtual
        viewport.update(width, height);
    }

    @Override
    public void dispose() {
        batch.dispose();
        atlasTextura.dispose();
    }
}
