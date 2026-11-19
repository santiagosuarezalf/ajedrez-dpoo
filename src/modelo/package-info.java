/**
 * <b>Modelo</b> del patrón MVC: las reglas del ajedrez. Aquí están la partida ({@link modelo.Juego}),
 * el tablero con sus casillas y las seis piezas, cada una con su forma de moverse.
 *
 * <p>Este paquete no sabe nada de ventanas ni de consola: no importa nada de {@code vista} ni de
 * {@code controlador}. Por eso las mismas reglas sirvieron primero para la versión de consola y ahora
 * para la gráfica sin cambiar ninguna regla.</p>
 *
 * <p>Clases principales: {@link modelo.Juego} (turno, legalidad de cada jugada, jaque, enroque,
 * captura al paso, coronación y fin de partida), {@link modelo.Tablero}, {@link modelo.Casilla} y
 * {@link modelo.Pieza}, la clase abstracta de la que heredan las seis piezas.</p>
 */
package modelo;
