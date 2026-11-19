package vista;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import modelo.Casilla;
import modelo.Pieza;
import modelo.Tablero;
import modelo.TipoPieza;

/**
 * La ventana del juego: un tablero de 8 × 8 botones, una etiqueta arriba con el estado de la
 * partida y un botón para empezar de nuevo.
 *
 * <p>Es la <b>vista</b> del patrón MVC (la carta del restaurante). Solo muestra y pregunta: no
 * sabe de reglas ni decide nada. Cuando el jugador hace clic, el botón le avisa al controlador,
 * que es el {@link ActionListener} que se registra con {@link #agregarControlador}.</p>
 *
 * @author Juan José Betancourt (67001968)
 * @author Santiago Suárez Alfonso (67001862)
 */
public class VentanaAjedrez extends JFrame {

    /** Color de las casillas claras. */
    private static final Color CLARA = new Color(240, 217, 181);
    /** Color de las casillas oscuras. */
    private static final Color OSCURA = new Color(181, 136, 99);
    /** Color de la casilla de la pieza seleccionada. */
    private static final Color SELECCION = new Color(246, 246, 105);
    /** Color de las casillas a las que puede ir la pieza seleccionada. */
    private static final Color DESTINO = new Color(130, 190, 110);

    /** Las 64 casillas del tablero; cada una es un botón. */
    private final JButton[][] botones;
    /** Texto con el estado de la partida: turno, jaque o resultado. */
    private final JLabel lblEstado;
    /** Botón para empezar una partida nueva. */
    private final JButton btnNuevaPartida;

    /**
     * Arma la ventana. Crea los 64 botones en una rejilla de 8 × 8 y a cada uno le pone como
     * «comando» su fila y su columna (por ejemplo {@code "6,4"}), para que el controlador sepa
     * cuál se pulsó.
     */
    public VentanaAjedrez() {
        setTitle("Ajedrez — DPOO");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        lblEstado = new JLabel("Turno de las blancas", JLabel.CENTER);
        lblEstado.setFont(new Font("SansSerif", Font.BOLD, 18));
        btnNuevaPartida = new JButton("Nueva partida");
        btnNuevaPartida.setActionCommand("nueva");

        JPanel panelArriba = new JPanel(new BorderLayout());
        panelArriba.add(lblEstado, BorderLayout.CENTER);
        panelArriba.add(btnNuevaPartida, BorderLayout.EAST);
        add(panelArriba, BorderLayout.NORTH);

        JPanel panelTablero = new JPanel(new GridLayout(8, 8));
        botones = new JButton[8][8];
        for (int f = 0; f < 8; f++) {
            for (int c = 0; c < 8; c++) {
                JButton boton = new JButton();
                boton.setFont(new Font("SansSerif", Font.PLAIN, 40));
                boton.setOpaque(true);
                boton.setBorderPainted(false);
                boton.setFocusPainted(false);
                boton.setActionCommand(f + "," + c);
                botones[f][c] = boton;
                panelTablero.add(boton);
            }
        }
        add(panelTablero, BorderLayout.CENTER);

        setSize(640, 680);
        setLocationRelativeTo(null);
    }

    /**
     * Conecta los botones con el controlador: desde aquí, cada clic en una casilla o en
     * «Nueva partida» llama a su método {@code actionPerformed}.
     *
     * @param controlador el objeto que atiende los clics
     */
    public void agregarControlador(ActionListener controlador) {
        for (int f = 0; f < 8; f++) {
            for (int c = 0; c < 8; c++) {
                botones[f][c].addActionListener(controlador);
            }
        }
        btnNuevaPartida.addActionListener(controlador);
    }

    /**
     * Pinta el tablero: pone en cada botón el símbolo de su pieza y su color de fondo. La
     * casilla seleccionada sale en amarillo y las casillas a las que puede ir, en verde.
     *
     * @param tablero el tablero de la partida (solo se lee)
     * @param seleccionada la casilla seleccionada, o {@code null} si no hay
     * @param destinos las casillas a las que puede ir la pieza seleccionada
     */
    public void mostrarTablero(Tablero tablero, Casilla seleccionada, List<Casilla> destinos) {
        for (int f = 0; f < 8; f++) {
            for (int c = 0; c < 8; c++) {
                Casilla casilla = tablero.getCasilla(f, c);
                JButton boton = botones[f][c];
                boton.setText(simbolo(casilla.getPieza()));
                if (casilla == seleccionada) {
                    boton.setBackground(SELECCION);
                } else if (destinos.contains(casilla)) {
                    boton.setBackground(DESTINO);
                } else if ((f + c) % 2 == 0) {
                    boton.setBackground(CLARA);
                } else {
                    boton.setBackground(OSCURA);
                }
            }
        }
    }

    /**
     * Cambia el texto de la etiqueta de estado.
     *
     * @param texto el turno, el aviso de jaque o el resultado
     */
    public void mostrarEstado(String texto) {
        lblEstado.setText(texto);
    }

    /**
     * Muestra un mensaje en una ventanita (por ejemplo, el resultado de la partida).
     *
     * @param texto el mensaje
     */
    public void mostrarMensaje(String texto) {
        JOptionPane.showMessageDialog(this, texto);
    }

    /**
     * Le pregunta al jugador en qué pieza quiere convertir su peón. Si cierra la ventanita sin
     * escoger, se queda con la dama.
     *
     * @return la pieza escogida
     */
    public TipoPieza pedirPiezaCoronacion() {
        String[] opciones = {"Dama", "Torre", "Alfil", "Caballo"};
        int respuesta = JOptionPane.showOptionDialog(this, "¿En qué pieza se convierte el peón?",
                "Coronación", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, opciones, opciones[0]);
        if (respuesta == 1) {
            return TipoPieza.TORRE;
        } else if (respuesta == 2) {
            return TipoPieza.ALFIL;
        } else if (respuesta == 3) {
            return TipoPieza.CABALLO;
        }
        return TipoPieza.DAMA;
    }

    /**
     * Devuelve el símbolo de ajedrez de una pieza: las blancas con el dibujo hueco (♔ ♕ ♖ ♗ ♘ ♙)
     * y las negras con el dibujo relleno (♚ ♛ ♜ ♝ ♞ ♟).
     *
     * @param pieza la pieza, o {@code null} si la casilla está vacía
     * @return el símbolo, o un texto vacío si no hay pieza
     */
    private String simbolo(Pieza pieza) {
        if (pieza == null) {
            return "";
        }
        boolean blanca = pieza.getColor() == modelo.Color.BLANCO;
        switch (pieza.getTipo()) {
            case REY:     return blanca ? "♔" : "♚";
            case DAMA:    return blanca ? "♕" : "♛";
            case TORRE:   return blanca ? "♖" : "♜";
            case ALFIL:   return blanca ? "♗" : "♝";
            case CABALLO: return blanca ? "♘" : "♞";
            default:      return blanca ? "♙" : "♟";
        }
    }
}
