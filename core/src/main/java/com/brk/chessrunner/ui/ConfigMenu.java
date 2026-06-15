package com.brk.chessrunner.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

public class ConfigMenu extends Dialog {

    private final com.badlogic.gdx.Preferences prefs;
    private final Slider musicSlider;
    private final Slider fxSlider;
    private final TextField ipField;
    private final Label statusLabel;
    private final TextButton btnAceptar;

    public ConfigMenu(String title, Skin skin) {
        super(title, skin);

        prefs = Gdx.app.getPreferences("ChessRunnerSettings");

        setMovable(false);
        setResizable(false);

        getContentTable().pad(20);

        getContentTable().add(new Label("Volumen Musica:", skin)).left().padRight(10);
        musicSlider = new Slider(0, 1, 0.1f, false, skin);
        musicSlider.setValue(prefs.getFloat("musicVolume", 0.8f));
        getContentTable().add(musicSlider).width(150).row();

        getContentTable().add(new Label("Efectos de Sonido:", skin)).left().padRight(10).padTop(10);
        fxSlider = new Slider(0, 1, 0.1f, false, skin);
        fxSlider.setValue(prefs.getFloat("fxVolume", 0.5f));
        getContentTable().add(fxSlider).width(150).row();

        getContentTable().add(new Label("IP del Servidor:", skin)).left().padRight(10).padTop(10);
        ipField = new TextField("", skin);
        ipField.setText(prefs.getString("serverIp", "192.168.1.100"));
        ipField.setMessageText("192.168.1.100");
        getContentTable().add(ipField).width(150).row();

        statusLabel = new Label("", skin);
        getContentTable().add(statusLabel).colspan(2).padTop(10).row();

        btnAceptar = new TextButton("Guardar y Cerrar", skin);
        btnAceptar.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                guardarYProbarConexion();
            }
        });
        getButtonTable().add(btnAceptar).pad(10);
    }

    private void guardarYProbarConexion() {
        String ip = ipField.getText().trim();
        if (ip.isEmpty()) ip = "192.168.1.100";

        prefs.putFloat("musicVolume", musicSlider.getValue());
        prefs.putFloat("fxVolume", fxSlider.getValue());
        prefs.putString("serverIp", ip);
        prefs.flush();

        btnAceptar.setDisabled(true);
        statusLabel.setColor(1, 1, 1, 1);
        statusLabel.setText("Probando conexión...");

        String url = "http://" + ip + ":58080/api/";
        Net.HttpRequest request = new Net.HttpRequest(Net.HttpMethods.GET);
        request.setUrl(url);
        request.setTimeOut(5000);

        Gdx.net.sendHttpRequest(request, new Net.HttpResponseListener() {
            @Override
            public void handleHttpResponse(Net.HttpResponse httpResponse) {
                Gdx.app.postRunnable(() -> {
                    statusLabel.setColor(0, 1, 0, 1);
                    statusLabel.setText("Conectado con el servidor");
                    Timer.schedule(new Timer.Task() {
                        @Override
                        public void run() {
                            ConfigMenu.this.hide();
                        }
                    }, 1.5f);
                });
            }

            @Override
            public void failed(Throwable t) {
                Gdx.app.postRunnable(() -> {
                    statusLabel.setColor(1, 0, 0, 1);
                    statusLabel.setText("Fallo en la conexión");
                    btnAceptar.setDisabled(false);
                });
            }

            @Override
            public void cancelled() {
                Gdx.app.postRunnable(() -> {
                    statusLabel.setColor(1, 0, 0, 1);
                    statusLabel.setText("Conexión cancelada");
                    btnAceptar.setDisabled(false);
                });
            }
        });
    }
}
