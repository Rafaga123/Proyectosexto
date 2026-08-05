package com.brk.chessrunner.android.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.brk.chessrunner.database.EstadisticasUsuario;
import com.brk.chessrunner.database.LocalDatabase;
import com.brk.chessrunner.database.PartidaLocal;
import com.brk.chessrunner.database.UsuarioLocal;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AndroidDatabaseManager implements LocalDatabase {

    private static final String DB_NAME = "chessrunner_local.db";
    private static final int DB_VERSION = 1;

    private SQLiteDatabase db;
    private final Context context;

    public AndroidDatabaseManager(Context context) {
        this.context = context;
    }

    @Override
    public void conectar(String ruta) {
        DatabaseHelper helper = new DatabaseHelper(context);
        db = helper.getWritableDatabase();
    }

    @Override
    public void cerrar() {
        if (db != null && db.isOpen()) {
            db.close();
        }
    }

    @Override
    public void guardarPartida(PartidaLocal partida) {
        ContentValues values = new ContentValues();
        values.put("id", partida.getId());
        values.put("usuario_id", partida.getUsuarioId());
        values.put("puntuacion", partida.getPuntuacion());
        values.put("nivel_alcanzado", partida.getNivelAlcanzado());
        values.put("pieza_mortal", partida.getPiezaMortal());
        values.put("tiempo_sobrevivido", partida.getTiempoSobrevivido());
        values.put("fecha_partida", partida.getFechaPartida());
        values.put("sincronizado", partida.isSincronizado() ? 1 : 0);
        db.insert("partida_local", null, values);
    }

    @Override
    public UsuarioLocal obtenerUsuarioActual() {
        Cursor cursor = db.rawQuery("SELECT * FROM usuario_local WHERE sesion_activa = 1 LIMIT 1", null);
        if (cursor.moveToFirst()) {
            UsuarioLocal usuario = new UsuarioLocal(
                cursor.getString(cursor.getColumnIndexOrThrow("id")),
                cursor.getString(cursor.getColumnIndexOrThrow("alias")),
                cursor.getString(cursor.getColumnIndexOrThrow("correo")),
                cursor.getInt(cursor.getColumnIndexOrThrow("sesion_activa")) == 1
            );
            cursor.close();
            return usuario;
        }
        cursor.close();
        return null;
    }

    @Override
    public void vincularCuenta(UsuarioLocal usuarioApi) {
        Cursor cursor = db.rawQuery("SELECT id FROM usuario_local WHERE sesion_activa = 1", null);
        String idInvitado = null;
        if (cursor.moveToFirst()) {
            idInvitado = cursor.getString(cursor.getColumnIndexOrThrow("id"));
        }
        cursor.close();

        if (idInvitado != null) {
            ContentValues partidaValues = new ContentValues();
            partidaValues.put("usuario_id", usuarioApi.getId());
            db.update("partida_local", partidaValues, "usuario_id = ?", new String[]{idInvitado});

            ContentValues userValues = new ContentValues();
            userValues.put("id", usuarioApi.getId());
            userValues.put("alias", usuarioApi.getAlias());
            userValues.put("correo", usuarioApi.getCorreo());
            db.update("usuario_local", userValues, "id = ?", new String[]{idInvitado});
        }
    }

    @Override
    public List<PartidaLocal> obtenerPartidasNoSincronizadas(String usuarioId) {
        List<PartidaLocal> lista = new ArrayList<>();
        Cursor cursor = db.rawQuery(
            "SELECT * FROM partida_local WHERE usuario_id = ? AND sincronizado = 0",
            new String[]{usuarioId}
        );
        while (cursor.moveToNext()) {
            lista.add(new PartidaLocal(
                cursor.getString(cursor.getColumnIndexOrThrow("id")),
                cursor.getString(cursor.getColumnIndexOrThrow("usuario_id")),
                cursor.getInt(cursor.getColumnIndexOrThrow("puntuacion")),
                cursor.getInt(cursor.getColumnIndexOrThrow("nivel_alcanzado")),
                cursor.getString(cursor.getColumnIndexOrThrow("pieza_mortal")),
                cursor.getInt(cursor.getColumnIndexOrThrow("tiempo_sobrevivido")),
                cursor.getString(cursor.getColumnIndexOrThrow("fecha_partida")),
                false
            ));
        }
        cursor.close();
        return lista;
    }

    @Override
    public void marcarComoSincronizada(String partidaId) {
        ContentValues values = new ContentValues();
        values.put("sincronizado", 1);
        db.update("partida_local", values, "id = ?", new String[]{partidaId});
    }

    @Override
    public EstadisticasUsuario obtenerEstadisticas(String usuarioId) {
        Cursor cursor = db.rawQuery(
            "SELECT COUNT(*) AS total, COALESCE(MAX(puntuacion), 0) AS mejor_puntuacion, " +
            "COALESCE(SUM(tiempo_sobrevivido), 0) AS tiempo_total, " +
            "COALESCE(MAX(nivel_alcanzado), 0) AS mejor_nivel " +
            "FROM partida_local WHERE usuario_id = ?",
            new String[]{usuarioId}
        );
        EstadisticasUsuario estadisticas = new EstadisticasUsuario(0, 0, 0, 0);
        if (cursor.moveToFirst()) {
            estadisticas = new EstadisticasUsuario(
                cursor.getInt(cursor.getColumnIndexOrThrow("total")),
                cursor.getInt(cursor.getColumnIndexOrThrow("mejor_puntuacion")),
                cursor.getLong(cursor.getColumnIndexOrThrow("tiempo_total")),
                cursor.getInt(cursor.getColumnIndexOrThrow("mejor_nivel"))
            );
        }
        cursor.close();
        return estadisticas;
    }

    @Override
    public void cerrarSesion() {
        UsuarioLocal usuario = obtenerUsuarioActual();
        if (usuario == null) return;

        db.delete("usuario_local", "id = ?", new String[]{usuario.getId()});

        String idInvitado = "guest_" + UUID.randomUUID().toString().substring(0, 8);
        ContentValues values = new ContentValues();
        values.put("id", idInvitado);
        values.put("alias", "Jugador");
        values.put("sesion_activa", 1);
        db.insert("usuario_local", null, values);
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE IF NOT EXISTS usuario_local (" +
                "id TEXT PRIMARY KEY, " +
                "alias TEXT NOT NULL, " +
                "correo TEXT, " +
                "sesion_activa INTEGER DEFAULT 0" +
                ")");

            db.execSQL("CREATE TABLE IF NOT EXISTS partida_local (" +
                "id TEXT PRIMARY KEY, " +
                "usuario_id TEXT NOT NULL, " +
                "puntuacion INTEGER DEFAULT 0, " +
                "nivel_alcanzado INTEGER DEFAULT 0, " +
                "pieza_mortal TEXT, " +
                "tiempo_sobrevivido INTEGER DEFAULT 0, " +
                "fecha_partida TEXT, " +
                "sincronizado INTEGER DEFAULT 0" +
                ")");

            Cursor cursor = db.rawQuery("SELECT COUNT(*) AS total FROM usuario_local", null);
            if (cursor.moveToFirst() && cursor.getInt(cursor.getColumnIndexOrThrow("total")) == 0) {
                String idInvitado = "guest_" + UUID.randomUUID().toString().substring(0, 8);
                ContentValues values = new ContentValues();
                values.put("id", idInvitado);
                values.put("alias", "Jugador");
                values.put("sesion_activa", 1);
                db.insert("usuario_local", null, values);
            }
            cursor.close();
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS partida_local");
            db.execSQL("DROP TABLE IF EXISTS usuario_local");
            onCreate(db);
        }
    }
}
