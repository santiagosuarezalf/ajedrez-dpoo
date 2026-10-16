package ajedrez;

  import java.util.ArrayList;
  import java.util.List;

/**
 * La torre. Se mueve en línea recta cualquier número de casillas, manteniendo fija la fila o la
 * columna, sin saltar por encima de otras piezas.
 *
 * @author Juan José Betancourt (67001968)
 * @author Santiago Suárez Alfonso (67001862)
 */
public class Torre extends Pieza {


  /**
   * Crea una torre del color indicado.
   *
   * @param color bando al que pertenece la torre
   */
  public Torre(Color color){
    super(color);
  }

  /**
   * Indica que esta pieza es una torre.
   *
   * @return {@link TipoPieza#TORRE}
   */
  @Override
  public TipoPieza getTipo(){
    return TipoPieza.TORRE;
  }

  /**
   * Devuelve las casillas a las que la torre se puede mover, recorriendo con
   * {@link Pieza#deslizar} sus cuatro direcciones: arriba (-1, 0), abajo (1, 0),
   * izquierda (0, -1) y derecha (0, 1).
   *
   * @param t el tablero de la partida
   * @return la lista de casillas de destino posibles
   */
  @Override
  public List<Casilla> getMovimientosPosibles(Tablero t){
    List<Casilla> moves = new ArrayList<>();
    deslizar(moves, t, -1, 0);
    deslizar(moves, t, 1, 0);
    deslizar(moves, t, 0, -1);
    deslizar(moves, t, 0, 1);
    return moves;
  }

}
