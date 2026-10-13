# Especificación técnica de UI – App Eco (Spec-Driven Development)

Este documento describe, pantalla por pantalla y componente por componente, cómo debe verse y comportarse la app, para usarlo como fuente de verdad en spec-driven development.

## 1. Alcance y mapa de navegación

La app tiene 5 destinos principales en una barra inferior fija, y 1 pantalla de detalle (Caminar). El diseño parte de una pantalla móvil vertical; usamos 390 × 844 pt como lienzo de referencia (las medidas de este documento son aproximadas, tomadas a ojo de la imagen, y deben validarse contra el archivo de diseño original).

### 1.1 Pantallas incluidas en el diseño

- **1. Inicio**
    - Tab activo: Inicio
    - Propósito: Resumen de puntos, progreso, actividades del día y contenido sugerido
    - Cómo se llega: Pantalla de arranque o tab Inicio
- **2. Actividades**
    - Tab activo: Actividades
    - Propósito: Listado completo de actividades, filtrable por estado
    - Cómo se llega: Tab Actividades, botón «Ver todas las actividades» o enlace «Ver todas»
- **3. Caminar (detalle)**
    - Tab activo: Actividades
    - Propósito: Seguimiento de distancia de una actividad, con anillo de progreso
    - Cómo se llega: Tocar la tarjeta Caminar en Inicio o en Actividades
- **4. Recompensas**
    - Tab activo: Recompensas
    - Propósito: Catálogo para canjear puntos
    - Cómo se llega: Tab Recompensas
- **5. Educación**
    - Tab activo: Educación
    - Propósito: Videos, artículos e IA para resolver dudas
    - Cómo se llega: Tab Educación
- **6. Perfil**
    - Tab activo: Perfil
    - Propósito: Nivel, estadísticas y menú de cuenta
    - Cómo se llega: Tab Perfil o avatar de Inicio

### 1.2 Mapa de navegación

La barra inferior está siempre visible y lleva a los cinco destinos principales: Inicio, Actividades, Recompensas, Educación y Perfil. Desde cada uno se llega a pantallas más profundas:

- Desde Inicio, tocar una tarjeta de actividad abre su detalle (Caminar es el único diseñado). El botón «Ver todas las actividades» y los enlaces «Ver todas» llevan a Actividades.
- Desde Actividades, tocar una tarjeta abre el detalle de esa actividad.
- Desde Recompensas, tocar una tarjeta abre el detalle y canje de la recompensa (sin diseñar).
- Desde Educación, tocar un video abre el reproductor, y las pestañas Artículos e IA muestran su propio contenido (sin diseñar).
- Desde Perfil, cada fila del menú abre su propia pantalla (sin diseñar).

El tab de Actividades queda resaltado también dentro de Caminar, porque esa pantalla es un hijo de Actividades. Los destinos marcados «sin diseñar» son los que el diseño insinúa (por flechas, chevrones o botones de reproducir) pero cuya pantalla no se mostró.

### 1.3 Pantallas referenciadas pero no diseñadas

Cada una de estas necesita su propia especificación antes de implementarse.

- **Detalle de las otras 5 actividades (Reciclar, Separar residuos, Usar menos plástico, Ahorrar agua, Elegí tu actividad)**: Tarjetas de la grilla de Actividades; la nota del diseño dice que «cada actividad tiene su propia pantalla»
- **Detalle y canje de recompensa**: Chevron (›) en cada tarjeta de Recompensas
- **Reproductor de video**: Botón de reproducir sobre las miniaturas de Inicio y Educación
- **Listado de videos destacados**: Enlace «Ver todos» en Educación
- **Pestañas Artículos e IA**: Segmentos «Artículos» e «IA» en Educación
- **Mi perfil, Mis actividades, Mis recompensas, Configuración, Ayuda**: Filas del menú de Perfil
- **Destino de la tarjeta de puntos**: Chevron (›) en la tarjeta «Tus puntos» de Inicio
- **Pestaña «Para Kids» de Recompensas**: Segmento superior de Recompensas

## 2. Sistema de diseño (tokens)

Todos los valores salen de medir la imagen a ojo: son la propuesta inicial de tokens y deben confirmarse contra el archivo de diseño. Cada token tiene un nombre para que la spec y el código hablen el mismo idioma.

### 2.1 Paleta de color

- **`color.bg.screen`**: #FBF8F0 (crema). Dónde se usa: Fondo de todas las pantallas.
- **`color.surface.card`**: #FFFFFF. Dónde se usa: Tarjetas, filas de menú, tarjeta de progreso.
- **`color.green.700` (primario)**: #1F5E34. Dónde se usa: Botones principales, chip/segmento activo, tab activo, anillo de progreso.
- **`color.green.900`**: #0F4D2E. Dónde se usa: Texto sobre píldoras claras y acentos oscuros.
- **`color.green.500`**: #2E7D3E. Dónde se usa: Relleno de barras de progreso, iconos de actividad.
- **`color.green.100`**: #E6F2DF. Dónde se usa: Tarjeta de actividad completada, píldora «+ 50 puntos», tarjeta «Tus puntos».
- **`color.leaf`**: #4FA24A. Dónde se usa: Icono de hoja junto a subtítulos y a los puntos.
- **`color.state.progress`**: #E3F1EE con borde #CFE5E0. Dónde se usa: Tarjeta de actividad en curso (Caminar).
- **`color.state.idle`**: #EAF0F2. Dónde se usa: Tarjeta de actividad sin iniciar.
- **`color.text.primary`**: #1B2B1F. Dónde se usa: Títulos y valores.
- **`color.text.secondary`**: #6B6F68. Dónde se usa: Subtítulos, metadatos, tabs inactivos.
- **`color.track`**: #E3E5DF. Dónde se usa: Fondo (riel) de barras y anillo de progreso.
- **`color.water`**: #3E9BD6. Dónde se usa: Icono de gota en «Ahorrar agua».

### 2.2 Tipografía

Familia sans-serif geométrica y redondeada, de aspecto similar a Inter o SF Pro. Una sola familia para toda la app.

