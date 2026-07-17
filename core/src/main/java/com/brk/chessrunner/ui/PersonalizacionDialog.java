package com.brk.chessrunner.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
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

        // --- SECCIÓN 1: TABLEROS (Deslizable) ---
        getContentTable().add(new Label("ESCOGE TABLERO", skin)).padBottom(10).row();

        Table tablaTableros = new Table();

        // Generamos los 8 botones de tableros dinámicamente y les asignamos el guardado
        for(int i = 1; i <= 8; i++) {
            TextButton btnTab = new TextButton("Tablero\n" + i, skin);
            final int indexTablero = i;

            btnTab.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    // Guardamos el número de tablero elegido
                    prefs.putInteger("estiloTablero", indexTablero);
                    prefs.flush();
                    System.out.println("Guardado: Tablero " + indexTablero);
                }
            });

            tablaTableros.add(btnTab).width(120).height(100).pad(5);
        }


        ScrollPane scrollTableros = new ScrollPane(tablaTableros, skin);
        scrollTableros.setScrollingDisabled(false, true); // Scroll horizontal activado, vertical bloqueado
        scrollTableros.setFadeScrollBars(false);

        // Ancho fijo de 380 para forzar que el contenido no quepa y se pueda deslizar con el dedo
        getContentTable().add(scrollTableros).width(380).height(130).padBottom(30).row();

        // --- SECCIÓN 2: COLORES ---
        getContentTable().add(new Label("ESCOGE COLOR", skin)).padBottom(10).row();

        Table tablaColores = new Table();
        TextButton btnNegra = new TextButton("Ficha\nNegra", skin);
        TextButton btnBlanca = new TextButton("Ficha\nBlanca", skin);

        btnNegra.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                prefs.putString("estiloPiezas", "negras");
                prefs.flush();
                System.out.println("Guardado: Fichas Negras");
            }
        });

        btnBlanca.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                prefs.putString("estiloPiezas", "blancas");
                prefs.flush();
                System.out.println("Guardado: Fichas Blancas");
            }
        });

        tablaColores.add(btnNegra).width(140).height(80).pad(10);
        tablaColores.add(btnBlanca).width(140).height(80).pad(10);
        getContentTable().add(tablaColores).row();

        // --- BOTÓN DE VOLVER ---
        TextButton btnAceptar = new TextButton("Aceptar", skin);
        btnAceptar.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                hide(); // Cierre del dialog para volver al menu config
            }
        });

        getButtonTable().add(btnAceptar).pad(10).width(150);
    }
}
