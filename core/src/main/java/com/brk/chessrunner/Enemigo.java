package com.brk.chessrunner;

public class Enemigo {
    public TipoPieza tipo;
    public int colLogica;
    public int filLogica;

    public Enemigo(TipoPieza tipo, int col, int fil){
        this.tipo = tipo;
        this.colLogica = col;
        this.filLogica = fil;
    }
    // Metodo que recibe la posicion donde se quiere mover el juegador, regresara un valor
    // Positivo, True, si un enemigo esta atacando esa posicion.
    public boolean atacaCasilla(int targetCol, int targetFila) {
        int difCol = Math.abs(targetCol - colLogica);
        int difFila = Math.abs(targetFila - filLogica);

        // limite y control visual para balanceo: Ninguna pieza puede atacarte desde más de 4 casillas de distancia vertical.
        // Esto evita muertes instantáneas por piezas que aparecen fuera de cámara.
        // Esto esta en revision
        if (difFila > 5) {
            return false;
        }
        switch (tipo) {
            case TORRE:
                // Una torre ataca toda su misma columna y toda su misma fila
                return (targetCol == colLogica) || (targetFila == filLogica);

            case PEON:
                // Los peones enemigos miran hacia abajo.
                // Atacan la fila inmediatamente anterior a ellos, esto es por la ley que no recuerdo el nombre
                // y sus columnas diagonales
                return (targetFila == filLogica - 1) &&
                    (targetCol == colLogica - 1 || targetCol == colLogica + 1);

            case ALFIL:
                // Un alfil ataca si la diferencia absoluta de filas es igual a la de columnas
                return Math.abs(targetCol - colLogica) == Math.abs(targetFila - filLogica);

            case CABALLO:
                // El caballo se mueve en "L": 2 en un eje y 1 en el otro
                int diffCol = Math.abs(targetCol - colLogica);
                int diffFil = Math.abs(targetFila - filLogica);
                return (diffCol == 1 && diffFil == 2) || (diffCol == 2 && diffFil == 1);

            case REINA:
                // La reina combina los movimientos de torre y alfil
                return (targetCol == colLogica) || (targetFila == filLogica) ||
                    Math.abs(targetCol - colLogica) == Math.abs(targetFila - filLogica);

            default:
                return false;
        }
    }
}
