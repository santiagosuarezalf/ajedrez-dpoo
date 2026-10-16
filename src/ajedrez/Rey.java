package ajedrez;

  import java.util.ArrayList;
  import java.util.List;

/**
 * El rey. Se mueve una sola casilla en cualquiera de las ocho direcciones. El enroque, que es el
 * único movimiento en el que avanza dos casillas, no está aquí sino en
 * {@link Juego#intentarEnroque}, porque involucra también a una torre.
 *
 * @author Juan José Betancourt (67001968)
 * @author Santiago Suárez Alfonso (67001862)
 */
public class Rey extends Pieza {

 /**
  * Crea un rey del color indicado.
  *
  * @param color bando al que pertenece el rey
  */
 public Rey(Color color){
  super(color);
 }

 /**
  * Indica que esta pieza es un rey.
  *
  * @return {@link TipoPieza#REY}
  */
 @Override
 public TipoPieza getTipo(){
  return TipoPieza.REY;
 }

 /**
  * Devuelve las casillas vecinas a las que el rey se puede mover. Los dos ciclos generan las
  * combinaciones de Δfila y Δcolumna entre -1 y 1, se descarta (0, 0), y cada una de las ocho
  * casillas se revisa con {@link Pieza#agregarSiValida}. Que el rey no se ponga en jaque lo
  * verifica después {@link Juego}.
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
            agregarSiValida(moves, t, casilla.getFila() + df, casilla.getColumna() + dc);
          }
        }
       }
       return moves;

}
}
