package interfaz;

import mundo.Pieza;
import mundo.Tablero;
import mundo.Color;

/**
 * Dibuja el tablero en la consola. Antes esto lo hacía {@code Tablero.imprimir()}, pero cómo
 * se ve el tablero es un asunto de la interfaz, no de las reglas del ajedrez: una ventana
 * gráfica lo dibujaría de otra forma y {@link Tablero} no tendría por qué enterarse.
 *
 * <p>Solo <em>lee</em> el tablero (con {@code getCasilla}); nunca lo modifica.</p>
 *
 * @author Juan José Betancourt (67001968)
 * @author Santiago Suárez Alfonso (67001862)
 */
public class VistaConsola {

    /**
     * Imprime el tablero en la consola con las letras de las columnas (a–h) y los números de las
     * filas (8 arriba, 1 abajo). Cada pieza se muestra con su letra: mayúscula si es blanca,
     * minúscula si es negra, y un punto si la casilla está vacía.
     *
     * @param t el tablero que se dibuja
     */
    public static void imprimirTablero(Tablero t) {
        System.out.println("    a  b  c  d  e  f  g  h");
        for (int f = 0; f < 8; f++) {
            System.out.print((8 - f) + " ");
            for (int c = 0; c < 8; c++) {
                Pieza p = t.getCasilla(f, c).getPieza();
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
    private static String simbolo(Pieza p) {
        if (p == null) return ".";
        String s;
        switch (p.getTipo()) {
            case PEON:    s = "P"; break;
            case TORRE:   s = "T"; break;
            case CABALLO: s = "C"; break;
            case ALFIL:   s = "A"; break;
            case DAMA:    s = "D"; break;
            case REY:     s = "R"; break;
            default:      s = "?"; break;
        }
        return p.getColor() == Color.BLANCO ? s : s.toLowerCase();
    }
}
