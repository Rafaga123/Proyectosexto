package com.brk.chessrunner.network;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.JsonValue;
import com.brk.chessrunner.database.LocalDatabase;
import com.brk.chessrunner.database.PartidaLocal;
import com.brk.chessrunner.database.UsuarioLocal;
import com.brk.chessrunner.ui.Toast;

import java.util.List;

public class SyncManager {

    /**
     * Intenta sincronizar las partidas pendientes de forma silenciosa.
     */
    public static void syncSilently(final LocalDatabase db, final Stage stage, final Skin skin) {
        final UsuarioLocal usuario = db.obtenerUsuarioActual();
        
        // Solo sincronizamos si es un usuario real (no invitado)
        if (usuario == null || usuario.getId().startsWith("guest")) {
            return;
        }

        final List<PartidaLocal> pendientes = db.obtenerPartidasNoSincronizadas(usuario.getId());
        if (pendientes.isEmpty()) return;

        ApiClient.sincronizarPartidas(usuario.getId(), pendientes, new ApiClient.ApiCallback() {
            @Override
            public void onExito(JsonValue respuesta) {
                Gdx.app.postRunnable(() -> {
                    for (PartidaLocal p : pendientes) {
                        db.marcarComoSincronizada(p.getId());
                    }
                    if (stage != null && skin != null) {
                        Toast.show(stage, "✓ Partidas sincronizadas con la nube", skin);
                    }
                });
            }

            @Override
            public void onError(String mensajeError) {
                Gdx.app.postRunnable(() -> {
                    if (stage != null && skin != null) {
                        Toast.show(stage, "✗ " + mensajeError, skin, new com.badlogic.gdx.graphics.Color(0.15f, 0.05f, 0.05f, 0.9f));
                    }
                });
            }
        });
    }
}