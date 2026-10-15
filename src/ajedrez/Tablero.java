package ajedrez;

/**
 *
 * @author juanj
 */
public class Tablero {

  private final Casilla[][] casillas = new Casilla[8][8];

  public Tablero(){
    for(int f=0; f < 8; f++){
      for(int c =0; c < 8; c++){
        casillas[f][c] = new Casilla(f,c);
      }
    }
  }

  public Casilla getCasilla(int f, int c){
    if (f < 0 || f > 7 || c < 0 || c > 7) return null;
    return casillas [f][c];
  }

  public void colocarPieza(Pieza pieza, int f, int c){
    Casilla casilla = getCasilla(f,c);
    casilla.setPieza(pieza);
    pieza.setCasilla(casilla);
  }

  public void moverPieza(Casilla origen, Casilla destino){
    Pieza pieza = origen.getPieza();
    origen.setPieza(null);
    destino.setPieza(pieza);
    pieza.setCasilla(destino);
    pieza.marcarComoMovida();
  }

   public void configurarInicial() {
        // Piezas negras (fila 0)
        colocarPieza(new Torre(Color.NEGRO),  0, 0);
        colocarPieza(new Caballo(Color.NEGRO),0, 1);
        colocarPieza(new Alfil(Color.NEGRO),  0, 2);
        colocarPieza(new Dama(Color.NEGRO),  0, 3);
        colocarPieza(new Rey(Color.NEGRO),    0, 4);
        colocarPieza(new Alfil(Color.NEGRO),  0, 5);
        colocarPieza(new Caballo(Color.NEGRO),0, 6);
        colocarPieza(new Torre(Color.NEGRO),  0, 7);
        for (int c = 0; c < 8; c++) colocarPieza(new Peon(Color.NEGRO), 1, c);

        // Piezas blancas (fila 7)
        colocarPieza(new Torre(Color.BLANCO),  7, 0);
        colocarPieza(new Caballo(Color.BLANCO),7, 1);
        colocarPieza(new Alfil(Color.BLANCO),  7, 2);
        colocarPieza(new Dama(Color.BLANCO),  7, 3);
        colocarPieza(new Rey(Color.BLANCO),    7, 4);
        colocarPieza(new Alfil(Color.BLANCO),  7, 5);
        colocarPieza(new Caballo(Color.BLANCO),7, 6);
        colocarPieza(new Torre(Color.BLANCO),  7, 7);
        for (int c = 0; c < 8; c++) colocarPieza(new Peon(Color.BLANCO), 6, c);
    }

    /** Imprime el tablero con coordenadas y símbolos de pieza. */
    public void imprimir() {
        System.out.println("    a  b  c  d  e  f  g  h");
        for (int f = 0; f < 8; f++) {
            System.out.print((8 - f) + " ");
            for (int c = 0; c < 8; c++) {
                Pieza p = casillas[f][c].getPieza();
                System.out.print(" " + simbolo(p) + " ");
            }
            System.out.println(" " + (8 - f));
        }
        System.out.println("    a  b  c  d  e  f  g  h");
    }

    private String simbolo(Pieza p) {
        if (p == null) return ".";
        String s;
        switch (p.getTipo()) {
            case PEON:    s = "P"; break;
            case TORRE:   s = "T"; break;
            case CABALLO: s = "C"; break;
            case ALFIL:   s = "A"; break;
            case DAMA:   s = "D"; break;
            case REY:     s = "R"; break;
            default:      s = "?"; break;
        }
        return p.getColor() == Color.BLANCO ? s : s.toLowerCase();
    }

    public boolean casillaAtacada(Casilla casilla, Color porQuien){
      for (int f = 0; f < 8; f++){
        for (int c = 0; c <8; c++){
          Pieza pieza = casillas[f][c].getPieza();
          if(pieza != null && pieza.getColor() == porQuien && pieza.casillasAtacadas(this).contains(casilla)){
            System.out.println("   ATACA a " + casilla + " la pieza " + pieza + " en (" + f + "," + c + ")");
            return true;
          }
        }
      }
      return false;
    }

}
