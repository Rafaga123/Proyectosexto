package com.brk.chessrunner.ui;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
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

       Label pauseLabel= new Label("JUEGO EN PAUSA", skin);
       pauseLabel.setFontScale(1.8f);

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
              ConfigGame settingsDialog = new ConfigGame("Configuracion",skin);
              settingsDialog.show(stage);
          }
       });

       btnSalir.addListener(new ClickListener(){
          public void clicked(InputEvent event, float x, float y ) {
              mostrarConfirmacionSalida();
          }
       });

       add(pauseLabel).padBottom(40f).row();
       add(btnReanudar).size(220f,50f).padBottom(15f).row();
       add(btnConfig).size(220f,50f).padBottom(15f).row();
       add(btnSalir).size(220f,50f);
   }

   private void mostrarConfirmacionSalida(){

       Dialog confirmarDialog = new Dialog("Alerta",skin){
           @Override
           protected void result(Object object){
               if( object instanceof Boolean && (Boolean) object){
                   com.badlogic.gdx.Screen pantallaActual= game.getScreen();
                   game.setScreen(new MainMenuScreen(game, game.db));

                   if(pantallaActual!=null){
                        pantallaActual.dispose();
                   }
               }

           }

       };

       confirmarDialog.text("¿Seguro que quieres salir?");

       TextButton btnSi = new TextButton("SI",skin);
       TextButton btnNo= new TextButton("NO",skin);

       confirmarDialog.button(btnSi, true);
       confirmarDialog.button(btnNo, false);

       confirmarDialog.setMovable(false);
       confirmarDialog.show(stage);

   }


}
