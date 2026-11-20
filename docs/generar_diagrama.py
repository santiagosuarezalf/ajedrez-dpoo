"""Genera docs/diagrama-clases.svg (diagrama de clases UML del ajedrez, corte III).

Uso:  python3 docs/generar_diagrama.py
Después, el PNG y el PDF se sacan con Brave (ver docs/README del repo o el commit).
Cada caja se escribe a mano con los miembros exactos del código; si el código cambia,
hay que cambiarlo aquí también.
"""
from pathlib import Path
from xml.sax.saxutils import escape

CW = 8.0          # ancho de un carácter de la fuente monoespaciada (13 px)
LH = 18           # alto de un renglón
PAD = 12
W, H = 3740, 2190
DY = 160          # todo el contenido baja 160 px para dejar arriba el corredor de los «create»

CAPAS = {}
ORDEN_CAPAS = []


def capa(nombre):
    """Empieza a escribir en una capa (en Illustrator sale como un grupo con ese nombre)."""
    global out
    if nombre not in CAPAS:
        CAPAS[nombre] = []
        ORDEN_CAPAS.append(nombre)
    out = CAPAS[nombre]


out = []
cajas = {}
MIEMBROS = {}


def caja(id_, x, y, nombre, atributos, metodos, estereotipo=None, abstracta=False,
         fondo="#ffffff", ancho_min=0, externa=None):
    """Dibuja una clase con sus tres compartimientos y guarda su geometría."""
    lineas = [t for t, _ in atributos] + [t for t, _ in metodos] + [nombre]
    ancho = max(ancho_min, max(len(t) for t in lineas) * CW + 2 * PAD)
    cab = 30 + (14 if estereotipo else 0) + (14 if externa else 0)
    alto_a = max(1, len(atributos)) * LH + 10 if atributos else 10
    alto_m = max(1, len(metodos)) * LH + 10 if metodos else 10
    alto = cab + alto_a + alto_m
    out.append(f'<g class="clase" id="c-{id_}">')
    MIEMBROS[id_] = dict(nombre=nombre, atributos=atributos, metodos=metodos, estereotipo=estereotipo,
                         abstracta=abstracta, externa=externa)
    out.append(f'<rect class="caja" x="{x}" y="{y}" width="{ancho}" height="{alto}" fill="{fondo}" stroke="#222" stroke-width="1.4"/>')
    cx = x + ancho / 2
    yy = y + 16
    if estereotipo:
        out.append(f'<text x="{cx}" y="{yy}" class="est">«{estereotipo}»</text>')
        yy += 15
    estilo = ' font-style="italic"' if abstracta else ''
    out.append(f'<text x="{cx}" y="{yy + 3}" class="nom"{estilo}>{escape(nombre)}</text>')
    if externa:
        out.append(f'<text x="{cx}" y="{yy + 18}" class="est">({externa})</text>')
    y1 = y + cab
    out.append(f'<line x1="{x}" y1="{y1}" x2="{x + ancho}" y2="{y1}" stroke="#222"/>')
    ty = y1 + 15
    for t, fmt in atributos:
        out.append(texto(x + PAD, ty, t, fmt))
        ty += LH
    y2 = y1 + alto_a
    out.append(f'<line x1="{x}" y1="{y2}" x2="{x + ancho}" y2="{y2}" stroke="#222"/>')
    ty = y2 + 15
    for t, fmt in metodos:
        out.append(texto(x + PAD, ty, t, fmt))
        ty += LH
    out.append('</g>')
    cajas[id_] = (x, y, ancho, alto)
    return cajas[id_]


def texto(x, y, t, fmt):
    attrs = ''
    if 'i' in fmt:
        attrs += ' font-style="italic"'
    t2 = t.replace('  ', '\u00a0\u00a0')
    res = f'<text x="{x}" y="{y}" class="mie"{attrs}>{escape(t2)}</text>'
    if 'u' in fmt:   # subrayado (static) dibujado como línea, para que se vea igual en Illustrator
        res += f'<line x1="{x}" y1="{y + 3}" x2="{x + len(t) * CW}" y2="{y + 3}" stroke="#111" stroke-width="1"/>'
    return res


