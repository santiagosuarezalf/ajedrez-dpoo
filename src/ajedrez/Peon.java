/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ajedrez;

/**
 *
 * @author juanj
 */

import java.util.ArrayList;
import java.util.List;

public class Peon extends Pieza {

    public Peon(Color color) {
        super(color);
    }

    @Override
    public TipoPieza getTipo() {
        return TipoPieza.PEON;
    }

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
     * Casillas que este peón ATACA (aunque estén vacías).
     * Necesario para jaque, enroque y casillaAtacada.
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
