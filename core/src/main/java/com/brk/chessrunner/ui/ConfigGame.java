package com.brk.chessrunner.ui;

import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;

public class ConfigGame extends Dialog {

    private final com.badlogic.gdx.Preferences prefs;
    private final Slider musicSlider;
    private final Slider fxSlider;

    public ConfigGame(String title, Skin skin) {
        super(title, skin);

        prefs = com.badlogic.gdx.Gdx.app.getPreferences("ChessRunnerSettings");

        // Configuraciones básicas del cuadro de diálogo
        setMovable(false);
        setResizable(false);

        // Contenido de la pagina
        getContentTable().pad(20);

        // Control de Audio (Música)
        Label musicLabel = new Label("Volumen Musica:", skin, "hud");
        musicSlider = new Slider(0, 1, 0.1f, false, skin);
        musicSlider.setValue(prefs.getFloat("musicVolume", 0.8f));

        // Efectos de Sonido
        Label fxLabel = new Label("Efectos de Sonido:", skin, "hud");
        fxSlider = new Slider(0, 1, 0.1f, false, skin);
        fxSlider.setValue(prefs.getFloat("fxVolume", 0.5f));


        // Añadimos los elementos a la tabla interna del diálogo organizados por filas
        getContentTable().add(musicLabel).left().padRight(10);
        getContentTable().add(musicSlider).width(150).row();

        getContentTable().add(fxLabel).left().padRight(10).padTop(10);
        getContentTable().add(fxSlider).width(150).row();


        // Botón de cierre en la parte inferior
        TextButton btnAceptar = new TextButton("Guardar y Cerrar", skin);
        button(btnAceptar, true); // El 'true' es el objeto que retornará en result()
    }

    @Override
    protected void result(Object object) {
        // Esta lógica se ejecuta automáticamente cuando se presiona el botón del diálogo
        if (object instanceof Boolean && (Boolean) object) {
            prefs.putFloat("musicVolume", musicSlider.getValue());
            prefs.putFloat("fxVolume", fxSlider.getValue());
            prefs.flush();

            System.out.println("Configuraciones de juego guardadas.");
            hide();
        }
    }
}
