package com.brk.chessrunner.database;

import java.util.List;

public interface LocalDatabase {
    void conectar(String ruta);
    void cerrar();
    void guardarPartida(PartidaLocal partida);
    UsuarioLocal obtenerUsuarioActual();
    void vincularCuenta(UsuarioLocal usuarioApi);
    List<PartidaLocal> obtenerPartidasNoSincronizadas(String usuarioId);
    void marcarComoSincronizada(String partidaId);
    EstadisticasUsuario obtenerEstadisticas(String usuarioId);
    void cerrarSesion();
}