def m(*lineas):
    """Lista de miembros. Un sufijo '|u' subraya (static) y '|i' pone cursiva (abstracto)."""
    res = []
    for l in lineas:
        if '|' in l:
            t, f = l.rsplit('|', 1)
            res.append((t, f))
        else:
            res.append((l, ''))
    return res


def lado(id_, cual, f=0.5):
    x, y, w, h = cajas[id_]
    return {'arriba': (x + w * f, y), 'abajo': (x + w * f, y + h),
            'izq': (x, y + h * f), 'der': (x + w, y + h * f)}[cual]


def _u(a, b):
    dx, dy = b[0] - a[0], b[1] - a[1]
    n = (dx * dx + dy * dy) ** 0.5
    return dx / n, dy / n


def _p(pts):
    return ' '.join(f'{x:.1f},{y:.1f}' for x, y in pts)


def linea(puntos, tipo, etiquetas=(), id_=None):
    """tipo: comp, agreg, asoc, dep, hereda, implementa, nada. Las puntas se dibujan como figuras
    (no como marker) para que Illustrator las abra bien. Con id_ queda en su propio grupo clicable."""
    if id_:
        rel(id_)
    rayas = ' stroke-dasharray="8,6"' if tipo in ('dep', 'implementa') else ''
    out.append(f'<polyline class="linea" points="{_p(puntos)}" fill="none" stroke="#222" stroke-width="1.4"{rayas}/>')
    # punta al final
    e, a = puntos[-1], puntos[-2]
    ux, uy = _u(a, e)
    nx, ny = -uy, ux
    if tipo in ('hereda', 'implementa'):
        b = (e[0] - 18 * ux, e[1] - 18 * uy)
        out.append(f'<polygon class="hueco" points="{_p([e, (b[0] + 9 * nx, b[1] + 9 * ny), (b[0] - 9 * nx, b[1] - 9 * ny)])}" fill="#fff" stroke="#222" stroke-width="1.4"/>')
    elif tipo != 'nada':
        b = (e[0] - 14 * ux, e[1] - 14 * uy)
        out.append(f'<polyline class="punta" points="{_p([(b[0] + 7 * nx, b[1] + 7 * ny), e, (b[0] - 7 * nx, b[1] - 7 * ny)])}" fill="none" stroke="#222" stroke-width="1.6"/>')
    # rombo al inicio
    if tipo in ('comp', 'agreg'):
        s0, s1 = puntos[0], puntos[1]
        ux, uy = _u(s0, s1)
        nx, ny = -uy, ux
        pts = [s0, (s0[0] + 14 * ux + 7 * nx, s0[1] + 14 * uy + 7 * ny), (s0[0] + 28 * ux, s0[1] + 28 * uy),
               (s0[0] + 14 * ux - 7 * nx, s0[1] + 14 * uy - 7 * ny)]
        if tipo == 'comp':
            out.append(f'<polygon class="lleno" points="{_p(pts)}" fill="#222" stroke="#222" stroke-width="1"/>')
        else:
            out.append(f'<polygon class="hueco" points="{_p(pts)}" fill="#fff" stroke="#222" stroke-width="1.4"/>')
    for (ex, ey, t, *ancla) in etiquetas:
        an = f' text-anchor="{ancla[0]}"' if ancla else ''
        out.append(f'<text x="{ex}" y="{ey}" class="eti"{an}>{escape(t)}</text>')
    if id_:
        fin_rel()


def rel(id_):
    out.append(f'<g class="rel" id="r-{id_}">')


def fin_rel():
    out.append('</g>')


