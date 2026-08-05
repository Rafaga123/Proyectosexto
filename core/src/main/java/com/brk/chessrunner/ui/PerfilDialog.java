package com.brk.chessrunner.ui;

import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.brk.chessrunner.MainGame;
import com.brk.chessrunner.database.EstadisticasUsuario;
import com.brk.chessrunner.database.LocalDatabase;
import com.brk.chessrunner.database.UsuarioLocal;

public class PerfilDialog extends Dialog {

    private static final String CORREO_ADMIN = "rafael.telles1@hotmail.com";

    private final LocalDatabase db;
    private final MainGame juego;

    public PerfilDialog(String title, Skin skin, LocalDatabase db, MainGame juego) {
        super(title, skin);
        this.db = db;
        this.juego = juego;
        getTitleLabel().setFontScale(1.2f);
        getTitleLabel().setColor(com.badlogic.gdx.graphics.Color.valueOf("#DEAD2A"));

        Table titleTable = getTitleTable();
        titleTable.add(new Label("", skin)).expandX().fillX();
        TextButton btnCerrarVentana = new TextButton("X", skin);
        titleTable.add(btnCerrarVentana).padRight(6).padTop(2);

        btnCerrarVentana.addListener(new ClickListener() {
            @Override
            public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                hide();
            }
        });

        getContentTable().pad(20);

        UsuarioLocal usuario = db.obtenerUsuarioActual();
        boolean invitado = (usuario == null) || usuario.getId().startsWith("guest");

        if (invitado) {
            construirVistaInvitado(skin);
        } else {
            construirVistaSesion(skin, usuario);
        }

        construirMensajeAdmin(skin);
    }

    private void construirVistaInvitado(Skin skin) {
        Label lblTitulo = new Label("Jugador Invitado", skin, "titulo");
        getContentTable().add(lblTitulo).padBottom(10).row();

        Label lblInfo = new Label("Juega sin cuenta. Inicia sesión o regístrate para guardar tus estadísticas y sincronizarlas en la nube.", skin, "hud");
        lblInfo.setWrap(true);
        getContentTable().add(lblInfo).width(280).padBottom(20).row();

        Table botonTable = new Table();
        TextButton btnIniciar = new TextButton("Iniciar Sesión", skin);
        TextButton btnRegistrar = new TextButton("Registrarse", skin);

        botonTable.add(btnIniciar).pad(6).width(130);
        botonTable.add(btnRegistrar).pad(6).width(130);
        getContentTable().add(botonTable).row();

        btnIniciar.addListener(new ClickListener() {
            @Override
            public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                hide();
                LoginDialog dialog = new LoginDialog("Iniciar Sesion", skin, db, juego);
                dialog.show(getStage());
            }
        });

        btnRegistrar.addListener(new ClickListener() {
            @Override
            public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                hide();
                RegisterDialog dialog = new RegisterDialog("Registro", skin, db, juego);
                dialog.show(getStage());
            }
        });
    }

    private void construirVistaSesion(Skin skin, UsuarioLocal usuario) {
        Label lblAlias = new Label(usuario.getAlias(), skin, "titulo");
        getContentTable().add(lblAlias).padBottom(4).row();

        Label lblCorreo = new Label(usuario.getCorreo() != null ? usuario.getCorreo() : "", skin, "hud");
        lblCorreo.setWrap(true);
        getContentTable().add(lblCorreo).width(280).padBottom(15).row();

        EstadisticasUsuario stats = db.obtenerEstadisticas(usuario.getId());

        Table statsTable = new Table();
        anadirFilaStats(skin, statsTable, "Partidas jugadas", String.valueOf(stats.getPartidasJugadas()));
        anadirFilaStats(skin, statsTable, "Mejor puntuación", String.valueOf(stats.getMejorPuntuacion()));
        anadirFilaStats(skin, statsTable, "Tiempo total", formatearTiempo(stats.getTiempoTotalJugado()));
        anadirFilaStats(skin, statsTable, "Mejor nivel", String.valueOf(stats.getMejorNivel()));

        Table panelStats = new Table();
        panelStats.setBackground(skin.newDrawable("default-rect", new com.badlogic.gdx.graphics.Color(1, 1, 1, 0.1f)));
        panelStats.add(statsTable).pad(10);

        getContentTable().add(panelStats).width(280).padBottom(15).row();

        Table botonTable = new Table();
        TextButton btnSincronizar = new TextButton("Sincronizar", skin);
        TextButton btnCerrarSesion = new TextButton("Cerrar Sesión", skin);

        botonTable.add(btnSincronizar).pad(6).width(130);
        botonTable.add(btnCerrarSesion).pad(6).width(130);
        getContentTable().add(botonTable).row();

        btnSincronizar.addListener(new ClickListener() {
            @Override
            public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                hide();
                SyncDialog dialog = new SyncDialog("Sincronizando", skin, db);
                dialog.show(getStage());
            }
        });

        btnCerrarSesion.addListener(new ClickListener() {
            @Override
            public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                final com.badlogic.gdx.scenes.scene2d.Stage stage = getStage();
                PerfilDialog.this.hide();

                Dialog confirm = new Dialog("Cerrar Sesión", skin, "dialog") {
                    @Override
                    protected void result(Object object) {
                        if (Boolean.TRUE.equals(object)) {
                            db.cerrarSesion();
                            juego.setScreen(new MainMenuScreen(juego, db));
                        } else {
                            PerfilDialog.this.show(stage);
                        }
                    }
                };
                confirm.getContentTable().pad(20);
                confirm.text("¿Seguro que deseas cerrar sesión?");
                confirm.button("Sí", true);
                confirm.button("No", false);
                confirm.show(stage);
            }
        });
    }

    private void anadirFilaStats(Skin skin, Table tabla, String nombre, String valor) {
        Label lblNombre = new Label(nombre, skin, "hud");
        Label lblValor = new Label(valor, skin, "hud");
        lblValor.setColor(com.badlogic.gdx.graphics.Color.GOLD);
        tabla.add(lblNombre).left().pad(3);
        tabla.add(lblValor).right().pad(3).row();
    }

    private void construirMensajeAdmin(Skin skin) {
        Label lblAdmin = new Label("¿Perdiste tu contraseña? Escríbenos a " + CORREO_ADMIN, skin, "normal");
        lblAdmin.setWrap(true);
        lblAdmin.setAlignment(com.badlogic.gdx.utils.Align.center);
        lblAdmin.setColor(0.6f, 0.6f, 0.6f, 1);
        getContentTable().add(lblAdmin).width(280).padTop(20).row();
    }

    private String formatearTiempo(long segundos) {
        if (segundos <= 0) return "0 s";
        long horas = segundos / 3600;
        long minutos = (segundos % 3600) / 60;
        long secs = segundos % 60;
        if (horas > 0) return horas + " h " + minutos + " min";
        if (minutos > 0) return minutos + " min " + secs + " s";
        return secs + " s";
    }
}
