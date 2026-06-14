package com.brk.chessrunner;

public class PowerUp {
    public TipoPowerUp tipo;
    public int colLogica;
    public int filLogica;

    public PowerUp(TipoPowerUp tipo, int col, int fil) {
        this.tipo = tipo;
        this.colLogica = col;
        this.filLogica = fil;
    }
}