- **`type.display`**: 40 pt / Bold. Ejemplo en el diseño: «1.200 m» dentro del anillo de Caminar.
- **`type.points`**: 32 pt / Bold. Ejemplo en el diseño: «1.250» en la tarjeta de puntos.
- **`type.title`**: 24 pt / Bold. Ejemplo en el diseño: «Actividades», «Recompensas», «Educación», «Caminar».
- **`type.greeting`**: 20 pt / Bold. Ejemplo en el diseño: «¡Hola, Martina!».
- **`type.section`**: 16 pt / SemiBold. Ejemplo en el diseño: «Actividades de hoy», «Contenido para vos», «Cómo se registra».
- **`type.card-title`**: 13 pt / SemiBold. Ejemplo en el diseño: Nombre de actividad o recompensa.
- **`type.body`**: 12 pt / Regular. Ejemplo en el diseño: Subtítulos, textos explicativos.
- **`type.meta`**: 10–11 pt / Regular. Ejemplo en el diseño: «1.200 / 2.000 m», «+ 50 pts», «3 min · Medio ambiente».
- **`type.chip`**: 12 pt / Medium. Ejemplo en el diseño: Texto de chips y segmentos.
- **`type.tab`**: 10 pt / Medium. Ejemplo en el diseño: Etiquetas de la barra inferior.
- **`type.link`**: 12 pt / SemiBold, color primario. Ejemplo en el diseño: «Ver todas», «Ver todos».

### 2.3 Espaciado, radios y elevación

- **`space.screen-x`**: 16 pt. Uso: Margen lateral de todas las pantallas.
- **`space.gap-card`**: 12 pt. Uso: Separación entre tarjetas de una grilla.
- **`space.section`**: 24 pt. Uso: Separación entre secciones verticales.
- **`radius.card`**: 16 pt. Uso: Tarjetas de actividad, recompensa, progreso, puntos.
- **`radius.media`**: 20 pt. Uso: Miniaturas y tarjetas de video.
- **`radius.pill`**: 999 (completo). Uso: Chips, segmentos, botón principal, píldora de puntos.
- **`radius.icon-circle`**: círculo de 36–40 pt. Uso: Fondo de iconos de actividad.
- **`shadow.card`**: 0 2 8 rgba(0,0,0,0.06). Uso: Todas las tarjetas, muy suave.
- **`shadow.phone`**: 0 8 24 rgba(0,0,0,0.12). Uso: Solo el marco del teléfono en la lámina de presentación (no es parte de la app).

### 2.4 Iconografía e ilustración

- **Iconos de interfaz**: trazo lineal de 1,75 pt con puntas redondeadas, caja de 24 × 24 pt. Tab activo: versión rellena en verde primario; inactivo: línea en gris.
- **Iconos de actividad**: el mismo estilo lineal, en verde, sobre un círculo de fondo claro (excepto la gota de agua, que es azul).
- **Hoja decorativa**: pequeño icono de hoja verde al final de cada subtítulo de pantalla y al lado del valor de puntos. Es el motivo de marca.
- **Ilustraciones**: estilo vectorial plano con formas redondeadas. Hay dos: el planeta Tierra con hojas (Inicio) y el paisaje de colinas verdes (Caminar).
- **Fotografías**: se usan solo en miniaturas de video y tarjetas de recompensa, siempre recortadas con esquinas redondeadas.

## 3. Componentes globales

Estos componentes se repiten en varias pantallas. Se especifican una sola vez acá y las pantallas los referencian por su ID (`C-01`, `C-02`…).

### C-01 · Barra de estado del sistema

La dibuja el sistema operativo, no la app. Muestra la hora «9:41» a la izquierda; señal de red, Wi-Fi y batería a la derecha. Alto: 44 pt. Texto e iconos en color oscuro sobre fondo claro.

### C-02 · Barra de navegación inferior (Tab bar)

- **Posición**: Fija al pie, ocupa todo el ancho, por encima del área segura inferior
- **Alto**: 64 pt + área segura
- **Fondo**: `color.surface.card` con borde superior de 1 pt en `color.track`
- **Ítems**: 5, distribuidos en partes iguales: Inicio, Actividades, Recompensas, Educación, Perfil
- **Icono**: 24 pt; en reposo línea gris `color.text.secondary`; activo, relleno en `color.green.700`
- **Etiqueta**: `type.tab`; activa en negrita y verde primario, inactiva en gris
- **Iconos**: Casa, figura caminando, medalla circular, libro abierto, persona
- **Zona táctil**: Mínimo 48 × 48 pt por ítem
- **Comportamiento**: Un toque cambia de destino y conserva el estado de scroll de cada pestaña. Caminar (detalle) mantiene resaltado el tab Actividades

### C-03 · Encabezado de pantalla (Screen header)

Bloque de dos líneas alineado a la izquierda, con margen `space.screen-x`.

- **Título**: `type.title`, `color.text.primary` (en Inicio se reemplaza por el saludo `type.greeting`)
- **Subtítulo**: `type.body`, `color.text.secondary`, seguido de un icono de hoja `color.leaf` de 12 pt
- **Acción a la derecha (opcional)**: Avatar circular de 36 pt en Inicio
- **Flecha de volver (opcional)**: Icono ← de 24 pt sobre el título, solo en pantallas de detalle (Caminar)

### C-04 · Chip de filtro y segmento (Chip / Segmented control)

Un chip seleccionado lleva fondo `color.green.700` con texto blanco. Un chip no seleccionado lleva fondo `color.green.100` con texto `color.green.700`.

- Forma de píldora, alto 32 pt, padding horizontal 14 pt, texto `type.chip`.
- Una fila de chips es de **selección única**: tocar uno deselecciona al anterior.
- Se usan en tres variantes: chips sueltos (Actividades, filtros de Recompensas), segmento de dos opciones a todo el ancho (Para adultos / Para Kids) y segmento de tres opciones (Videos / Artículos / IA).
- Cuando hay más chips que ancho disponible, la fila hace scroll horizontal y el último chip queda cortado en el borde para indicarlo (ocurre en la fila Todos · Comida · Descuentos · Productos).

### C-05 · Barra de progreso lineal

- Alto 6 pt, esquinas completamente redondeadas.
- Riel `color.track`; relleno `color.green.500`.
- El ancho del relleno es `valor actual ÷ meta`, entre 0 % y 100 %.
- Se usa en la tarjeta «Tu progreso general» (ancho completo) y dentro de las tarjetas de actividad medibles (Caminar).

### C-06 · Tarjeta de actividad (Activity card)

El componente más reutilizado. Aparece en Inicio (carrusel horizontal) y en Actividades (grilla de 2 columnas).

