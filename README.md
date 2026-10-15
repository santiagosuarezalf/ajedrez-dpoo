# Ajedrez — Proyecto integrador DPOO

Diseño y Programación Orientados a Objetos · Universidad Católica de Colombia · 2026-3
Profesor: Juan Carlos Navarro

Ajedrez para dos jugadores en consola (Java, sin interfaz gráfica).

## Equipo

| Integrante | Código |
|---|---|
| Juan José Betancourt | 67001968 |
| Santiago Suárez Alfonso | 67001862 |

## Cómo abrirlo

En NetBeans: *File → Open Project* y escoger esta carpeta. Se corre con *Run* (clase principal
`ajedrez.Ajedrez`). Necesita JDK 26, que es el que trae NetBeans.

## Cómo se juega

Cada jugada se escribe con la casilla de origen y la de destino, separadas por un espacio:

```
e2 e4
```

Para el enroque se mueve el rey dos casillas (`e1 g1` corto, `e1 c1` largo). Para terminar: `salir`.

En el tablero, las mayúsculas son las piezas blancas y las minúsculas las negras:
P peón · T torre · C caballo · A alfil · D dama · R rey.

## Cómo trabajamos

Antes de empezar a editar y antes de cada *Push*: **Fetch / Pull** en GitHub Desktop.
