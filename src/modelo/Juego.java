package modelo;

import java.util.ArrayList;
import java.util.List;


/**
 * Una partida de ajedrez. Tiene el tablero, sabe de quién es el turno y decide si cada jugada es
 * legal. También detecta el jaque y el fin de la partida (jaque mate o rey ahogado).
 *
 * @author Juan José Betancourt (67001968)
 * @author Santiago Suárez Alfonso (67001862)
 */
public class Juego {

  /** El tablero de la partida, que se crea con la partida. */
  private final Tablero tablero;
  /** El bando al que le toca jugar. */
  private Color turno;
  /** Indica si la partida ya terminó. */
  private boolean terminado;
  /** Cómo terminó la partida, o {@code null} si todavía no ha terminado. */
  private String resultado;
  /**
   * Casilla del peón que acaba de llegar a la última fila y está esperando que se escoja en qué
   * pieza se convierte, o {@code null} si no hay ninguna coronación pendiente. Mientras no sea
   * {@code null}, la jugada está a medias: el turno no ha cambiado y no se aceptan más jugadas.
   */
  private Casilla coronacionPendiente;
  /**
   * Casilla por la que acaba de pasar un peón que avanzó dos casillas en la jugada anterior, o
   * {@code null} si la jugada anterior no fue un doble paso. Es la casilla de destino de una
   * posible captura al paso, y ese derecho dura una sola jugada: se borra después de cualquier
   * jugada del bando que responde.
   */
  private Casilla objetivoAlPaso;

  /**
   * Crea una partida nueva: arma el tablero con las piezas en su posición inicial y le da el
   * primer turno a las blancas.
   */
  public Juego(){
    this.tablero = new Tablero();
    this.tablero.configurarInicial();
    this.turno = Color.BLANCO;
    this.terminado = false;
  }

  /**
   * Devuelve el tablero de la partida.
   *
   * @return el tablero
   */
  public Tablero getTablero(){
    return tablero;
  }

  /**
   * Devuelve el bando al que le toca jugar.
   *
   * @return el color en turno
   */
  public Color getTurno(){
    return turno;
  }

  /**
   * Indica si la partida ya terminó.
   *
   * @return {@code true} si terminó
   */
  public boolean isTerminado(){
    return terminado;
  }

  /**
   * Devuelve cómo terminó la partida.
   *
   * @return el resultado, o {@code null} si la partida sigue
   */
  public String getResultado(){
    return resultado;
  }

  /**
   * Indica si hay un peón en la última fila esperando que se escoja su pieza. La interfaz lo
   * consulta después de cada {@link #mover}: si es {@code true}, debe preguntarle al jugador y
   * llamar a {@link #coronacion(TipoPieza)}.
   *
   * @return {@code true} si hay una coronación pendiente
   */
  public boolean hayCoronacionPendiente(){
    return coronacionPendiente != null;
  }