La tarjeta apila, de arriba hacia abajo: un icono de 36 pt, el nombre de la actividad (`type.card-title`), el progreso numérico (`type.meta`), la barra de progreso C-05 (solo si la actividad es medible) y la recompensa «+ N pts» (`type.meta`, solo en Actividades). En Inicio el contenido va centrado; en Actividades, alineado a la izquierda.

Tiene tres estados visuales, que dependen del progreso:

- **Sin iniciar**
    - Cuándo: Progreso 0
    - Fondo: `color.state.idle`
    - Indicador: Texto «0 / 1» sin marca
- **En curso**
    - Cuándo: 0 < progreso < meta
    - Fondo: `color.state.progress` con borde
    - Indicador: Barra de progreso visible
- **Completada**
    - Cuándo: Progreso = meta
    - Fondo: `color.green.100`
    - Indicador: Círculo verde con tilde (✓) en la esquina inferior derecha (en Inicio, abajo al centro). La tarjeta «Elegí una» de Inicio muestra un tilde tenue con 0 / 1, que sería un indicador de pendiente (a confirmar)

Zona táctil: toda la tarjeta, que navega al detalle de la actividad.

### C-07 · Botón principal (Primary button)

- Ancho completo menos márgenes, alto 48 pt, forma de píldora, fondo `color.green.700`, texto blanco `type.chip` en SemiBold.
- Puede llevar una flecha → después del texto («Ver todas las actividades →»).
- Estados: reposo, presionado (oscurece 8 %), deshabilitado (opacidad 50 %).
- En Caminar se usa como indicador de estado, con el texto «¡Seguimiento en curso!» (ver sección 6).

### C-08 · Enlace de sección (Section link)

Texto «Ver todas» o «Ver todos» en `type.link`, alineado a la derecha del título de su sección. Navega al listado completo de esa sección.

### C-09 · Tarjeta de contenido de video (Video card)

- Miniatura fotográfica con esquinas `radius.media` y degradé oscuro inferior para dar contraste al texto.
- Botón de reproducir: círculo semitransparente de 32 pt con triángulo blanco, en la esquina inferior izquierda.
- Título en blanco, `type.card-title`, sobre la imagen. En Educación suma una línea de metadato «duración · categoría».
- Dos tamaños: compacta de 150 × 100 pt (carrusel de Inicio) y ancha de ancho completo × 100 pt de alto (Educación).

### C-10 · Tarjeta de recompensa (Reward card)

- Imagen superior con esquinas redondeadas, relación aproximada 4:3.
- Debajo: título en una o dos líneas (`type.card-title`), y en una fila inferior el costo («800 pts», `type.meta`) a la izquierda y un chevron › a la derecha.
- Toda la tarjeta es tocable y lleva al detalle y canje.

### C-11 · Tarjeta de puntos (Points card)

- Fondo `color.green.100`, `radius.card`, alto aprox. 80 pt.
- Etiqueta «Tus puntos» (`type.meta`), debajo el valor en `type.points` seguido de una hoja `color.leaf`, y un chevron › a la derecha.
- En Inicio, la ilustración del planeta ocupa el lado izquierdo y se sale un poco de la tarjeta por arriba.

## 4. Pantalla 1 · Inicio (S-01)

**Objetivo:** dar un resumen de todo de un vistazo. Según la nota del diseño, las actividades se ven sin tener que hacer scroll vertical largo. Es la pantalla de arranque.

El contenido es más alto que la pantalla: la zona entre el encabezado y la tab bar hace scroll vertical. El saludo y la tab bar no se mueven.

### 4.1 Especificación de cada elemento

- **S01-A · Saludo**
    - Descripción visual: «¡Hola, Martina!», `type.greeting`, alineado a la izquierda
    - Comportamiento: Estático
    - Datos: Nombre de pila del usuario
- **S01-B · Avatar**
    - Descripción visual: Círculo de 36 pt con ilustración de una mujer de pelo verde sobre fondo turquesa, arriba a la derecha
    - Comportamiento: Un toque abre Perfil
    - Datos: URL de imagen de perfil; si no hay, iniciales
- **S01-C · Subtítulo**
    - Descripción visual: «Pequeñas acciones, grandes cambios» + hoja verde
    - Comportamiento: Estático
    - Datos: Texto fijo de marca
- **S01-D · Tarjeta de puntos**
    - Descripción visual: C-11. Planeta Tierra ilustrado con hojas a la izquierda; «Tus puntos»; «1.250» en 32 pt con hoja; chevron ›
    - Comportamiento: Un toque navega a la pantalla de puntos o canje (destino por definir, ver sección 13)
    - Datos: `points.total`
- **S01-E · Tarjeta de progreso general**
    - Descripción visual: Tarjeta blanca de 16 pt de radio. Título «Tu progreso general» (`type.card-title`), barra C-05 a ancho completo con relleno al 75 %, abajo «3/4 actividades» a la izquierda y enlace «Ver todas» a la derecha
    - Comportamiento: El enlace navega a Actividades. La barra se llena animada al cargar
    - Datos: `completed ÷ total` de actividades del día
- **S01-F · Título «Actividades de hoy»**
    - Descripción visual: `type.section` + enlace C-08 «Ver todas» a la derecha
    - Comportamiento: El enlace navega a Actividades
    - Datos: Texto fijo
- **S01-G · Carrusel de actividades**
    - Descripción visual: Tarjetas C-06 de unos 100 × 130 pt, con scroll horizontal; la tercera queda cortada en el borde derecho para sugerir más contenido
    - Comportamiento: Scroll horizontal libre; tocar una tarjeta abre su detalle
    - Datos: Lista de actividades del día
- **S01-G1 · Tarjeta «Caminar»**
    - Descripción visual: Icono de persona caminando, «1.200 / 2.000 m», barra de progreso al 60 %. Estado: en curso
    - Comportamiento: Abre Caminar (S-03)
    - Datos: `distance` y `goal`
- **S01-G2 · Tarjeta «Reciclar»**
    - Descripción visual: Icono de reciclaje, «1 / 1», círculo verde con ✓ abajo. Estado: completada, fondo verde claro
    - Comportamiento: Abre el detalle de Reciclar
    - Datos: `count`, `goal`
- **S01-G3 · Tarjeta «Elegí una»**
    - Descripción visual: Icono de reciclaje con estrella, «0 / 1», tilde tenue. Estado: sin iniciar
    - Comportamiento: Abre su detalle o un selector (a confirmar)
    - Datos: `count`, `goal`
