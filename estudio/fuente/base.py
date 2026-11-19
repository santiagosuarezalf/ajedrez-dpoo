"""Piezas comunes para construir las páginas de estudio a partir del código actual."""
import json, re, difflib
from pathlib import Path

REPO = Path(__file__).resolve().parents[2]
BASE = Path(__file__).with_name("base-corte2.html")

MODELO = ["Color", "TipoPieza", "Casilla", "Movible", "Pieza", "Torre", "Alfil", "Dama", "Caballo",
          "Rey", "Peon", "Tablero", "Juego"]
RUTA = {n: f"src/modelo/{n}.java" for n in MODELO}
RUTA.update({"VentanaAjedrez": "src/vista/VentanaAjedrez.java",
             "ControladorJuego": "src/controlador/ControladorJuego.java",
             "Ajedrez": "src/controlador/Ajedrez.java"})
ORDEN = MODELO + ["VentanaAjedrez", "ControladorJuego", "Ajedrez"]


def lineas(nombre):
    t = (REPO / RUTA[nombre]).read_text(encoding="utf-8").split("\n")
    return t[:-1] if t and t[-1] == "" else t


def viejo():
    L = BASE.read_text(encoding="utf-8").split("\n")
    s = L[216]
    return {a["nombre"]: a for a in json.loads(s[s.index('['):s.rindex(';')])}, L


def mapa_lineas(viejas, nuevas):
    m = {}
    sm = difflib.SequenceMatcher(None, viejas, nuevas, autojunk=False)
    for op, a1, a2, b1, b2 in sm.get_opcodes():
        if op == "equal":
            for k in range(a2 - a1):
                m[a1 + k + 1] = b1 + k + 1
    usadas = set(m.values())
    for i, l in enumerate(viejas, 1):
        if i in m or not l.strip():
            continue
        candidatas = [j for j, x in enumerate(nuevas, 1) if x == l and j not in usadas]
        if len(candidatas) == 1 and viejas.count(l) == 1:
            m[i] = candidatas[0]
            usadas.add(candidatas[0])
    return m


def clasificar(ls):
    """blank / doc / close / code por línea, y para cada '}' la línea que abrió ese bloque."""
    tipo, abre, pila, en_doc = {}, {}, [], False
    for i, l in enumerate(ls, 1):
        s = l.strip()
        if en_doc or s.startswith("/**"):
            tipo[i] = "doc"
            en_doc = not s.endswith("*/")
            continue
        if s == "":
            tipo[i] = "blank"
            continue
        limpio = re.sub(r'"(?:\\.|[^"\\])*"|\'(?:\\.|[^\'\\])\'', '""', l)
        limpio = re.sub(r"//.*", "", limpio)
        if re.fullmatch(r"\}\s*;?", s):
            tipo[i] = "close"
            abre[i] = pila.pop() if pila else None
            continue
        tipo[i] = "code"
        for ch in limpio:
            if ch == "{":
                pila.append(i)
            elif ch == "}" and pila:
                pila.pop()
    return tipo, abre


def grupos_auto(ls):
    """Grupos para lo que no necesita explicación escrita: blancos, Javadoc y llaves de cierre."""
    tipo, abre = clasificar(ls)
    g, i = {}, 1
    while i <= len(ls):
        t = tipo[i]
        if t == "doc":
            j = i
            while j + 1 <= len(ls) and tipo[j + 1] == "doc" and not ls[j - 1].strip().endswith("*/"):
                j += 1
            g[i] = {"kind": "doc", "a": i, "b": j}
            i = j + 1
            continue
        if t == "blank":
            g[i] = {"kind": "blank", "a": i, "b": i}
        elif t == "close":
            a = abre[i]
            txt = f"Llave de cierre: termina el bloque que abre la línea {a} (`{ls[a - 1].strip()}`)." if a else "Llave de cierre."
            g[i] = {"kind": "close", "a": i, "b": i, "tags": [], "t": txt}
        i += 1
    return g, tipo


REF = re.compile(r"\b(" + "|".join(sorted(set(ORDEN), key=len, reverse=True)) + r"):(\d+)\b")