  /**
   * Intenta hacer una jugada. Verifica, en este orden:
   * <ol>
   *   <li>Que la partida no haya terminado y que las dos casillas existan.</li>
   *   <li>Que haya una pieza en el origen y sea del jugador en turno.</li>
   *   <li>Si es un enroque, lo resuelve aparte con {@link #intentarEnroque}.</li>
   *   <li>Que el destino esté entre los movimientos posibles de la pieza (su forma de moverse,
   *       sin pasar por encima de piezas y sin caer en una casilla propia).</li>
   *   <li>Que la jugada no deje al propio rey en jaque ({@link #dejaReyEnJaque}).</li>
   * </ol>
   * Si pasa todo, mueve la pieza, cambia el turno y revisa si la partida terminó.
   *
   * <p>Si la jugada lleva un peón a la última fila, es una coronación: el peón se mueve, pero la
   * jugada queda a medias. No se cambia el turno ni se revisa el fin de la partida hasta que se
   * llame a {@link #coronacion(TipoPieza)}. Mientras tanto, {@code mover} rechaza todo.</p>
   *
   * <p>La captura al paso se resuelve aparte ({@link #esCapturaAlPaso}), porque su destino es una
   * casilla vacía y por eso el peón no la incluye en sus movimientos posibles. Después de
   * cualquier jugada se actualiza {@link #objetivoAlPaso}: queda con la casilla saltada si fue un
   * doble paso de peón, y en {@code null} en cualquier otro caso.</p>
   *
   * @param origen casilla donde está la pieza que se quiere mover
   * @param destino casilla a donde se quiere mover
   * @return {@code true} si la jugada era legal y se hizo; {@code false} si no se hizo
   */
  public boolean mover(Casilla origen, Casilla destino){
    if (terminado || coronacionPendiente != null){
      return false;
    }
    if(origen == null || destino == null){
        return false;
    }
    Pieza pieza = origen.getPieza();
    if(pieza == null || pieza.getColor() != turno){
      return false;
    }
    if(intentarEnroque(origen, destino)){
      objetivoAlPaso = null;
      return true;
    }
    if (esCapturaAlPaso(origen, destino)){
      return hacerCapturaAlPaso(origen, destino);
    }
    List<Casilla> posibles = pieza.getMovimientosPosibles(tablero);
    if (!posibles.contains(destino)){
      return false;
    }
    if (dejaReyEnJaque(origen, destino)){
      return false;
    }
    tablero.moverPieza(origen, destino);
    objetivoAlPaso = null;
    if (pieza.getTipo() == TipoPieza.PEON && Math.abs(destino.getFila() - origen.getFila()) == 2){
      objetivoAlPaso = tablero.getCasilla((origen.getFila() + destino.getFila()) / 2, origen.getColumna());
    }
    if (pieza.getTipo() == TipoPieza.PEON && destino.getFila() == filaDeCoronacion(pieza.getColor())){
      coronacionPendiente = destino;
      return true;
    }
    turno = turno.opuesto();
    verificarFinDePartida();
    return true;
  }

  /**
   * Indica si mover de {@code origen} a {@code destino} sería una captura al paso. Lo es cuando
   * se cumplen las cuatro condiciones: la pieza es un peón; el destino es la casilla por la que
   * pasó el peón rival en su doble paso de la jugada anterior ({@link #objetivoAlPaso}); el
   * peón avanza una fila en diagonal; y al lado del peón, en la fila de origen y la columna del
   * destino, está el peón rival que se va a capturar.
   *
   * <p>No revisa si la jugada deja al rey en jaque; eso lo hace {@link #dejaReyEnJaque}.</p>
   *
   * @param origen casilla donde está el peón
   * @param destino casilla a la que se quiere mover
   * @return {@code true} si la jugada es una captura al paso
   */
  private boolean esCapturaAlPaso(Casilla origen, Casilla destino){
    if (objetivoAlPaso == null || destino != objetivoAlPaso || !destino.estaVacia()){
      return false;
    }
    Pieza pieza = origen.getPieza();
    if (pieza == null || pieza.getTipo() != TipoPieza.PEON){
      return false;
    }
    int direccion = (pieza.getColor() == Color.BLANCO) ? -1 : 1;
    if (destino.getFila() - origen.getFila() != direccion
        || Math.abs(destino.getColumna() - origen.getColumna()) != 1){
      return false;
    }
    Pieza vecino = tablero.getCasilla(origen.getFila(), destino.getColumna()).getPieza();
    return vecino != null && vecino.getTipo() == TipoPieza.PEON && vecino.getColor() != pieza.getColor();
  }

  /**
   * Hace una captura al paso que ya se sabe que lo es ({@link #esCapturaAlPaso}). Primero revisa
   * que no deje al propio rey en jaque. Luego quita el peón rival, que <b>no está en el
   * destino</b> sino a un lado (fila de origen, columna de destino), mueve el peón propio a la
   * casilla de destino, borra el derecho al paso y cierra la jugada (turno y fin de partida).
   *
   * @param origen casilla donde está el peón que captura
   * @param destino casilla a la que llega, la que el peón rival saltó
   * @return {@code true} si se hizo; {@code false} si dejaría al propio rey en jaque
   */
  private boolean hacerCapturaAlPaso(Casilla origen, Casilla destino){
    if (dejaReyEnJaque(origen, destino)){
      return false;
    }
    tablero.getCasilla(origen.getFila(), destino.getColumna()).setPieza(null);
    tablero.moverPieza(origen, destino);
    objetivoAlPaso = null;
    turno = turno.opuesto();
    verificarFinDePartida();
    return true;
  }