- **S01-H · Botón «Ver todas las actividades →»**
    - Descripción visual: C-07, ancho completo
    - Comportamiento: Navega a Actividades (S-02)
    - Datos: Texto fijo
- **S01-I · Título «Contenido para vos»**
    - Descripción visual: `type.section`
    - Comportamiento: Estático
    - Datos: Texto fijo
- **S01-J · Carrusel de contenido**
    - Descripción visual: Dos tarjetas C-09 compactas: «¿Cómo afecta la contaminación?» (foto de costa con rocas y mar) y «Tu guía de reciclaje» (ilustración verde con hojas); botón ▶ abajo a la izquierda
    - Comportamiento: Scroll horizontal; tocar abre el video
    - Datos: Lista de contenidos recomendados
- **S01-K · Tab bar**
    - Descripción visual: C-02, Inicio activo
    - Comportamiento: Cambia de destino

### 4.2 Reglas de la pantalla

- La barra de progreso general indica 3 de 4 actividades hechas. El diseño muestra solo 3 tarjetas (una completada, una en curso y una sin iniciar), así que el «3/4» no se puede comprobar a simple vista: el carrusel sigue hacia la derecha con el resto de las actividades del día.
- El porcentaje de la barra se calcula con los mismos datos de las tarjetas, nunca por separado.
- La nota del diseño pide que la pantalla funcione «de un vistazo»: no se agregan más bloques entre el encabezado y el botón «Ver todas las actividades».

## 5. Pantalla 2 · Actividades (S-02)

**Objetivo:** reunir todas las actividades en un solo lugar, con iconos simples y diseño visual para elegir rápido (nota del diseño).

### 5.1 Especificación de cada elemento

- **S02-A · Título**
    - Descripción visual: «Actividades», `type.title`
    - Comportamiento: Estático
- **S02-B · Subtítulo**
    - Descripción visual: «Elegí qué querés hacer hoy» + hoja
    - Comportamiento: Estático
- **S02-C · Chips de filtro**
    - Descripción visual: Tres chips: **Todas** (seleccionado por defecto), En progreso, Completadas. Componente C-04
    - Comportamiento: Selección única. Filtra la grilla por estado. «En progreso» muestra las que tienen 0 < progreso < meta; «Completadas», las que llegaron a la meta
- **S02-D · Grilla de actividades**
    - Descripción visual: 2 columnas, separación 12 pt, 6 tarjetas C-06 en el diseño, alto de tarjeta aprox. 130 pt
    - Comportamiento: Scroll vertical de toda la zona bajo los chips
- **S02-E · Tab bar**
    - Descripción visual: C-02, Actividades activo
    - Comportamiento: Cambia de destino

### 5.2 Las seis tarjetas, una por una

- **1. Caminar**
    - Icono: Persona caminando (verde)
    - Progreso mostrado: «1.200 / 2.000 m» + barra al 60 %
    - Estado visual: En curso: fondo turquesa claro con borde
    - Recompensa: + 50 pts
- **2. Reciclar**
    - Icono: Símbolo de reciclaje (verde)
    - Progreso mostrado: «1 / 1» + círculo verde con ✓
    - Estado visual: Completada: fondo verde claro
    - Recompensa: + 100 pts
- **3. Separar residuos**
    - Icono: Tacho de basura (verde)
    - Progreso mostrado: «0 / 1»
    - Estado visual: Sin iniciar: fondo azul grisáceo
    - Recompensa: + 80 pts
- **4. Usar menos plástico**
    - Icono: Botella (verde)
    - Progreso mostrado: «0 / 1»
    - Estado visual: Sin iniciar
    - Recompensa: + 70 pts
- **5. Ahorrar agua**
    - Icono: Gota (azul)
    - Progreso mostrado: «0 / 1»
    - Estado visual: Sin iniciar
    - Recompensa: + 60 pts
- **6. Elegí tu actividad**
    - Icono: Diana con flecha (verde)
    - Progreso mostrado: «0 / 1»
    - Estado visual: Sin iniciar
    - Recompensa: + 50 pts

### 5.3 Reglas de la pantalla

- A diferencia de Inicio, acá la tarjeta incluye siempre la recompensa «+ N pts» al pie.
- Las actividades sin unidad (0 / 1) no muestran barra; solo las medibles, como Caminar, la muestran.
- Un filtro sin resultados muestra el estado vacío de la sección 10.
- «Elegí tu actividad» es una tarjeta especial: permite al usuario escoger una actividad propia en lugar de una predefinida (a confirmar).

## 6. Pantalla 3 · Caminar, detalle de actividad (S-03)

**Objetivo:** mostrar el progreso de una actividad con su puntaje y explicar en lenguaje simple cómo se registra (nota del diseño: cada actividad tiene su propia pantalla). Esta pantalla es el modelo para el detalle de las demás.

### 6.1 Anatomía del anillo de progreso

El anillo se dibuja con un riel `color.track` y un arco `color.green.700` de 8 pt de trazo. El arco empieza en las 12 en punto y avanza en sentido horario. Su ángulo es 360° × (valor ÷ meta): con 1.200 de 2.000 m el progreso es 60 %, es decir 216°. En el centro, de arriba hacia abajo, van el icono de persona caminando, el valor y la meta.

### 6.2 Especificación de cada elemento

- **S03-A · Flecha volver**
    - Descripción visual: Icono ← de 24 pt, arriba a la izquierda, zona táctil de 48 pt
    - Comportamiento: Vuelve a la pantalla anterior (Inicio o Actividades)
- **S03-B · Título**
    - Descripción visual: «Caminar», `type.title`
    - Comportamiento: Estático
    - Datos: Nombre de la actividad
- **S03-C · Subtítulo**
    - Descripción visual: «Sumá distancia, cuidá el planeta» + hoja
    - Comportamiento: Estático
    - Datos: Texto descriptivo de la actividad
- **S03-D · Anillo de progreso**
    - Descripción visual: Diámetro aprox. 160 pt, trazo de 8 pt, riel `color.track`, arco `color.green.700`. Centro: icono de persona caminando (24 pt, verde), valor «1.200» en `type.display` con «m» más chico, y «de 2.000 m» en `type.body` gris
    - Comportamiento: El arco se anima de 0 a su valor al abrir. Se actualiza en vivo mientras el seguimiento está activo
    - Datos: `progress.value`, `progress.goal`, unidad
