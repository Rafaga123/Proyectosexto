package com.brk.chessrunner.database;

public class EstadisticasUsuario {
    private final int partidasJugadas;
    private final int mejorPuntuacion;
    private final long tiempoTotalJugado;
    private final int mejorNivel;

    public EstadisticasUsuario(int partidasJugadas, int mejorPuntuacion, long tiempoTotalJugado, int mejorNivel) {
        this.partidasJugadas = partidasJugadas;
        this.mejorPuntuacion = mejorPuntuacion;
        this.tiempoTotalJugado = tiempoTotalJugado;
        this.mejorNivel = mejorNivel;
    }

    public int getPartidasJugadas() {
        return partidasJugadas;
    }

    public int getMejorPuntuacion() {
        return mejorPuntuacion;
    }

    public long getTiempoTotalJugado() {
        return tiempoTotalJugado;
    }

    public int getMejorNivel() {
        return mejorNivel;
    }
}
