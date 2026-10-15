package ajedrez;

/**
 *
 * @author juanj
 */
public abstract class Pieza {

  protected Color color;
  protected Casilla casilla;
  protected boolean seHaMovido;

  public Pieza(Color color){
    this.color = color;
    this.seHaMovido = false;
  }

  public Color getColor(){
    return color;
  }

  public Casilla getCasilla(){
    return casilla;
  }

  public void setCasilla(Casilla casilla){
    this.casilla = casilla;
  }

  public boolean seHaMovido(){
    return seHaMovido;
  }

  public void marcarComoMovida(){
    this.seHaMovido = true;
  }

  public abstract TipoPieza getTipo();

  /** Casillas que esta pieza ataca (para jaque/enroque). Por defecto = movimientos. */
    public java.util.List<Casilla> casillasAtacadas(Tablero t){
        return getMovimientosPosibles(t);
    }


  public abstract java.util.List<Casilla> getMovimientosPosibles(Tablero tablero);

  @Override
  public String toString(){
    return color + " " + getTipo();
  }

  protected void agregarSiValida(java.util.List<Casilla> lista, Tablero t, int f, int c){
    Casilla destino = t.getCasilla(f,c);
    if(destino == null){
      return;
    }
    if(destino.estaVacia() || destino.getPieza().getColor() != this.color){
      lista.add(destino);
    }
  }

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

  public void setSeHaMovido(boolean v){
    this.seHaMovido = v;
  }
}