- **S03-E · Píldora de recompensa**
    - Descripción visual: Fondo `color.green.100`, ancho completo, alto 40 pt, hoja + «+ 50 puntos» centrado
    - Comportamiento: Estática. Muestra lo que se gana al completar
    - Datos: `reward.points`
- **S03-F · Botón de estado**
    - Descripción visual: C-07, texto «¡Seguimiento en curso!»
    - Comportamiento: Indica que el sensor está registrando. Ver reglas más abajo
    - Datos: Estado de seguimiento
- **S03-G · Título «Cómo se registra»**
    - Descripción visual: `type.section`
    - Comportamiento: Estático
- **S03-H · Fila informativa 1**
    - Descripción visual: Tarjeta blanca con borde, icono verde circular a la izquierda y texto «Usa los sensores de tu dispositivo para detectar el movimiento y calcular la distancia.»
    - Comportamiento: Estática
    - Datos: Texto de la actividad
- **S03-I · Fila informativa 2 (tip)**
    - Descripción visual: Igual a la anterior, icono de escudo con hoja; «**Tip:** podés salir a caminar, hacer una caminata o incluso moverte en tu día a día.» (la palabra «Tip:» va en negrita)
    - Comportamiento: Estática
    - Datos: Texto de la actividad
- **S03-J · Ilustración de paisaje**
    - Descripción visual: Colinas verdes en capas con un camino, pegada al borde inferior, sobre la tab bar, ancho completo
    - Comportamiento: Decorativa, no tocable, no la lee el lector de pantalla

### 6.3 Reglas de la pantalla

- El botón «¡Seguimiento en curso!» describe un estado, no una acción. Mientras el seguimiento está activo se ve en verde sólido. Si el seguimiento está pausado o el permiso de movimiento está denegado, debe cambiar de texto y de color (ver sección 10).
- La distancia se calcula con los sensores del dispositivo; la app necesita el permiso de movimiento y actividad física.
- Cuando la distancia llega a la meta, el anillo se completa, la píldora cambia a «¡Completada! + 50 puntos» (a confirmar) y la tarjeta Caminar pasa a estado completada en Inicio y Actividades.
- Las otras actividades usan este mismo esqueleto: cambian título, subtítulo, anillo (o un indicador de «0 / 1»), recompensa y los textos de «Cómo se registra».

## 7. Pantalla 4 · Recompensas (S-04)

**Objetivo:** mostrar el catálogo de premios en tarjetas visuales, cada una con su valor en puntos y una imagen representativa (nota del diseño).

### 7.1 Especificación de cada elemento

- **S04-A · Título**
    - Descripción visual: «Recompensas», `type.title`
    - Comportamiento: Estático
- **S04-B · Subtítulo**
    - Descripción visual: «Canjeá tus puntos y disfrutá»
    - Comportamiento: Estático
- **S04-C · Segmento de público**
    - Descripción visual: Control de dos opciones a todo el ancho: **Para adultos** (activo, verde oscuro) y Para Kids (inactivo, claro)
    - Comportamiento: Cambia el catálogo completo. Cada opción tiene su propio catálogo
- **S04-D · Chips de categoría**
    - Descripción visual: Cuatro chips visibles: **Todos** (activo), Comida, Descuentos, Productos. El último queda cortado en el borde
    - Comportamiento: Selección única, con scroll horizontal. Filtra la grilla por categoría; la categoría «Todos» muestra todo
- **S04-E · Grilla de recompensas**
    - Descripción visual: 2 columnas, separación 12 pt, tarjetas C-10 de aprox. 150 × 190 pt
    - Comportamiento: Scroll vertical
- **S04-F · Tab bar**
    - Descripción visual: C-02, Recompensas activo
    - Comportamiento: Cambia de destino

### 7.2 Las seis recompensas del diseño

- **1. Combo café + medialuna**
    - Categoría sugerida: Comida
    - Imagen: Foto cenital de una taza de café oscuro sobre mesa de madera
    - Costo: 800 pts
- **2. 15% de descuento en tiendas eco**
    - Categoría sugerida: Descuentos
    - Imagen: Ilustración de un cupón verde con estrella sobre degradé verde claro
    - Costo: 1.000 pts
- **3. Cine**
    - Categoría sugerida: Descuentos
    - Imagen: Foto de lentes 3D sobre degradé rosa, naranja y azul
    - Costo: 1.200 pts
- **4. Producto sustentable**
    - Categoría sugerida: Productos
    - Imagen: Foto de producto de limpieza o cuidado en tonos verdes
    - Costo: 1.500 pts
- **5. 20% de descuento en ropa**
    - Categoría sugerida: Descuentos
    - Imagen: Foto de prendas violetas colgadas
    - Costo: 1.000 pts
- **6. Promoción en gimnasio**
    - Categoría sugerida: Descuentos
    - Imagen: Foto de equipo de gimnasio sobre fondo verde
    - Costo: 1.200 pts

### 7.3 Reglas de la pantalla

- Los costos usan punto como separador de miles («1.000 pts»), según el uso rioplatense del resto de la app.
- El título de la tarjeta admite hasta 2 líneas; si es más largo se corta con puntos suspensivos.
- Una recompensa que cuesta más que el saldo del usuario sigue visible, pero con el costo en gris y, al tocarla, el detalle explica cuántos puntos faltan (a confirmar).
- Las imágenes de las tarjetas se recortan al centro (`cover`) para no deformarse.
- El catálogo se alimenta con ofertas de comercios reales, no con ejemplos.

## 8. Pantalla 5 · Educación (S-05)

**Objetivo:** ofrecer contenido educativo para que el usuario aprenda mientras usa la app. Incluye videos, artículos y una IA para resolver dudas (nota del diseño).

### 8.1 Especificación de cada elemento

- **S05-A · Título**
    - Descripción visual: «Educación», `type.title`
    - Comportamiento: Estático
- **S05-B · Subtítulo**
    - Descripción visual: «Aprendé, informate, hacé la diferencia» + hoja
    - Comportamiento: Estático
- **S05-C · Segmento de contenido**
    - Descripción visual: Tres opciones: **Videos** (activo), Artículos, IA. Componente C-04. El fondo de «IA» se ve más gris que el de «Artículos» (ver sección 13)
    - Comportamiento: Cambia el cuerpo de la pantalla entre videos, artículos y asistente de IA
