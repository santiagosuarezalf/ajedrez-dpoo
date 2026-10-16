package ajedrez;

  import java.util.ArrayList;
  import java.util.List;

/**
 * La dama. Combina los movimientos de la torre y del alfil: se mueve cualquier número de
 * casillas en línea recta o en diagonal, sin saltar por encima de otras piezas.
 *
 * @author Juan José Betancourt (67001968)
 * @author Santiago Suárez Alfonso (67001862)
 */
public class Dama extends Pieza {

 /**
  * Crea una dama del color indicado.
  *
  * @param color bando al que pertenece la dama
  */
 public Dama(Color color){
  super(color);
 }

 /**
  * Indica que esta pieza es una dama.
  *
  * @return {@link TipoPieza#DAMA}
  */
 @Override
 public TipoPieza getTipo(){
  return TipoPieza.DAMA;
 }

 /**
  * Devuelve las casillas a las que la dama se puede mover. Los dos ciclos generan todas las
  * combinaciones de Δfila y Δcolumna entre -1 y 1; se descarta (0, 0), que sería quedarse
  * quieta, y las ocho direcciones que quedan se recorren con {@link Pieza#deslizar}.
  *
  * @param t el tablero de la partida
  * @return la lista de casillas de destino posibles
  */
 @Override
 public List<Casilla> getMovimientosPosibles(Tablero t) {
        List<Casilla> moves = new ArrayList<>();
       for(int df = -1; df <= 1; df++){
        for(int dc = -1; dc <= 1; dc++){
          if (df != 0 || dc != 0){
            deslizar(moves, t ,df, dc);
          }
        }
       }
       return moves;

}
}
