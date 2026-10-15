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

public class Alfil extends Pieza {

 public Alfil(Color color){
  super(color);
 }

 @Override
 public TipoPieza getTipo(){
  return TipoPieza.ALFIL;
 }

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