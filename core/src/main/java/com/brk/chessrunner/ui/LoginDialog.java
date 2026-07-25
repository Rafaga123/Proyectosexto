package com.brk.chessrunner.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.JsonValue;
import com.brk.chessrunner.MainGame;
import com.brk.chessrunner.database.LocalDatabase;
import com.brk.chessrunner.database.UsuarioLocal;
import com.brk.chessrunner.network.ApiClient;

public class LoginDialog extends Dialog {

    public LoginDialog(String title, Skin skin, LocalDatabase db, MainGame juego) {
        super(title, skin);

        getContentTable().pad(20);

        Label lblTitulo = new Label("Iniciar Sesión", skin, "titulo");
        getContentTable().add(lblTitulo).padBottom(20).row();

        TextField emailField = new TextField("", skin);
        emailField.setMessageText("Correo electrónico");

        TextField passField = new TextField("", skin);
        passField.setMessageText("Contraseña");
        passField.setPasswordMode(true);
        passField.setPasswordCharacter('*');

        Label errorLabel = new Label("", skin, "hud");
        errorLabel.setWrap(true);

        getContentTable().add(emailField).width(260).padBottom(12).row();
        getContentTable().add(passField).width(260).padBottom(12).row();
        getContentTable().add(errorLabel).width(260).padBottom(5).row();

        TextButton btnLogin = new TextButton("Entrar", skin);
        TextButton btnCancelar = new TextButton("Cancelar", skin);

        btnLogin.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                errorLabel.setColor(1, 1, 1, 1);
                errorLabel.setText("Conectando...");

                String email = emailField.getText();
                String pass = passField.getText();

                ApiClient.login(email, pass, new ApiClient.ApiCallback() {
                    @Override
                    public void onExito(JsonValue respuesta) {
                        Gdx.app.postRunnable(() -> {
                            String id = respuesta.getString("id");
                            String alias = respuesta.getString("alias");
                            String correo = respuesta.getString("correo");

                            UsuarioLocal usuarioApi = new UsuarioLocal(id, alias, correo, true);
                            db.vincularCuenta(usuarioApi);

                            hide();
                            juego.setScreen(new MainMenuScreen(juego, db));
                        });
                    }

                    @Override
                    public void onError(String mensajeError) {
                        Gdx.app.postRunnable(() -> {
                            errorLabel.setColor(1, 0.3f, 0.3f, 1);
                            errorLabel.setText(mensajeError);
                        });
                    }
                });
            }
        });

        btnCancelar.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                hide();
            }
        });

        getButtonTable().add(btnLogin).pad(8).width(120);
        getButtonTable().add(btnCancelar).pad(8).width(120);
    }
}
