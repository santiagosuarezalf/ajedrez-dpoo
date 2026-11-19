"""Construye estudio/ajedrez-linea-a-linea.html con el código actual (corte III, MVC).

Uso:  python3 estudio/fuente/construir_linea_a_linea.py
Toma la página del corte II (base-corte2.html) como plantilla, conserva las explicaciones de las
líneas que no cambiaron (corrigiendo sus números de línea), agrega las de textos_codigo.py y
revisa al final que toda referencia del tipo Juego:121 apunte a una línea de código real.
"""
import json, re, sys
from pathlib import Path
from base import *
from textos_codigo import NUEVOS, OVR, INFO, ROL as ROL_NUEVOS, PAQ_MODELO

V, L = viejo()
ACT = {n: lineas(n) for n in ORDEN}
MAPAS = {n: mapa_lineas(V[n]["lineas"], ACT[n]) for n in MODELO}


def ln(archivo, texto, k=1):
    """Número de la k-ésima línea del archivo que contiene el texto (para no escribir números a mano)."""
    vistas = [i for i, l in enumerate(ACT[archivo], 1) if texto in l]
    if len(vistas) < k:
        sys.exit(f"No encontré «{texto}» en {archivo}")
    return vistas[k - 1]


def remapear(t):
    """Pasa las referencias de la numeración del corte II a la actual."""
    def f(m):
        a, n = m.group(1), int(m.group(2))
        if a in MAPAS and n in MAPAS[a]:
            return f"{a}:{MAPAS[a][n]}"
        return m.group(0)
    return REF.sub(f, t)


# ---------------- archivos ----------------
archivos = []
for n in ORDEN:
    ls = ACT[n]
    auto, tipo = grupos_auto(ls)
    explic = {}
    if n in MAPAS:
        m = MAPAS[n]
        for g in V[n]["grupos"]:
            if g["kind"] != "code":
                continue
            r = range(g["a"], g["b"] + 1)
            if all(k in m for k in r) and m[g["b"]] - m[g["a"]] == g["b"] - g["a"]:
                a = m[g["a"]]
                explic[a] = {"kind": "code", "a": a, "b": m[g["b"]], "tags": g.get("tags", []), "t": remapear(g["t"])}
    for i, l in enumerate(ls, 1):
        if l.startswith("package modelo;"):
            explic[i] = {"kind": "code", "a": i, "b": i, "tags": ["MVC"], "t": PAQ_MODELO}
    for (a, b, tags, t) in NUEVOS.get(n, []):
        explic[a] = {"kind": "code", "a": a, "b": b, "tags": tags, "t": t}
    for (arch, a), t in OVR.items():
        if arch == n and a in explic:
            explic[a]["t"] = t
    grupos, cubiertas = [], set()
    for a in sorted(explic):
        g = explic[a]
        grupos.append(g)
        cubiertas.update(range(g["a"], g["b"] + 1))
    for i, g in auto.items():
        if not cubiertas.intersection(range(g["a"], g["b"] + 1)):
            grupos.append(g)
            cubiertas.update(range(g["a"], g["b"] + 1))
    grupos.sort(key=lambda g: g["a"])
    faltan = [i for i in range(1, len(ls) + 1) if i not in cubiertas]
    if faltan:
        sys.exit(f"{n}: líneas sin explicar {faltan}")
    for x, y in zip(grupos, grupos[1:]):
        if x["b"] >= y["a"]:
            sys.exit(f"{n}: grupos encimados {x['a']}-{x['b']} y {y['a']}-{y['b']}")
    info = {k: remapear(v) for k, v in V[n].get("info", {}).items()} if n in MAPAS else {}
    info.update(INFO.get(n, {}))
    archivos.append({"nombre": n, "lineas": ls, "grupos": grupos, "info": info})

