package com.brk.chessrunner.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

public class PersonalizacionDialog extends Dialog {

    private final com.badlogic.gdx.Preferences prefs;

    public PersonalizacionDialog(String title, Skin skin) {
        super(title, skin);

        prefs = Gdx.app.getPreferences("ChessRunnerSettings");

        setMovable(false);
        setResizable(false);
        getContentTable().pad(20);

        // --- SECCIÓN 1: PIEZAS ---
        getContentTable().add(new Label("Color de las Piezas:", skin)).colspan(2).padBottom(10).row();

        Table tablaPiezas = new Table();
        TextButton btnPiezasBlancas = new TextButton("Blancas", skin);
        TextButton btnPiezasNegras = new TextButton("Negras", skin);

        tablaPiezas.add(btnPiezasBlancas).padRight(15).width(90);
        tablaPiezas.add(btnPiezasNegras).width(90);
        getContentTable().add(tablaPiezas).colspan(2).padBottom(25).row();

        // --- SECCIÓN 2: TABLERO ---
        getContentTable().add(new Label("Estilo del Tablero:", skin)).colspan(2).padBottom(10).row();

        Table tablaTableros = new Table();
        TextButton btnTableroClasico = new TextButton("Clasico", skin);
        TextButton btnTableroMadera = new TextButton("Madera", skin);

        tablaTableros.add(btnTableroClasico).padRight(15).width(90);
        tablaTableros.add(btnTableroMadera).width(90);
        getContentTable().add(tablaTableros).colspan(2).padBottom(20).row();

        // --- Aqui se colocaria la logica de guardado  ---
        btnPiezasBlancas.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                prefs.putString("estiloPiezas", "blancas");
                prefs.flush();
                // Se podria agregar que se cambie el color del boton para indicar que esta seleccionado o
                //se cambias a un checkbox para aceptar
            }
        });

        // --- BOTÓN DE VOLVER ---
        TextButton btnAceptar = new TextButton("Aceptar", skin);
        btnAceptar.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                hide(); //Cierre del dialog para volver al menu config
            }
        });

        getButtonTable().add(btnAceptar).pad(10).width(120);
    }
}
