# Ajedrez — Proyecto integrador DPOO

Diseño y Programación Orientados a Objetos · Universidad Católica de Colombia · 2026-3
Profesor: Juan Carlos Navarro

Ajedrez para dos jugadores con **interfaz gráfica** (Java Swing), organizado en **MVC**:

| Paquete | Qué tiene |
|---|---|
| `modelo` | Las reglas: `Juego`, `Tablero`, `Casilla`, la clase abstracta `Pieza`, sus seis piezas, la interfaz `Movible` y los enum `Color` y `TipoPieza`. No conoce la ventana. |
| `vista` | `VentanaAjedrez`: el tablero de 8 × 8 botones, el estado de la partida y el botón de nueva partida. |
| `controlador` | `ControladorJuego` (recibe los clics y se los pasa al modelo) y `Ajedrez` (el `main`). |

📐 **[Diagrama de clases](docs/diagrama-clases.pdf)** (también en [PNG](docs/diagrama-clases.png) y [SVG](docs/diagrama-clases.svg)).
📄 [Avance del corte II](AVANCE.md) (versión de consola).

## Equipo

| Integrante | Código |
|---|---|
| Juan José Betancourt | 67001968 |
| Santiago Suárez Alfonso | 67001862 |

## Cómo abrirlo

En NetBeans: *File → Open Project* y escoger esta carpeta. Se corre con *Run* (clase principal
`controlador.Ajedrez`). Necesita JDK 26, que es el que trae NetBeans.

## Cómo se juega

- **Primer clic:** una pieza del bando que juega. Se pone en amarillo y sus jugadas legales salen en verde.
- **Segundo clic:** la casilla a donde va. Si la jugada no es legal, no pasa nada y se vuelve a escoger.
- **Enroque:** se mueve el rey dos casillas hacia la torre.
- **Captura al paso:** se mueve el peón en diagonal a la casilla que saltó el peón rival.
- **Coronación:** al llegar a la última fila sale una ventanita para escoger dama, torre, alfil o caballo.
- Arriba se ve de quién es el turno y si hay jaque. Al terminar (jaque mate o rey ahogado) sale el resultado.
- **Nueva partida** reinicia el tablero.

## Material de estudio

En [`estudio/`](estudio/): el código explicado línea por línea y el diagrama de clases explicado flecha por flecha.

## Cómo trabajamos

Antes de empezar a editar y antes de cada *Push*: **Fetch / Pull** en GitHub Desktop.