def paquete(x, y, w, h, nombre, color):
    out.append(f'<rect x="{x}" y="{y - 26}" width="{len(nombre) * 10 + 40}" height="26" fill="{color}" stroke="#555" stroke-width="1.2"/>')
    out.append(f'<text x="{x + 12}" y="{y - 8}" class="paq">{nombre}</text>')
    out.append(f'<rect x="{x}" y="{y}" width="{w}" height="{h}" fill="{color}" stroke="#555" stroke-width="1.2"/>')


# ---------------- paquetes ----------------
capa("Paquetes")
paquete(30, 120, 700, 520, "controlador", "#eef4fb")
paquete(30, 1180, 700, 420, "vista", "#f1f8ee")
paquete(780, 20, 2940, 1830, "modelo", "#fbf8f1")

capa("Clases")

# ---------------- controlador ----------------
caja("Ajedrez", 60, 160, "Ajedrez", [], m("+ main(args: String[]): void|u"))
caja("ControladorJuego", 60, 360, "ControladorJuego",
     m("- juego: Juego", "- vista: VentanaAjedrez {readOnly}", "- seleccionada: Casilla"),
     m("+ ControladorJuego(juego: Juego, vista: VentanaAjedrez)",
       "+ actionPerformed(e: ActionEvent): void",
       "- casillaPulsada(fila: int, columna: int): void",
       "- nuevaPartida(): void",
       "- actualizarVista(): void",
       "- textoEstado(): String"))

# ---------------- clases de Java (externas) ----------------
caja("ActionListener", 380, 720, "ActionListener", [],
     m("+ actionPerformed(e: ActionEvent): void|i"),
     estereotipo="interface", abstracta=True, fondo="#eef1fb", externa="java.awt.event")
caja("JFrame", 220, 960, "JFrame", [], [], fondo="#f2f2f2", ancho_min=200, externa="javax.swing")

# ---------------- vista ----------------
caja("VentanaAjedrez", 60, 1230, "VentanaAjedrez",
     m("- CLARA: java.awt.Color {readOnly}|u", "- OSCURA: java.awt.Color {readOnly}|u",
       "- SELECCION: java.awt.Color {readOnly}|u", "- DESTINO: java.awt.Color {readOnly}|u",
       "- botones: JButton[8][8] {readOnly}", "- lblEstado: JLabel {readOnly}",
       "- btnNuevaPartida: JButton {readOnly}"),
     m("+ VentanaAjedrez()",
       "+ agregarControlador(controlador: ActionListener): void",
       "+ mostrarTablero(tablero: Tablero, seleccionada: Casilla,",
       "      destinos: List<Casilla>): void",
       "+ mostrarEstado(texto: String): void",
       "+ mostrarMensaje(texto: String): void",
       "+ pedirPiezaCoronacion(): TipoPieza",
       "- simbolo(pieza: Pieza): String"))

# ---------------- modelo ----------------
caja("Juego", 820, 160, "Juego",
     m("- tablero: Tablero {readOnly}", "- turno: Color", "- terminado: boolean",
       "- resultado: String", "- coronacionPendiente: Casilla", "- objetivoAlPaso: Casilla"),
     m("+ Juego()", "+ getTablero(): Tablero", "+ getTurno(): Color", "+ isTerminado(): boolean",
       "+ getResultado(): String", "+ hayCoronacionPendiente(): boolean",
       "+ mover(origen: Casilla, destino: Casilla): boolean",
       "- esCapturaAlPaso(origen: Casilla, destino: Casilla): boolean",
       "- hacerCapturaAlPaso(origen: Casilla, destino: Casilla): boolean",
       "- filaDeCoronacion(color: Color): int",
       "+ coronacion(tipo: TipoPieza): boolean",
       "- dejaReyEnJaque(origen: Casilla, destino: Casilla): boolean",
       "+ estaEnJaque(color: Color): boolean", "- buscarRey(color: Color): Casilla",
       "+ hayMovimientosLegales(): boolean", "- verificarFinDePartida(): void",
       "- esEnroqueValido(origen: Casilla, destino: Casilla): boolean",
       "+ intentarEnroque(origen: Casilla, destino: Casilla): boolean",
       "+ movimientosLegales(origen: Casilla): List<Casilla>"))