  /**
   * Devuelve la fila donde un peón de ese color se corona: la 0 (fila 8 del tablero) para las
   * blancas y la 7 (fila 1) para las negras, porque cada bando avanza hacia el lado contrario.
   *
   * @param color el bando del peón
   * @return la fila de la matriz donde se corona
   */
  private int filaDeCoronacion(Color color){
    return color == Color.BLANCO ? 0 : 7;
  }

  /**
   * Termina una coronación: reemplaza el peón que está en la última fila por una pieza nueva del
   * mismo color, y recién entonces cierra la jugada (cambia el turno y revisa si la partida
   * terminó, porque la pieza nueva puede dar jaque o jaque mate).
   *
   * <p>Solo se puede escoger dama, torre, alfil o caballo. Si no hay coronación pendiente, o el
   * tipo es peón, rey o {@code null}, no hace nada y devuelve {@code false}, para que la interfaz
   * vuelva a preguntar.</p>
   *
   * @param tipo la pieza en la que se convierte el peón
   * @return {@code true} si se coronó; {@code false} si no había coronación o el tipo no vale
   */
  public boolean coronacion(TipoPieza tipo){
    if (coronacionPendiente == null || tipo == null){
      return false;
    }
    Pieza nueva;
    switch (tipo){
      case DAMA:    nueva = new Dama(turno);    break;
      case TORRE:   nueva = new Torre(turno);   break;
      case ALFIL:   nueva = new Alfil(turno);   break;
      case CABALLO: nueva = new Caballo(turno); break;
      default:      return false;
    }
    nueva.marcarComoMovida();
    tablero.colocarPieza(nueva, coronacionPendiente.getFila(), coronacionPendiente.getColumna());
    coronacionPendiente = null;
    turno = turno.opuesto();
    verificarFinDePartida();
    return true;
  }

  /**
   * Revisa si una jugada dejaría al propio rey en jaque, sin hacerla de verdad. Simula el
   * movimiento en el tablero, revisa si el rey quedó atacado y deshace todo para dejar el
   * tablero exactamente como estaba: la pieza vuelve al origen, la pieza capturada (si había)
   * vuelve al destino, y la marca de "ya se movió" recupera su valor anterior.
   *
   * <p>Con esto se cubren a la vez tres reglas: no se puede mover una pieza clavada, el rey no
   * puede ir a una casilla atacada, y estando en jaque solo valen las jugadas que lo resuelven.</p>
   *
   * @param origen casilla donde está la pieza
   * @param destino casilla a donde iría
   * @return {@code true} si después de la jugada el rey propio quedaría en jaque
   */
  private boolean dejaReyEnJaque(Casilla origen, Casilla destino){
    Pieza pieza = origen.getPieza();
    Pieza capturada = destino.getPieza();
    boolean piezaMovidaAntes = pieza.seHaMovido();

    Casilla casillaPeonAlPaso = null;
    Pieza peonAlPaso = null;
    if (esCapturaAlPaso(origen, destino)){
      casillaPeonAlPaso = tablero.getCasilla(origen.getFila(), destino.getColumna());
      peonAlPaso = casillaPeonAlPaso.getPieza();
      casillaPeonAlPaso.setPieza(null);
    }

    tablero.moverPieza(origen, destino);
    boolean enJaque = estaEnJaque(pieza.getColor());

    origen.setPieza(pieza);
    pieza.setCasilla(origen);
    pieza.setSeHaMovido(piezaMovidaAntes);
    destino.setPieza(capturada);
    if (capturada != null){
      capturada.setCasilla(destino);
    }
    if (casillaPeonAlPaso != null){
      casillaPeonAlPaso.setPieza(peonAlPaso);
    }
    return enJaque;
  }

    /**
     * Indica si el rey de un bando está en jaque: busca al rey y revisa si alguna pieza rival
     * puede moverse a su casilla.
     *
     * @param color el bando cuyo rey se revisa
     * @return {@code true} si ese rey está atacado
     */
    public boolean estaEnJaque(Color color){
      Casilla casillaRey = buscarRey(color);
      if (casillaRey == null){
        return false;
      }
      for (int f = 0; f < 8; f++){
        for (int c = 0; c < 8; c++){
          Casilla casilla = tablero.getCasilla(f,c);
          Pieza pieza = casilla.getPieza();
          if (pieza == null || pieza.getColor() == color){
            continue;
          }
          if (pieza.getMovimientosPosibles(tablero).contains(casillaRey)){
            return true;
          }
        }
      }
      return false;
    }