- **S05-D · Título «Videos destacados»**
    - Descripción visual: `type.section` + enlace «Ver todos»
    - Comportamiento: El enlace abre el listado completo de videos
- **S05-E · Video 1**
    - Descripción visual: Tarjeta C-09 ancha, alto aprox. 100 pt. Foto submarina con una tortuga y plástico; botón ▶ en círculo gris abajo a la izquierda. Título «La contaminación en los océanos» y metadato «3 min · Medio ambiente»
    - Comportamiento: Un toque abre el reproductor
- **S05-F · Video 2**
    - Descripción visual: Tarjeta C-09 ancha. Foto de manos sosteniendo un símbolo de reciclaje verde. Título «¿Cómo reciclar en casa?» y metadato «4 min · Reciclaje»
    - Comportamiento: Un toque abre el reproductor
- **S05-G · Tab bar**
    - Descripción visual: C-02, Educación activo
    - Comportamiento: Cambia de destino

### 8.2 Reglas de la pantalla

- El metadato de cada video tiene formato «N min · Categoría».
- Los títulos van en blanco sobre un degradé oscuro inferior para garantizar contraste mínimo de 4,5:1.
- La pestaña **Artículos** mostraría tarjetas de lectura; la pestaña **IA** mostraría un chat con asistente. El diseño no las muestra, así que necesitan su propia especificación.

## 9. Pantalla 6 · Perfil (S-06)

**Objetivo:** mostrar el progreso del usuario, sus puntos y recompensas, y dar acceso a la configuración de la app (nota del diseño).

### 9.1 Especificación de cada elemento

- **S06-A · Avatar**
    - Descripción visual: Círculo de 56 pt con la ilustración de la usuaria
    - Comportamiento: Un toque abre «Mi perfil» para cambiar la foto
    - Datos: `user.avatarUrl`
- **S06-B · Nombre**
    - Descripción visual: «Martina», `type.greeting` en Bold
    - Comportamiento: Estático
    - Datos: `user.firstName`
- **S06-C · Nivel y puntos**
    - Descripción visual: «Nivel 3 · 1.250 pts», `type.body` gris
    - Comportamiento: Estático
    - Datos: `user.level`, `points.total`
- **S06-D · Tarjeta de estadísticas**
    - Descripción visual: Tarjeta blanca con 3 columnas iguales separadas por líneas verticales finas. Cada columna: número en negrita (`type.card-title`) arriba y etiqueta gris abajo
    - Comportamiento: Estática. Cada columna podría llevar a su listado (a confirmar)
    - Datos: Ver abajo
- **S06-E · Lista de opciones**
    - Descripción visual: 5 filas de 52 pt separadas por una línea fina. Cada una: icono lineal a la izquierda, texto, chevron › a la derecha
    - Comportamiento: Un toque abre la pantalla correspondiente
- **S06-F · Tab bar**
    - Descripción visual: C-02, Perfil activo
    - Comportamiento: Cambia de destino

Estadísticas de la tarjeta:

- **Actividades**: 4. Significado propuesto: Actividades completadas en total o en el período actual (a definir).
- **Recompensas**: 2. Significado propuesto: Recompensas canjeadas.
- **Racha**: 3 meses. Significado propuesto: Tiempo seguido de uso con al menos una actividad cumplida.

Filas del menú:

- **Mi perfil**: Persona. Destino: Edición de datos personales.
- **Mis actividades**: Lista con tilde. Destino: Historial de actividades.
- **Mis recompensas**: Caja de regalo. Destino: Recompensas canjeadas y cupones.
- **Configuración**: Engranaje. Destino: Ajustes de la app (notificaciones, permisos, idioma, cuenta).
- **Ayuda**: Signo de pregunta en círculo. Destino: Centro de ayuda y contacto.

## 10. Estados e interacciones transversales

El diseño muestra solo el «camino feliz». Esta sección propone el comportamiento para los estados que faltan, con el mismo lenguaje visual. Todo lo de esta sección es **propuesta** y debe validarse con diseño.

### 10.1 Estados de pantalla

- **Carga**: Primera carga o actualización de datos. Qué se muestra: Esqueletos grises (shimmer) con la forma exacta de cada tarjeta; el encabezado y la tab bar se ven de inmediato.
- **Vacío**: Un filtro no tiene resultados (ej. «Completadas» sin ninguna). Qué se muestra: Ilustración chica de hoja, texto «Todavía no hay actividades acá» y, si corresponde, botón «Ver todas».
- **Error de red**: Falla la carga. Qué se muestra: Mensaje «No pudimos cargar esto», botón «Reintentar» y los datos en caché si existen.
- **Sin conexión**: El dispositivo no tiene red. Qué se muestra: Barra fina en la parte superior con «Sin conexión»; las actividades medibles siguen registrando y se sincronizan después.
- **Actualizar**: El usuario tira hacia abajo. Qué se muestra: Indicador circular verde en el borde superior.

### 10.2 Estados de componentes

- **Botón principal C-07**
    - Reposo: Verde 700
    - Presionado: Verde 700 oscurecido 8 %
    - Deshabilitado: Opacidad 50 %
- **Chip C-04**
    - Reposo: Fondo verde 100
    - Presionado: Fondo verde 100 oscurecido
    - Deshabilitado: Texto gris, sin interacción
    - Seleccionado: Fondo verde 700, texto blanco
- **Tarjeta C-06 / C-10**
    - Reposo: Con sombra suave
    - Presionado: Se achica a 97 %
    - Deshabilitado: Opacidad 60 %
- **Fila de menú**
    - Reposo: Fondo blanco
    - Presionado: Fondo `color.green.100`
- **Tab de la barra inferior**
    - Reposo: Gris
    - Presionado: Gris más oscuro
    - Seleccionado: Verde 700 relleno

### 10.3 Estados del seguimiento de Caminar

- **Activo**
    - Texto del botón S03-F: «¡Seguimiento en curso!»
    - Aspecto: Verde sólido (el del diseño)
    - Condición: Permiso concedido, sensor registrando
- **Pausado**
    - Texto del botón S03-F: «Seguimiento en pausa»
    - Aspecto: Verde 100 con texto verde 700
    - Condición: El usuario o el sistema detuvo el registro
