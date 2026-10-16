package ajedrez;

  import java.util.ArrayList;
  import java.util.List;

/**
 * El caballo. Se mueve en forma de L: dos casillas en una dirección y una en la perpendicular.
 * Es la única pieza que puede saltar por encima de otras, porque solo importa la casilla de
 * llegada.
 *
 * @author Juan José Betancourt (67001968)
 * @author Santiago Suárez Alfonso (67001862)
 */
public class Caballo extends Pieza {

 /**
  * Crea un caballo del color indicado.
  *
  * @param color bando al que pertenece el caballo
  */
 public Caballo(Color color){
  super(color);
 }

 /**
  * Indica que esta pieza es un caballo.
  *
  * @return {@link TipoPieza#CABALLO}
  */
 @Override
 public TipoPieza getTipo(){
  return TipoPieza.CABALLO;
 }

 /**
  * Devuelve las casillas a las que el caballo se puede mover. Prueba los ocho saltos posibles
  * (±2, ±1) y (±1, ±2), y con {@link Pieza#agregarSiValida} se queda con los que caen dentro
  * del tablero y no tienen una pieza propia.
  *
  * @param t el tablero de la partida
  * @return la lista de casillas de destino posibles
  */
 @Override
 public List<Casilla> getMovimientosPosibles(Tablero t) {
        List<Casilla> moves = new ArrayList<>();
        int [][] saltos = {{-2,-1},{-2,1},{-1,-2},{-1,2},{1,-2},{1,2},{2,-1},{2,1}};
        for (int [] s : saltos){
          agregarSiValida(moves, t, casilla.getFila() + s[0], casilla.getColumna() + s[1]);
        }
        return moves;

}
}
