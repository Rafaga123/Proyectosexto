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

        // Campos de texto
        TextField emailField = new TextField("", skin);
        emailField.setMessageText("Correo electrónico");

        TextField passField = new TextField("", skin);
        passField.setMessageText("Contraseña");
        passField.setPasswordMode(true);
        passField.setPasswordCharacter('*');

        Label errorLabel = new Label("", skin);
        errorLabel.setColor(1, 0, 0, 1); // Color Rojo para errores

        // Construir la estructura visual (Tabla central)
        getContentTable().add(new Label("Ingresa tus credenciales:", skin)).padBottom(10).row();
        getContentTable().add(emailField).width(250).padBottom(10).row();
        getContentTable().add(passField).width(250).padBottom(10).row();
        getContentTable().add(errorLabel).padBottom(10).row();

        // Botones inferiores
        TextButton btnLogin = new TextButton("Entrar", skin);
        TextButton btnCancelar = new TextButton("Cancelar", skin);

        // Lógica del botón Entrar
        btnLogin.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                errorLabel.setColor(1, 1, 1, 1); // Blanco
                errorLabel.setText("Conectando...");

                String email = emailField.getText();
                String pass = passField.getText();

                // Llamada a tu API de Spring Boot
                ApiClient.login(email, pass, new ApiClient.ApiCallback() {
                    @Override
                    public void onExito(JsonValue respuesta) {
                        // Gdx.app.postRunnable es obligatorio para modificar la UI desde la respuesta de red
                        Gdx.app.postRunnable(() -> {
                            String id = respuesta.getString("id");
                            String alias = respuesta.getString("alias");
                            String correo = respuesta.getString("correo");

                            // Guardamos en SQLite
                            UsuarioLocal usuarioApi = new UsuarioLocal(id, alias, correo, true);
                            db.vincularCuenta(usuarioApi);

                            hide(); // Cierra el modal
                            // Refresca la pantalla principal para que muestre tu Alias real
                            juego.setScreen(new MainMenuScreen(juego, db));
                        });
                    }

                    @Override
                    public void onError(String mensajeError) {
                        Gdx.app.postRunnable(() -> {
                            errorLabel.setColor(1, 0, 0, 1); // Rojo
                            errorLabel.setText(mensajeError);
                        });
                    }
                });
            }
        });

        // Lógica del botón Cancelar
        btnCancelar.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                hide();
            }
        });

        getButtonTable().add(btnLogin).pad(10);
        getButtonTable().add(btnCancelar).pad(10);
    }
}