caja("Tablero", 1700, 160, "Tablero",
     m("- casillas: Casilla[8][8] {readOnly}"),
     m("+ Tablero()", "+ getCasilla(f: int, c: int): Casilla",
       "+ colocarPieza(pieza: Pieza, f: int, c: int): void",
       "+ moverPieza(origen: Casilla, destino: Casilla): void",
       "+ configurarInicial(): void",
       "+ casillaAtacada(casilla: Casilla, porQuien: Color): boolean"))
caja("Casilla", 2600, 160, "Casilla",
     m("- fila: int {readOnly}", "- columna: int {readOnly}", "- pieza: Pieza"),
     m("+ Casilla(fila: int, columna: int)", "+ getFila(): int", "+ getColumna(): int",
       "+ getPieza(): Pieza", "+ setPieza(pieza: Pieza): void", "+ estaVacia(): boolean",
       "+ toString(): String"))
caja("Color", 820, 880, "Color", m("BLANCO", "NEGRO"), m("+ opuesto(): Color"),
     estereotipo="enumeration", fondo="#fdf6e3", ancho_min=170)
caja("TipoPieza", 820, 1040, "TipoPieza", m("PEON", "TORRE", "CABALLO", "ALFIL", "DAMA", "REY"), [],
     estereotipo="enumeration", fondo="#fdf6e3", ancho_min=170)
caja("Pieza", 1350, 800, "Pieza",
     m("# color: Color", "# casilla: Casilla", "# seHaMovido: boolean"),
     m("+ Pieza(color: Color)", "+ getColor(): Color", "+ getCasilla(): Casilla",
       "+ setCasilla(casilla: Casilla): void", "+ seHaMovido(): boolean",
       "+ marcarComoMovida(): void", "+ setSeHaMovido(v: boolean): void",
       "+ getTipo(): TipoPieza|i",
       "+ getMovimientosPosibles(tablero: Tablero): List<Casilla>|i",
       "+ casillasAtacadas(t: Tablero): List<Casilla>", "+ toString(): String",
       "# agregarSiValida(lista: List<Casilla>, t: Tablero, f: int, c: int): void",
       "# deslizar(lista: List<Casilla>, t: Tablero, df: int, dc: int): void"),
     abstracta=True)
caja("Movible", 2950, 930, "Movible", [],
     m("+ getMovimientosPosibles(tablero: Tablero): List<Casilla>|i",
       "+ casillasAtacadas(tablero: Tablero): List<Casilla>|i"),
     estereotipo="interface", abstracta=True, fondo="#eef1fb")

subs = [("Peon", True), ("Torre", False), ("Caballo", False), ("Alfil", False), ("Dama", False), ("Rey", False)]
xs = 820
for nombre, ataca in subs:
    met = [f"+ {nombre}(color: Color)", "+ getTipo(): TipoPieza",
           "+ getMovimientosPosibles(t: Tablero): List<Casilla>"]
    if ataca:
        met.append("+ casillasAtacadas(t: Tablero): List<Casilla>")
    caja(nombre, xs, 1520, nombre, [], m(*met))
    xs += cajas[nombre][2] + 18

