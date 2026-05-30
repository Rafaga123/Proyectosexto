package com.brk.chessrunner.ui;

import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;

public class ConfigGame extends Dialog {

    public ConfigGame(String title, Skin skin) {
        super(title, skin);

        // Configuraciones básicas del cuadro de diálogo
        setMovable(false);
        setResizable(false);

        // Contenido de la pagina
        getContentTable().pad(20);

        // Control de Audio (Música)
        Label musicLabel = new Label("Volumen Musica:", skin);
        Slider musicSlider = new Slider(0, 1, 0.1f, false, skin);
        musicSlider.setValue(0.8f); // Valor por defecto o cargado de las preferencias

        // Efectos de Sonido
        Label fxLabel = new Label("Efectos de Sonido:", skin);
        Slider fxSlider = new Slider(0, 1, 0.1f, false, skin);
        fxSlider.setValue(0.5f);


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
            System.out.println("Configuraciones guardadas localmente.");
            // Aquí puedes aplicar los cambios de volumen o guardarlos en Gdx.app.getPreferences()
            hide();
        }
    }
}
