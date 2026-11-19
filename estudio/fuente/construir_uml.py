"""Construye estudio/uml-ajedrez.html: el diagrama de clases explicado (corte III, MVC).

Uso:  python3 estudio/fuente/construir_uml.py
Usa el motor de la página «UML Batalla Naval» (base-uml-bn.html), el SVG de docs/diagrama-clases.svg
y los textos de datos_uml.py. Cada renglón de cada caja sale de los mismos datos que dibujan el
diagrama (docs/generar_diagrama.py), así que la página y el PDF siempre dicen lo mismo.
"""
import json, re, sys, io, contextlib
from pathlib import Path
from base import REPO, lineas, RUTA, REF
from datos_uml import REL, CLASES, NOTAS, NOHAY, NOTACION, METODO, PREG

sys.path.insert(0, str(REPO / "docs"))
with contextlib.redirect_stdout(io.StringIO()):
    import generar_diagrama as gd

PLANTILLA = Path(__file__).with_name("base-uml-bn.html")
SVG = (REPO / "docs/diagrama-clases.svg").read_text(encoding="utf-8")
ORDEN = ["Ajedrez", "ControladorJuego", "VentanaAjedrez", "Juego", "Tablero", "Casilla", "Pieza", "Peon", "Torre",
         "Caballo", "Alfil", "Dama", "Rey", "Movible", "Color", "TipoPieza", "ActionListener", "JFrame"]
NUESTRAS = [n for n in ORDEN if n in RUTA]
VIS = {"+": "`+` público", "-": "`-` privado", "#": "`#` protegido"}


def buscar_linea(clase, uml):
    """Línea del código donde está el miembro que dibuja ese renglón."""
    if clase not in RUTA:
        return None, ""
    ls = lineas(clase)
    m = re.match(r"([+\-#])\s*([A-Za-z_]\w*)\s*(\(|:)", uml)
    if not m:
        nombre = uml.strip()
        for i, l in enumerate(ls, 1):
            if re.match(rf"\s*{nombre}\s*[,;]", l):
                return i, l.strip()
        return None, ""
    nombre, es_metodo = m.group(2), m.group(3) == "("
    for i, l in enumerate(ls, 1):
        s = l.strip()
        if s.startswith(("*", "/**", "//")):
            continue
        if es_metodo:
            if re.search(rf"\b{nombre}\s*\(", s) and (re.match(r"(public|private|protected)\b", s) or clase == "Movible"):
                return i, s
        elif re.match(r"(public|private|protected)\b", s) and re.search(rf"\b{nombre}\s*(=|;)", s):
            return i, s
    return None, ""


def lectura(uml, fmt, enum=False):
    if enum:
        return f"valor fijo del enum: `{uml}`"
    m = re.match(r"([+\-#])\s*(.*)", uml)
    vis, resto = m.group(1), m.group(2)
    partes = [VIS[vis]]
    if "(" in resto:
        nombre = resto[:resto.index("(")]
        params = resto[resto.index("(") + 1:resto.rindex(")")].strip()
        ret = resto[resto.rindex(")") + 1:].lstrip(": ").strip()
        if not ret:
            partes.append(f"constructor `{nombre}`: se llama como la clase y no lleva tipo de retorno")
        else:
            partes.append(f"método `{nombre}`")
        partes.append(f"recibe `{params}`" if params else "no recibe nada")
        if ret:
            partes.append("no devuelve nada (`void`)" if ret == "void" else f"devuelve `{ret}`")
    else:
        nombre, tipo = [x.strip() for x in resto.split(":", 1)]
        solo = tipo.replace("{readOnly}", "").strip()
        partes.append(f"atributo `{nombre}` de tipo `{solo}`")
        partes.append("`{readOnly}` = final, no cambia" if "{readOnly}" in tipo else "sin {readOnly}: puede cambiar")
    if "u" in fmt:
        partes.append("subrayado = `static`")
    if "i" in fmt:
        partes.append("en cursiva = abstracto, sin código aquí")
    return " · ".join(partes)


