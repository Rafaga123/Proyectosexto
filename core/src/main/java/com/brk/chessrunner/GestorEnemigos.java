package com.brk.chessrunner;

import com.badlogic.gdx.math.MathUtils;
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

    // Metodo para generar enemigos automaticamente segun el jugador avanza (Hay que mejorarlo para calcular que sea posible el camino)
    public void intentarGenerarEnemigos(int filaJugador) {
        // Generamos los enemigos 6 filas por delante de la posicion actual del jugador
        int filaAparicion = filaJugador + 6;

        // 40% de probabilidades de generar una pieza nueva en esta fila
        if (MathUtils.randomBoolean(0.4f)) {
            int colAleatoria = MathUtils.random(0, 4);

            // Elegimos un tipo de pieza al azar
            TipoPieza[] tiposPosibles = {TipoPieza.PEON, TipoPieza.TORRE, TipoPieza.CABALLO, TipoPieza.ALFIL};
            TipoPieza tipoElegido = tiposPosibles[MathUtils.random(0, tiposPosibles.length - 1)];

            // Condicional para evitar poner dos enemigos exactamente en la misma casilla
            if (!hayEnemigoEnCasilla(colAleatoria, filaAparicion)) {
                generarEnemigo(tipoElegido, colAleatoria, filaAparicion);
            }
        }
    }

    // Metodo para verificar si una casilla ya esta ocupada
    private boolean hayEnemigoEnCasilla(int col, int fila) {
        for (Enemigo e : activos) {
            if (e.colLogica == col && e.filLogica == fila) {
                return true;
            }
        }
        return false;
    }
}
