package com.brk.chessrunner.ui;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.brk.chessrunner.MainGame;
import com.brk.chessrunner.ui.MainMenuScreen;

public class PauseWidget extends Table {

   private final MainGame game;
   private final Skin skin;
   private final Stage stage;
   private final IPauseListener listener;

   public interface IPauseListener{
       void onResume();
   }

   public PauseWidget(final MainGame game, final Skin skin, final Stage stage, final IPauseListener listener){

       this.game= game;
       this.skin= skin;
       this.stage = stage;
       this.listener= listener;

       setFillParent(true);

       Image bg = new Image(skin, "overlay");
       bg.setFillParent(true);
       addActor(bg);

       Label pauseLabel= new Label("JUEGO EN PAUSA", skin, "titulo");

       TextButton btnReanudar= new TextButton("REANUDAR",skin);
       TextButton btnConfig= new TextButton("CONFIGURACION",skin);
       TextButton btnSalir= new TextButton("SALIR",skin);

       btnReanudar.addListener(new ClickListener(){
           @Override
           public void clicked(InputEvent event, float x, float y){
               listener.onResume();
           }
       });

        btnConfig.addListener(new ClickListener(){
           @Override
           public void clicked(InputEvent event, float x, float y){
               PauseWidget.this.remove();
               ConfigGame settingsDialog = new ConfigGame("Configuracion",skin);
               settingsDialog.show(stage);
           }
        });

        btnSalir.addListener(new ClickListener(){
           public void clicked(InputEvent event, float x, float y ) {
               PauseWidget.this.setVisible(false);
               mostrarConfirmacionSalida();
           }
        });

       add(pauseLabel).padBottom(40f).row();
       add(btnReanudar).size(220f,50f).padBottom(15f).row();
       add(btnConfig).size(220f,50f).padBottom(15f).row();
       add(btnSalir).size(220f,50f);

       getColor().a = 0f;
       addAction(Actions.fadeIn(0.25f, Interpolation.sineOut));
   }

    private void mostrarConfirmacionSalida(){
        Dialog confirmarDialog = new Dialog("Alerta",skin){
            @Override
            protected void result(Object object){
                if( object instanceof Boolean && (Boolean) object){
                    com.badlogic.gdx.Screen pantallaActual= game.getScreen();
                    game.switchScreen(new MainMenuScreen(game, game.db));
                    if(pantallaActual!=null){
                         pantallaActual.dispose();
                    }
                } else {
                    PauseWidget.this.setVisible(true);
                }
            }
        };

        confirmarDialog.getTitleLabel().setFontScale(1.25f);
        confirmarDialog.getContentTable().pad(20, 40, 20, 40);
        Label lblMsg = new Label("¿Seguro que quieres salir?", skin, "hud");
        lblMsg.setFontScale(1.2f);
        confirmarDialog.text(lblMsg);
        TextButton btnSi = new TextButton("SI",skin);
        TextButton btnNo = new TextButton("NO",skin);
        btnSi.getLabel().setFontScale(1.2f);
        btnNo.getLabel().setFontScale(1.2f);
        confirmarDialog.button(btnSi, true);
        confirmarDialog.button(btnNo, false);
        confirmarDialog.setMovable(false);
        confirmarDialog.show(stage);
    }

}
