package com.brk.chessrunner.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.JsonValue;
import com.brk.chessrunner.database.LocalDatabase;
import com.brk.chessrunner.database.PartidaLocal;
import com.brk.chessrunner.database.UsuarioLocal;
import com.brk.chessrunner.network.ApiClient;

import java.util.List;

public class SyncDialog extends Dialog {

    public SyncDialog(String title, Skin skin, LocalDatabase db) {
        super(title, skin);
        getTitleLabel().setFontScale(0.8f);

        getContentTable().pad(25);

        Label lblTitulo = new Label("Sincronización", skin, "titulo");
        getContentTable().add(lblTitulo).padBottom(15).row();

        Label infoLabel = new Label("Buscando partidas...", skin, "hud");
        infoLabel.setWrap(true);
        getContentTable().add(infoLabel).width(280).padBottom(10).row();

        Image icono = new Image(skin, "default-rect");
        getContentTable().add(icono).size(48, 48).padBottom(10).row();

        Table botonTable = new Table();
        TextButton btnCerrar = new TextButton("Cerrar", skin);
        botonTable.add(btnCerrar).pad(10).width(150);
        getButtonTable().add(botonTable);

        btnCerrar.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) { hide(); }
        });

        UsuarioLocal usuario = db.obtenerUsuarioActual();
        if (usuario != null) {
            List<PartidaLocal> pendientes = db.obtenerPartidasNoSincronizadas(usuario.getId());

            if (pendientes.isEmpty()) {
                infoLabel.setText("No hay partidas nuevas para sincronizar.");
            } else {
                infoLabel.setText("Subiendo " + pendientes.size() + " partidas a la nube...");

                ApiClient.sincronizarPartidas(usuario.getId(), pendientes, new ApiClient.ApiCallback() {
                    @Override
                    public void onExito(JsonValue respuesta) {
                        Gdx.app.postRunnable(() -> {
                            for (PartidaLocal p : pendientes) {
                                db.marcarComoSincronizada(p.getId());
                            }
                            infoLabel.setColor(0, 1, 0, 1);
                            infoLabel.setText("¡Sincronización exitosa!\n" + pendientes.size() + " partidas subidas.");
                        });
                    }

                    @Override
                    public void onError(String mensajeError) {
                        Gdx.app.postRunnable(() -> {
                            infoLabel.setColor(1, 0, 0, 1);
                            infoLabel.setText(mensajeError);
                        });
                    }
                });
            }
        }
    }
}
