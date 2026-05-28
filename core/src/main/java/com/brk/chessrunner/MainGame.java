package com.brk.chessrunner;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.brk.chessrunner.database.LocalDatabase;
import com.brk.chessrunner.ui.MainMenuScreen;

public class MainGame extends Game {

    // Estas variables ahora son públicas para que cualquier pantalla pueda usarlas
    public LocalDatabase db;
    public SpriteBatch batch;

    public MainGame(LocalDatabase db) {
        this.db = db;
    }

    @Override
    public void create() {
        batch = new SpriteBatch();

        // Conservamos la prueba de conexión a la API al arrancar
        System.out.println("Intentando conectar con la API...");
        com.brk.chessrunner.network.ApiClient.login("123@gmail.com", "123", new com.brk.chessrunner.network.ApiClient.ApiCallback() {
            @Override
            public void onExito(com.badlogic.gdx.utils.JsonValue respuesta) {
                System.out.println("¡API RESPONDIÓ OK! Datos: " + respuesta.toString());
            }

            @Override
            public void onError(String mensajeError) {
                System.err.println("API ERROR: " + mensajeError);
            }
        });

        // Le decimos al gestor que inicie mostrando el Menú Principal
        this.setScreen(new MainMenuScreen(this, db));
    }

    @Override
    public void render() {
        // Esta línea es CRÍTICA. Le dice a LibGDX: "Dibuja la pantalla que esté activa ahora mismo"
        super.render();
    }

    @Override
    public void dispose() {
        super.dispose();
        if (batch != null) {
            batch.dispose();
        }
    }
}
