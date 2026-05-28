package com.brk.chessrunner.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
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

        Label infoLabel = new Label("Buscando partidas...", skin);
        getContentTable().add(infoLabel).pad(20).row();

        TextButton btnCerrar = new TextButton("Cerrar", skin);
        getButtonTable().add(btnCerrar).pad(10);

        btnCerrar.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) { hide(); }
        });

        // Lógica de sincronización automática al abrir el modal
        UsuarioLocal usuario = db.obtenerUsuarioActual();
        if (usuario != null) {
            List<PartidaLocal> pendientes = db.obtenerPartidasNoSincronizadas(usuario.getId());

            if (pendientes.isEmpty()) {
                infoLabel.setText("No hay partidas nuevas\npara sincronizar.");
            } else {
                infoLabel.setText("Subiendo " + pendientes.size() + " partidas a la nube...");

                ApiClient.sincronizarPartidas(usuario.getId(), pendientes, new ApiClient.ApiCallback() {
                    @Override
                    public void onExito(JsonValue respuesta) {
                        Gdx.app.postRunnable(() -> {
                            // Si el servidor dijo OK, las marcamos en SQLite
                            for (PartidaLocal p : pendientes) {
                                db.marcarComoSincronizada(p.getId());
                            }
                            infoLabel.setColor(0, 1, 0, 1); // Verde
                            infoLabel.setText("¡Sincronización exitosa!\n" + pendientes.size() + " partidas subidas.");
                        });
                    }

                    @Override
                    public void onError(String mensajeError) {
                        Gdx.app.postRunnable(() -> {
                            infoLabel.setColor(1, 0, 0, 1); // Rojo
                            infoLabel.setText("Error: " + mensajeError);
                        });
                    }
                });
            }
        }
    }
}
