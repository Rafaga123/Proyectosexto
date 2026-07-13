package com.brk.chessrunner;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

public class GestorEnemigos {
    public Array<Enemigo> activos;
    public Array<PowerUp> powerUpsActivos;

    public GestorEnemigos() {
        activos = new Array<Enemigo>();
        powerUpsActivos = new Array<PowerUp>();
    }

    // Metodo para crear un nuevo enemigo y añadirlo a la lista
    public void generarEnemigo(TipoPieza tipo, int col, int fila) {
        activos.add(new Enemigo(tipo, col, fila));
    }

    // El escáner principal de la muerte
    public boolean estaCasillaAmenazada(int col, int fila) {
        for (int i = 0; i < activos.size; i++) {
            Enemigo e = activos.get(i);

            if (Math.abs(e.filLogica - fila) > 7) continue;
            // El ataque solo es válido si la línea de visión está limpia
            if (e.atacaCasilla(col, fila) && !caminoBloqueado(e, col, fila)) {
                return true;
            }
        }
        return false;
    }

    // verifica si podemos capturar una pieza de forma segura
    public boolean estaCasillaDefendida(int col, int fila) {
        for (int i = 0; i < activos.size; i++) {
            Enemigo e = activos.get(i);
            if (e.colLogica == col && e.filLogica == fila) continue;

            if (Math.abs(e.filLogica - fila) > 7) continue;
            if (e.atacaCasilla(col, fila) && !caminoBloqueado(e, col, fila)) {
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

    // Metodo para recoger un power-up si el jugador cae sobre él
    public TipoPowerUp intentarRecogerPowerUp(int col, int fila) {
        for (int i = powerUpsActivos.size - 1; i >= 0; i--) {
            PowerUp p = powerUpsActivos.get(i);
            if (p.colLogica == col && p.filLogica == fila) {
                TipoPowerUp tipo = p.tipo;
                powerUpsActivos.removeIndex(i);
                return tipo;
            }
        }
        return null;
    }

    // Limpieza de memoria para enemigos y power-ups
    public void limpiarObjetosPasados(int filaBasePantalla) {
        // Iteramos el array de atrás hacia adelante para poder borrar elementos sin que se rompa el bucle
        for (int i = activos.size - 1; i >= 0; i--) {
            Enemigo e = activos.get(i);
            // Si el enemigo quedó 2 filas por debajo del borde de la pantalla, lo borramos de la RAM
            if (e.filLogica < filaBasePantalla - 2) {
                activos.removeIndex(i);
            }
        }

        for (int i = powerUpsActivos.size - 1; i >= 0; i--) {
            PowerUp p = powerUpsActivos.get(i);
            if (p.filLogica < filaBasePantalla - 2) {
                powerUpsActivos.removeIndex(i);
            }
        }
    }

    // Limpia todas las listas de objetos activos
    public void vaciar() {
        activos.clear();
        powerUpsActivos.clear();
    }

    // Metodo para generar enemigos automaticamente segun el jugador avanza (Hay que mejorarlo para calcular que sea posible el camino)
    public void generarFilaDeEnemigos(int filaAparicion, int colJugador, int filaJugador, ModoJuego modo, int nivel, TipoPieza piezaJugador) {
        if (activos.size >= 15) return;

        for (int i = 0; i < 2; i++) {
            if (MathUtils.randomBoolean(0.4f)) {
                int colAleatoria = MathUtils.random(0, 4);
                TipoPieza tipoElegido = obtenerPiezaAleatoria(modo, nivel);

                if (!hayEnemigoEnCasilla(colAleatoria, filaAparicion) && !hayPowerUpEnCasilla(colAleatoria, filaAparicion)) {
                    Enemigo nuevoEnemigo = new Enemigo(tipoElegido, colAleatoria, filaAparicion);

                    // Si la pieza generada amenaza al jugador en el mismo instante en que nace, la descartamos.
                    if (nuevoEnemigo.atacaCasilla(colJugador, filaJugador) && !caminoBloqueado(nuevoEnemigo, colJugador, filaJugador)) {
                        continue; // Saltamos a la siguiente iteración sin agregar la pieza
                    }

                    activos.add(nuevoEnemigo);

                    if (!existeCaminoSeguro(colJugador, filaJugador, filaAparicion, piezaJugador)) {
                        activos.removeValue(nuevoEnemigo, true);
                    }
                }
            }
        }
    }

    private boolean existeCaminoSeguro(int colInicio, int filaInicio, int filaMeta, TipoPieza piezaJugador) {
        int filasDeDistancia = (filaMeta - filaInicio) + 1;
        if (filasDeDistancia <= 0) return true;

        boolean[][] visitado = new boolean[5][filasDeDistancia];
        Array<int[]> cola = new Array<>();

        cola.add(new int[]{colInicio, filaInicio});
        visitado[colInicio][0] = true;

        while (cola.size > 0) {
            int[] actual = cola.removeIndex(0);
            int c = actual[0];
            int f = actual[1];

            if (f >= filaMeta) return true;

            // Escanea TODO el tablero frente al jugador evaluando saltos dinámicos
            for (int nuevaCol = 0; nuevaCol < 5; nuevaCol++) {
                for (int nuevaFila = f; nuevaFila <= Math.min(f + 6, filaMeta); nuevaFila++) {
                    if (nuevaCol == c && nuevaFila == f) continue;

                    if (esMovimientoValidoParaBFS(piezaJugador, c, f, nuevaCol, nuevaFila)) {
                        if (!caminoBloqueadoJugador(piezaJugador, c, f, nuevaCol, nuevaFila)) {
                            int indiceFilaMatriz = nuevaFila - filaInicio;
                            if (indiceFilaMatriz >= 0 && indiceFilaMatriz < filasDeDistancia && !visitado[nuevaCol][indiceFilaMatriz]) {
                                if (!estaCasillaDefendida(nuevaCol, nuevaFila)) {
                                    visitado[nuevaCol][indiceFilaMatriz] = true;
                                    cola.add(new int[]{nuevaCol, nuevaFila});
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean esMovimientoValidoParaBFS(TipoPieza pieza, int c1, int f1, int c2, int f2) {
        int difCol = Math.abs(c2 - c1);
        int difFila = Math.abs(f2 - f1);
        if (difFila > 6) return false;

        if (pieza == null) return difCol <= 1 && difFila <= 1; // REY

        switch (pieza) {
            case PEON: return difCol <= 1 && difFila == 1;
            case TORRE: return difCol == 0 || difFila == 0;
            case ALFIL: return difCol == difFila;
            case CABALLO: return (difCol == 1 && difFila == 2) || (difCol == 2 && difFila == 1);
            case REINA: return difCol == 0 || difFila == 0 || difCol == difFila;
            default: return difCol <= 1 && difFila <= 1;
        }
    }

    // Metodo para generar power-ups aleatoriamente
    public void intentarGenerarPowerUp(int filaAparicion) {
        if (MathUtils.randomBoolean(0.05f)) {
            int colAleatoria = MathUtils.random(0, 4);

            if (!hayEnemigoEnCasilla(colAleatoria, filaAparicion) && !hayPowerUpEnCasilla(colAleatoria, filaAparicion)) {
                TipoPowerUp tipo = TipoPowerUp.values()[MathUtils.random(TipoPowerUp.values().length - 1)];
                powerUpsActivos.add(new PowerUp(tipo, colAleatoria, filaAparicion));
            }
        }
    }
    // Metodo público auxiliar para buscar enemigos por coordenadas
    public boolean hayEnemigoEnCasilla(int col, int fila) {
        for (int i = 0; i < activos.size; i++) {
            Enemigo e = activos.get(i);
            if (e.colLogica == col && e.filLogica == fila) return true;
        }
        return false;
    }

    // Metodo para buscar power-ups por coordenadas
    public boolean hayPowerUpEnCasilla(int col, int fila) {
        for (int i = 0; i < powerUpsActivos.size; i++) {
            PowerUp p = powerUpsActivos.get(i);
            if (p.colLogica == col && p.filLogica == fila) return true;
        }
        return false;
    }

    // Calcula si una pieza enemiga está bloqueada por otro cuerpo
    private boolean caminoBloqueado(Enemigo atacante, int targetCol, int targetFila) {
        if (atacante.tipo == TipoPieza.CABALLO) return false; // El caballo salta

        int dirCol = Integer.signum(targetCol - atacante.colLogica);
        int dirFila = Integer.signum(targetFila - atacante.filLogica);

        int actualCol = atacante.colLogica + dirCol;
        int actualFila = atacante.filLogica + dirFila;

        while (actualCol != targetCol || actualFila != targetFila) {
            if (hayEnemigoEnCasilla(actualCol, actualFila)) return true;
            actualCol += dirCol;
            actualFila += dirFila;
        }
        return false;
    }

    // Calcula si el JUGADOR está bloqueado al usar un Power-Up de larga distancia
    private boolean caminoBloqueadoJugador(TipoPieza pieza, int c1, int f1, int c2, int f2) {
        if (pieza == null || pieza == TipoPieza.CABALLO) return false;
        int dirCol = Integer.signum(c2 - c1);
        int dirFila = Integer.signum(f2 - f1);
        int ac = c1 + dirCol;
        int af = f1 + dirFila;
        while(ac != c2 || af != f2) {
            if (hayEnemigoEnCasilla(ac, af)) return true;
            ac += dirCol;
            af += dirFila;
        }
        return false;
    }

    // Sistema de pesos para balancear la aparición de piezas
    private TipoPieza obtenerPiezaAleatoria(ModoJuego modo, int nivel) {
        if (modo == ModoJuego.TUTORIAL) {
            if (nivel == 1) {
                return TipoPieza.PEON; // Nivel 1: solo se crearan peones
            } else if (nivel == 2) {
                // Nivel 2: Peones a un 70% y Caballos 30%
                return MathUtils.randomBoolean(0.7f) ? TipoPieza.PEON : TipoPieza.CABALLO;
            } else if (nivel == 3) {
                // Nivel 3: Peones (50%), Caballos (30%), Alfiles (20%)
                int tirada = MathUtils.random(1, 100);
                if (tirada <= 50) return TipoPieza.PEON;
                if (tirada <= 80) return TipoPieza.CABALLO;
                return TipoPieza.ALFIL;
            }
        }

        // Para el modo CLASICO, Infinito o Contrarreloj, usamos la distribución estándar
        int tirada = MathUtils.random(1, 100);
        if (tirada <= 45) return TipoPieza.PEON;
        if (tirada <= 70) return TipoPieza.CABALLO;
        if (tirada <= 85) return TipoPieza.ALFIL;
        if (tirada <= 95) return TipoPieza.TORRE;
        return TipoPieza.REINA;
    }
}