# ---------------- recorrido de una jugada (dos clics) ----------------
J, C, VA = "Juego", "ControladorJuego", "VentanaAjedrez"
PASOS = [
    ["Clic en e2: el botón avisa", f"Cada botón tiene como comando su fila y su columna (VentanaAjedrez:{ln(VA, 'setActionCommand(f')}). El de e2 tiene `\"6,4\"`. Al hacer clic, Swing llama solo a `actionPerformed` del controlador (ControladorJuego:{ln(C, 'public void actionPerformed')}). ControladorJuego:{ln(C, 'getActionCommand')} lee el comando; no es `\"nueva\"`, así que ControladorJuego:{ln(C, 'comando.split')} lo parte por la coma y ControladorJuego:{ln(C, 'int fila = Integer')} y ControladorJuego:{ln(C, 'int columna = Integer')} sacan `fila = 6` y `columna = 4`."],
    ["Se escoge el peón", f"ControladorJuego:{ln(C, 'private void casillaPulsada')} atiende el clic. La partida no ha terminado (ControladorJuego:{ln(C, 'if (juego.isTerminado()) {')}). ControladorJuego:{ln(C, 'Casilla casilla = juego.getTablero()')} busca la casilla `(6,4)` en el tablero del modelo (Tablero:38) y ControladorJuego:{ln(C, 'Pieza pieza = casilla.getPieza()')} toma el peón blanco. En ControladorJuego:{ln(C, 'pieza.getColor() == juego.getTurno()')} el peón es BLANCO y el turno es BLANCO, así que `seleccionada = (6,4)`."],
    ["El modelo calcula a dónde puede ir", f"ControladorJuego:{ln(C, 'actualizarVista();', 3)} llama a `actualizarVista`, y ControladorJuego:{ln(C, 'destinos = juego.movimientosLegales')} le pide al modelo `movimientosLegales((6,4))`. En Juego:{ln(J, 'for (Casilla destino : pieza.getMovimientosPosibles')} la variable es de tipo Pieza pero el objeto es un Peon (polimorfismo): Peon:52 devuelve `[(5,4), (4,4)]`. Juego:{ln(J, 'if (!dejaReyEnJaque(origen, destino)){', 2)} simula cada una (Juego:{ln(J, 'private boolean dejaReyEnJaque')}) y ninguna deja al rey en jaque. No hay captura al paso ni es el rey: la lista queda `[(5,4), (4,4)]`."],
    ["La vista pinta", f"ControladorJuego:{ln(C, 'vista.mostrarTablero(')} le pasa a la vista el tablero, la casilla escogida y los destinos. VentanaAjedrez:{ln(VA, 'public void mostrarTablero')} recorre los 64 botones: e2 sale amarillo (VentanaAjedrez:{ln(VA, 'setBackground(SELECCION)')}) y e3 y e4 salen verdes (VentanaAjedrez:{ln(VA, 'setBackground(DESTINO)')}). ControladorJuego:{ln(C, 'vista.mostrarEstado(')} deja «Turno de las blancas»."],
    ["Clic en e4: se pide la jugada", f"El comando es `\"4,4\"`, así que se llega otra vez a ControladorJuego:{ln(C, 'private void casillaPulsada')}. Ahora e4 está vacía: no es una pieza propia, y como ya hay `seleccionada`, ControladorJuego:{ln(C, '} else if (seleccionada != null)')} toma el segundo camino. ControladorJuego:{ln(C, 'juego.mover(seleccionada, casilla)')} le pide la jugada al modelo: `juego.mover((6,4), (4,4))`. El controlador no revisa nada: eso lo hace Juego."],
    ["Juego valida la jugada", f"Juego:{ln(J, 'if (terminado || coronacionPendiente != null){')}: no ha terminado ni hay coronación pendiente. Juego:{ln(J, 'if(origen == null || destino == null)')}: ninguna casilla es null. Juego:{ln(J, 'if(pieza == null || pieza.getColor() != turno)')}: el peón es del turno. Juego:{ln(J, 'if(intentarEnroque(origen, destino))')}: no es enroque (en Juego:{ln(J, 'if (rey == null || rey.getTipo() != TipoPieza.REY)')} la pieza no es REY). Juego:{ln(J, 'if (esCapturaAlPaso(origen, destino))')}: no es captura al paso (Juego:{ln(J, 'if (objetivoAlPaso == null || destino != objetivoAlPaso')}, porque `objetivoAlPaso` es null). Juego:{ln(J, 'List<Casilla> posibles = pieza.getMovimientosPosibles')}: Peon:52 devuelve otra vez `[(5,4), (4,4)]`, y Juego:{ln(J, 'if (!posibles.contains(destino))')} encuentra `(4,4)`. Juego:{ln(J, 'if (dejaReyEnJaque(origen, destino)){')}: no deja al rey en jaque."],
    ["Se mueve y se guarda el doble paso", f"Juego:{ln(J, 'tablero.moverPieza(origen, destino);')} mueve el peón (Tablero:66). Juego:{ln(J, 'objetivoAlPaso = null;', 2)} borra cualquier captura al paso vieja. Juego:{ln(J, 'Math.abs(destino.getFila() - origen.getFila()) == 2')}: el peón avanzó dos filas, así que Juego:{ln(J, 'objetivoAlPaso = tablero.getCasilla((origen.getFila()')} guarda `(5,4)`, que es e3: si un peón negro llega al lado, podrá capturar al paso. Juego:{ln(J, 'destino.getFila() == filaDeCoronacion')}: la fila 4 no es la de coronación. Juego:{ln(J, 'turno = turno.opuesto();')}: `turno = NEGRO`. Juego:{ln(J, 'verificarFinDePartida();')} revisa el final y Juego:{ln(J, 'verificarFinDePartida();') + 1} devuelve true."],
    ["De vuelta en el controlador", f"ControladorJuego:{ln(C, 'seleccionada = null;', 2)} suelta la selección. ControladorJuego:{ln(C, 'if (hecha && juego.hayCoronacionPendiente())')}: no hay coronación. ControladorJuego:{ln(C, 'actualizarVista();', 4)} repinta: el peón aparece en e4 y ya no hay casillas verdes. ControladorJuego:{ln(C, 'return \"Turno de \"')} escribe «Turno de las negras». La partida queda esperando el siguiente clic."],
]
ILEGAL = [
    ["Lo mismo hasta la lista de destinos", f"El primer clic escoge el peón de e2, igual que antes. El segundo clic es en e5, `(3,4)`: está vacía, así que ControladorJuego:{ln(C, 'juego.mover(seleccionada, casilla)')} pide la jugada. Peon:52 devuelve `[(5,4), (4,4)]`: el peón solo puede avanzar una o dos casillas."],
    ["Se rechaza", f"Juego:{ln(J, 'if (!posibles.contains(destino))')}: la lista no contiene `(3,4)`, así que Juego:{ln(J, 'if (!posibles.contains(destino))') + 1} devuelve false. No se simuló nada ni se movió nada."],
    ["El mismo jugador repite", f"De vuelta en ControladorJuego:{ln(C, 'seleccionada = null;', 2)} se suelta la selección y la vista se repinta sin amarillo ni verdes. Como el turno nunca cambió, siguen jugando las blancas."],
]

