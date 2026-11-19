package controlador;

import modelo.Juego;
import vista.VentanaAjedrez;

/**
 * Clase principal. Solo arma las tres partes del MVC y las conecta, igual que el {@code main}
 * de la nómina: crea el modelo ({@link Juego}), la vista ({@link VentanaAjedrez}) y el
 * controlador ({@link ControladorJuego}) que las une, y muestra la ventana.
 *
 * @author Juan José Betancourt (67001968)
 * @author Santiago Suárez Alfonso (67001862)
 */
public class Ajedrez {

    /**
     * Arranca el programa.
     *
     * @param args no se usan
     */
    public static void main(String[] args) {
        Juego juego = new Juego();
        VentanaAjedrez vista = new VentanaAjedrez();
        ControladorJuego controlador = new ControladorJuego(juego, vista);
        vista.setVisible(true);
    }
}
