# Análisis técnico de la UI de escritorio → Roadmap para la app móvil

> Objetivo: documentar cómo está construida visualmente la app de escritorio
> (`C:/Proyectos/MisFinanzasDesktop`, C#/WPF) para replicar la misma identidad y
> patrones en la app Android (`C:/Proyectos/GP`, Kotlin/Compose). Los colores de marca
> ya son idénticos entre ambas; lo que falta es paridad de **layout, componentes y navegación**.

Fecha: 2026-09-17 · Complementa a `AUDITORIA-Y-ROADMAP-MOVIL.md`.

---

## 1. Arquitectura visual del escritorio (visión general)

La ventana principal (`MainWindow.xaml`) es un layout de **2 columnas fijas**:

```
┌────────────┬─────────────────────────────────────────────┐
│ Sidebar    │  Top bar: avatar + saludo | licencia | 🏪 negocio │
│ 224px      ├─────────────────────────────────────────────┤
│ (Navy)     │                                             │
│            │  Host: ContentControl que intercambia       │
│ Inicio     │  UserControls (Dashboard, Finanzas,         │
│ Movimientos│  Deudas, Agenda, Reportes, Negocios,        │
│ Quién debe │  Configuración)                             │
│ Agenda     │                                             │
│ Reportes   │  Cada vista = ScrollViewer > StackPanel     │
│ Negocios   │  con Margin=28                              │
│ ─────────  │                                             │
│ Config     │                                             │
└────────────┴─────────────────────────────────────────────┘
```

- **Navegación persistente lateral** (no drawer): siempre visible, con estado
  seleccionado resaltado. El item activo se pinta con fondo `Azul` y texto blanco;
  el resto texto `#D7E2EC` sobre `Navy`.
- **Top bar contextual**: avatar circular (imagen o inicial 👤), saludo, barra de días
  de licencia (oculta si es de por vida), y **selector de negocio** (ComboBox) — oculto
  en plan Personal (1 negocio).
- **Contenido** = intercambio de `UserControl` en un `ContentControl` (no hay stack de
  navegación real; es swap directo con `Refrescar()` por vista).
- Diálogos/gestión secundaria (Cuentas, Categorías, Metas, Recurrentes, Presupuestos,
  Remito, Calculadora) se abren como **Windows** aparte, no como vistas embebidas.

---

## 2. Design tokens

### 2.1 Paleta (idéntica en ambas apps ✅)

| Token | Hex | Uso |
|---|---|---|
| Navy | `#02223A` | Sidebar, tarjeta "Balance", fondo tema oscuro base |
| Azul | `#023A5D` | Primario: botones, item de nav activo |
| Marrón | `#98643A` | Secundario/acento |
| Tostado | `#C99A6D` | Terciario, textos sobre navy |
| TostadoClaro | `#EDDFCD` | Contenedor cálido (bloques de resumen, hover ghost) |
| AppBg | `#F5F1EA` | Fondo de la app (claro) |
| Surface | `#FFFFFF` | Tarjetas |
| TextMain | `#14202B` | Texto principal |
| TextMuted | `#6B6256` | Texto secundario |
| Outline | `#E2DACB` | Bordes de tarjetas/inputs |
| Ingreso | `#1E9E5B` | Verde ingresos |
| Gasto | `#D1493F` | Rojo gastos |
| Deuda | `#B8791F` | Ámbar deudas |

> En Android estos tokens ya existen en `core/ui/theme/Color.kt` con los mismos hex.
> Diferencia: **el escritorio no tiene modo oscuro real** (paleta clara fija con brushes
> `DynamicResource`); Android sí tiene `LightColors`/`DarkColors`. La paridad va de móvil→
> escritorio en este punto, no al revés.

### 2.2 Tipografía (Segoe UI en escritorio → system/Roboto en Android)

| Rol | Escritorio | Tamaño | Peso | Equivalente Compose |
|---|---|---|---|---|
| H1 (título de pantalla) | `H1` | 22 | Bold | `headlineSmall`/`titleLarge` bold |
| H2 (sección) | `H2` | 16 | SemiBold | `titleMedium` |
| Cuerpo | default | 14 | Regular | `bodyLarge` |
| Muted (labels) | `Muted` | 13 | Regular, `TextMuted` | `bodySmall` + onSurfaceVariant |
| Cifra destacada | inline | 20–26 | Bold | `headlineSmall`/`Medium` bold |

### 2.3 Espaciado, radios y formas

| Propiedad | Valor escritorio | Nota para móvil |
|---|---|---|
| Margen de pantalla | `28` | En móvil usar `16` (pantallas angostas) |
| Radio de tarjeta | `12` | Igual |
| Padding de tarjeta | `16` | Igual |
| Radio de botón | `10` | Igual |
| Alto botón primario | `44` | Igual (touch target ≥48 ideal) |
| Gap entre tarjetas de resumen | `12` | Igual |
| Borde | `1px` Outline | Igual |
| Sombra | **ninguna** (flat, solo borde) | Mantener flat en móvil |

**Insight de diseño**: el look es **flat con bordes cálidos** (sin sombras/elevación
Material fuerte). Para que Compose se vea igual conviene bajar la elevación de las `Card`
(`CardDefaults.cardColors` + `elevation = 0.dp` o borde en vez de sombra).

---

## 3. Componentes clave (specs para portar)

### 3.1 Sidebar de navegación (`Nav` style)
- Botón alto `46`, radio `8`, padding horizontal `14`, alineado a la izquierda.
- Ícono **Tabler** (línea 2px, `StreamGeometry` 24×24) + label; color del ícono sigue al
  `Foreground` del botón.
- Estado activo: fondo `Azul`, texto blanco. Hover: fondo `#0E3A5A`.
- Íconos usados: `IcHome, IcCoin, IcUsers, IcCalendar, IcChart, IcStore, IcSettings, IcPencil, TrashGeo`.
- **En móvil**: hoy es un `ModalNavigationDrawer` con emojis. Para paridad → migrar a
  **íconos vectoriales de línea** (los mismos trazos Tabler como `ImageVector`), y considerar
  una **NavigationBar inferior** para los 4–5 destinos raíz (más idiomático en Android) +
  drawer para el resto.

### 3.2 Tarjeta (`Card` style)
- `Surface` + borde `Outline 1px` + radio `12` + padding `16`. Sin sombra.
- Variante destacada: fondo `Navy` (Balance) o `TostadoClaro` (bloque de resumen).

### 3.3 Botones
- **Primary**: azul, blanco, alto 44, radio 10, hover baja opacidad, disabled 0.5.
- **Ghost**: transparente con borde Outline, texto azul; hover fondo `TostadoClaro`.
- Patrón "acceso rápido": Ghost alto `70` en `UniformGrid Columns=3` con emoji + texto.

### 3.4 Listas de movimientos (`ItemsControl` + `DataTemplate`)
- Fila = `Card` (padding `14,10`), Grid de 3 columnas:
  `[icono + título/subtítulo] [monto bold color semántico] [Editar | Eliminar]`.
- Título `SemiBold TextMain`, subtítulo `Muted`. Monto verde/rojo según tipo.
- **En móvil** ya existe `MovimientoRow.kt`; alinear a este layout exacto (icono 20sp,
  acciones Editar/Eliminar como texto, no íconos).

### 3.5 Inputs y filtros
- `TextBox`/`DatePicker` alto `32`, padding `8,4`, dentro de tarjeta de filtros.
- Barra de filtros de Movimientos: `WrapPanel` con Desde/Hasta/Buscar + fila de botones
  (Buscar primario, Hoy/Este mes ghost).

### 3.6 Bloque de resumen (3 métricas)
- `Border TostadoClaro` radio 12, padding 18, `Grid` 3 columnas iguales:
  Ingresos (verde) · Gastos (rojo) · Balance (TextMain). Cifras `16 Bold`.

### 3.7 Gráficos (Reportes)
- **Dona** de gastos por categoría: `Canvas 180×180` dibujado a mano + leyenda lateral
  (`LegendHost`). Paleta `CategoriaColores` (7 tonos derivados de marca).
- **Barras** ingresos/gastos: `StackPanel` (`BarsHost`) con barras proporcionales.
- **Evolución 12 meses**: `Grid` 160px de alto (`EvoHost`) + leyenda (punto verde/rojo).
- Export **PDF** (QuestPDF) y **CSV**.
- **En móvil**: ya están la dona, las barras y la evolución en Reportes (Tanda C ✅).

### 3.8 Diálogos (Windows)
- Ancho fijo (~430), `SizeToContent=Height`, `ResizeMode=NoResize`, `CenterOwner`.
- H1 arriba, campos con label `Muted` + input, mensaje de error `Gasto` oculto,
  fila inferior `[Cancelar Ghost] [Guardar Primary]`.
- **En móvil**: equivalen a `AlertDialog` (Cuentas/Metas/Negocios ya siguen este patrón).

### 3.9 Calculadora (nueva, ya en ambas)
- Escritorio: ventana flotante `Topmost`, pegada al costado, sigue a la ventana principal.
- Móvil: pantalla en el drawer. Misma lógica de ejecución inmediata.

---

## 4. Inventario de pantallas y estado de paridad

| Pantalla escritorio | Layout | ¿Existe en móvil? | Gap de UI a cerrar |
|---|---|---|---|
| **Inicio/Dashboard** | 4 tarjetas resumen (Ingresos/Gastos/Balance navy/Me deben) + recordatorio deudas + metas + accesos rápidos 3×2 | Sí | Alinear a 4 tarjetas + grilla de accesos + tarjeta Balance en navy |
| **Movimientos** | Filtros + bloque resumen 3 + botones + toolbar (Cuentas/Categorías/Presupuestos/Recurrentes/Remito) + 2 listas | Sí (con filtros) | Agregar toolbar de herramientas; Remito; paridad de filas |
| **Quién me debe** | Lista de deudas con estado | Sí | Revisar paridad de fila/acciones |
| **Agenda** | Calendario con tareas/turnos/productos | Sí (Tanda E ✅) | — |
| **Reportes** | Selector mes ◀▶ + toggle todos-negocios + balance/barras + dona + evolución + comparativa + presupuestos + export | Parcial (evolución, presupuestos) | Dona, barras, comparativa entre negocios, export PDF/CSV |
| **Negocios** | Lista + selector arriba (ComboBox) | Sí (Tanda 5) | Selector rápido en top bar |
| **Configuración** | Apariencia + moneda + herramientas | Sí | Revisar orden/estilo |
| Cuentas/Categorías/Metas/Recurrentes/Presupuestos | Windows de gestión | Sí (screens) | OK |
| Remito PDF | Window + QuestPDF | **No** | Portar (depende multi-negocio ✅) |
| Onboarding/Welcome/Login | Windows | Parcial (onboarding) | Revisar |

---

## 5. Diferencias estructurales escritorio ↔ móvil (decisiones de adaptación)

1. **Navegación**: escritorio usa **sidebar persistente**; móvil no tiene ancho para eso.
   - Decisión recomendada: **NavigationBar inferior** (Inicio, Movimientos, Deudas, Reportes)
     + **drawer** para el resto (Negocios, Calculadora, Config). Ya hay drawer; sumar la barra
     inferior mejora la ergonomía y acerca la sensación de "todo a la vista".
2. **Selector de negocio**: escritorio lo tiene en la top bar (ComboBox). Móvil: agregar un
   **chip/menú en el TopAppBar** que abra el selector rápido (además de la pantalla Negocios).
3. **Gestión secundaria**: escritorio abre Windows; móvil usa pantallas de nav. Mantener así
   (idiomático), replicando el contenido/campos 1:1.
4. **Densidad**: bajar márgenes de 28→16, mantener radios/paddings de tarjetas.
5. **Íconos**: pasar de emojis a **vectores de línea** (portar los `StreamGeometry` Tabler a
   `ImageVector`) para look consistente y nítido.
6. **Flat design**: reducir elevación de Cards de Compose; usar borde `Outline`.

---

## 6. Roadmap de paridad de UI (por tandas)

### 🎨 Tanda A — Sistema de diseño y navegación (base) ✅
- [x] Componentes reutilizables Compose en `core/ui/components/DisenioApp.kt`:
  `TarjetaApp` (flat, borde 1px, sin sombra), `TarjetaMetrica` (con variante navy destacada),
  `TituloPantalla` (H1), `TituloSeccion` (H2), `TextoMuted`, `BotonGhost`,
  `BloqueResumen3` + `MetricaResumen`.
- [x] Íconos Tabler portados a `ImageVector` en `core/ui/icons/IconosApp.kt`
  (Inicio, Movimientos, Deudas, Agenda, Reportes, Negocios, Configuración, Calculadora,
  Editar, Eliminar) — mismos trazados que el escritorio.
- [x] **NavigationBar inferior** (`core/navigation/BarraInferior.kt`) con los 4 destinos raíz;
  destinos compartidos en `DestinosNav.kt`; el drawer ahora usa los mismos íconos vectoriales
  y separa raíz/secundarios.
- [x] Cards flat (elevación 0 + borde `outline`) vía `TarjetaApp`.
- [ ] Pendiente: `FilaMovimiento` y `BarraFiltros` reutilizables (se hacen en Tanda B).

### 🏠 Tanda B — Dashboard y Movimientos a paridad
- [x] Dashboard: 4 tarjetas de resumen (Ingresos/Gastos/**Balance navy**/Me deben en 2×2),
  recordatorio de deudas pendientes y accesos rápidos en grilla.
- [x] Dashboard: metas de ahorro con barra de progreso y acceso "Gestionar" (`FilaMeta`).
- [x] Movimientos: título H1 + rango, `BloqueResumen3` (Ingresos/Gastos/Balance) y toolbar de
  herramientas desplazable (Cuentas/Categorías/Recurrentes/Metas). Pendiente: Presupuestos y
  Remito (aún no existen en móvil).

### 📊 Tanda C — Reportes completos ✅
> Corrección al análisis inicial: la dona, las barras, el selector de mes ◀▶ y el export
> PDF/CSV **ya existían** en móvil. Lo que realmente faltaba era el multi-negocio.
- [x] ~~Dona de gastos por categoría~~ — ya existía (`GraficoDona` + `CategoriaColores`).
- [x] ~~Barras ingresos/gastos~~ — ya existía (`BarrasIngresoGasto`).
- [x] ~~Selector de mes ◀▶~~ / ~~Export PDF/CSV~~ — ya existían.
- [x] **Toggle "Ver todos los negocios (combinado)"**: consultas agregadas sin filtro de
  negocio (`sumaRangoTodos`, `observarRangoTodos`) y `ObservarReporteMensualUseCase(mes, todos)`.
  Solo se muestra si hay más de un negocio.
- [x] **Comparativa entre negocios**: `TotalPorNegocio` (GROUP BY negocioId) +
  `ObservarComparativaNegociosUseCase` → `ResumenNegocio` (ingresos/gastos/balance por negocio),
  con barras proporcionales. Solo visible con más de un negocio.
- [x] Polish del sistema de diseño (TituloSeccion / TarjetaApp flat).

### 🏪 Tanda D — Multi-negocio en la top bar + Remito ✅
- [x] **Selector rápido de negocio** en el `TopAppBar` del Inicio (`SelectorNegocioTopBar`):
  menú desplegable con ✓ en el activo; se oculta si hay un solo negocio (como el escritorio
  en plan Personal).
- [x] **Remito PDF**: `RemitoScreen` (cliente, N°, fecha, ítems cantidad × precio con
  subtotal y total) + `RemitoPdf` (tabla A4 con la API nativa `PdfDocument`, sin dependencias)
  y compartir por intent. Accesible desde la toolbar de Movimientos (🧾 Remito).

### 📅 Tanda E — Agenda ✅
- [x] **Agenda portada**: tabla `agenda` (migración **v11→v12**, con índice por fecha y
  `negocioId`), entidad/DAO/modelo/mapper/repo/use cases y filtrado por negocio activo.
- [x] `AgendaScreen`: **calendario mensual** (grilla 7×N que arranca en lunes, con hoy
  remarcado, día seleccionado en primario y punto en los días con entradas), lista del día
  seleccionado, FAB para agregar y diálogo de alta/edición con tipo
  (Tarea / Turno / Producto), título, hora y descripción. Check para marcar hecho (tachado).
- [x] Acceso desde el menú lateral y desde los accesos rápidos del Inicio.
> Adaptación móvil: en el escritorio la Agenda está en la barra lateral principal; en móvil
> la barra inferior se mantiene en 4 destinos y la Agenda vive en el drawer + acceso rápido,
> para no saturar la navegación inferior.


### 🌙 Tanda F — Pulido ✅
- [x] **Configuración** a paridad: estructura por tarjetas con el título del grupo en
  MAYÚSCULAS estilo `Muted` (APARIENCIA / MONEDA / HERRAMIENTAS / APLICACIÓN), como el
  `SettingsView` del escritorio. Herramientas como lista con divisores + Agenda y Negocios.
- [x] **Estados vacíos** alineados con los textos guía del escritorio (metas, recurrentes)
  y agregado el de **presupuestos** ("No definiste presupuestos. Cargalos en
  Configuración → Categorías"), que en móvil simplemente ocultaba la sección.
- [x] **Onboarding** a paridad: textos y títulos alineados con el del escritorio, tarjetas flat
  y una tarjeta nueva de multi-negocio. Bug encontrado al probarlo: la pantalla no tenía
  `Surface` propio, así que quedaba con fondo blanco mientras el texto usaba los colores del
  tema (ilegible en modo oscuro). Corregido.

---

## 7. Riesgos y consideraciones móviles
- **Export PDF**: en escritorio es QuestPDF (.NET); en Android se resolvió con la API nativa
  `PdfDocument` (reportes y remitos), sin dependencias extra. CSV sin dependencias.
- **Agenda**: portada en la Tanda E con calendario propio (sin librerías externas).
- **Barra inferior + drawer**: evitar duplicar destinos; la barra son los 4 raíz, el drawer
  el resto. Mantener un único origen de verdad de rutas (`Destination`).
- **Touch targets**: subir alturas a ≥48dp donde el escritorio usa 30–34 (acciones de fila).
- **Modo oscuro**: el escritorio no lo tiene; no forzar paridad hacia atrás. Mantener el
  soporte oscuro que ya tiene Android.

---

## 8. Resumen ejecutivo
La identidad visual (paleta, tipografía, tarjetas flat, semántica de colores) **ya coincide**
a nivel de tokens. El trabajo de paridad es sobre todo **navegación (sidebar→barra+drawer),
componentes reutilizables, Reportes (dona/barras/comparativa/export) y features faltantes
(Agenda, Remito)**. Orden sugerido: **A → B → C → D**, dejando **E (Agenda)** como decisión
de alcance y **F** como pulido final.

---

## 9. Verificación en emulador (2026-09-18)

Primera ejecución real tras las tandas A–F. **La app se cerraba al arrancar.**

**Causa**: `AgendaEntity` no declaraba índices, pero `MIGRATION_11_12` creaba
`ix_agenda_fecha`. Room valida el esquema en cada apertura y aborta si encuentra un índice
que la entidad no declara:

```
IllegalStateException: Migration didn't properly handle: agenda(...)
  Expected: ... indices=[]
  Found:    ... indices=[Index{name='ix_agenda_fecha', columns=[fecha]}]
```

**Fix**: declarar el índice en la entidad con el mismo nombre
(`indices = [Index(value = ["fecha"], name = "ix_agenda_fecha")]`). Se conservó el nombre
`ix_agenda_fecha` (en vez de la convención `index_tabla_columna` que usan las tablas viejas)
para que sirva tanto si la migración ya corrió como si no.

**Lección**: cuando una migración crea un índice, la entidad tiene que declararlo con el
mismo nombre. Esto **no lo detecta el compilador**: solo falla en runtime.

**Verificado en emulador tras el fix**:
- Arranca sin crashes; base en `user_version = 12` con todas las tablas y el índice.
- Dashboard con las 4 tarjetas, metas, accesos rápidos y barra inferior con íconos vectoriales.
- Agenda: calendario correcto (arranca en lunes, hoy resaltado), alta/edición/borrado de
  entradas y el punto del día se actualiza solo (flujo reactivo Room → Flow → UI).

### Upgrade de toolchain (2026-09-24)

El working tree tenía un upgrade sin commitear (**AGP 8.13.2 → 9.3.3, Kotlin 2.0.21 → 2.2.10,
KSP → 2.3.6**) que rompía el build: KSP 2.3.6 eliminó KSP1 y el proyecto lo forzaba con
`ksp.useKSP2=false`. Con KSP2 y Room 2.6.1 aparecía `unexpected jvm signature V`.

**Resuelto subiendo Room 2.6.1 → 2.7.2** (soporta KSP2) y quitando `ksp.useKSP2=false`.
Verificado en emulador: Room 2.7.2 abre sin problema la base **v12 creada por Room 2.6.1**.

### Segundo hallazgo visual verificado

- **Tarjeta "Balance" en modo oscuro**: usaba `Navy`, que se confunde con el fondo oscuro
  (en el escritorio resalta porque las demás tarjetas son blancas). Ahora usa `Azul` cuando
  el tema es oscuro, detectado por luminancia del `background`.
- Los **emojis** en accesos rápidos y toolbar **no son una inconsistencia**: el escritorio hace
  exactamente lo mismo (vectores Tabler solo en la barra lateral). No se tocaron.