    /**
     * Busca en el tablero la casilla donde está el rey de un bando.
     *
     * @param color el bando del rey que se busca
     * @return la casilla del rey, o {@code null} si no lo encuentra
     */
    private Casilla buscarRey(Color color){
      for (int f =0; f < 8; f++){
        for (int c = 0; c < 8; c++){
          Pieza pieza = tablero.getCasilla(f,c).getPieza();
          if (pieza != null && pieza.getTipo() == TipoPieza.REY && pieza.getColor() == color){
            return pieza.getCasilla();
          }
        }
      }
      return null;
    }

     /**
      * Indica si el jugador en turno tiene al menos una jugada legal. Recorre todas sus piezas y
      * todos sus movimientos posibles, y se detiene en el primero que no deje a su rey en jaque.
      *
      * @return {@code true} si existe al menos una jugada legal
      */
     public boolean hayMovimientosLegales(){
      for (int f = 0; f < 8; f++){
        for (int c = 0; c < 8; c++){
          Casilla origen = tablero.getCasilla(f,c);
          Pieza pieza = origen.getPieza();
          if (pieza == null || pieza.getColor() != turno){
            continue;
          }
          for(Casilla destino : pieza.getMovimientosPosibles(tablero)){
            if (!dejaReyEnJaque(origen, destino)){
              return true;
            }
          }
          if (objetivoAlPaso != null && esCapturaAlPaso(origen, objetivoAlPaso)
              && !dejaReyEnJaque(origen, objetivoAlPaso)){
            return true;
          }
        }
      }
      return false;
     }
     /**
      * Revisa, después de cada jugada, si el jugador que sigue ya no tiene jugadas legales. Si
      * además está en jaque, es <b>jaque mate</b> y gana el rival; si no está en jaque, es
      * <b>rey ahogado</b> y la partida termina en tablas.
      */
     private void verificarFinDePartida(){
      if(estaEnJaque(turno) && !hayMovimientosLegales()){
        terminado = true;
        resultado = "Jaque mate. Ganan " + turno.opuesto();
      }
      else if(!estaEnJaque(turno) && !hayMovimientosLegales()){
        terminado = true;
        resultado = " Ahogado (tablas)";
      }
     }
     /**
      * Revisa si mover de {@code origen} a {@code destino} sería un enroque válido, <b>sin
      * hacerlo</b>. El enroque se indica moviendo el rey dos casillas hacia la torre. Verifica, en
      * este orden:
      * <ol>
      *   <li>Que la pieza sea el rey y que no se haya movido nunca.</li>
      *   <li>Que vaya dos columnas hacia un lado, en la misma fila, a una casilla vacía.</li>
      *   <li>Que el rey no esté en jaque.</li>
      *   <li>Que en la esquina de ese lado esté una torre que tampoco se haya movido.</li>
      *   <li>Que todas las casillas entre el rey y la torre estén vacías.</li>
      *   <li>Que el rey no pase por una casilla atacada ni termine en una.</li>
      * </ol>
      * Está separado de {@link #intentarEnroque} para que la vista pueda marcar el enroque como
      * jugada posible antes de que el jugador lo haga.
      *
      * @param origen casilla donde está la pieza
      * @param destino casilla a donde se quiere mover
      * @return {@code true} si es un enroque que cumple todas las condiciones
      */
private boolean esEnroqueValido(Casilla origen, Casilla destino){
    Pieza rey = origen.getPieza();
    if (rey == null || rey.getTipo() != TipoPieza.REY){
        return false;
    }
    if (rey.seHaMovido()){
        return false;
    }
    if (origen.getFila() != destino.getFila()){
        return false;
    }
    if (Math.abs(destino.getColumna() - origen.getColumna()) != 2){
        return false;
    }
    if (estaEnJaque(turno)){
        return false;
    }
    if (!destino.estaVacia()){
        return false;
    }

    int fila = origen.getFila();
    boolean corto = destino.getColumna() > origen.getColumna();
    int colTorre = corto ? 7 : 0;
    Casilla casillaTorre = tablero.getCasilla(fila, colTorre);
    if (casillaTorre == null || casillaTorre.estaVacia()){
        return false;
    }

    Pieza torre = casillaTorre.getPieza();
    if (torre.getTipo() != TipoPieza.TORRE || torre.seHaMovido()){
        return false;
    }

    int paso = corto ? 1 : -1;
    for (int c = origen.getColumna() + paso; c != colTorre; c += paso){
        if (!tablero.getCasilla(fila, c).estaVacia()){
            return false;
        }
    }

    for (int i = 1; i <= 2; i++){
        Casilla pasoRey = tablero.getCasilla(fila, origen.getColumna() + paso * i);
        if (tablero.casillaAtacada(pasoRey, turno.opuesto())){
            return false;
        }
    }

    return true;
}

