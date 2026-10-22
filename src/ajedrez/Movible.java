package ajedrez;

import java.util.List;

/**
 * Contrato de todo lo que se puede mover por el tablero. Dice <em>qué</em> tiene que saber
 * responder una pieza, sin decir <em>cómo</em>: cada clase que lo implementa pone su propia
 * forma de moverse.
 *
 * <p>{@link Juego} y {@link Tablero} solo necesitan estos dos métodos para validar jugadas y
 * detectar jaques, así que no dependen de qué pieza concreta están revisando. Lo implementa
 * {@link Pieza}, y a través de ella todas sus subclases.</p>
 *
 * @author Juan José Betancourt (67001968)
 * @author Santiago Suárez Alfonso (67001862)
 */
public interface Movible {

  /**
   * Devuelve las casillas a las que se puede mover según su forma de moverse, sin revisar
   * todavía si la jugada deja al propio rey en jaque.
   *
   * @param tablero el tablero de la partida
   * @return la lista de casillas de destino posibles
   */
  List<Casilla> getMovimientosPosibles(Tablero tablero);

  /**
   * Devuelve las casillas que amenaza. Se usa para saber si un rey está en jaque.
   *
   * @param tablero el tablero de la partida
   * @return la lista de casillas atacadas
   */
  List<Casilla> casillasAtacadas(Tablero tablero);
}
