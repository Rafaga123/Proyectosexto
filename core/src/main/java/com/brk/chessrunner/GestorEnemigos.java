package com.brk.chessrunner;

import com.badlogic.gdx.utils.Array;

public class GestorEnemigos {
    public Array<Enemigo> activos;

    public GestorEnemigos() {
        activos = new Array<Enemigo>();
    }

    // Metodo para crear un nuevo enemigo y añadirlo a la lista
    public void generarEnemigo(TipoPieza tipo, int col, int fila) {
        activos.add(new Enemigo(tipo, col, fila));
    }

    // El escáner principal de la muerte
    public boolean estaCasillaAmenazada(int col, int fila) {
        for (Enemigo e : activos) {
            if (e.atacaCasilla(col, fila)) {
                return true; // Basta con que UNO ataque la casilla para morir
            }
        }
        return false; // Si revisa todos y ninguno ataca, estás a salvo
    }

    // Limpieza de memoria
    public void limpiarEnemigosPasados(int filaBasePantalla) {
        // Iteramos el array de atrás hacia adelante para poder borrar elementos sin que se rompa el bucle
        for (int i = activos.size - 1; i >= 0; i--) {
            Enemigo e = activos.get(i);
            // Si el enemigo quedó 2 filas por debajo del borde de la pantalla, lo borramos de la RAM
            if (e.filLogica < filaBasePantalla - 2) {
                activos.removeIndex(i);
            }
        }
    }
}