     /**
      * Si la jugada es un enroque válido ({@link #esEnroqueValido}), lo hace: mueve el rey dos
      * casillas y pone la torre en la casilla que el rey atravesó (enroque corto hacia la columna
      * h, largo hacia la columna a). Después cambia el turno y revisa si la partida terminó.
      *
      * @param origen casilla donde está la pieza
      * @param destino casilla a donde se quiere mover
      * @return {@code true} si era un enroque válido y se hizo; {@code false} en otro caso
      */
public boolean intentarEnroque(Casilla origen, Casilla destino){
    if (!esEnroqueValido(origen, destino)){
        return false;
    }
    int fila = origen.getFila();
    boolean corto = destino.getColumna() > origen.getColumna();
    int paso = corto ? 1 : -1;
    Casilla casillaTorre = tablero.getCasilla(fila, corto ? 7 : 0);
    tablero.moverPieza(origen, destino);
    tablero.moverPieza(casillaTorre, tablero.getCasilla(fila, origen.getColumna() + paso));
    turno = turno.opuesto();
    verificarFinDePartida();
    return true;
}

  /**
   * Devuelve las casillas a las que la pieza de {@code origen} puede ir <b>legalmente</b> en
   * esta jugada: sus movimientos posibles que no dejan al propio rey en jaque, más la captura
   * al paso y el enroque si están disponibles. Es la misma lista de reglas que usa
   * {@link #mover}, pero sin mover nada: la vista la usa para marcar en el tablero a dónde se
   * puede jugar.
   *
   * <p>Va en {@code Juego} y no en la vista porque es <b>Experto en información</b> (GRASP):
   * solo {@code Juego} sabe de quién es el turno, cuál fue el doble paso anterior y cómo se
   * revisa el jaque.</p>
   *
   * @param origen casilla donde está la pieza
   * @return las casillas de destino legales; vacía si no hay pieza del jugador en turno, si la
   *         partida terminó o si hay una coronación pendiente
   */
  public List<Casilla> movimientosLegales(Casilla origen){
    List<Casilla> legales = new ArrayList<>();
    if (terminado || coronacionPendiente != null || origen == null){
      return legales;
    }
    Pieza pieza = origen.getPieza();
    if (pieza == null || pieza.getColor() != turno){
      return legales;
    }
    for (Casilla destino : pieza.getMovimientosPosibles(tablero)){
      if (!dejaReyEnJaque(origen, destino)){
        legales.add(destino);
      }
    }
    if (objetivoAlPaso != null && esCapturaAlPaso(origen, objetivoAlPaso)
        && !dejaReyEnJaque(origen, objetivoAlPaso)){
      legales.add(objetivoAlPaso);
    }
    if (pieza.getTipo() == TipoPieza.REY){
      int fila = origen.getFila();
      int col = origen.getColumna();
      if (col + 2 < 8 && esEnroqueValido(origen, tablero.getCasilla(fila, col + 2))){
        legales.add(tablero.getCasilla(fila, col + 2));
      }
      if (col - 2 >= 0 && esEnroqueValido(origen, tablero.getCasilla(fila, col - 2))){
        legales.add(tablero.getCasilla(fila, col - 2));
      }
    }
    return legales;
  }
  }