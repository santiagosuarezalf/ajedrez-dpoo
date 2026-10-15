package ajedrez;

/**
 *
 * @author juanj
 */
  import java.util.ArrayList;
  import java.util.List;

public class Torre extends Pieza {


  public Torre(Color color){
    super(color);
  }

  @Override
  public TipoPieza getTipo(){
    return TipoPieza.TORRE;
  }

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
