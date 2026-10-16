package ajedrez;

import java.util.List;
import java.util.Scanner;


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
   * @param origen casilla donde está la pieza que se quiere mover
   * @param destino casilla a donde se quiere mover
   * @return {@code true} si la jugada era legal y se hizo; {@code false} si no se hizo
   */
  public boolean mover(Casilla origen, Casilla destino){
    if (terminado){
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
      return true;
    }
    //Revisar si es geometricamente posible
    List<Casilla> posibles = pieza.getMovimientosPosibles(tablero);
    if (!posibles.contains(destino)){
      return false;
    }
    //Revisar si deja al propio rey en jaque (simula y deshace sin dejar rastro)
    if (dejaReyEnJaque(origen, destino)){
      return false;
    }
    //Movimiento legal: ahora si se hace de verdad (esto si marca la pieza como movida)
    tablero.moverPieza(origen, destino);
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
  //Simula el movimiento, mira si el rey propio queda en jaque y deja el tablero EXACTAMENTE como estaba
  private boolean dejaReyEnJaque(Casilla origen, Casilla destino){
    Pieza pieza = origen.getPieza();
    Pieza capturada = destino.getPieza();
    boolean piezaMovidaAntes = pieza.seHaMovido();   // guardar estado antes de simular

    tablero.moverPieza(origen, destino);              // simulacion (marca la pieza como movida)
    boolean enJaque = estaEnJaque(pieza.getColor());

    //Deshacer: piezas, casillas y la bandera seHaMovido
    origen.setPieza(pieza);
    pieza.setCasilla(origen);
    pieza.setSeHaMovido(piezaMovidaAntes);
    destino.setPieza(capturada);
    if (capturada != null){
      capturada.setCasilla(destino);
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
    //El rey del color en turno esta atacado por alguna pieza enemiga
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
    //Se revisa si el jugador del turno actual tiene movimientos legales 
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
      * Revisa si la jugada es un enroque y, si lo es y cumple todas las condiciones, lo hace.
      * El enroque se indica moviendo el rey dos casillas hacia la torre. Verifica, en este orden:
      * <ol>
      *   <li>Que la pieza sea el rey y que no se haya movido nunca.</li>
      *   <li>Que vaya dos columnas hacia un lado, en la misma fila.</li>
      *   <li>Que el rey no esté en jaque.</li>
      *   <li>Que en la esquina de ese lado esté una torre que tampoco se haya movido.</li>
      *   <li>Que todas las casillas entre el rey y la torre estén vacías.</li>
      *   <li>Que el rey no pase por una casilla atacada ni termine en una.</li>
      * </ol>
      * Si cumple todo, mueve el rey dos casillas y pone la torre en la casilla que el rey
      * atravesó: enroque corto hacia la columna h, largo hacia la columna a.
      *
      * @param origen casilla donde está la pieza
      * @param destino casilla a donde se quiere mover
      * @return {@code true} si era un enroque válido y se hizo; {@code false} en otro caso
      */
     public boolean intentarEnroque(Casilla origen, Casilla destino){
             System.out.println(">>> intentarEnroque " + origen + " -> " + destino);
        Pieza rey = origen.getPieza();
        if(rey == null || rey.getTipo() != TipoPieza.REY){
            System.out.println("    corta: no es rey");
            return false;
        }
        if (rey.seHaMovido()){
            System.out.println("    corta: rey ya movido");
            return false;
        }
        if (origen.getFila() != destino.getFila()){
            System.out.println("    corta: filas distintas");
            return false;
        }
        if(Math.abs(destino.getColumna() - origen.getColumna()) != 2){
            System.out.println("    corta: distancia != 2 (es " + Math.abs(destino.getColumna() - origen.getColumna()) + ")");
            return false;
        }
        if(estaEnJaque(turno)){
            System.out.println("    corta: rey en jaque");
            return false;
        }
        if(!destino.estaVacia()){
            System.out.println("    corta: destino ocupado");
            return false;
        }

        int fila = origen.getFila();
        boolean corto = destino.getColumna() > origen.getColumna();
        int colTorre = corto ? 7 : 0;
        Casilla casillaTorre = tablero.getCasilla(fila, colTorre);
        if (casillaTorre == null || casillaTorre.estaVacia()){
            System.out.println("    corta: casilla torre vacia/null");
            return false;
        }
        Pieza torre = casillaTorre.getPieza();
        if(torre.getTipo() != TipoPieza.TORRE || torre.seHaMovido()){
            System.out.println("    corta: torre no valida o movida (tipo=" + torre.getTipo() + " movida=" + torre.seHaMovido() + ")");
            return false;
        }

        int paso = corto ? 1 : -1;
        for (int c = origen.getColumna() + paso; c != colTorre; c += paso){
            if (!tablero.getCasilla(fila, c).estaVacia()){
                System.out.println("    corta: casilla intermedia ocupada en col " + c);
                return false;
            }
        }

        for (int i = 1; i <= 2; i++){
            Casilla pasoRey = tablero.getCasilla(fila, origen.getColumna() + paso * i);
            if (tablero.casillaAtacada(pasoRey, turno.opuesto())){
                System.out.println("    corta: paso del rey atacado en col " + (origen.getColumna() + paso * i));
                return false;
            }
        }

        System.out.println("    ENROQUE APLICADO");
        tablero.moverPieza(origen, destino);
        tablero.moverPieza(casillaTorre, tablero.getCasilla(fila, origen.getColumna() + paso));
        turno = turno.opuesto();
        verificarFinDePartida();
        return true;
     }
  }
