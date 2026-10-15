
package ajedrez;

public class Casilla {

private final int fila;
private final int columna;
private Pieza pieza;

public Casilla(int fila, int columna){
  this.fila = fila;
  this.columna = columna;
}

public int getFila(){
  return fila;
}

public int getColumna(){
  return columna;
}

public Pieza getPieza(){
  return pieza;
}

public void setPieza(Pieza pieza){
  this.pieza = pieza;
}

public boolean estaVacia(){
  return pieza == null;
}

@Override
public String toString(){
  return "(" + fila + "," + columna + ")";
}

}