def cabecera(datos):
    t = "Franja de arriba: el nombre."
    if datos["estereotipo"] == "interface":
        t += " «interface» arriba y el nombre en cursiva: es una interfaz; todos sus métodos son abstractos."
    elif datos["estereotipo"] == "enumeration":
        t += " «enumeration» arriba: es un enum; sus valores van en la franja del medio."
    elif datos["abstracta"]:
        t += " En cursiva: es una clase abstracta (no se puede hacer `new`)."
    else:
        t += " En letra normal (no cursiva): no es abstracta. Sin estereotipo: es una clase común."
    if datos["externa"]:
        t += f" Debajo dice ({datos['externa']}): es de la librería de Java, no nuestra."
    return t


clases = []
for n in ORDEN:
    d = gd.MIEMBROS[n]
    filas_uml = []
    es_enum = d["estereotipo"] == "enumeration"
    for grupo, enum in ((d["atributos"], es_enum), (d["metodos"], False)):
        for (t, fmt) in grupo:
            if t.startswith(" ") and filas_uml:
                filas_uml[-1][0] += " " + t.strip()
            else:
                filas_uml.append([t, fmt, enum])
    filas = []
    for t, fmt, enum in filas_uml:
        n_ln, cod = buscar_linea(n, t)
        if n in RUTA and n_ln is None:
            sys.exit(f"No encontré en el código el renglón «{t}» de {n}")
        if n not in RUTA:
            cod = "(es de Java: no está en nuestro código)"
        clave = next((k for k in NOTAS if k[0] == n and t.startswith(k[1])), None)
        filas.append({"uml": t, "ln": n_ln, "lectura": lectura(t, fmt, enum), "nota": NOTAS.get(clave, ""),
                      "codigo": cod, "est": "u" in fmt, "enum": enum})
    info = CLASES[n]
    clases.append({"n": n, "paq": info["paq"], "resumen": info["resumen"], "decl": info["decl"],
                   "cab": cabecera(d), "filas": filas})

# referencias usadas en los textos -> código de esa línea (para el globo de cada referencia)
textos = [x for r in REL for k in ("leer", "codigo", "porque", "trampa") for x in [r.get(k, "")]]
textos += [r for r in sum((r["lineas"] for r in REL), [])]
textos += [b for _, b in NOHAY] + [b for _, b in PREG] + [c["resumen"] for c in clases]
ref = {}
for t in textos:
    for m in REF.finditer(t):
        a, k = m.group(1), int(m.group(2))
        ls = lineas(a)
        if k > len(ls) or not ls[k - 1].strip() or ls[k - 1].strip().startswith(("*", "/**")):
            sys.exit(f"Referencia que no apunta a código: {m.group(0)}")
        ref[m.group(0)] = ls[k - 1].strip()

D = {"clases": clases, "rel": REL, "nohay": NOHAY, "notacion": NOTACION, "preg": PREG, "metodo": METODO, "ref": ref}

# ---------------- plantilla ----------------
h = PLANTILLA.read_text(encoding="utf-8")


def cambiar(a, b, n=1):
    global h
    if h.count(a) < 1:
        sys.exit(f"No encontré en la plantilla: {a[:80]}")
    h = h.replace(a, b) if n == 0 else h.replace(a, b, n)


cambiar("<title>UML Batalla Naval</title>", "<title>UML Ajedrez</title>")
cambiar('<h1>UML Batalla Naval</h1><span>diagrama de clases línea por línea · commit e280e9e · DPOO corte II</span>',
        '<h1>UML Ajedrez</h1><span>diagrama de clases línea por línea · MVC con interfaz gráfica · DPOO corte III</span>')

# el SVG del diagrama
i = h.index('<div class="u-hoja" id="hoja">') + len('<div class="u-hoja" id="hoja">')
j = h.index("</svg>", i) + len("</svg>")
svg = SVG.replace("<svg ", '<svg id="diagrama-svg" role="img" aria-label="Diagrama de clases del ajedrez MVC" ', 1)
h = h[:i] + svg + h[j:]

