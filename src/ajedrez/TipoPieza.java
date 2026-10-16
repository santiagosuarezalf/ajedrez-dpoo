package ajedrez;

/**
 * Los seis tipos de pieza del ajedrez. Cada subclase de {@link Pieza} devuelve el suyo en
 * {@link Pieza#getTipo()}, lo que permite preguntar qué pieza es sin revisar su clase.
 *
 * @author Juan José Betancourt (67001968)
 * @author Santiago Suárez Alfonso (67001862)
 */
public enum TipoPieza{

  /** El peón. */
  PEON,
  /** La torre. */
  TORRE,
  /** El caballo. */
  CABALLO,
  /** El alfil. */
  ALFIL,
  /** La dama. */
  DAMA,
  /** El rey. */
  REY;

}