# ---------------- relaciones ----------------
capa("Relaciones")
# main crea las tres partes (variables locales con new)
ax, ay, aw, ah = cajas["Ajedrez"]
cx_, cy_, cw_, ch_ = cajas["ControladorJuego"]
linea([(ax + 60, ay + ah), (ax + 60, cy_)], "dep", [(ax + 70, ay + ah + 40, "«create»")], id_="Ajedrez-ControladorJuego")
jx, jy, jw, jh = cajas["Juego"]
linea([(ax + aw, ay + 40), (jx, ay + 40)], "dep", [(ax + aw + 20, ay + 32, "«create»")], id_="Ajedrez-Juego")
vx, vy, vw, vh = cajas["VentanaAjedrez"]
linea([(ax, ay + 30), (45, ay + 30), (45, vy + 30), (vx, vy + 30)], "dep", [(50, 1100, "«create»")], id_="Ajedrez-VentanaAjedrez")

# ControladorJuego implementa ActionListener
alx, aly, alw, alh = cajas["ActionListener"]
linea([(460, cy_ + ch_), (460, aly)], "implementa", [(470, cy_ + ch_ + 50, "implementa")], id_="ControladorJuego-ActionListener")
# ControladorJuego -> Juego (asociación)
linea([(cx_ + cw_, cy_ + 40), (jx, cy_ + 40)], "asoc",
      [(jx - 12, cy_ + 32, "-juego", "end"), (jx - 12, cy_ + 58, "1", "end")], id_="ControladorJuego-Juego")
# ControladorJuego <>-> VentanaAjedrez (agregación: la recibe ya creada)
linea([(120, cy_ + ch_), (120, vy)], "agreg", [(130, vy - 30, "-vista"), (130, vy - 12, "1")], id_="ControladorJuego-VentanaAjedrez")
# ControladorJuego -> Casilla (seleccionada 0..1)
sx, sy, sw, sh = cajas["Casilla"]
linea([(480, cy_), (480, 135), (sx + sw / 2, 135), (sx + sw / 2, sy)], "asoc",
      [(sx + sw / 2 + 8, sy - 26, "-seleccionada"), (sx + sw / 2 + 8, sy - 8, "0..1")], id_="ControladorJuego-Casilla")

# VentanaAjedrez hereda de JFrame
fx, fy, fw, fh = cajas["JFrame"]
linea([(320, vy), (320, fy + fh)], "hereda", id_="VentanaAjedrez-JFrame")
# VentanaAjedrez usa ActionListener como parámetro (agregarControlador)
linea([(500, vy), (500, aly + alh)], "dep", [(510, aly + alh + 40, "«parameter»")], id_="VentanaAjedrez-ActionListener")
tx, ty_, tw, th = cajas["Tablero"]
# Juego <*>- Tablero (composición: lo crea en el constructor)
linea([(jx + jw, jy + 40), (tx, jy + 40)], "comp",
      [(jx + jw + 30, jy + 62, "1"), (tx - 12, jy + 32, "-tablero", "end"), (tx - 12, jy + 62, "1", "end")], id_="Juego-Tablero")
# Tablero <*>- Casilla (64)
linea([(tx + tw, ty_ + 40), (sx, ty_ + 40)], "comp",
      [(tx + tw + 30, ty_ + 62, "1"), (sx - 12, ty_ + 32, "-casillas", "end"), (sx - 12, ty_ + 62, "64", "end")], id_="Tablero-Casilla")
# Casilla <>- Pieza (cada casilla tiene 0..1 pieza; la pieza sabe en qué casilla está)
px, py, pw, ph = cajas["Pieza"]
linea([(sx + sw / 2, sy + sh), (sx + sw / 2, py + 40), (px + pw, py + 40)], "agreg",
      [(sx + sw / 2 + 10, sy + sh + 40, "0..1  #casilla"), (px + pw + 14, py + 32, "-pieza 0..1")], id_="Casilla-Pieza")
# Juego -> Casilla (coronacionPendiente y objetivoAlPaso, 0..1 cada una)
linea([(jx + jw - 80, jy + jh), (jx + jw - 80, jy + jh + 40), (sx + 60, jy + jh + 40), (sx + 60, sy + sh)], "asoc",
      [(jx + jw - 70, jy + jh + 32, "-coronacionPendiente 0..1 · -objetivoAlPaso 0..1")], id_="Juego-Casilla")
