package controlador;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import modelo.Casilla;
import modelo.Color;
import modelo.Juego;
import modelo.Pieza;
import modelo.TipoPieza;
import vista.VentanaAjedrez;

/**
 * El intermediario entre la ventana y las reglas del ajedrez.
 *
 * <p>Es el <b>controlador</b> del patrón MVC y también el patrón <b>Controlador</b> de GRASP.
 * Con la analogía del restaurante: la ventana es la carta, este objeto es el mesero y
 * {@link Juego} es la cocina. El mesero recibe el pedido (el clic), se lo lleva a la cocina
 * (le pide la jugada a {@code Juego}) y vuelve con el plato (le dice a la ventana qué mostrar).
 * No cocina: si una jugada es legal lo decide el modelo.</p>
 *
 * <p>Implementa la interfaz {@link ActionListener}: así la ventana le puede avisar los clics
 * sin conocer esta clase. Una jugada se hace con dos clics: el primero escoge una pieza propia
 * y el segundo, la casilla a donde va.</p>
 *
 * @author Juan José Betancourt (67001968)
 * @author Santiago Suárez Alfonso (67001862)
 */
public class ControladorJuego implements ActionListener {

    /** El modelo: la partida que se está jugando. */
    private Juego juego;
    /** La vista: la ventana del juego. */
    private final VentanaAjedrez vista;
    /** Casilla de la pieza que se escogió con el primer clic, o {@code null} si no hay. */
    private Casilla seleccionada;

    /**
     * Une el modelo con la vista: se registra en la ventana para recibir los clics y la pinta
     * por primera vez.
     *
     * @param juego la partida
     * @param vista la ventana
     */
    public ControladorJuego(Juego juego, VentanaAjedrez vista) {
        this.juego = juego;
        this.vista = vista;
        this.seleccionada = null;
        vista.agregarControlador(this);
        actualizarVista();
    }

    /**
     * Lo llama Swing cada vez que se pulsa un botón de la ventana. Si es «Nueva partida»,
     * reinicia; si es una casilla, saca la fila y la columna del comando (por ejemplo
     * {@code "6,4"}) y atiende el clic.
     *
     * @param e el evento del clic
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        String comando = e.getActionCommand();
        if (comando.equals("nueva")) {
            nuevaPartida();
        } else {
            String[] partes = comando.split(",");
            int fila = Integer.parseInt(partes[0]);
            int columna = Integer.parseInt(partes[1]);
            casillaPulsada(fila, columna);
        }
    }

    /**
     * Atiende el clic en una casilla.
     * <ul>
     *   <li>Si el clic es en una pieza del jugador en turno, la escoge.</li>
     *   <li>Si ya había una escogida y el clic es en otra casilla, intenta la jugada.</li>
     * </ul>
     * Si la jugada lleva un peón a la última fila, pregunta en qué pieza corona. Si la partida
     * termina, muestra el resultado.
     *
     * @param fila fila de la casilla (0 es la fila 8 del tablero)
     * @param columna columna de la casilla (0 es la columna a)
     */
    private void casillaPulsada(int fila, int columna) {
        if (juego.isTerminado()) {
            return;
        }
        Casilla casilla = juego.getTablero().getCasilla(fila, columna);
        Pieza pieza = casilla.getPieza();

        if (pieza != null && pieza.getColor() == juego.getTurno()) {
            seleccionada = casilla;
        } else if (seleccionada != null) {
            boolean hecha = juego.mover(seleccionada, casilla);
            seleccionada = null;
            if (hecha && juego.hayCoronacionPendiente()) {
                actualizarVista();
                TipoPieza elegida = vista.pedirPiezaCoronacion();
                juego.coronacion(elegida);
            }
        }
        actualizarVista();
        if (juego.isTerminado()) {
            vista.mostrarMensaje(textoEstado());
        }
    }

    /** Empieza una partida nueva: crea otro {@link Juego} y borra la selección. */
    private void nuevaPartida() {
        juego = new Juego();
        seleccionada = null;
        actualizarVista();
    }

    /**
     * Le dice a la vista qué mostrar: el tablero, la pieza escogida con sus destinos legales
     * (que los calcula el modelo) y el texto de estado.
     */
    private void actualizarVista() {
        List<Casilla> destinos = new ArrayList<>();
        if (seleccionada != null) {
            destinos = juego.movimientosLegales(seleccionada);
        }
        vista.mostrarTablero(juego.getTablero(), seleccionada, destinos);
        vista.mostrarEstado(textoEstado());
    }

    /**
     * Arma el texto de estado según cómo va la partida.
     *
     * @return el turno, el aviso de jaque o el resultado final
     */
    private String textoEstado() {
        Color turno = juego.getTurno();
        String nombre = (turno == Color.BLANCO) ? "las blancas" : "las negras";
        String rival = (turno == Color.BLANCO) ? "las negras" : "las blancas";
        if (juego.isTerminado()) {
            if (juego.estaEnJaque(turno)) {
                return "Jaque mate: ganan " + rival;
            }
            return "Rey ahogado: tablas";
        }
        if (juego.estaEnJaque(turno)) {
            return "¡Jaque! Juegan " + nombre;
        }
        return "Turno de " + nombre;
    }
}