# datos
i = h.index("const D = ")
j = h.index("\n", i)
h = h[:i] + "const D = " + json.dumps(D, ensure_ascii=False) + ";" + h[j:]

nombres = "|".join(sorted(NUESTRAS, key=len, reverse=True))
cambiar("BatallaNaval|Juego|Interfaz|Jugador|Tablero|Barco|Coordenada", nombres)
cambiar('const TIPO = {comp:"Composición", agr:"Agregación", dep:"Dependencia"};',
        'const TIPO = {comp:"Composición", agr:"Agregación", asoc:"Asociación", dep:"Dependencia", her:"Herencia", imp:"Implementación"};')
cambiar('''function glifo(tipo){
  const fin = `<polyline points="74,4 84,10 74,16" fill="none" stroke="currentColor" stroke-width="1.6"/>`;''',
        '''function glifo(tipo){
  const fin = `<polyline points="74,4 84,10 74,16" fill="none" stroke="currentColor" stroke-width="1.6"/>`;
  const tri = `<polygon points="72,3 84,10 72,17" fill="var(--surface)" stroke="currentColor" stroke-width="1.6"/>`;
  if (tipo === "asoc") return `<svg class="u-glifo" viewBox="0 0 86 20" aria-hidden="true"><line x1="2" y1="10" x2="83" y2="10" stroke="currentColor" stroke-width="1.6"/>${fin}</svg>`;
  if (tipo === "her") return `<svg class="u-glifo" viewBox="0 0 86 20" aria-hidden="true"><line x1="2" y1="10" x2="72" y2="10" stroke="currentColor" stroke-width="1.6"/>${tri}</svg>`;
  if (tipo === "imp") return `<svg class="u-glifo" viewBox="0 0 86 20" aria-hidden="true"><line x1="2" y1="10" x2="72" y2="10" stroke="currentColor" stroke-width="1.6" stroke-dasharray="5 4"/>${tri}</svg>`;''')
cambiar('''const flechaTxt = r => `${r.de} ${r.tipo === "comp" ? "◆→" : r.tipo === "agr" ? "◇→" : "⇢"} ${r.a}`;''',
        '''const SIGNO = {comp:"◆→", agr:"◇→", asoc:"→", dep:"⇢", her:"—▷", imp:"┄▷"};
const flechaTxt = r => `${r.de} ${SIGNO[r.tipo]} ${r.a}`;''')
cambiar("const sale = D.rel.filter(r => r.de === n), llega = D.rel.filter(r => r.a === n);",
        "const sale = D.rel.filter(r => r.de === n || (r.des || []).includes(n)), llega = D.rel.filter(r => r.a === n);")
cambiar('''<div class="u-que"><span>Franja de arriba: el nombre. Va en letra normal (no cursiva): no es abstracta. Sin «interface»: es una clase común.</span></div><div class="u-src"><button class="u-ref" style="align-self:flex-start">public class ${esc(c.n)}</button></div>''',
        '''<div class="u-que"><span>${texto(c.cab)}</span></div><div class="u-src"><button class="u-ref" style="align-self:flex-start">${esc(c.decl)}</button></div>''')
cambiar('''<div class="u-src"><button class="u-ref" title="${esc(f.codigo)}" style="align-self:flex-start">${archivo}:${f.ln}</button><pre class="u-codigo">${esc(f.codigo)}</pre></div>''',
        '''<div class="u-src">${f.ln ? `<button class="u-ref" title="${esc(f.codigo)}" style="align-self:flex-start">${archivo}:${f.ln}</button><pre class="u-codigo">${esc(f.codigo)}</pre>` : `<span class="u-vacio">${esc(f.codigo)}</span>`}</div>''')