# Juego -> Color (turno)
colx, coly, colw, colh = cajas["Color"]
linea([(jx + 60, jy + jh), (jx + 60, coly)], "asoc",
      [(jx + 70, coly - 26, "-turno"), (jx + 70, coly - 8, "1")], id_="Juego-Color")
# Pieza -> Color (color)
linea([(px, py + 150), (colx + colw, py + 150)], "asoc",
      [(colx + colw + 12, py + 142, "#color 1")], id_="Pieza-Color")
# Pieza ..> TipoPieza (getTipo devuelve TipoPieza)
tpx, tpy, tpw, tph = cajas["TipoPieza"]
linea([(px, 1090), (tpx + tpw, 1090)], "dep",
      [(tpx + tpw + 12, 1082, "«return»")], id_="Pieza-TipoPieza")
# Pieza implementa Movible
mx, my, mw, mh = cajas["Movible"]
linea([(px + pw, my + mh / 2), (mx, my + mh / 2)], "implementa",
      [(px + pw + 20, my + mh / 2 - 8, "implementa")], id_="Pieza-Movible")
# herencia de las seis piezas: cada subclase con su propia línea y su propio triángulo
# (Navarro: las herencias no se juntan en una sola punta, son seis relaciones distintas).
# Cada línea llega a un punto distinto del borde de abajo de Pieza; los niveles horizontales
# están escalonados para que ninguna línea cruce a otra.
rel("Piezas-Pieza")
llegada = {"Peon": px + 70, "Torre": None, "Caballo": px + pw * 0.40,
           "Alfil": px + pw * 0.55, "Dama": px + pw * 0.70, "Rey": px + pw * 0.85}
nivel = {"Peon": 1400, "Caballo": 1400, "Alfil": 1340, "Dama": 1280, "Rey": 1220}
for n, _ in subs:
    x_, y_, w_, h_ = cajas[n]
    c = x_ + w_ / 2
    if llegada[n] is None:          # Torre queda justo debajo de Pieza: línea recta
        linea([(c, y_), (c, py + ph)], "hereda")
    else:
        e = llegada[n]
        linea([(c, y_), (c, nivel[n]), (e, nivel[n]), (e, py + ph)], "hereda")
fin_rel()

# ---------------- dependencias de uso (capa aparte) ----------------
# Cada dependencia con su propia línea. Van en su propia capa para poder ocultarlas en Illustrator
# cuando el diagrama se ve muy cargado.
capa("Dependencias de uso")
C = lambda n: cajas[n]


def caja_xy(n):
    x, y, w, h = cajas[n]
    return x, y, x + w, y + h


cjx0, cjy0, cjx1, cjy1 = caja_xy("ControladorJuego")
vx0, vy0, vx1, vy1 = caja_xy("VentanaAjedrez")
colx0, coly0, colx1, coly1 = caja_xy("Color")
tpx0, tpy0, tpx1, tpy1 = caja_xy("TipoPieza")
px0, py0, px1, py1 = caja_xy("Pieza")
tbx0, tby0, tbx1, tby1 = caja_xy("Tablero")
csx0, csy0, csx1, csy1 = caja_xy("Casilla")
jx0, jy0, jx1, jy1 = caja_xy("Juego")
mvx0, mvy0, mvx1, mvy1 = caja_xy("Movible")

# controlador -> modelo (variables locales en casillaPulsada y textoEstado)
linea([(cjx1, 492), (768, 492), (768, 930), (colx0, 930)], "dep", [(cjx1 + 10, 486, "«use»")], id_="ControladorJuego-Color")
linea([(cjx1, 522), (748, 522), (748, 1080), (tpx0, 1080)], "dep", [(cjx1 + 10, 516, "«use»")], id_="ControladorJuego-TipoPieza")
linea([(cjx1, 552), (738, 552), (738, 1010), (1300, 1010), (1300, 1050), (px0, 1050)], "dep",
      [(cjx1 + 10, 546, "«use»")], id_="ControladorJuego-Pieza")

