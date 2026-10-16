package ajedrez;

  import java.util.ArrayList;
  import java.util.List;

/**
 * El alfil. Se mueve en diagonal cualquier número de casillas, sin saltar por encima de otras
 * piezas. En cada paso la fila y la columna cambian las dos en una unidad, así que el alfil nunca
 * cambia de color de casilla.
 *
 * @author Juan José Betancourt (67001968)
 * @author Santiago Suárez Alfonso (67001862)
 */
public class Alfil extends Pieza {

 /**
  * Crea un alfil del color indicado.
  *
  * @param color bando al que pertenece el alfil
  */
 public Alfil(Color color){
  super(color);
 }

 /**
  * Indica que esta pieza es un alfil.
  *
  * @return {@link TipoPieza#ALFIL}
  */
 @Override
 public TipoPieza getTipo(){
  return TipoPieza.ALFIL;
 }

 /**
  * Devuelve las casillas a las que el alfil se puede mover, recorriendo con
  * {@link Pieza#deslizar} sus cuatro diagonales: (-1, -1), (-1, 1), (1, -1) y (1, 1).
  *
  * @param t el tablero de la partida
  * @return la lista de casillas de destino posibles
  */
 @Override
 public List<Casilla> getMovimientosPosibles(Tablero t) {
        List<Casilla> moves = new ArrayList<>();
        deslizar(moves, t, -1, -1);
        deslizar(moves, t, -1, 1);
        deslizar(moves, t, 1, -1);
        deslizar(moves, t, 1, 1);
        return moves;
    }

}
