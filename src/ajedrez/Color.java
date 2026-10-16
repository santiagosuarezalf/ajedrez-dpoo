package ajedrez;

/**
 * Representa los dos bandos de una partida de ajedrez: las piezas blancas y las negras.
 * Se usa tanto para saber de quién es una pieza como para saber de quién es el turno.
 *
 * @author Juan José Betancourt (67001968)
 * @author Santiago Suárez Alfonso (67001862)
 */
public enum Color{
  /** Las piezas blancas, que empiezan la partida. */
  BLANCO,
  /** Las piezas negras. */
  NEGRO;

  /**
   * Devuelve el bando contrario a este. Se usa para cambiar el turno después de cada jugada
   * y para saber quién es el rival de una pieza.
   *
   * @return {@code NEGRO} si este color es {@code BLANCO}, y {@code BLANCO} si es {@code NEGRO}
   */
  public Color opuesto(){
    return this == BLANCO ? NEGRO : BLANCO;
  }
}
