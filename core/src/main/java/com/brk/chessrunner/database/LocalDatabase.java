package com.brk.chessrunner.database;

public interface LocalDatabase {
    void conectar(String ruta);
    void cerrar();

    // Luego agregare los métodos para guardar y leer (saludos Rafa y Kevin)
}
