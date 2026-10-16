
package ajedrez;

/**
 * Una de las 64 casillas del tablero. Conoce su posición, que no cambia nunca, y la pieza que
 * la ocupa en este momento, que puede no existir.
 *
 * <p>La posición se guarda como índices de la matriz del tablero: la fila 0 corresponde a la
 * fila 8 del ajedrez y la columna 0 a la columna a.</p>
 *
 * @author Juan José Betancourt (67001968)
 * @author Santiago Suárez Alfonso (67001862)
 */
public class Casilla {

/** Fila de la casilla en la matriz del tablero, de 0 a 7. */
private final int fila;
/** Columna de la casilla en la matriz del tablero, de 0 (columna a) a 7 (columna h). */
private final int columna;
/** Pieza que ocupa la casilla, o {@code null} si está vacía. */
private Pieza pieza;

/**
 * Crea una casilla vacía en la posición indicada.
 *
 * @param fila fila de la casilla en la matriz, de 0 a 7
 * @param columna columna de la casilla en la matriz, de 0 a 7
 */
public Casilla(int fila, int columna){
  this.fila = fila;
  this.columna = columna;
}

/**
 * Devuelve la fila de la casilla.
 *
 * @return la fila en la matriz, de 0 a 7
 */
public int getFila(){
  return fila;
}

/**
 * Devuelve la columna de la casilla.
 *
 * @return la columna en la matriz, de 0 a 7
 */
public int getColumna(){
  return columna;
}

/**
 * Devuelve la pieza que ocupa la casilla.
 *
 * @return la pieza, o {@code null} si la casilla está vacía
 */
public Pieza getPieza(){
  return pieza;
}

/**
 * Pone una pieza en la casilla, o la deja vacía. Solo cambia la casilla: no le avisa a la
 * pieza dónde quedó, eso lo hace quien la mueve ({@link Tablero#moverPieza}).
 *
 * @param pieza la pieza que queda en la casilla, o {@code null} para vaciarla
 */
public void setPieza(Pieza pieza){
  this.pieza = pieza;
}

/**
 * Indica si la casilla no tiene ninguna pieza.
 *
 * @return {@code true} si está vacía
 */
public boolean estaVacia(){
  return pieza == null;
}

/**
 * Devuelve la posición de la casilla en la forma {@code (fila,columna)}.
 *
 * @return la posición como texto
 */
@Override
public String toString(){
  return "(" + fila + "," + columna + ")";
}

}
