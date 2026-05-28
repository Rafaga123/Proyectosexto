package com.brk.chessrunner.database;

public interface LocalDatabase {
    void conectar(String ruta);
    void cerrar();
    void guardarPartida(PartidaLocal partida);

    UsuarioLocal obtenerUsuarioActual();
}