# ---------------- preguntas ----------------
ini = next(i for i, l in enumerate(L) if l.startswith("const PREGUNTAS = ["))
fin = next(i for i in range(ini, len(L)) if L[i] == "];")
js = "\n".join(L[ini:fin + 1])
js = js[js.index("["):].rstrip(";")
js = re.sub(r",\s*\]", "]", js)
PREG = json.loads(js)


def cambiar(q, nueva_r, nuevo_d=None, debil=None):
    for _, lista in PREG:
        for item in lista:
            if item[0] == q:
                item[1] = nueva_r
                if nuevo_d is not None:
                    item[2] = nuevo_d
                if debil is not None:
                    while len(item) < 4:
                        item.append(False)
                    item[3] = debil
                return
    sys.exit(f"No encontré la pregunta {q}")


for _, lista in PREG:
    for item in lista:
        item[1] = remapear(item[1])
        item[2] = remapear(item[2])

cambiar("¿Dónde está el encapsulamiento?",
        f"Los atributos de Casilla (Casilla:17, Casilla:19, Casilla:21), Tablero (Tablero:16) y Juego (Juego:17 a Juego:36) son `private` y se leen con getters. Solo hay setter donde el valor cambia en el juego: hay `setPieza` pero no `setFila`, porque una casilla no se mueve. Los métodos internos también son privados: `simbolo` (VentanaAjedrez:{ln(VA, 'private String simbolo')}), `dejaReyEnJaque` (Juego:{ln(J, 'private boolean dejaReyEnJaque')}) y todos los del controlador menos `actionPerformed`.")
