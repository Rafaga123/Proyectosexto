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
        return false; // Si revisa todos y ninguno ataca, el jugador a salvo
    }

    // verifica si podemos capturar una pieza de forma segura
    public boolean estaCasillaDefendida(int col, int fila) {
        for (Enemigo e : activos) {
            // Si el enemigo está en la casilla exacta a la que nos queremos mover, lo IGNORAMOS
            if (e.colLogica == col && e.filLogica == fila) continue;

            // Si cualquier OTRO enemigo tiene esta casilla en su línea de ataque, es suicidio ir
            if (e.atacaCasilla(col, fila)) {
                return true;
            }
        }
        return false;
    }

    // Metodo para destruir una pieza si el jugador cae sobre ella
    public void intentarCapturar(int col, int fila) {
        // Iteramos en reversa porque vamos a eliminar elementos del Array
        for (int i = activos.size - 1; i >= 0; i--) {
            Enemigo e = activos.get(i);
            if (e.colLogica == col && e.filLogica == fila) {
                activos.removeIndex(i);
                System.out.println("¡Pieza capturada en la columna " + col + "!");
                break; // Solo puede haber una pieza por casilla, así que detenemos la búsqueda
            }
        }
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
    // Algoritmo de Búsqueda en Anchura (BFS) para garantizar que el nivel es pasable
    private boolean existeCaminoSeguro(int colInicio, int filaInicio, int filaMeta) {
        // Calculo de cuántas filas hay de diferencia para dimensionar nuestro mapa de visitados
        int filasDeDistancia = (filaMeta - filaInicio) + 1;
        if (filasDeDistancia <= 0) return true;

        boolean[][] visitado = new boolean[5][filasDeDistancia];
        Array<int[]> cola = new Array<>();

        // Empezamos desde la posición actual del jugador
        cola.add(new int[]{colInicio, filaInicio});
        visitado[colInicio][0] = true;

        // Posibles movimientos del Rey que nos hacen avanzar o esquivar (Se excluye calcular hacia atrás para optimizar)
        int[][] movimientos = {
            {0, 1}, {-1, 1}, {1, 1}, // Avanzar recto, diagonal izq, diagonal der
            {-1, 0}, {1, 0}          // Esquivar lateral izq, lateral der
        };

        while (cola.size > 0) {
            int[] actual = cola.removeIndex(0);
            int c = actual[0];
            int f = actual[1];

            // Si un camino logró llegar a la fila donde queremos poner la pieza nueva, el nivel es pasable
            if (f >= filaMeta) {
                return true;
            }

            for (int[] mov : movimientos) {
                int nuevaCol = c + mov[0];
                int nuevaFila = f + mov[1];

                // Verificamos que no se salga de los límites del tablero (0 a 4) y no pase de la meta
                if (nuevaCol >= 0 && nuevaCol < 5 && nuevaFila <= filaMeta) {
                    int indiceFilaMatriz = nuevaFila - filaInicio;

                    // Si la fila está dentro del rango y no la hemos evaluado aún
                    if (indiceFilaMatriz >= 0 && indiceFilaMatriz < filasDeDistancia && !visitado[nuevaCol][indiceFilaMatriz]) {

                        // REGLA CLAVE: La casilla es transitable si no está amenazada por un defensor.
                        // Esto permite al BFS considerar caminos donde el jugador captura una pieza siempre y cuando esa pieza no esté protegida por otra
                        if (!estaCasillaDefendida(nuevaCol, nuevaFila)) {
                            visitado[nuevaCol][indiceFilaMatriz] = true;
                            cola.add(new int[]{nuevaCol, nuevaFila});
                        }
                    }
                }
            }
        }

        // Si la cola se vacía y nunca llegamos a la fila meta, significa que es un bloqueo imposible
        return false;
    }

    // Metodo para generar enemigos automaticamente segun el jugador avanza (Hay que mejorarlo para calcular que sea posible el camino)
    public void intentarGenerarEnemigos(int filaJugador, int colJugador) {
        // LÍMITE DE POBLACIÓN: Si hay 10 o más enemigos en pantalla, no generamos más.
        // Esto evita la saturación extrema del 100% que veías en la imagen.
        if (activos.size >= 10) {
            return;
        }

        int filaAparicion = filaJugador + 6;

        // Bajamos a 2 intentos por fila para dar más espacio orgánico
        int intentosDeGeneracion = 2;

        for (int i = 0; i < intentosDeGeneracion; i++) {
            // 50% de probabilidad base por cada intento de colocar una pieza
            if (MathUtils.randomBoolean(0.5f)) {
                int colAleatoria = MathUtils.random(0, 4);

                // Usamos nuestro nuevo sistema de pesos en lugar del arreglo plano
                TipoPieza tipoElegido = obtenerPiezaAleatoria();

                if (!hayEnemigoEnCasilla(colAleatoria, filaAparicion)) {
                    Enemigo nuevoEnemigo = new Enemigo(tipoElegido, colAleatoria, filaAparicion);
                    activos.add(nuevoEnemigo);

                    boolean esPasable = existeCaminoSeguro(colJugador, filaJugador, filaAparicion);

                    if (!esPasable) {
                        activos.removeValue(nuevoEnemigo, true);
                        System.out.println("Generación vetada: El " + tipoElegido + " bloqueaba todos los caminos.");
                    }
                }
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

    // Sistema de pesos para balancear la aparición de piezas
    private TipoPieza obtenerPiezaAleatoria() {
        int tirada = MathUtils.random(1, 100);

        // 45% de probabilidad de ser un Peón
        if (tirada <= 45) return TipoPieza.PEON;

        // 25% de probabilidad de ser un Caballo
        if (tirada <= 70) return TipoPieza.CABALLO;

        // 15% de probabilidad de ser un Alfil
        if (tirada <= 85) return TipoPieza.ALFIL;

        // 10% de probabilidad de ser una Torre
        if (tirada <= 95) return TipoPieza.TORRE;

        // Solo 5% de probabilidad de ser una Reina
        return TipoPieza.REINA;
    }
}
