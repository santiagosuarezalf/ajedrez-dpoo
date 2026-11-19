/**
 * <b>Vista</b> del patrón MVC: lo que el jugador ve y donde hace clic. Tiene una sola clase,
 * {@link vista.VentanaAjedrez}: un tablero de 8 × 8 botones, la etiqueta con el estado de la
 * partida y el botón de nueva partida.
 *
 * <p>La vista solo <em>lee</em> el modelo para mostrarlo; nunca lo cambia. Los clics se los avisa
 * al controlador a través de la interfaz {@code ActionListener}.</p>
 */
package vista;
