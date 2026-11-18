package interfaz;

import java.util.Scanner;
import mundo.Casilla;
import mundo.Juego;
import mundo.Tablero;
import mundo.TipoPieza;

/**
 * Clase principal. Maneja la partida en la consola: imprime el tablero, avisa de quién es el
 * turno y si hay jaque, lee la jugada del jugador y se la pasa a {@link mundo.Juego}.
 *
 * @author Juan José Betancourt (67001968)
 * @author Santiago Suárez Alfonso (67001862)
 */
public class Ajedrez {

    /**
     * Arranca una partida para dos jugadores en la consola. En cada turno pide la jugada como
     * casilla de origen y de destino separadas por un espacio (por ejemplo {@code e2 e4}).
     * Verifica que vengan exactamente dos casillas y que existan en el tablero; si la jugada no
     * es legal, avisa y vuelve a pedirla. La partida sigue hasta que termina o hasta que alguien
     * escribe {@code salir}, y al final imprime el resultado.
     *
     * @param args no se usan
     */
    public static void main(String[] args) {
        Juego juego = new Juego();
        Scanner sc = new Scanner(System.in);

        while (!juego.isTerminado()) {
            VistaConsola.imprimirTablero(juego.getTablero());
            System.out.println("Turno: " + juego.getTurno());
            if (juego.estaEnJaque(juego.getTurno())) System.out.println("¡JAQUE!");
            System.out.print("Movimiento (ej. e2 e4) o 'salir': ");
            String linea = sc.nextLine().trim().toLowerCase();
            if (linea.equals("salir")) break;

            String[] partes = linea.split("\\s+");
            if (partes.length != 2) { System.out.println("Formato: origen destino"); continue; }

            Casilla o = parsearCasilla(juego.getTablero(), partes[0]);
            Casilla d = parsearCasilla(juego.getTablero(), partes[1]);
            if (o == null || d == null) { System.out.println("Casilla inválida."); continue; }

            if (!juego.mover(o, d)) { System.out.println("Movimiento ilegal."); continue; }

            if (juego.hayCoronacionPendiente()) {
                VistaConsola.imprimirTablero(juego.getTablero());
                TipoPieza elegida = pedirPiezaCoronacion(sc);
                if (elegida == null) break;
                juego.coronacion(elegida);
            }
        }

        VistaConsola.imprimirTablero(juego.getTablero());
        if (juego.getResultado() != null) System.out.println(juego.getResultado());
    }

    /**
     * Le pregunta al jugador en qué pieza quiere convertir su peón y no se detiene hasta que
     * escriba una opción válida. Preguntar es leer el teclado, por eso vive aquí y no en
     * {@link Juego}: las reglas solo reciben la respuesta.
     *
     * @param sc el lector del teclado
     * @return la pieza elegida, o {@code null} si el jugador escribió {@code salir}
     */
    private static TipoPieza pedirPiezaCoronacion(Scanner sc) {
        while (true) {
            System.out.print("¡Coronación! D (dama), T (torre), A (alfil), C (caballo) o 'salir': ");
            String r = sc.nextLine().trim().toLowerCase();
            switch (r) {
                case "d": return TipoPieza.DAMA;
                case "t": return TipoPieza.TORRE;
                case "a": return TipoPieza.ALFIL;
                case "c": return TipoPieza.CABALLO;
                case "salir": return null;
                default: System.out.println("Opción inválida.");
            }
        }
    }

    /**
     * Convierte una casilla escrita en notación algebraica (por ejemplo {@code e2}) en la
     * casilla del tablero. La letra da la columna ({@code a} = 0 … {@code h} = 7) y el número da
     * la fila contando desde arriba ({@code 8} = 0 … {@code 1} = 7).
     *
     * @param t el tablero de la partida
     * @param s la casilla escrita, de dos caracteres
     * @return la casilla, o {@code null} si el texto no tiene dos caracteres o cae fuera del tablero
     */
    private static Casilla parsearCasilla(Tablero t, String s) {
        if (s.length() != 2) {
          return null;
        }
        int c = s.charAt(0) - 'a';
        int f = 8 - (s.charAt(1) - '0');
        return t.getCasilla(f, c);
    }
}
