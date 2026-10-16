package ajedrez;

import java.util.ArrayList;
import java.util.List;

/**
 * El peón. Es la única pieza que solo avanza hacia adelante, cuya dirección depende del color, y
 * que no captura igual a como se mueve: avanza en línea recta pero captura en diagonal.
 *
 * <p>Como la fila 0 de la matriz es la fila 8 del tablero, las blancas avanzan restando una fila
 * (Δfila = -1) y las negras sumando una (Δfila = +1).</p>
 *
 * @author Juan José Betancourt (67001968)
 * @author Santiago Suárez Alfonso (67001862)
 */
public class Peon extends Pieza {

    /**
     * Crea un peón del color indicado.
     *
     * @param color bando al que pertenece el peón
     */
    public Peon(Color color) {
        super(color);
    }

    /**
     * Indica que esta pieza es un peón.
     *
     * @return {@link TipoPieza#PEON}
     */
    @Override
    public TipoPieza getTipo() {
        return TipoPieza.PEON;
    }

    /**
     * Devuelve las casillas a las que el peón se puede mover. Verifica, en este orden:
     * <ol>
     *   <li><b>Avance de una casilla:</b> la casilla de adelante tiene que estar vacía; el peón
     *       nunca captura de frente.</li>
     *   <li><b>Doble paso:</b> solo si el peón no se ha movido, está en su fila inicial (6 para
     *       las blancas, 1 para las negras) y las dos casillas de adelante están vacías.</li>
     *   <li><b>Captura:</b> las dos casillas en diagonal hacia adelante, solo si tienen una
     *       pieza rival.</li>
     * </ol>
     *
     * @param t el tablero de la partida
     * @return la lista de casillas de destino posibles
     */
    @Override
    public List<Casilla> getMovimientosPosibles(Tablero t) {
        List<Casilla> moves = new ArrayList<>();
        int direction = (color == Color.BLANCO) ? -1 : 1; // blanco sube (fila decrece)
        int filaInicial = (color == Color.BLANCO) ? 6 : 1;

        // Avance de una casilla
        Casilla adelante = t.getCasilla(casilla.getFila() + direction, casilla.getColumna());
        if (adelante != null && adelante.estaVacia()) {
            moves.add(adelante);
            // Doble paso solo desde posición inicial
            if (!seHaMovido && casilla.getFila() == filaInicial) {
                Casilla doble = t.getCasilla(casilla.getFila() + 2 * direction, casilla.getColumna());
                if (doble != null && doble.estaVacia()) moves.add(doble);
            }
        }
        // Captura en diagonal (solo si hay enemigo)
        for (int dc : new int[]{-1, 1}) {
            Casilla diagonal = t.getCasilla(casilla.getFila() + direction, casilla.getColumna() + dc);
            if (diagonal != null && !diagonal.estaVacia()
                    && diagonal.getPieza().getColor() != this.color) {
                moves.add(diagonal);
            }
        }
        return moves;
    }

    /**
     * Devuelve las casillas que el peón amenaza: las dos diagonales de adelante, estén vacías u
     * ocupadas. Se diferencia de {@link #getMovimientosPosibles(Tablero)} porque un peón amenaza
     * su diagonal aunque no haya nada que capturar ahí, y eso es lo que importa para saber si el
     * rey rival puede pararse en esa casilla o pasar por ella al enrocar.
     *
     * @param t el tablero de la partida
     * @return la lista de casillas atacadas
     */
    @Override
    public List<Casilla> casillasAtacadas(Tablero t) {
        List<Casilla> ataques = new ArrayList<>();
        int direction = (color == Color.BLANCO) ? -1 : 1;
        for (int dc : new int[]{-1, 1}) {
            Casilla diagonal = t.getCasilla(casilla.getFila() + direction, casilla.getColumna() + dc);
            if (diagonal != null) {
                ataques.add(diagonal);
            }
        }
        return ataques;
    }
}