cambiar("¿Por qué la fila 0 es la fila 8?",
        f"Porque arriba van las negras. GridLayout acomoda los botones de izquierda a derecha y de arriba hacia abajo (VentanaAjedrez:{ln(VA, 'panelTablero.add(boton)')}), así que la fila 0 de la matriz queda arriba y es la fila 8 del tablero. Por eso las blancas avanzan restando fila (Peon:54) y por eso e2 es `(6,4)`.")
cambiar("¿Qué falta del reglamento?",
        f"En el corte III ya entraron la coronación (Juego:{ln(J, 'public boolean coronacion')}) y la captura al paso (Juego:{ln(J, 'private boolean esCapturaAlPaso')}). Ya están todas las reglas de movimiento: jaque, jaque mate, ahogado, enroque, coronación y captura al paso. Solo faltan las otras tablas: 50 movimientos, triple repetición, material insuficiente y acuerdo.",
        "«Están todas las reglas de movimiento. Lo único que falta son las tablas por 50 movimientos, triple repetición, material insuficiente o acuerdo.»", True)
cambiar("¿Qué queda por pulir?",
        f"Solo hay Javadoc, sin comentarios `//`, y cada paquete tiene su `package-info.java`. Quedan detalles menores: el espacio de más en « Ahogado (tablas)» (Juego:{ln(J, 'Ahogado (tablas)')}), que no se ve porque el texto de la ventana lo arma el controlador (ControladorJuego:{ln(C, 'Rey ahogado: tablas')}); `setSeHaMovido` (Pieza:186), que es público; y la sangría de Juego, que no es pareja en algunos métodos (en NetBeans se arregla con Alt+Shift+F).",
        "", True)