- **Sin permiso**
    - Texto del botón S03-F: «Activar seguimiento»
    - Aspecto: Botón principal con acción: abre los ajustes del sistema
    - Condición: Permiso de movimiento denegado
- **Completado**
    - Texto del botón S03-F: «¡Meta cumplida!»
    - Aspecto: Verde con ✓
    - Condición: Distancia ≥ meta

### 10.4 Animaciones

- **Barras y anillo de progreso**: Se llenan de 0 al valor actual al aparecer. Duración: 600 ms, curva ease-out.
- **Contador de puntos**: Cuenta de 0 al valor al cargar; suma con un pequeño salto al ganar puntos. Duración: 800 ms.
- **Tarjeta que se completa**: Aparece el círculo ✓ con escala de 0 a 100 % y el fondo cambia a verde 100. Duración: 300 ms.
- **Cambio de pantalla**: Deslizamiento horizontal en navegación hacia adelante; fundido entre tabs. Duración: 250 ms.
- **Respeto a preferencias**: Con «reducir movimiento» activo, todo cambia sin animar

### 10.5 Accesibilidad

- **Contraste:** texto normal con relación mínima 4,5:1. Los textos blancos sobre fotos usan el degradé oscuro (C-09); verificar los grises sobre crema.
- **Zonas táctiles:** mínimo 48 × 48 pt, también en la flecha de volver, los chevrones y los iconos de la tab bar.
- **Lector de pantalla:** cada tarjeta se lee como un solo elemento: «Caminar, 1.200 de 2.000 metros, 50 puntos, en curso». El anillo se lee como «60 por ciento». Las ilustraciones decorativas se ignoran.
- **No depender solo del color:** el estado «completada» incluye el ✓ además del fondo verde.
- **Tamaño de texto:** la interfaz debe soportar texto del sistema hasta 130 % sin cortar contenido crítico.
- **Idioma:** español rioplatense (voseo: «elegí», «canjeá», «podés»), con punto como separador de miles.

## 11. Modelo de datos que sostiene la UI (propuesta)

Todo lo que se ve en pantalla sale de seis entidades. Este modelo es una propuesta mínima para que la spec de datos y la de UI coincidan; los nombres pueden cambiar.

Las relaciones son estas. Un `User` tiene muchos `ActivityProgress`, y cada `ActivityProgress` pertenece a una `Activity`. Un `User` tiene muchas `Redemption`, y cada `Redemption` apunta a una `Reward`. `EducationItem` es independiente de las demás entidades.

- **`User`**: `firstName`, `avatarUrl`, `level`, `pointsTotal`, `streakMonths`. Alimenta: Saludo, avatar, tarjeta de puntos, Perfil.
- **`Activity`**: `id`, `name`, `icon`, `goalValue`, `unit` (`m`, `count`), `rewardPoints`, `howItIsTracked` (texto), `tip` (texto). Alimenta: Tarjetas de actividad, detalle de actividad (Caminar), textos de «Cómo se registra».
- **`ActivityProgress`**: `userId`, `activityId`, `date`, `currentValue`, `status` (`idle`, `in_progress`, `completed`). Alimenta: Barras, anillo, ✓, filtros «En progreso» y «Completadas», «3/4 actividades».
- **`Reward`**: `id`, `title`, `imageUrl`, `costPoints`, `category` (`food`, `discount`, `product`), `audience` (`adult`, `kids`), `partner`. Alimenta: Grilla de Recompensas.
- **`Redemption`**: `userId`, `rewardId`, `date`. Alimenta: Estadística «Recompensas», pantalla «Mis recompensas».
- **`EducationItem`**: `id`, `type`, `title`, `durationMin`, `category`, `thumbnailUrl`. Alimenta: Carruseles de Inicio y de Educación.

### Cálculos derivados que muestra la UI

- **«3/4 actividades» y barra al 75 %**: Actividades del día con `status = completed` ÷ total de actividades del día
- **Relleno de la barra de una tarjeta**: `currentValue ÷ goalValue`, con tope de 100 %
- **Arco del anillo en Caminar**: `360° × currentValue ÷ goalValue`
- **Estado visual de una tarjeta**: `currentValue = 0` → sin iniciar; `0 < currentValue < goalValue` → en curso; `currentValue ≥ goalValue` → completada
- **«Nivel 3»**: Función de `pointsTotal` con umbrales de nivel (definir)
- **Racha «3 meses»**: Meses consecutivos con al menos una actividad completada (definir regla exacta)

## 12. Criterios de aceptación para la spec

Cada criterio está escrito como «Dado / Cuando / Entonces», para convertirlo directo en una prueba automática o en una historia de la spec.

- **AC-01 · Global**
    - Dado: Cualquier pantalla principal
    - Cuando: La persona toca un tab de la barra inferior
    - Entonces: Se abre ese destino, el tab queda resaltado en verde primario y los otros 4 quedan en gris
- **AC-02 · Inicio**
    - Dado: 3 de 4 actividades completadas
    - Cuando: Se abre Inicio
    - Entonces: La barra de progreso general se ve al 75 % y el texto dice «3/4 actividades»
- **AC-03 · Inicio**
    - Dado: Hay más actividades que las visibles
    - Cuando: La persona desliza el carrusel
    - Entonces: Se mueve en horizontal y la última tarjeta visible queda parcialmente cortada
- **AC-04 · Inicio**
    - Dado: Cualquier estado
    - Cuando: La persona toca «Ver todas las actividades»
    - Entonces: Se abre Actividades con el chip «Todas» seleccionado
- **AC-05 · Inicio**
    - Dado: Cualquier estado
    - Cuando: La persona toca la tarjeta Caminar
    - Entonces: Se abre el detalle de Caminar con el mismo progreso que mostraba la tarjeta
- **AC-06 · Actividades**
    - Dado: Se abre la pantalla
    - Entonces: Se ven 6 tarjetas en 2 columnas y el chip «Todas» seleccionado
- **AC-07 · Actividades**
    - Dado: Filtro «Todas»
    - Cuando: La persona toca «Completadas»
    - Entonces: Solo se ven las tarjetas completadas y el chip «Completadas» pasa a verde oscuro con texto blanco
- **AC-08 · Actividades**
    - Dado: Una actividad con progreso 1/1
    - Cuando: Se muestra su tarjeta
    - Entonces: Fondo verde claro y círculo verde con ✓
