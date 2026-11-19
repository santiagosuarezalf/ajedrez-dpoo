# Material de estudio — sustentación del ajedrez (corte III)

Las dos páginas se abren con doble clic en el navegador (no necesitan instalar nada).

**`ajedrez-linea-a-linea.html`**: el código explicado.

- **Código**: las 16 clases, línea por línea, con la explicación al lado. Los botones de la izquierda resaltan un
  concepto (herencia, polimorfismo, MVC, GRASP, interfaz gráfica…) para ver solo esas líneas.
- **Recorrido de una jugada**: qué pasa en el código, paso a paso, con dos clics (e2 y después e4): del botón al
  controlador, del controlador al modelo y de vuelta a la vista. También el caso de una jugada ilegal.
- **Preguntas del profe**: con la respuesta y las líneas del código. Las de color son los puntos débiles.

**`uml-ajedrez.html`**: el diagrama de clases explicado.

- **El plano**: el diagrama entero; se toca una flecha o una clase y sale qué relación es y en qué línea está.
- **Clase por clase**: cada renglón de cada caja, leído símbolo por símbolo, con su línea de código.
- **Las flechas**: las 19 relaciones agrupadas por tipo, más las flechas que *no* hay y la notación completa.
- **Practicar**: tarjetas para decir cada respuesta en voz alta antes de verla.
- **Preguntas de Navarro**: cómo leer cualquier diagrama y las preguntas que puede hacer.

Las dos páginas se generan desde el código con los scripts de `fuente/`
(`construir_linea_a_linea.py` y `construir_uml.py`). Si el código cambia, se vuelven a correr y los números de
línea se actualizan solos; los scripts avisan si alguna explicación quedó apuntando a una línea que no es código.
