package com.brk.chessrunner.database;

public class PartidaLocal {
    private String id;
    private String usuarioId;
    private int puntuacion;
    private int nivelAlcanzado;
    private String piezaMortal;
    private int tiempoSobrevivido;
    private String fechaPartida;
    private boolean sincronizado; // Controla si las partidas del usuario están sincronizadas

    public PartidaLocal(String id, String usuarioId, int puntuacion, int nivelAlcanzado, String piezaMortal, int tiempoSobrevivido, String fechaPartida, boolean sincronizado) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.puntuacion = puntuacion;
        this.nivelAlcanzado = nivelAlcanzado;
        this.piezaMortal = piezaMortal;
        this.tiempoSobrevivido = tiempoSobrevivido;
        this.fechaPartida = fechaPartida;
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

    public int getNivelAlcanzado() {
        return nivelAlcanzado;
    }

    public void setNivelAlcanzado(int nivelAlcanzado) {
        this.nivelAlcanzado = nivelAlcanzado;
    }

    public String getPiezaMortal() {
        return piezaMortal;
    }

    public void setPiezaMortal(String piezaMortal) {
        this.piezaMortal = piezaMortal;
    }

    public int getTiempoSobrevivido() {
        return tiempoSobrevivido;
    }

    public void setTiempoSobrevivido(int tiempoSobrevivido) {
        this.tiempoSobrevivido = tiempoSobrevivido;
    }

    public String getFechaPartida() {
        return fechaPartida;
    }

    public void setFechaPartida(String fechaPartida) {
        this.fechaPartida = fechaPartida;
    }

    public boolean isSincronizado() {
        return sincronizado;
    }

    public void setSincronizado(boolean sincronizado) {
        this.sincronizado = sincronizado;
    }
}