- **AC-09 · Caminar**
    - Dado: Progreso 1.200 de 2.000 m
    - Cuando: Se abre la pantalla
    - Entonces: El anillo está al 60 %, el centro dice «1.200 m» y «de 2.000 m» y la píldora dice «+ 50 puntos»
- **AC-10 · Caminar**
    - Dado: Seguimiento activo
    - Cuando: La persona camina
    - Entonces: El valor del anillo y el arco se actualizan sin salir de la pantalla
- **AC-11 · Caminar**
    - Dado: Permiso de movimiento denegado
    - Cuando: Se abre la pantalla
    - Entonces: El botón dice «Activar seguimiento» y al tocarlo se abren los ajustes del sistema
- **AC-12 · Caminar**
    - Dado: Cualquier estado
    - Cuando: La persona toca ←
    - Entonces: Vuelve a la pantalla de la que venía
- **AC-13 · Recompensas**
    - Dado: Segmento «Para adultos»
    - Cuando: La persona toca «Para Kids»
    - Entonces: La grilla muestra solo recompensas de Kids
- **AC-14 · Recompensas**
    - Dado: Chip «Todos»
    - Cuando: La persona toca «Comida»
    - Entonces: Se ven solo las recompensas de esa categoría
- **AC-15 · Recompensas**
    - Dado: Cualquier estado
    - Cuando: La persona toca una tarjeta
    - Entonces: Se abre el detalle con la opción de canjear
- **AC-16 · Educación**
    - Dado: Pestaña «Videos»
    - Cuando: La persona toca un video
    - Entonces: Se abre el reproductor
- **AC-17 · Educación**
    - Dado: Cualquier estado
    - Cuando: La persona toca «Artículos» o «IA»
    - Entonces: Cambia el contenido de la pantalla y el segmento seleccionado pasa a verde oscuro
- **AC-18 · Perfil**
    - Dado: Se abre la pantalla
    - Entonces: Se ven nombre, «Nivel N · puntos», 3 estadísticas y 5 filas de menú con chevron
- **AC-19 · Perfil**
    - Dado: Cualquier estado
    - Cuando: La persona toca una fila del menú
    - Entonces: Se abre su pantalla
- **AC-20 · Global**
    - Dado: Texto del sistema al 130 %
    - Cuando: Se abre cualquier pantalla
    - Entonces: Ningún texto de título, valor o botón queda cortado
- **AC-21 · Global**
    - Dado: Lector de pantalla activo
    - Cuando: Se recorre una tarjeta de actividad
    - Entonces: Se lee como un solo elemento con nombre, progreso, puntos y estado

## 13. Inconsistencias detectadas y preguntas abiertas

Estos puntos salen de comparar las pantallas entre sí. Conviene resolverlos antes de cerrar la spec, porque cada uno cambia lo que se implementa.

### 13.1 Inconsistencias visuales

- **1. Inicio, tarjeta de video 2**
    - Qué se ve: El título dice «Tu guía de reciclaje no», con un «no» suelto al final
    - Decisión a tomar: Corregir el texto; parece una palabra cortada o sobrante
- **2. Recompensas, tarjeta 1**
    - Qué se ve: El título se ve como «Combo café + medi… medialuna», con texto repetido
    - Decisión a tomar: Texto final: «Combo café + medialuna»
- **3. Inicio vs. Actividades**
    - Qué se ve: En Inicio las tarjetas son centradas y sin «+ pts»; en Actividades son alineadas a la izquierda y con «+ pts»
    - Decisión a tomar: Confirmar que son dos variantes de C-06 (compacta y completa)
- **4. Educación, segmento**
    - Qué se ve: «IA» tiene fondo más gris que «Artículos»
    - Decisión a tomar: Unificar el estilo de los segmentos inactivos
- **5. Caminar, anillo**
    - Qué se ve: El arco verde se ve más largo que el 60 % que indica «1.200 de 2.000 m»
    - Decisión a tomar: El arco debe respetar la fórmula; confirmar el punto de arranque (12 en punto, horario)

### 13.2 Preguntas abiertas de producto

1. ¿A dónde navega la tarjeta de puntos (chevron ›)? ¿A Recompensas, a un historial de puntos o a Perfil? Impacto: Navegación de Inicio.
2. ¿Qué hace la tarjeta «Elegí una» en Inicio y «Elegí tu actividad» en Actividades? ¿Abren un selector, o son la misma función? Impacto: Pantalla nueva.
3. ¿Cuándo se reinicia el progreso diario («Actividades de hoy»)? ¿A medianoche local? Impacto: Cálculo de «3/4» y de estados.
4. ¿Las actividades dan puntos una vez por día o una sola vez? Impacto: Economía de puntos, evita abuso.
5. ¿Cómo se verifican las actividades no medibles por sensor (reciclar, separar residuos, ahorrar agua)? ¿Autodeclaradas, con foto, con código QR? Impacto: Pantalla de detalle de cada actividad, riesgo de fraude.
6. ¿Cómo y dónde se canjea una recompensa? ¿Cupón con código, QR, retiro en comercio? Impacto: Detalle de recompensa, integración con comercios.
7. ¿La pestaña «IA» es un chat libre o responde sobre contenido de la app? Impacto: Alcance, moderación y costos.
8. ¿Qué cuenta la estadística «Actividades = 4» del Perfil, si en Inicio dice 3/4 hoy? Impacto: Definición de métricas.
9. ¿Cómo se calcula el nivel (Nivel 3 con 1.250 pts) y la racha (3 meses)? Impacto: Lógica de gamificación.
10. ¿La app funciona sin conexión? ¿Se guarda el avance de Caminar sin red? Impacto: Arquitectura de sincronización.
11. Plataforma objetivo (iOS, Android o ambas) y tamaños de pantalla a soportar. Impacto: Componentes nativos, medidas finales.

### 13.3 Cómo usar este documento en spec-driven development

1. Tomá cada **ID** (`C-xx`, `Sxx-x`, `AC-xx`) como una unidad trazable: una spec, un componente y una prueba por ID.
2. Resolvé primero las preguntas abiertas de 13.2 que afectan la navegación (1, 2 y 6).
3. Convertí los tokens de la sección 2 en el archivo de tema del código y no uses valores sueltos en las pantallas.
4. Implementá primero los componentes globales (sección 3) y después las pantallas, de la más simple (Perfil) a la más compleja (Caminar).
5. Todo lo marcado como «propuesta» o «a confirmar» es una hipótesis, no un requisito.