cambiar('''const attrs = c.filas.filter(f => !f.uml.includes("(")), mets = c.filas.filter(f => f.uml.includes("("));''',
        '''const attrs = c.filas.filter(f => !f.uml.includes("(")), mets = c.filas.filter(f => f.uml.includes("("));
  const enumTxt = c.filas.some(f => f.enum) ? " (aquí van los valores del enum)" : "";''')
cambiar('''<div class="u-fila"><div class="u-sep">Franja del medio: atributos${attrs.length ? "" : " (vacía)"}</div></div>''',
        '''<div class="u-fila"><div class="u-sep">Franja del medio: atributos${attrs.length ? enumTxt : " (vacía)"}</div></div>''')
cambiar('''<div class="u-fila"><div class="u-sep">Franja de abajo: constructor y métodos</div></div>''',
        '''<div class="u-fila"><div class="u-sep">Franja de abajo: constructor y métodos${mets.length ? "" : " (vacía)"}</div></div>''')
cambiar('''    grupo("dep", "Dependencia", ''',
        '''    grupo("asoc", "Asociación", "Línea <b>continua</b> con flecha y <b>sin rombo</b>. Es un atributo: la clase conoce a la otra, pero no es su dueña.") +
    grupo("dep", "Dependencia", ''')
cambiar('''"Línea <b>punteada</b>. No hay atributo: la otra clase se usa como parámetro (<code>«parameter»</code>), variable local (<code>«local»</code>) o se crea y se devuelve (<code>«create»</code>).") +''',
        '''"Línea <b>punteada</b>. No hay atributo: la otra clase se usa como parámetro (<code>«parameter»</code>), se crea con <code>new</code> en una variable local (<code>«create»</code>) o se devuelve (<code>«return»</code>).") +
    grupo("her", "Herencia", "Línea <b>continua</b> con <b>triángulo hueco</b> hacia la clase padre: <code>extends</code>. Se lee «es un».") +
    grupo("imp", "Implementación", "Línea <b>punteada</b> con <b>triángulo hueco</b> hacia la interfaz: <code>implements</code>.") +''')
cambiar("Las 13 relaciones del diagrama.", f"Las {len(REL)} relaciones del diagrama.")
h = h.replace("uml-bn-", "uml-aj-")
h = h.replace('let claseActual = "Tablero";', 'let claseActual = "ControladorJuego";')

css = '''
.u-badge.asoc{background:var(--asoc-soft);color:var(--asoc)}
.u-badge.her{background:var(--her-soft);color:var(--her)}
.u-badge.imp{background:var(--imp-soft);color:var(--imp)}
:root{--asoc:#5B3F86;--asoc-soft:#EEE7F7;--her:#7A2E3B;--her-soft:#F6E5E8;--imp:#2F6E7A;--imp-soft:#E1F0F2}
@media (prefers-color-scheme: dark){:root:not([data-theme="light"]){--asoc:#C3A9EC;--asoc-soft:#2A2238;--her:#EBA3AF;--her-soft:#3A2228;--imp:#8DD0DB;--imp-soft:#1D3236}}
:root[data-theme="dark"]{--asoc:#C3A9EC;--asoc-soft:#2A2238;--her:#EBA3AF;--her-soft:#3A2228;--imp:#8DD0DB;--imp-soft:#1D3236}
.u-hoja{overflow:auto;max-height:82vh}
.u-hoja svg{width:2700px;max-width:none}
@media (max-width:900px){.u-hoja svg{width:2200px;min-width:0}}
'''
cambiar("</style>\n<link", css + "</style>\n<link") if "</style>\n<link" in h else None
if css not in h:
    k = h.index("</style>", h.index("--f-mono"))
    h = h[:k] + css + h[k:]

salida = REPO / "estudio/uml-ajedrez.html"
salida.write_text(h, encoding="utf-8")
print("listo:", salida, f"{len(clases)} clases, {len(REL)} relaciones, {len(PREG)} preguntas, {len(ref)} referencias")
