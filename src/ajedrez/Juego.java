/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ajedrez;

/**
 *
 * @author juanj
 */
import java.util.List;
import java.util.Scanner;


public class Juego {

  private final Tablero tablero;
  private Color turno;
  private boolean terminado;
  private String resultado;

  public Juego(){
    this.tablero = new Tablero();
    this.tablero.configurarInicial();
    this.turno = Color.BLANCO;
    this.terminado = false;
  }

  public Tablero getTablero(){
    return tablero;
  }

  public Color getTurno(){
    return turno;
  }

  public boolean isTerminado(){
    return terminado;
  }

  public String getResultado(){
    return resultado;
  }

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
