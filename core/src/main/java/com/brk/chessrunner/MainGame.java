package com.brk.chessrunner;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.brk.chessrunner.database.LocalDatabase;
import com.brk.chessrunner.ui.MainMenuScreen;

public class MainGame extends Game {

    public LocalDatabase db;
    public SpriteBatch batch;
    public ScreenManager screenManager;

    public BitmapFont fontTitulo;
    public BitmapFont fontHUD;
    public BitmapFont fontNormal;

    public BitmapFont fontMini;

    public MainGame(LocalDatabase db) {
        this.db = db;
    }

    @Override
    public void create() {
        batch = new SpriteBatch();
        generarFuentes();

        screenManager = new ScreenManager(this);

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

        this.setScreen(new MainMenuScreen(this, db));
    }

    private void generarFuentes() {
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/PressStart2P-Regular.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter param = new FreeTypeFontGenerator.FreeTypeFontParameter();

        param.minFilter = Texture.TextureFilter.Nearest;
        param.magFilter = Texture.TextureFilter.Nearest;
        param.characters = FreeTypeFontGenerator.DEFAULT_CHARS + "ÁÉÍÓÚáéíóúÑñ¿¡✓✗";

        param.size = 14;
        fontTitulo = generator.generateFont(param);

        param.size = 9;
        fontHUD = generator.generateFont(param);

        param.size = 7;
        fontNormal = generator.generateFont(param);

        generator.dispose();
    }

    @Override
    public void render() {
        super.render();
        if (screenManager != null) {
            screenManager.render(Gdx.graphics.getDeltaTime());
        }
    }

    public void switchScreen(Screen screen) {
        if (screenManager != null) {
            screenManager.setScreen(screen);
        } else {
            setScreen(screen);
        }
    }

    public void switchScreen(Screen screen, float duration) {
        if (screenManager != null) {
            screenManager.setScreen(screen, duration);
        } else {
            setScreen(screen);
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        if (batch != null) batch.dispose();
        if (fontTitulo != null) fontTitulo.dispose();
        if (fontHUD != null) fontHUD.dispose();
        if (fontNormal != null) fontNormal.dispose();
    }
}
