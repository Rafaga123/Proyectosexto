package com.brk.chessrunner.lwjgl3.database;

import com.brk.chessrunner.database.EstadisticasUsuario;
import com.brk.chessrunner.database.LocalDatabase;
import com.brk.chessrunner.database.PartidaLocal;
import com.brk.chessrunner.database.UsuarioLocal;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DesktopDatabaseManager implements LocalDatabase {
    private Connection conexion;

    @Override
    public void conectar(String ruta) {
        try {
            Class.forName("org.sqlite.JDBC");
            conexion = DriverManager.getConnection("jdbc:sqlite:" + ruta);
            System.out.println("¡Exito! Conectado a la base de datos SQLite en: " + ruta);

            // CREACIÓN DE TABLAS
            try (java.sql.Statement stmt = conexion.createStatement()) {
                // Tabla de Usuarios
                String sqlUsuarios = "CREATE TABLE IF NOT EXISTS usuario_local (" +
                    "id TEXT PRIMARY KEY, " +
                    "alias TEXT NOT NULL, " +
                    "correo TEXT, " +
                    "sesion_activa INTEGER DEFAULT 0" +
                    ");";
                stmt.execute(sqlUsuarios);

                // Tabla de Partidas
                String sqlPartidas = "CREATE TABLE IF NOT EXISTS partida_local (" +
                    "id TEXT PRIMARY KEY, " +
                    "usuario_id TEXT NOT NULL, " +
                    "puntuacion INTEGER DEFAULT 0, " +
                    "nivel_alcanzado INTEGER DEFAULT 0, " +
                    "pieza_mortal TEXT, " +
                    "tiempo_sobrevivido INTEGER DEFAULT 0, " +
                    "fecha_partida TEXT, " +
                    "sincronizado INTEGER DEFAULT 0, " +
                    "FOREIGN KEY (usuario_id) REFERENCES usuario_local(id)" +
                    ");";
                stmt.execute(sqlPartidas);

                // Verificamos si la tabla de usuarios está vacía
                java.sql.ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS total FROM usuario_local");
                if (rs.next() && rs.getInt("total") == 0) {
                    // Generamos un ID local fijo o aleatorio para el invitado
                    String idInvitado = "guest_" + java.util.UUID.randomUUID().toString().substring(0, 8);
                    String insertGuest = "INSERT INTO usuario_local (id, alias, correo, sesion_activa) VALUES ('"
                        + idInvitado + "', 'Jugador', NULL, 1)";
                    stmt.execute(insertGuest);
                    System.out.println("Perfil de Invitado creado automáticamente: " + idInvitado);
                }

                System.out.println("Tablas de base de datos listas para usar.");
            }

        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("Error crítico al conectar a SQLite o crear tablas: " + e.getMessage());
        }
    }

    @Override
    public UsuarioLocal obtenerUsuarioActual() {
        String sql = "SELECT * FROM usuario_local WHERE sesion_activa = 1 LIMIT 1";
        try (java.sql.Statement stmt = conexion.createStatement();
             java.sql.ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return new UsuarioLocal(
                    rs.getString("id"),
                    rs.getString("alias"),
                    rs.getString("correo"),
                    rs.getInt("sesion_activa") == 1
                );
            }
        } catch (java.sql.SQLException e) {
            System.err.println("Error al obtener usuario actual: " + e.getMessage());
        }
        return null; // En un caso crítico donde falló todo
    }

    @Override
    public void cerrar() {
        try {
            if (conexion != null && !conexion.isClosed()) {
                conexion.close();
                System.out.println("Conexión a SQLite cerrada correctamente.");
            }
        } catch (SQLException e) {
            System.err.println("Error al cerrar la base de datos: " + e.getMessage());
        }
    }

    @Override
    public void guardarPartida(PartidaLocal partida) {
        // Usamos PreparedStatement para evitar inyecciones SQL y formatear fácil
        String sql = "INSERT INTO partida_local (id, usuario_id, puntuacion, nivel_alcanzado, pieza_mortal, tiempo_sobrevivido, fecha_partida, sincronizado) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (java.sql.PreparedStatement pstmt = conexion.prepareStatement(sql)) {
            pstmt.setString(1, partida.getId());
            pstmt.setString(2, partida.getUsuarioId());
            pstmt.setInt(3, partida.getPuntuacion());
            pstmt.setInt(4, partida.getNivelAlcanzado());
            pstmt.setString(5, partida.getPiezaMortal());
            pstmt.setInt(6, partida.getTiempoSobrevivido());
            pstmt.setString(7, partida.getFechaPartida());
            pstmt.setInt(8, partida.isSincronizado() ? 1 : 0); // SQLite no tiene booleanos puros, usamos 1 y 0

            pstmt.executeUpdate();
            System.out.println("¡Partida guardada en SQLite exitosamente! ID: " + partida.getId());
        } catch (java.sql.SQLException e) {
            System.err.println("Error al guardar la partida: " + e.getMessage());
        }
    }

    @Override
    public void vincularCuenta(UsuarioLocal usuarioApi) {
        try {
            // 1. Obtenemos el ID del invitado actual
            String idInvitado = null;
            try (java.sql.Statement stmt = conexion.createStatement();
                 java.sql.ResultSet rs = stmt.executeQuery("SELECT id FROM usuario_local WHERE sesion_activa = 1")) {
                if (rs.next()) {
                    idInvitado = rs.getString("id");
                }
            }

            if (idInvitado != null) {
                // 2. Actualizamos las partidas para que apunten al nuevo ID real
                String sqlPartidas = "UPDATE partida_local SET usuario_id = ? WHERE usuario_id = ?";
                try (java.sql.PreparedStatement pstmt = conexion.prepareStatement(sqlPartidas)) {
                    pstmt.setString(1, usuarioApi.getId());
                    pstmt.setString(2, idInvitado);
                    pstmt.executeUpdate();
                }

                // 3. Actualizamos el perfil local con los datos de PostgreSQL
                String sqlUsuario = "UPDATE usuario_local SET id = ?, alias = ?, correo = ? WHERE id = ?";
                try (java.sql.PreparedStatement pstmt = conexion.prepareStatement(sqlUsuario)) {
                    pstmt.setString(1, usuarioApi.getId());
                    pstmt.setString(2, usuarioApi.getAlias());
                    pstmt.setString(3, usuarioApi.getCorreo());
                    pstmt.setString(4, idInvitado);
                    pstmt.executeUpdate();
                }
                System.out.println("Cuenta vinculada exitosamente. Partidas migradas al ID: " + usuarioApi.getId());
            }
        } catch (java.sql.SQLException e) {
            System.err.println("Error al vincular cuenta en SQLite: " + e.getMessage());
        }
    }

    @Override
    public java.util.List<PartidaLocal> obtenerPartidasNoSincronizadas(String usuarioId) {
        java.util.List<PartidaLocal> lista = new java.util.ArrayList<>();
        String sql = "SELECT * FROM partida_local WHERE usuario_id = ? AND sincronizado = 0";

        try (java.sql.PreparedStatement pstmt = conexion.prepareStatement(sql)) {
            pstmt.setString(1, usuarioId);
            try (java.sql.ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(new PartidaLocal(
                        rs.getString("id"),
                        rs.getString("usuario_id"),
                        rs.getInt("puntuacion"),
                        rs.getInt("nivel_alcanzado"),
                        rs.getString("pieza_mortal"),
                        rs.getInt("tiempo_sobrevivido"),
                        rs.getString("fecha_partida"),
                        false
                    ));
                }
            }
        } catch (java.sql.SQLException e) {
            System.err.println("Error al obtener partidas pendientes: " + e.getMessage());
        }
        return lista;
    }

    @Override
    public void marcarComoSincronizada(String partidaId) {
        String sql = "UPDATE partida_local SET sincronizado = 1 WHERE id = ?";
        try (java.sql.PreparedStatement pstmt = conexion.prepareStatement(sql)) {
            pstmt.setString(1, partidaId);
            pstmt.executeUpdate();
        } catch (java.sql.SQLException e) {
            System.err.println("Error al marcar partida como sincronizada: " + e.getMessage());
        }
    }

    @Override
    public EstadisticasUsuario obtenerEstadisticas(String usuarioId) {
        String sql = "SELECT COUNT(*) AS total, COALESCE(MAX(puntuacion), 0) AS mejor_puntuacion, " +
            "COALESCE(SUM(tiempo_sobrevivido), 0) AS tiempo_total, " +
            "COALESCE(MAX(nivel_alcanzado), 0) AS mejor_nivel " +
            "FROM partida_local WHERE usuario_id = ?";

        try (java.sql.PreparedStatement pstmt = conexion.prepareStatement(sql)) {
            pstmt.setString(1, usuarioId);
            try (java.sql.ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new EstadisticasUsuario(
                        rs.getInt("total"),
                        rs.getInt("mejor_puntuacion"),
                        rs.getLong("tiempo_total"),
                        rs.getInt("mejor_nivel")
                    );
                }
            }
        } catch (java.sql.SQLException e) {
            System.err.println("Error al obtener estadísticas: " + e.getMessage());
        }
        return new EstadisticasUsuario(0, 0, 0, 0);
    }

    @Override
    public void cerrarSesion() {
        UsuarioLocal usuario = obtenerUsuarioActual();
        if (usuario == null) return;

        try (java.sql.PreparedStatement pstmt = conexion.prepareStatement("DELETE FROM usuario_local WHERE id = ?")) {
            pstmt.setString(1, usuario.getId());
            pstmt.executeUpdate();
        } catch (java.sql.SQLException e) {
            System.err.println("Error al cerrar sesión: " + e.getMessage());
        }

        String idInvitado = "guest_" + java.util.UUID.randomUUID().toString().substring(0, 8);
        String sqlInsert = "INSERT INTO usuario_local (id, alias, correo, sesion_activa) VALUES (?, 'Jugador', NULL, 1)";
        try (java.sql.PreparedStatement pstmt = conexion.prepareStatement(sqlInsert)) {
            pstmt.setString(1, idInvitado);
            pstmt.executeUpdate();
        } catch (java.sql.SQLException e) {
            System.err.println("Error al crear invitado tras cerrar sesión: " + e.getMessage());
        }
    }
}
