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

public class Dama extends Pieza {

 public Dama(Color color){
  super(color);
 }

 @Override
 public TipoPieza getTipo(){
  return TipoPieza.DAMA;
 }

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