PREG.insert(0, ["MVC e interfaz gráfica (lo nuevo del corte III)", [
    ["¿Qué es MVC y dónde está en el proyecto?",
     f"Modelo-Vista-Controlador: separar el programa en tres partes, cada una en su paquete. `modelo` tiene las reglas (Juego, Tablero, Casilla y las piezas). `vista` tiene la ventana (VentanaAjedrez). `controlador` tiene a ControladorJuego, que conecta las dos, y al main (Ajedrez). El modelo no importa nada de la vista ni del controlador (Juego:{ln(J, 'import java.util.ArrayList')} y Juego:{ln(J, 'import java.util.List')} son sus únicos import): por eso las mismas reglas de la versión de consola sirvieron para la gráfica sin cambiar ninguna regla.",
     "«Como en el restaurante: la ventana es la carta, el controlador es el mesero y Juego es la cocina. El mesero no cocina y la cocina no habla con el cliente.»"],
    ["¿Cómo viaja un clic por el programa?",
     f"VentanaAjedrez:{ln(VA, 'setActionCommand(f')} le pone a cada botón su fila y columna. Al hacer clic, Swing llama a ControladorJuego:{ln(C, 'public void actionPerformed')}. El controlador le pide la jugada al modelo (ControladorJuego:{ln(C, 'juego.mover(seleccionada, casilla)')} → Juego:{ln(J, 'public boolean mover')}) y después le dice a la vista qué mostrar (ControladorJuego:{ln(C, 'vista.mostrarTablero(')} → VentanaAjedrez:{ln(VA, 'public void mostrarTablero')}). Vista → controlador → modelo, y de vuelta controlador → vista.",
     ""],
    ["¿Por qué ControladorJuego implementa ActionListener?",
     f"Porque así Swing le puede avisar los clics. `ActionListener` es una interfaz de Java con un solo método, `actionPerformed`, y al implementarla (ControladorJuego:{ln(C, 'implements ActionListener')}) el controlador queda obligado a escribirlo (ControladorJuego:{ln(C, 'public void actionPerformed')}). Es el mismo ejemplo de la guía de UML: `Botón implements ControladordeBoton`.",
     "«Implementar ActionListener es firmar el contrato de “yo atiendo los clics”: por eso la ventana me puede registrar en sus botones.»"],
    ["¿Por qué agregarControlador recibe un ActionListener y no un ControladorJuego?",
     f"Por bajo acoplamiento (GRASP). Así la vista no depende del controlador: solo sabe que recibe «alguien que escucha clics» (VentanaAjedrez:{ln(VA, 'public void agregarControlador')}). Si mañana cambia el controlador, la vista no se toca. En el diagrama, la vista solo tiene una dependencia hacia ActionListener y ninguna flecha hacia ControladorJuego.",
     ""],
    ["¿Qué patrones GRASP hay?",
     f"**Controlador**: ControladorJuego recibe los eventos del usuario. **Creador**: el main crea las tres partes (Ajedrez:22 a Ajedrez:24), Juego crea el Tablero (Juego:44), el Tablero crea las casillas y las piezas, y Juego crea la pieza coronada (Juego:{ln(J, 'case DAMA:')}). **Experto en información**: `mover` y `movimientosLegales` (Juego:{ln(J, 'public List<Casilla> movimientosLegales')}) van en Juego porque Juego tiene los datos. **Polimorfismo**: cada pieza responde `getMovimientosPosibles` a su manera. **Bajo acoplamiento**: la vista recibe un ActionListener y el modelo no conoce a nadie. **Alta cohesión**: cada clase hace una sola cosa (la vista solo pinta, Juego solo arbitra).",
     ""],
    ["¿Por qué hay una dependencia entre VentanaAjedrez y Tablero?",
     f"Nunca «porque depende». Hay dos de las tres razones: `mostrarTablero` recibe un Tablero como parámetro (VentanaAjedrez:{ln(VA, 'public void mostrarTablero')}) y usa un método suyo, `getCasilla` (VentanaAjedrez:{ln(VA, 'Casilla casilla = tablero.getCasilla')}). No es asociación porque no lo guarda en ningún atributo: lo usa mientras pinta y lo suelta.",
     "«Es dependencia «parameter»: recibe el tablero en mostrarTablero y le pide las casillas, pero no lo guarda.»"],
    ["¿Por qué Controlador–Vista es agregación y Controlador–Juego es asociación?",
     f"La vista se recibe ya creada en el constructor y se guarda en un atributo `final` (ControladorJuego:{ln(C, 'private final VentanaAjedrez vista')}): la recibe, no la crea → agregación (rombo vacío). Con Juego pasa algo distinto: la primera la recibe, pero «Nueva partida» crea otra con `new` (ControladorJuego:{ln(C, 'juego = new Juego()')}). Como a veces la recibe y a veces la crea, la dibujamos como asociación simple: lo importante es que la guarda en el atributo `-juego`.",
     "«Las dos son atributos, por eso son líneas continuas. La vista la recibe ya hecha: agregación. El juego lo recibe y además lo puede volver a crear: asociación.»", True],
    ["¿Cómo sabe el controlador qué botón se pulsó?",
     f"Por el comando: VentanaAjedrez:{ln(VA, 'setActionCommand(f')} le pone a cada botón el texto `\"fila,columna\"` y al de nueva partida `\"nueva\"` (VentanaAjedrez:{ln(VA, 'setActionCommand(\"nueva\")')}). El controlador lo lee con `e.getActionCommand()` (ControladorJuego:{ln(C, 'getActionCommand')}) y lo parte por la coma.",
     ""],
    ["¿Cómo funciona la coronación?",
     f"Cuando un peón llega a la última fila, `mover` lo mueve pero deja la jugada a medias: guarda la casilla en `coronacionPendiente` y no cambia el turno (Juego:{ln(J, 'coronacionPendiente = destino;')}). El controlador lo nota (ControladorJuego:{ln(C, 'if (hecha && juego.hayCoronacionPendiente())')}), le pregunta a la vista (VentanaAjedrez:{ln(VA, 'public TipoPieza pedirPiezaCoronacion')}, una ventanita con cuatro botones) y le pasa la respuesta al modelo: `coronacion` (Juego:{ln(J, 'public boolean coronacion')}) crea la pieza nueva con `new`, la pone en la casilla y ahí sí cambia el turno.",
     ""],
    ["¿Cómo funciona la captura al paso?",
     f"Cuando un peón avanza dos casillas, Juego:{ln(J, 'objetivoAlPaso = tablero.getCasilla((origen.getFila()')} guarda la casilla que saltó. En la jugada siguiente, un peón rival que llegue en diagonal a esa casilla puede capturarlo: `esCapturaAlPaso` (Juego:{ln(J, 'private boolean esCapturaAlPaso')}) revisa las condiciones y `hacerCapturaAlPaso` quita el peón, que está al lado y no en el destino (Juego:{ln(J, 'tablero.getCasilla(origen.getFila(), destino.getColumna()).setPieza(null)')}). Si no se usa enseguida, se pierde (Juego:{ln(J, 'objetivoAlPaso = null;', 2)}).",
     ""],
    ["¿Por qué el main ya no tiene un while?",
     f"Porque con interfaz gráfica el programa espera eventos. El main crea las tres partes, muestra la ventana (Ajedrez:25) y termina; desde ahí Swing llama a `actionPerformed` cada vez que alguien hace clic. En la versión de consola, en cambio, el while pedía la jugada una y otra vez.",
     ""],
]])