# vista -> modelo (parámetros y retorno)
linea([(vx1, 1250), (758, 1250), (758, 975), (colx0, 975)], "dep", [(vx1 + 10, 1244, "«use»")], id_="VentanaAjedrez-Color")
linea([(vx1, 1270), (776, 1270), (776, 1180), (tpx0, 1180)], "dep", [(vx1 + 10, 1288, "«return»")], id_="VentanaAjedrez-TipoPieza")
linea([(vx1, 1340), (1380, 1340), (1380, py1)], "dep", [(1100, 1334, "«parameter»")], id_="VentanaAjedrez-Pieza")
linea([(vx1, 1300), (1240, 1300), (1240, 745), (1780, 745), (1780, tby1)], "dep",
      [(1790, 730, "«parameter»")], id_="VentanaAjedrez-Tablero")
linea([(vx1, 1320), (1320, 1320), (1320, 765), (2620, 765), (2620, csy1)], "dep",
      [(2450, 758, "«parameter»")], id_="VentanaAjedrez-Casilla")

# Juego -> Pieza (variable local en mover) y -> TipoPieza (parámetro de coronacion)
linea([(1335, jy1), (1335, 830), (px0, 830)], "dep", [(1260, 822, "«use»")], id_="Juego-Pieza")
linea([(1040, jy1), (1040, 1150), (tpx1, 1150)], "dep", [(1048, 1142, "«parameter»")], id_="Juego-TipoPieza")

# Tablero <-> Pieza y Tablero -> Color
linea([(1900, tby1), (1900, py0)], "dep", [(1908, 790 - 12, "«parameter»")], id_="Tablero-Pieza")
linea([(1940, py0), (1940, tby1)], "dep", [(1948, tby1 + 30, "«parameter»")], id_="Pieza-Tablero")
linea([(1720, tby1), (1720, 720), (1060, 720), (1060, 870), (colx1 - 40, 870), (colx1 - 40, coly0)], "dep",
      [(1450, 714, "«parameter»")], id_="Tablero-Color")

# Movible -> Tablero (parámetro) y -> Casilla (retorno)
linea([(3000, mvy0), (3000, 600), (2140, 600), (2140, tby1)], "dep", [(2700, 594, "«parameter»")], id_="Movible-Tablero")
linea([(3100, mvy0), (3100, 540), (2860, 540), (2860, csy1)], "dep", [(2930, 534, "«return»")], id_="Movible-Casilla")

# «create»: Tablero crea las seis piezas (configurarInicial) y Juego crea la pieza de la coronación.
# Suben a un corredor por encima del paquete, bajan por la derecha y entran por debajo de cada pieza.
centro = {n: cajas[n][0] + cajas[n][2] / 2 for n, _ in subs}
fondo_p = {n: cajas[n][1] + cajas[n][3] for n, _ in subs}
creates = ([("Juego", n, x) for n, x in zip(["Torre", "Caballo", "Alfil", "Dama"], [1180, 1220, 1260, 1300])] +
           [("Tablero", n, x) for n, x in zip(["Peon", "Torre", "Caballo", "Alfil", "Dama", "Rey"],
                                             [1960, 2000, 2040, 2080, 2120, 2160])])
grupos = {"Juego": [], "Tablero": []}
for k, (src, dst, sx_) in enumerate(creates):
    nivel_arriba = -70 + k * 8                 # corredor de arriba (fuera del paquete, encima del título del paquete)
    xc = 3700 - k * 14                         # corredor de la derecha
    nivel_abajo = 1830 - k * 14                # franja de abajo
    ent = centro[dst] + (-60 if src == "Juego" else 60)
    sy_ = cajas[src][1]
    grupos[src].append(([(sx_, sy_), (sx_, nivel_arriba), (xc, nivel_arriba), (xc, nivel_abajo),
                         (ent, nivel_abajo), (ent, fondo_p[dst])], (ent + 4, fondo_p[dst] + 15, "«create»")))
