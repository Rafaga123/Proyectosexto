package com.brk.chessrunner.database;

public class PartidaLocal {
    private String id;
    private String usuarioId;
    private int puntuacion;

    private int tiempoSobrevivido;
    private boolean sincronizado; // Controla si las partidas del usuario están sincronizadas

    public PartidaLocal(String id, String usuarioId, int puntuacion, int tiempoSobrevivido, boolean sincronizado) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.puntuacion = puntuacion;
        this.tiempoSobrevivido = tiempoSobrevivido;
        this.sincronizado = sincronizado;
    }

    // Getters y Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(String usuarioId) {
        this.usuarioId = usuarioId;
    }

    public int getPuntuacion() {
        return puntuacion;
    }

    public void setPuntuacion(int puntuacion) {
        this.puntuacion = puntuacion;
    }

    public int getTiempoSobrevivido() {
        return tiempoSobrevivido;
    }

    public void setTiempoSobrevivido(int tiempoSobrevivido) {
        this.tiempoSobrevivido = tiempoSobrevivido;
    }

    public boolean isSincronizado() {
        return sincronizado;
    }

    public void setSincronizado(boolean sincronizado) {
        this.sincronizado = sincronizado;
    }
}