# ---------------- armar la página ----------------
html = BASE.read_text(encoding="utf-8")
lineas_html = html.split("\n")
lineas_html[216] = "const ARCHIVOS = " + json.dumps(archivos, ensure_ascii=False) + ";"
html = "\n".join(lineas_html)


def reemplazar_bloque(h, inicio, fin_marca, nuevo):
    i = h.index(inicio)
    j = h.index(fin_marca, i) + len(fin_marca)
    return h[:i] + nuevo + h[j:]


def js_lista(nombre, datos):
    return f"const {nombre} = " + json.dumps(datos, ensure_ascii=False, indent=1) + ";"


html = reemplazar_bloque(html, "const PASOS = [", "\n];", js_lista("PASOS", PASOS))
html = reemplazar_bloque(html, "const ILEGAL = [", "\n];", js_lista("ILEGAL", ILEGAL))
html = reemplazar_bloque(html, "const PREGUNTAS = [", "\n];", js_lista("PREGUNTAS", PREG))

rol_js = ('const ROL = {Color:"enum",TipoPieza:"enum",Casilla:"clase",Pieza:"abstracta",Torre:"subclase",Alfil:"subclase",\n'
          '  Dama:"subclase",Caballo:"subclase",Rey:"subclase",Peon:"subclase",Tablero:"clase",Juego:"clase",Movible:"interfaz",'
          + ",".join(f'{k}:"{v}"' for k, v in ROL_NUEVOS.items()) + "};")
