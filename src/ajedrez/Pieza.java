package ajedrez;

/**
 * Clase abstracta que reúne lo que tienen en común todas las piezas: su color, la casilla donde
 * están y si ya se movieron alguna vez. No se puede crear una "pieza" genérica; solo sus
 * subclases {@link Peon}, {@link Torre}, {@link Caballo}, {@link Alfil}, {@link Dama} y
 * {@link Rey}.
 *
 * <p>Cada subclase decide cómo se mueve implementando {@link #getMovimientosPosibles(Tablero)}.
 * Para eso se apoya en dos métodos de esta clase: {@link #deslizar} para las piezas que avanzan
 * varias casillas en línea recta (torre, alfil y dama) y {@link #agregarSiValida} para las que
 * dan un solo salto (caballo y rey).</p>
 *
 * @author Juan José Betancourt (67001968)
 * @author Santiago Suárez Alfonso (67001862)
 */
public abstract class Pieza {

  /** Bando al que pertenece la pieza. */
  protected Color color;
  /** Casilla donde está la pieza en este momento. */
  protected Casilla casilla;
  /** Indica si la pieza ya se movió al menos una vez. Lo necesitan el doble paso del peón y el enroque. */
  protected boolean seHaMovido;

  /**
   * Crea una pieza del color indicado, todavía sin casilla y sin haberse movido.
   *
   * @param color bando al que pertenece la pieza
   */
  public Pieza(Color color){
    this.color = color;
    this.seHaMovido = false;
  }

  /**
   * Devuelve el bando de la pieza.
   *
   * @return el color de la pieza
   */
  public Color getColor(){
    return color;
  }

  /**
   * Devuelve la casilla donde está la pieza.
   *
   * @return la casilla actual de la pieza
   */
  public Casilla getCasilla(){
    return casilla;
  }

  /**
   * Cambia la casilla donde está la pieza. Solo actualiza la pieza: la casilla se actualiza
   * aparte con {@link Casilla#setPieza}.
   *
   * @param casilla la nueva casilla de la pieza
   */
  public void setCasilla(Casilla casilla){
    this.casilla = casilla;
  }

  /**
   * Indica si la pieza ya se movió alguna vez en la partida.
   *
   * @return {@code true} si ya se movió
   */
  public boolean seHaMovido(){
    return seHaMovido;
  }

  /**
   * Deja registrado que la pieza ya se movió. Lo llama {@link Tablero#moverPieza} en cada
   * movimiento.
   */
  public void marcarComoMovida(){
    this.seHaMovido = true;
  }

  /**
   * Devuelve qué tipo de pieza es. Cada subclase devuelve el suyo.
   *
   * @return el tipo de la pieza
   */
  public abstract TipoPieza getTipo();

  /**
   * Devuelve las casillas que esta pieza amenaza. Se usa para saber si un rey está en jaque y
   * si el rey puede pasar por una casilla al enrocar.
   *
   * <p>Para casi todas las piezas, las casillas que ataca son las mismas a las que se puede
   * mover, así que por defecto devuelve {@link #getMovimientosPosibles(Tablero)}. El peón es la
   * excepción y sobrescribe este método.</p>
   *
   * @param t el tablero de la partida
   * @return la lista de casillas atacadas
   */
    public java.util.List<Casilla> casillasAtacadas(Tablero t){
        return getMovimientosPosibles(t);
    }


  /**
   * Devuelve las casillas a las que la pieza se puede mover según su forma de moverse, sin
   * revisar todavía si la jugada deja al propio rey en jaque (eso lo verifica {@link Juego}).
   * Las casillas ocupadas por piezas propias nunca se incluyen; las ocupadas por piezas rivales
   * sí, porque moverse ahí es una captura.
   *
   * @param tablero el tablero de la partida
   * @return la lista de casillas de destino posibles
   */
  public abstract java.util.List<Casilla> getMovimientosPosibles(Tablero tablero);

  /**
   * Describe la pieza con su color y su tipo, por ejemplo {@code BLANCO TORRE}.
   *
   * @return la descripción de la pieza
   */
  @Override
  public String toString(){
    return color + " " + getTipo();
  }

  /**
   * Agrega a la lista la casilla de la posición indicada si la pieza puede llegar ahí de un solo
   * salto. Verifica dos cosas: que la posición esté dentro del tablero, y que la casilla esté
   * vacía o tenga una pieza rival (captura). Si tiene una pieza propia, no la agrega.
   * Lo usan el {@link Caballo} y el {@link Rey}.
   *
   * @param lista la lista de movimientos donde se agrega la casilla
   * @param t el tablero de la partida
   * @param f fila de destino
   * @param c columna de destino
   */
  protected void agregarSiValida(java.util.List<Casilla> lista, Tablero t, int f, int c){
    Casilla destino = t.getCasilla(f,c);
    if(destino == null){
      return;
    }
    if(destino.estaVacia() || destino.getPieza().getColor() != this.color){
      lista.add(destino);
    }
  }

  /**
   * Recorre el tablero desde la casilla de la pieza en una dirección fija (Δfila, Δcolumna) y
   * agrega a la lista cada casilla alcanzable. Avanza mientras encuentre casillas vacías; se
   * detiene en el borde del tablero o en la primera pieza que encuentre. Si esa pieza es rival,
   * la incluye (captura); si es propia, no. Así se garantiza que la pieza no salte por encima de
   * otras. Lo usan la {@link Torre}, el {@link Alfil} y la {@link Dama}.
   *
   * @param lista la lista de movimientos donde se agregan las casillas
   * @param t el tablero de la partida
   * @param df cuánto cambia la fila en cada paso (-1, 0 o 1)
   * @param dc cuánto cambia la columna en cada paso (-1, 0 o 1)
   */
  protected void deslizar(java.util.List<Casilla> lista, Tablero t, int df, int dc){
    int f = casilla.getFila() + df;
    int c = casilla.getColumna() + dc;
    Casilla destino = t.getCasilla(f,c);
    while(destino != null){
      if (destino.estaVacia()){
        lista.add(destino);
      }
      else {
        if(destino.getPieza().getColor() != this.color){
          lista.add(destino);
        }
        break;
      }
      f += df;
      c += dc;
      destino = t.getCasilla(f,c);
    }
  }

  /**
   * Cambia directamente la marca de "ya se movió". Lo usa {@link Juego} para dejar la pieza
   * como estaba después de simular un movimiento.
   *
   * @param v el nuevo valor de la marca
   */
  public void setSeHaMovido(boolean v){
    this.seHaMovido = v;
  }
}