for src, rid in (("Tablero", "Tablero-Piezas"), ("Juego", "Juego-Coronacion")):
    rel(rid)
    for pts, eti in grupos[src]:
        linea(pts, "dep", [eti])
    fin_rel()

# ---------------- notas ----------------
capa("Notas")
nota = [
    "Las dependencias de uso (líneas punteadas con «use», «parameter», «return» y «create») van en la capa «Dependencias de uso»; se pueden ocultar sin perder el resto del diagrama.",
    "Las seis subclases heredan de Pieza sus dependencias con Tablero (parámetro de getMovimientosPosibles) y con Casilla; por eso esas líneas salen de Pieza.",
    "Las clases de Java que solo aparecen como tipo (JButton, JLabel, String, List, ActionEvent) no se dibujan como caja; solo JFrame y ActionListener, porque hay herencia e implementación.",
]
ny = 1900
for k, t in enumerate(nota):
    out.append(f'<text x="40" y="{ny + k * 22}" class="nota">{escape(t)}</text>')
out.append(f'<text x="40" y="{H - DY - 24}" class="ley">{escape("+ público · − privado · # protegido · cursiva = abstracto · subrayado = estático (static) · {readOnly} = final · ◆ composición (el todo crea la parte) · ◇ agregación (la recibe ya creada) · ▷ continua = herencia · ▷ punteada = implementa · → asociación (atributo) · ⇢ dependencia (parámetro, variable local, retorno o creación)")}</text>')

cabeza = f'''<svg xmlns="http://www.w3.org/2000/svg" width="{W}" height="{H}" viewBox="0 0 {W} {H}">
<style>
text {{ font-family: Menlo, Consolas, monospace; fill: #111; }}
.mie {{ font-size: 13px; }}
.nom {{ font-size: 15px; font-weight: bold; text-anchor: middle; font-family: Helvetica, Arial, sans-serif; }}
.est {{ font-size: 12px; text-anchor: middle; font-family: Helvetica, Arial, sans-serif; }}
.eti {{ font-size: 12px; font-family: Helvetica, Arial, sans-serif; fill: #333; }}
.paq {{ font-size: 15px; font-weight: bold; font-family: Helvetica, Arial, sans-serif; }}
.tit {{ font-size: 24px; font-weight: bold; font-family: Helvetica, Arial, sans-serif; }}
.sub {{ font-size: 14px; font-family: Helvetica, Arial, sans-serif; fill: #444; }}
.nota {{ font-size: 13px; font-family: Helvetica, Arial, sans-serif; fill: #333; }}
.ley {{ font-size: 13px; font-family: Helvetica, Arial, sans-serif; fill: #444; }}
</style>
<rect id="Fondo" width="{W}" height="{H}" fill="#fff"/>
<text x="40" y="44" class="tit">Diagrama de clases — Ajedrez MVC con interfaz gráfica · Diseño y Programación Orientados a Objetos · Corte III</text>
<text x="40" y="70" class="sub">Juan José Betancourt Quintero (67001968) · Santiago Suárez Alfonso (67001862) · paquetes modelo, vista y controlador</text>
'''
cuerpo = []
for nombre in ORDEN_CAPAS:
    gid = nombre.replace(" ", "_")
    cuerpo.append(f'<g id="{gid}" data-name="{nombre}" transform="translate(0,{DY})">')
    cuerpo += CAPAS[nombre]
    cuerpo.append('</g>')
Path(__file__).with_name("diagrama-clases.svg").write_text(cabeza + "\n".join(cuerpo) + "\n</svg>\n", encoding="utf-8")
print("listo", {k: tuple(round(v) for v in b) for k, b in cajas.items()})