html = reemplazar_bloque(html, "const ROL = {", "};", rol_js)
html = html.replace('["REGLA","Regla del ajedrez"]', '["MVC","MVC"],["GRASP","GRASP"],["GUI","Interfaz gráfica"],\n  ["REGLA","Regla del ajedrez"]')

cambios = [
    ("repo ajedrez-dpoo · commit 4dea57a · DPOO corte II", "repo ajedrez-dpoo · MVC con interfaz gráfica · DPOO corte III"),
    ("Qué pasa cuando se escribe <code>e2 e4</code>", "Qué pasa con dos clics: <code>e2</code> y después <code>e4</code>"),
    ("La prueba de escritorio de la primera jugada de la partida, con los valores reales de cada variable. Cada enlace abre el archivo en esa línea.",
     "La prueba de escritorio de la primera jugada de la partida, con los valores reales de cada variable: del botón al controlador, del controlador al modelo y de vuelta a la vista. Cada enlace abre el archivo en esa línea."),
    ("Antes (lo que imprime Tablero.imprimir)", "Antes"),
    ("Y si la jugada es ilegal: <code>e2 e5</code>", "Y si la jugada es ilegal: clic en <code>e2</code> y después en <code>e5</code>"),
]
for a, b in cambios:
    if a not in html:
        sys.exit(f"No encontré en la plantilla: {a}")
    html = html.replace(a, b)

# las dos capturas de la ventana real en lugar de los tableros de consola
import base64
def img(nombre, alt):
    datos = base64.b64encode((Path(__file__).with_name(nombre)).read_bytes()).decode()
    return f'<img src="data:image/jpeg;base64,{datos}" alt="{alt}" style="display:block;width:100%;max-width:420px;border:1px solid var(--line);border-radius:8px">'
i = html.index('<div class="tablero">')
j = html.index("</div>", html.index("<figcaption>Después</figcaption>")) + len("</div>")
html = (html[:i] + '<div class="tablero">\n      <figure><figcaption>Después del primer clic: e2 en amarillo, sus destinos en verde</figcaption>'
        + img("recorrido-r1.jpg", "Ventana con el peón de e2 seleccionado y e3 y e4 en verde")
        + '</figure>\n      <figure><figcaption>Después del segundo clic: el peón en e4 y turno de las negras</figcaption>'
        + img("recorrido-r2.jpg", "Ventana con el peón en e4 y el texto Turno de las negras")
        + "</figure>\n    </div>" + html[j:])

# ---------------- revisión de referencias ----------------
NOMBRES = {a["nombre"]: a for a in archivos}
textos = [g["t"] for a in archivos for g in a["grupos"] if g.get("t")]
textos += [v for a in archivos for v in a["info"].values()]
textos += [p for _, p in PASOS] + [p for _, p in ILEGAL]
textos += [x for _, lista in PREG for item in lista for x in item[1:3] if isinstance(x, str)]
malas = []
for t in textos:
    for m in REF.finditer(t):
        a, k = m.group(1), int(m.group(2))
        tipo, _ = clasificar(NOMBRES[a]["lineas"])
        if k > len(NOMBRES[a]["lineas"]) or tipo.get(k) in ("blank", "doc"):
            malas.append(m.group(0))
if malas:
    sys.exit("Referencias que no apuntan a código: " + ", ".join(sorted(set(malas))))

salida = REPO / "estudio/ajedrez-linea-a-linea.html"
salida.write_text(html, encoding="utf-8")
print("listo:", salida, f"{len(archivos)} archivos,", sum(len(a['grupos']) for a in archivos), "grupos")
if "-v" in sys.argv:
    for t in sorted(set(m.group(0) for t in textos for m in REF.finditer(t))):
        a, k = t.split(":")
        print(f"  {t:24} {NOMBRES[a]['lineas'][int(k) - 1].strip()[:90]}")
