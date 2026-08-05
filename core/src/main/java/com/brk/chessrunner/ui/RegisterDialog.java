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

import java.util.UUID;

public class RegisterDialog extends Dialog {

    public RegisterDialog(String title, Skin skin, LocalDatabase db, MainGame juego) {
        super(title, skin);

        getContentTable().pad(20);

        Label lblTitulo = new Label("Crear Cuenta", skin, "titulo");
        getContentTable().add(lblTitulo).padBottom(20).row();

        TextField aliasField = new TextField("", skin);
        aliasField.setMessageText("Alias");

        TextField emailField = new TextField("", skin);
        emailField.setMessageText("Correo electrónico");

        TextField passField = new TextField("", skin);
        passField.setMessageText("Contraseña");
        passField.setPasswordMode(true);
        passField.setPasswordCharacter('*');

        Label lblRequisitos = new Label(
            "REQUISITOS DE LA CUENTA:\n" +
            "- Alias: 3 a 20 caracteres, sin espacios\n" +
            "- Correo: formato valido, ej. nombre@dominio.com\n" +
            "- Contraseña: minimo 8 caracteres, al menos 1 mayuscula y 1 numero",
            skin, "hud");
        lblRequisitos.setWrap(true);
        lblRequisitos.setColor(0.8f, 0.8f, 0.5f, 1);

        Label errorLabel = new Label("", skin, "hud");
        errorLabel.setWrap(true);

        getContentTable().add(aliasField).width(260).padBottom(12).row();
        getContentTable().add(emailField).width(260).padBottom(12).row();
        getContentTable().add(passField).width(260).padBottom(12).row();
        getContentTable().add(lblRequisitos).width(260).padBottom(8).row();
        getContentTable().add(errorLabel).width(260).padBottom(5).row();

        TextButton btnRegistrar = new TextButton("Registrarse", skin);
        TextButton btnCancelar = new TextButton("Cancelar", skin);

        btnRegistrar.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                String alias = aliasField.getText().trim();
                String correo = emailField.getText().trim();
                String pass = passField.getText();

                if (alias.isEmpty() || correo.isEmpty() || pass.isEmpty()) {
                    errorLabel.setColor(1, 0.3f, 0.3f, 1);
                    errorLabel.setText("Complete todos los campos.");
                    return;
                }

                if (alias.length() < 3 || alias.length() > 20 || alias.contains(" ")) {
                    errorLabel.setColor(1, 0.3f, 0.3f, 1);
                    errorLabel.setText("Alias: minimo 3 y maximo 20 caracteres, sin espacios.");
                    return;
                }

                if (!correo.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
                    errorLabel.setColor(1, 0.3f, 0.3f, 1);
                    errorLabel.setText("Correo invalido. Usa un formato como nombre@dominio.com");
                    return;
                }

                if (pass.length() < 8 || !pass.matches(".*[A-Z].*") || !pass.matches(".*[0-9].*")) {
                    errorLabel.setColor(1, 0.3f, 0.3f, 1);
                    errorLabel.setText("Contraseña: minimo 8 caracteres, al menos 1 mayuscula (A-Z) y 1 numero (0-9).");
                    return;
                }

                errorLabel.setColor(1, 1, 1, 1);
                errorLabel.setText("Registrando...");

                ApiClient.registrar(UUID.randomUUID().toString(), alias, correo, pass, new ApiClient.ApiCallback() {
                    @Override
                    public void onExito(JsonValue respuesta) {
                        Gdx.app.postRunnable(() -> {
                            String id = respuesta.getString("id");
                            String aliasNuevo = respuesta.getString("alias");
                            String correoNuevo = respuesta.getString("correo");

                            UsuarioLocal usuarioApi = new UsuarioLocal(id, aliasNuevo, correoNuevo, true);
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

        getButtonTable().add(btnRegistrar).pad(8).width(120);
        getButtonTable().add(btnCancelar).pad(8).width(120);
    }
}
