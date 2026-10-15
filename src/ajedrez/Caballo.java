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

public class Caballo extends Pieza {

 public Caballo(Color color){
  super(color);
 }

 @Override
 public TipoPieza getTipo(){
  return TipoPieza.CABALLO;
 }

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