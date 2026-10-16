package ajedrez;

/**
 * El tablero de 8×8. Crea sus 64 casillas al construirse y no existen fuera de él (composición).
 * Las piezas, en cambio, se crean aparte y se colocan en las casillas (agregación).
 *
 * <p>La matriz se recorre como {@code casillas[fila][columna]}. La fila 0 es la fila 8 del
 * ajedrez, donde empiezan las negras, y la fila 7 es la fila 1, donde empiezan las blancas.</p>
 *
 * @author Juan José Betancourt (67001968)
 * @author Santiago Suárez Alfonso (67001862)
 */
public class Tablero {

  /** Las 64 casillas del tablero, indexadas por fila y columna. */
  private final Casilla[][] casillas = new Casilla[8][8];

  /**
   * Crea el tablero con sus 64 casillas vacías, cada una con su fila y su columna.
   */
  public Tablero(){
    for(int f=0; f < 8; f++){
      for(int c =0; c < 8; c++){
        casillas[f][c] = new Casilla(f,c);
      }
    }
  }

  /**
   * Devuelve la casilla de la posición indicada. Verifica que la posición esté dentro del
   * tablero; si no lo está, devuelve {@code null}. Las piezas usan esto para saber cuándo
   * llegaron al borde.
   *
   * @param f fila, de 0 a 7
   * @param c columna, de 0 a 7
   * @return la casilla, o {@code null} si la posición está fuera del tablero
   */
  public Casilla getCasilla(int f, int c){
    if (f < 0 || f > 7 || c < 0 || c > 7) return null;
    return casillas [f][c];
  }

  /**
   * Pone una pieza en una casilla y le avisa a la pieza dónde quedó, para que la casilla y la
   * pieza se conozcan entre sí.
   *
   * @param pieza la pieza que se coloca
   * @param f fila de la casilla
   * @param c columna de la casilla
   */
  public void colocarPieza(Pieza pieza, int f, int c){
    Casilla casilla = getCasilla(f,c);
    casilla.setPieza(pieza);
    pieza.setCasilla(casilla);
  }

  /**
   * Lleva la pieza del origen al destino sin revisar si el movimiento es legal; eso ya lo
   * verificó {@link Juego}. Vacía el origen, pone la pieza en el destino (si había una pieza
   * rival, queda reemplazada, o sea capturada), actualiza la casilla de la pieza y la marca como
   * movida.
   *
   * @param origen casilla donde está la pieza
   * @param destino casilla a donde va
   */
  public void moverPieza(Casilla origen, Casilla destino){
    Pieza pieza = origen.getPieza();
    origen.setPieza(null);
    destino.setPieza(pieza);
    pieza.setCasilla(destino);
    pieza.marcarComoMovida();
  }

   /**
    * Coloca las 32 piezas en su posición inicial. Las negras van en las filas 0 y 1 de la
    * matriz (filas 8 y 7 del tablero) y las blancas en las filas 7 y 6 (filas 1 y 2). La dama
    * queda en la columna d, sobre la casilla de su propio color.
    */
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

    /**
     * Imprime el tablero en la consola con las letras de las columnas (a–h) y los números de las
     * filas (8 arriba, 1 abajo). Cada pieza se muestra con su letra: mayúscula si es blanca,
     * minúscula si es negra, y un punto si la casilla está vacía.
     */
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

    /**
     * Devuelve la letra con la que se dibuja una pieza: P peón, T torre, C caballo, A alfil,
     * D dama, R rey. En mayúscula para las blancas y en minúscula para las negras.
     *
     * @param p la pieza, o {@code null} si la casilla está vacía
     * @return la letra de la pieza, o {@code "."} si no hay pieza
     */
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

    /**
     * Indica si alguna pieza de un bando amenaza una casilla. Recorre todo el tablero y le
     * pregunta a cada pieza de ese color por sus {@link Pieza#casillasAtacadas}. Se usa en el
     * enroque, porque el rey no puede pasar por una casilla atacada.
     *
     * @param casilla la casilla que se quiere revisar
     * @param porQuien el bando cuyas piezas se revisan
     * @return {@code true} si al menos una pieza de ese bando ataca la casilla
     */
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
