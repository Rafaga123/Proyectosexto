package com.brk.chessrunner.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
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
    private final TextButton[] botonesTablero = new TextButton[8];
    private TextButton btnNegra;
    private TextButton btnBlanca;

    public PersonalizacionDialog(String title, Skin skin) {
        super(title, skin);

        prefs = Gdx.app.getPreferences("ChessRunnerSettings");

        setMovable(false);
        setResizable(false);
        getContentTable().pad(20);

        // --- SECCIÓN 1: TABLEROS (Deslizable) ---
        getContentTable().add(new Label("ESCOGE TABLERO", skin, "hud")).padBottom(10).row();

        Table tablaTableros = new Table();

        int tableroActual = prefs.getInteger("estiloTablero", 1);

        for(int i = 1; i <= 8; i++) {
            final int indexTablero = i;
            String texto = (i == tableroActual) ? "Tablero\n" + i : "Tablero\n" + i;
            TextButton btnTab = new TextButton(texto, skin);
            if (i == tableroActual) {
                btnTab.setColor(Color.GOLD);
            }
            botonesTablero[i - 1] = btnTab;

            btnTab.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    prefs.putInteger("estiloTablero", indexTablero);
                    prefs.flush();
                    actualizarSeleccionTableros(indexTablero);
                    System.out.println("Guardado: Tablero " + indexTablero);
                }
            });

            tablaTableros.add(btnTab).width(120).height(100).pad(5);
        }

        ScrollPane scrollTableros = new ScrollPane(tablaTableros, skin);
        scrollTableros.setScrollingDisabled(false, true);
        scrollTableros.setFadeScrollBars(false);

        getContentTable().add(scrollTableros).width(380).height(130).padBottom(30).row();

        // --- SECCIÓN 2: COLORES ---
        getContentTable().add(new Label("ESCOGE COLOR", skin, "hud")).padBottom(10).row();

        Table tablaColores = new Table();
        btnNegra = new TextButton("Ficha\nNegra", skin);
        btnBlanca = new TextButton("Ficha\nBlanca", skin);

        String colorActual = prefs.getString("estiloPiezas", "blancas");
        if (colorActual.equals("negras")) {
            btnNegra.setText("Ficha\nNegra");
            btnNegra.setColor(Color.GOLD);
        } else {
            btnBlanca.setText("Ficha\nBlanca");
            btnBlanca.setColor(Color.GOLD);
        }

        btnNegra.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                prefs.putString("estiloPiezas", "negras");
                prefs.flush();
                actualizarSeleccionColor("negras");
                System.out.println("Guardado: Fichas Negras");
            }
        });

        btnBlanca.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                prefs.putString("estiloPiezas", "blancas");
                prefs.flush();
                actualizarSeleccionColor("blancas");
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
                hide();
            }
        });

        getButtonTable().add(btnAceptar).pad(10).width(150);
    }

    private void actualizarSeleccionTableros(int seleccionado) {
        for (int i = 0; i < botonesTablero.length; i++) {
            int numTablero = i + 1;
            if (numTablero == seleccionado) {
                botonesTablero[i].setText("Tablero\n" + numTablero);
                botonesTablero[i].setColor(Color.GOLD);
            } else {
                botonesTablero[i].setText("Tablero\n" + numTablero);
                botonesTablero[i].setColor(Color.WHITE);
            }
        }
    }

    private void actualizarSeleccionColor(String seleccionado) {
        boolean negras = seleccionado.equals("negras");
        btnNegra.setText(negras ? "Ficha\nNegra" : "Ficha\nNegra");
        btnNegra.setColor(negras ? Color.GOLD : Color.WHITE);
        btnBlanca.setText(negras ? "Ficha\nBlanca" : "Ficha\nBlanca");
        btnBlanca.setColor(negras ? Color.WHITE : Color.GOLD);
    }
}
