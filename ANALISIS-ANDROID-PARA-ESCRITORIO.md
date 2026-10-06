# Mis Finanzas — Análisis de la app Android (referencia para el escritorio)

> Documento de paridad. Describe **qué hace y cómo está hecha la app Android** (`C:\Proyectos\GP`,
> paquete `com.tallerapp`) con el nivel de detalle necesario para que la app de escritorio
> (`C:\Proyectos\MisFinanzasDesktop`, C#/WPF) pueda replicar cada función y las dos versiones
> avancen juntas.
>
> Estado al momento de escribirlo: Android `versionName 1.4` / `versionCode 9`, esquema de base **v15**.
> Escritorio `1.0.2`, esquema `PRAGMA user_version` objetivo **6**.
>
> Mantenimiento: cada vez que se agrega o cambia una función en cualquiera de las dos apps, actualizar
> la sección 11 (paridad) y, si cambia el modelo de datos, la sección 4.

---

## 1. Qué es la app

App Android de **finanzas personales + gestión de varios negocios**, 100 % local (offline-first, sin
servidor). Mismo producto que la app de escritorio, con la misma marca ("Mis Finanzas"), la misma
paleta y la misma nomenclatura de pantallas.

Modelo de negocio: **uso personal gratis, uso comercial pago** (plan Pro por suscripción de Google
Play). En el escritorio el equivalente es la licencia Ed25519 (`Licensing/`).

---

## 2. Stack y build

| | Android | Escritorio |
|---|---|---|
| Lenguaje | Kotlin | C# |
| UI | Jetpack Compose + Material 3 | WPF (XAML) |
| Base de datos | SQLite vía **Room 2.7.2** | SQLite vía `Microsoft.Data.Sqlite` (SQL propio) |
| PDF | `android.graphics.pdf` (dibujo propio) | QuestPDF |
| Gráficos | Canvas de Compose (`core/ui/components/Charts.kt`) | Canvas/Path de WPF |
| Monetización | Google Play Billing 9.1.0 | Licencia Ed25519 + BouncyCastle |
| Actualización | Play In-App Updates 2.1.0 | `Services/Online/UpdateService` |
| Extra | Escáner de códigos (`play-services-code-scanner`) | — |

- `compileSdk`/`targetSdk` 36, `minSdk` 26, Java/Kotlin target 17.
- `release` con `isMinifyEnabled = true` + `shrinkResources`; firma desde `keystore.properties`
  (fuera de git; sin ese archivo la app compila igual y queda sin firmar).
- Sin Gradle wrapper en el repo: compilar con el Gradle instalado o desde Android Studio.
- Tests JVM puros del dominio: `DineroTest`, `IngresoValidatorTest`, fakes en `testutil/Fakes.kt`.

---

## 3. Arquitectura (capas)

Clean Architecture simple, sin framework de inyección de dependencias:

```
features/<pantalla>/        Composables + ViewModel (estado de UI)
core/                       navegación, tema, componentes, utilidades, billing, backup, export, DI
domain/model/               modelos puros (data class)
domain/repository/          interfaces
domain/usecase/             casos de uso (una clase por acción, operator invoke)
domain/validation/          validadores + objetos de error
data/local/                 Room: Entities, DAOs, Database, Migraciones, DatosIniciales
data/mapper/                Entity <-> Model
data/repository/            implementaciones de los repositorios
```

Piezas clave:

- **`core/di/AppContainer.kt`**: contenedor manual. Construye la base, los repositorios y **todos** los
  casos de uso como `val`. `AppContainerCompose.kt` lo expone a los Composables.
- **`core/NegocioActual`**: `StateFlow<Long>` global con el negocio seleccionado, persistido en
  `SharedPreferences` (`mis_finanzas_prefs` / `negocio_actual_id`, default `1`). Los casos de uso
  combinan ese Flow con las consultas, así que al cambiar de negocio **toda** la UI se refresca sola.
  Equivale al selector de negocio de la barra superior del escritorio.
- **`core/billing/EstadoPlan`**: `StateFlow<Plan>` global. **No se persiste a propósito** (sería trivial
  falsificarlo editando un archivo); manda Google Play en cada arranque.
- **`core/ui/theme/TemaApp`**: preferencias de apariencia, moneda y perfil como estado observable de Compose.

Reglas transversales:

1. **Dinero siempre en centavos** (`Long`). Ningún `double` para importes. Ver `core/util/Dinero.kt`.
2. **Fechas en epoch millis** (`Long`), normalizadas al **inicio del día en hora local**. Ver `core/util/Fechas.kt`.
   *Divergencia importante con el escritorio, que guarda `fecha` como `TEXT`.* Ver sección 11.
3. Los rangos son siempre `[inicio, fin)` (mes calendario, día).
4. Todo filtro por negocio se hace en SQL (`WHERE negocioId = :ng`), no en memoria.

---

## 4. Modelo de datos (Room, esquema v15)

Archivo de base: `tallerapp.db`. Versión expuesta en `TallerDatabase.VERSION_ESQUEMA = 15`.
`fallbackToDestructiveMigrationOnDowngrade()` — hacia adelante **siempre** hay migración real.

### 4.1 Tablas con `negocioId` (datos por negocio)

| Tabla | Columnas |
|---|---|
| `ingreso` | `id`, `montoCentavos`, `concepto`, `cuenta` (def. `'Efectivo'`), `negocioId` (def. `1`), `fecha`, `fechaRegistro`, `origen`, `trabajoId` — índice por `fecha` |
| `egreso` | `id`, `montoCentavos`, `categoria`, `concepto`, `cuenta` (def. `'Efectivo'`), `negocioId` (def. `1`), `fecha`, `fechaRegistro`, `proveedorId` — índice por `fecha` |
| `deuda` | `id`, `nombre`, `montoCentavos`, `fecha`, `nota`, `cobrada`, `fechaCobro`, `fechaRegistro`, `fechaLimite`, `negocioId` (def. `1`), `contactoId` — índice por `cobrada` |
| `agenda` | `id`, `tipo` (`tarea` / `turno` / `producto`), `titulo`, `descripcion`, `fecha`, `hora` (`"HH:mm"` o `""`), `hecho`, `createdAt`, `negocioId` — índice `ix_agenda_fecha` |
| `recurrente` | `id`, `tipo` (`ingreso` / `egreso`), `montoCentavos`, `concepto`, `categoria`, `cuenta`, `diaMes` (1..28), `activo`, `ultimoGenerado` (`"yyyy-MM"`), `createdAt`, `negocioId` |
| `contacto` | `id`, `negocioId`, `nombre`, `tipo` (`cliente` / `proveedor` / `ambos`), `documento`, `telefono`, `email`, `direccion`, `nota`, `createdAt` — único `(negocioId, nombre)` |
| `producto` | `id`, `negocioId`, `nombre`, `codigoBarras`, `descripcion`, `precioCentavos`, `descuentoPct` (Double), `stock` (Double), `stockMinimo` (Double), `imagen` (ruta), `createdAt` — índices por negocio y por código |
| `factura` | `id`, `negocioId`, `numero`, `cliente`, `documento`, `fecha`, `descuentoPct`, `subtotalCentavos`, `totalCentavos`, `notas`, `cuenta`, `ingresoId`, `clienteId`, `createdAt` |
| `factura_item` | `id`, `facturaId`, `productoId`, `descripcion`, `cantidad` (Double), `precioUnitCentavos`, `descuentoPct`, `subtotalCentavos` |

### 4.2 Tablas globales (compartidas por todos los negocios)

| Tabla | Columnas | Nota |
|---|---|---|
| `negocio` | `id`, `nombre`, `createdAt` | Se siembra "Personal" con id 1 |
| `categoria` | `id`, `tipo` (`ingreso` / `egreso`), `nombre`, `color` (`#RRGGBB`), `icono` (emoji), `orden`, `activo`, `presupuestoCentavos` | **No** tiene `negocioId` |
| `cuenta` | `id`, `nombre`, `icono`, `saldoInicialCentavos`, `orden`, `activo` | **No** tiene `negocioId`; los saldos se calculan sobre **todos** los negocios |
| `meta` | `id`, `nombre`, `objetivoCentavos`, `actualCentavos`, `fechaObjetivo`, `createdAt` | **No** tiene `negocioId` |

> Decisión a replicar tal cual: categorías, cuentas y metas son globales; movimientos, deudas, agenda,
> recurrentes, contactos, productos y facturas van por negocio.

### 4.3 Relaciones por id (todas opcionales, arrancan en NULL)

- `egreso.proveedorId` → `contacto.id`
- `deuda.contactoId` → `contacto.id`
- `factura.clienteId` → `contacto.id`
- `factura.ingresoId` → `ingreso.id` (la factura que se registró en caja)
- `factura_item.productoId` → `producto.id`

No hay foreign keys declaradas: el historial ya emitido conserva el **texto** que guardó en su momento
y nada se reescribe si después se borra la ficha.

### 4.4 Historial de migraciones (contexto útil para el escritorio)

| | Cambio |
|---|---|
| 1→2 | Tablas `ingreso` y `egreso` |
| 2→3 | `fechaEntrega` en trabajos (etapa "taller", ya retirada) |
| 3→4 | Se retira la tabla de trabajos; entra `deuda` |
| 4→5 | `categoria` personalizable (color, ícono) + migración de los enums en MAYÚSCULAS a nombres visibles |
| 5→6 | `cuenta` + columna `cuenta` en ingreso y egreso; el "método" del ingreso pasa a cuenta |
| 6→7 | `presupuestoCentavos` en categoría + tabla `meta` |
| 7→8 | `fechaLimite` en deuda |
| 8→9 | `recurrente` |
| 9→10 | `contacto` (lista de nombres, para autocompletar deudas) |
| 10→11 | Multi-negocio: tabla `negocio`, siembra "Personal", `negocioId` en los movimientos |
| 11→12 | `agenda` |
| 12→13 | Se **retira** `metodo` del ingreso (duplicaba `cuenta`). Se pierde solo el detalle del reparto de un pago mixto; el monto queda intacto |
| 13→14 | `producto`, `factura`, `factura_item` |
| 14→15 | `contacto` se recrea como ficha de cliente/proveedor por negocio; los existentes pasan al negocio 1 con tipo `cliente` |

Lección registrada en el código: **los nombres de los índices en el SQL de la migración tienen que
coincidir exactamente** con los que declara la entidad, o Room aborta el arranque.

### 4.5 Datos iniciales (`DatosIniciales.kt`)

Categorías de egreso: Alimentos `#4A7C59` 🍽️, Transporte `#0A6E8C` 🚗, Servicios `#023A5D` 🧾,
Hogar `#98643A` 🏠, Salud `#C0544B` ⚕️, Ocio `#7A5AA6` 🎉, Otros `#8A8D91` 📦.

Categorías de ingreso: Sueldo `#3E8E6E` 💼, Ventas `#0A6E8C` 🛒, Extras `#C99A6D` ✨, Otros `#8A8D91` 📦.

Cuentas: Efectivo 💵, Banco 🏦, MercadoPago 💳.

`"Efectivo"` no es opcional: es el default de la columna `cuenta` y el repositorio **se niega a
borrarla**. Al renombrar una cuenta se actualizan en cascada los movimientos que la usaban
(`UPDATE ingreso SET cuenta = :nuevo WHERE cuenta = :viejo`, ídem egreso).

---

## 5. Lógica de negocio a replicar

### 5.1 Dinero (`core/util/Dinero.kt`)

- `parsearACentavos(texto)`: regla **unificada con el escritorio** (punto A1 del plan, ya aplicado). La
  coma es siempre el decimal y entonces los puntos son miles; sin coma, el punto es decimal salvo que el
  texto tenga forma de miles (parte entera de 1 a 3 dígitos que no empieza con 0 y grupos siguientes de
  exactamente 3 dígitos), así `"1.500"` = 1500 y `"1500.50"` = 1500,50. Rechaza vacío, no numérico,
  negativos, dos comas, varios puntos que no son miles, exponentes e `Infinity`.
- `formatear(centavos)`: `$ 1.500,50` — punto para miles, coma para decimales, signo adelante del símbolo.
- `centavosAEntrada(centavos)`: `1500.50`, para los formularios. Usa punto porque es el **valor crudo**
  de `CampoMonto`, que lo muestra con coma y separador de miles; el escritorio devuelve `1500,50` porque
  ahí el usuario ve el texto tal cual. El parseo acepta las dos formas.
- `equivalente(centavos)`: segunda moneda opcional, `≈ US$ 12,50`, solo si está activada y hay tasa > 0.
- Estado global: `simbolo`, `simboloSecundario`, `tasa`, `mostrarEquivalente`.

### 5.2 Fechas (`core/util/Fechas.kt`)

Zona horaria del dispositivo. Formato visible `dd/MM/yyyy`. Mes en texto con locale `es-AR`
("Agosto 2026"). Helpers: `hoyInicioMillis`, `rangoDeHoy`, `rangoDelMesActual`, `rangoDelMes(YearMonth)`,
`esHoy`, y la conversión de ida y vuelta al `DatePicker` (que trabaja en medianoche UTC).

### 5.3 Validaciones

| Entidad | Reglas |
|---|---|
| Ingreso | monto > 0 ("Ingresá un monto válido"), concepto no vacío ("Ingresá el concepto") |
| Egreso | monto > 0, categoría elegida ("Elegí la categoría"), concepto no vacío |
| Deuda | nombre no vacío ("Ingresá quién te debe"), monto > 0 |

Cada validador devuelve un objeto de errores por campo (`esValido` si todos son `null`); el caso de uso
devuelve un `sealed interface` (`Exito` / `Invalido(errores)`). Los textos van tal cual en la UI.

### 5.4 Recurrentes (`GenerarRecurrentesUseCase`)

Corre al iniciar. Para cada recurrente **activo** cuyo `diaMes` ya pasó y cuyo `ultimoGenerado` no es el
`"yyyy-MM"` del mes actual: crea el ingreso o el egreso con fecha `min(diaMes, últimoDíaDelMes)` y marca
el mes como generado. **Idempotente por mes.** Devuelve cuántos creó.

### 5.5 Precios y descuentos (`domain/model/Producto.kt`, objeto `Precios`)

- `descuento(centavos, pct)`: `pct` se recorta a 100 como máximo; redondeo con `roundToLong`.
- `subtotal(cantidad, precioUnit, pct)`: `round(cantidad * precioUnit)` y después se aplica el descuento.
- Producto: `precioFinalCentavos`, `tieneDescuento`, `stockBajo` (= `stockMinimo > 0 && stock <= stockMinimo`).

### 5.6 Emitir factura (`EmitirFacturaUseCase`) — la pieza más delicada

1. Requiere **Pro**; si no, devuelve `RequierePro`. Rechaza sin ítems y sin cliente.
2. Si la factura no tiene `clienteId`, **da de alta el contacto** con ese nombre y tipo `cliente`
   (reusa el existente, no duplica por facturar de nuevo).
3. **Recalcula** el total en el dominio: `subtotal = Σ subtotal de ítems` (cada ítem ya trae su descuento)
   y `total = subtotal − descuento general`. Nunca se confía en el total que llega de la pantalla.
4. Guarda cabecera + ítems.
5. **Opcional** `descontarStock`: resta `cantidad` del stock de cada ítem que venga del catálogo.
6. **Opcional** `registrarIngreso`: crea un ingreso por el total, concepto
   `Factura N° <numero> — <cliente>`, cuenta elegida (o `Efectivo`), y vincula `factura.ingresoId`.
7. Con los dos opcionales en `false`, la factura es **solo un documento**.

Borrar una factura borra cabecera e ítems, pero **no** revierte el stock ni borra el ingreso: eso ya pasó
en la realidad y se corrige desde Movimientos.

`SiguienteNumeroFacturaUseCase`: `(cantidad de facturas del negocio + 1)` con 4 dígitos (`0001`), como
**sugerencia editable** — el usuario puede llevar su propia numeración.

### 5.7 Guardar producto

Requiere Pro. Nombre obligatorio. `descuentoPct` se recorta a `0..100`. El **código de barras repetido
se avisa, no se bloquea** (`CodigoRepetido(existente)`): un escaneo en la cinta no debe hacer fallar la
operación, decide el usuario.

---

## 6. Navegación y pantallas

Rutas centralizadas en `core/navigation/Destination.kt`. Estructura: barra inferior (4 destinos raíz) +
menú lateral (`AppDrawer`) con el resto, agrupado.

**Raíz (barra inferior):** Inicio (dashboard) · Movimientos (finanzas) · Me deben (deudas) · Reportes.

**Secundarios (menú):** Agenda · Negocios · Calculadora · Planes · Configuración.

**Bloque de herramientas comerciales (menú, con candado si el plan es Gratis):** Productos · Clientes ·
Proveedores · Facturas.

> Criterio de UX a respetar: el bloque Pro **se muestra siempre** ("nadie compra lo que no sabe que
> existe"). El candado salta **al guardar**, no esconde la pantalla; al tocar una entrada bloqueada se
> abre Planes.

| Pantalla | Qué hace |
|---|---|
| **Inicio** | 4 tarjetas del mes: Ingresos, Gastos, **Balance** destacado (con equivalente en 2ª moneda) y "Me deben". Recordatorio de deudas pendientes, metas de ahorro con acceso a "Gestionar", bloque "Registrar" y "Accesos rápidos" |
| **Movimientos** | Caja del día + búsqueda por texto (concepto, categoría, cuenta), filtro de rango con atajo "Este mes", chips Ingresos/Gastos, totales (Ingresos, Gastos, Balance), listas separadas con edición y anulación con diálogo de confirmación |
| **Me deben** | Lista ordenada por `cobrada ASC, fecha DESC`, total pendiente, marcar cobrada, editar, eliminar, fecha límite |
| **Reportes** | Mes navegable; "Ver todos los negocios (combinado)"; balance del mes; dona de gastos por categoría; evolución de 12 meses; comparativa entre negocios (barras proporcionales); presupuestos del mes; exportar a **PDF** y **Excel/CSV** (Pro, con aviso y botón "Ver Pro"). Texto comparativo automático: "Gastaste X % más/menos que el mes pasado" |
| **Agenda** | Grilla mensual de 7 columnas empezando en **lunes**, con marcas por día + lista del día elegido. Tarea / Turno / Producto, con hora opcional y check de "hecho" |
| **Negocios** | Crear, renombrar, eliminar. Límite del plan Gratis: **1 negocio** (`LimitesPlan.NEGOCIOS_GRATIS`) |
| **Productos** | Catálogo del negocio: buscar, **escanear**, crear, editar, eliminar. Fila con foto, nombre, código, precio (tachado si hay descuento) y stock. El escáner es un atajo de búsqueda: si el código existe abre ese producto, si no arranca uno nuevo con el código puesto |
| **Clientes / Proveedores** | Una sola pantalla parametrizada por tipo: buscar, crear, editar, eliminar. Ficha con documento, teléfono, correo, dirección y nota |
| **Facturas** | Listado + formulario de emisión. El escáner agrega el producto con su precio y descuento; escanear de nuevo el mismo artículo **suma cantidad**. Líneas libres (flete, mano de obra) a mano. Dos casillas al emitir: descontar stock / registrar en caja. PDF del comprobante |
| **Remito** | Cliente, número, fecha e ítems (cantidad × precio) con PDF. Equivale a `RemitoWindow` |
| **Metas** | Metas de ahorro con barra de progreso (`fraccion`; verde "cumplida" `#27AE60`) |
| **Categorías** | CRUD con color, ícono y **presupuesto mensual** |
| **Cuentas** | CRUD con ícono y saldo inicial; saldo calculado = inicial + ingresos − egresos por nombre de cuenta |
| **Recurrentes** | CRUD de movimientos automáticos mensuales |
| **Calculadora** | Calculadora simple de ejecución inmediata (acumulado + operador pendiente + operando) |
| **Configuración** | Grupos en MAYÚSCULAS como el escritorio: APARIENCIA (modo sistema/claro/oscuro + 6 acentos), MONEDA (moneda, equivalente en 2ª moneda y tasa), HERRAMIENTAS (accesos a todas las pantallas de gestión), COPIA DE SEGURIDAD (guardar / restaurar), APLICACIÓN (base local, offline-first) |
| **Perfil** | Nombre y avatar elegible (anillo en el seleccionado) |
| **Planes** | Comparativa Gratis vs Pro y compra de la suscripción |
| **Onboarding** | Mismos temas y textos guía que el onboarding del escritorio |

---

## 7. Planes Gratis / Pro

Fuente de verdad: `PLANES.md`. División: **uso personal gratis, uso comercial Pro.**

**Regla que no se rompe: lo que ya cargaste nunca se te bloquea.** Si alguien tiene 4 negocios en Pro y
deja de pagar, los 4 siguen **visibles y exportables**; no puede crear más ni cargar movimientos nuevos
en los que exceden el límite, pero nada se borra ni se esconde. Motivo doble: es una app de finanzas y la
confianza es todo; y retener datos del usuario para forzar un pago puede costar una suspensión en Play.

**Gratis:** 1 negocio, movimientos ilimitados, categorías y cuentas, "Me deben" con fecha límite, metas,
recurrentes, agenda, reportes del mes (balance, dona, barras, evolución 12 meses), presupuestos,
calculadora, temas y multi-moneda.

**Pro:** negocios ilimitados, comparativa entre negocios, vista combinada en reportes, remitos PDF,
exportar PDF/Excel, catálogo de productos, clientes y proveedores, facturas.

**Por construir (en las dos plataformas):** historial y cuenta corriente por cliente; estadísticas
avanzadas (rentabilidad, producto más vendido, mejor cliente).

Implementación Android: `FacturacionPlay` con producto `pro` y planes base `mensual` / `anual`; el
resultado se escribe en `EstadoPlan`. Los casos de uso que son Pro devuelven `RequierePro` en vez de
lanzar excepción.

---

## 8. Copia de seguridad (`core/backup/CopiaSeguridad.kt`)

Formato: **un `.zip`** con dos entradas.

```
manifiesto.json      { "app": "<packageName>", "esquema": 15, "fecha": <epochMillis> }
base/tallerapp.db    el archivo de la base, copiado tal cual
```

Nombre sugerido: `mis-finanzas-YYYY-MM-DD.zip`.

Decisiones a replicar:

- Se copia **el archivo de la base**, no un volcado tabla por tabla: así ninguna tabla nueva queda afuera
  por olvido. El precio es que la copia queda atada a la versión del esquema.
- Antes de copiar, `PRAGMA wal_checkpoint(FULL)`: si no, los últimos movimientos viven todavía en el
  `-wal` y la copia sale incompleta.
- Al restaurar se trabaja primero en un archivo temporal y solo al final se pisa la base: si la copia está
  rota, el usuario conserva lo que ya tenía.
- Se **rechaza** una copia con `esquema` mayor que el de la app ("Actualizá Mis Finanzas y probá de nuevo"):
  abrirla sería un downgrade destructivo, justo lo que esta función viene a evitar.
- Después de pisar la base se borran `-wal` y `-shm` (son de la base vieja y la corromperían) y **la app se
  reinicia**, porque Room tiene la base vieja abierta en memoria.
- Las **fotos de productos no entran** en la copia (archivos aparte, pesan). Tras restaurar, esos productos
  quedan con el ícono genérico; el resto de la ficha está entero.

---

## 9. Exportación

`core/export/`:

- `ReporteContenido.construir(reporteMensual, etiquetaMes)`: arma un `ReporteExportable`
  (encabezado + secciones de filas etiqueta/valor), común a los dos formatos.
- `CsvExporter.generar(...)` → `Uri` (lo que la UI llama "Excel").
- `PdfExporter.generar(...)` → `Uri`.
- `FacturaPdf.generar(context, factura, negocio)` y `RemitoPdf.generar(context, remito)`.
- `Compartir.archivo(context, uri, mime)`: abre el menú nativo de compartir. Los archivos salen por el
  `FileProvider` declarado como `${applicationId}.fileprovider`.

---

## 10. Apariencia

Paleta de marca (`core/ui/theme/Color.kt`), compartida con el escritorio:

| Token | Hex |
|---|---|
| Navy | `#02223A` |
| Azul | `#023A5D` |
| Marrón | `#98643A` |
| Tostado | `#C99A6D` |
| Tostado claro | `#EDDFCD` |
| Fondo claro | `#F5F1EA` |
| Navy profundo (fondo oscuro) | `#011526` |
| Superficie oscura | `#032B47` |
| Blanco cálido | `#EDE6DB` |

Semánticos: **Ingreso** `#1E9E5B`, **Gasto** `#D1493F`, **Deuda** `#B8791F`, **Meta cumplida** `#27AE60`.

Paleta de la dona por categoría: `#023A5D`, `#98643A`, `#C99A6D`, `#0A6E8C`, `#6B4E2E`, `#4A7C59`, `#8A8D91`.

Acentos elegibles: Azul `#023A5D`, Rosa `#C25E7A`, Verde `#3E8E6E`, Violeta `#7A5AA6`, Rojo `#C0544B`,
Gris `#5F6B76`.

Monedas: `$` Peso, `US$` Dólar, `€` Euro, `R$` Real, `Gs` Guaraní.

Responsive: el contenido se limita a **600 dp** de ancho (`ANCHO_MAXIMO_CONTENIDO`, el límite "compact" de
Material 3) y se centra en pantallas más anchas.

Preferencias en `SharedPreferences` `mis_finanzas_prefs`: `tema_modo`, `moneda`, `moneda_sec`, `tasa`,
`mostrar_equivalente`, `accent`, `onboarding_visto`, `perfil_nombre`, `perfil_avatar`,
`negocio_actual_id`.

---

## 11. Paridad Android ↔ Escritorio

### 11.1 Lo que el escritorio no tiene todavía (el grueso del trabajo)

| Función de Android | Qué falta en el escritorio |
|---|---|
| **Catálogo de productos** | Tabla `producto` y CRUD con precio, `descuentoPct`, stock, `stockMinimo`, imagen y código de barras. (El escáner no aplica en escritorio: alta manual del código.) |
| **Clientes y proveedores** | Hoy `contacto` es solo `(id, nombre, createdAt)`, **global y único por nombre**. Hay que recrearla como ficha por negocio: `negocioId`, `tipo`, `documento`, `telefono`, `email`, `direccion`, `nota`, con único `(negocioId, nombre)`. Equivale a la migración 14→15 de Android |
| **Facturas** | `factura` + `factura_item`, numeración correlativa sugerida por negocio, descuento por línea y general, emisión con descuento de stock y registro en caja opcionales, PDF del comprobante |
| **Vínculos por id** | `egreso.proveedorId`, `deuda.contactoId`, `factura.clienteId`, `factura.ingresoId`, `factura_item.productoId` |
| **Copia de seguridad en `.zip` con manifiesto** | El escritorio ya tiene `BackupService`, pero conviene unificar el formato (manifiesto con `esquema` + la base adentro) para que una copia sea legible por las dos apps |
| **Pantalla de Planes** | Comparativa Gratis vs Pro dentro de la app, con el bloque comercial siempre a la vista y candado al guardar. El escritorio solo tiene "Administrar licencia" en Configuración |
| **Modo de tema "Seguir al sistema"** | El escritorio solo tiene Claro / Oscuro |
| **Avatar de catálogo** | Android ofrece avatares elegibles; el escritorio usa foto del disco con recorte (`CropWindow`). Decidir si se unifica o cada plataforma mantiene lo suyo |

### 11.2 Divergencias de datos a resolver antes de cualquier sincronización

| | Android | Escritorio | Acción sugerida |
|---|---|---|---|
| `fecha` de movimientos y agenda | `INTEGER` epoch millis, inicio de día local | `TEXT` | Elegir uno (recomendado: epoch millis, como Android) y migrar |
| Marca de creación | `fechaRegistro` (ingreso, egreso, deuda) | `createdAt` | Unificar el nombre |
| `ingreso.metodo` | **Retirado** en v13 (duplicaba `cuenta`) | Sigue existiendo (`MetodoPago`) | Retirar en escritorio: conservar el monto y pasar el método a `cuenta` |
| `ingreso.categoria` | **No existe** | Existe (default `'Otros'`) | Decidir: se agrega en Android o se deja de usar en escritorio |
| `recurrente.metodo` | `cuenta` | `metodo` | Unificar a `cuenta` |
| `deuda.fechaLimite` / `negocioId` | Sí | Sí | Verificar paridad de uso en la UI |
| `categoria`, `cuenta`, `meta` | Globales | Globales | Correcto, mantener así |
| Versión de esquema | `VERSION_ESQUEMA = 15` (Room) | `PRAGMA user_version` objetivo 6 | Documentar la tabla de equivalencias al unificar el formato de backup |
| Nombre del archivo de base | `tallerapp.db` | `misfinanzas.db` | Pueden seguir distintos si el backup lleva manifiesto |

### 11.3 Lo que el escritorio tiene y Android no (no hace falta copiar todo)

- **Login local con usuario y contraseña** (`AuthenticationService`, Argon2, tabla `user_profile`) y
  `ProtectedStore` con DPAPI. En Android no existe: el dispositivo ya tiene su propio bloqueo.
- **Licencia Ed25519** con `license.dat` / `clock.dat`. En Android el equivalente es Play Billing.
- **Actualizador propio** (`UpdateService`, `UpdateInstaller`, `UpdateWindow`). En Android, Play In-App Updates.
- **Agenda con tres niveles de zoom** (año / mes / día). Android solo tiene mes + día:
  **candidato a mejora en Android.**
- **Ventana de presupuestos** dedicada (`PresupuestosWindow`). En Android los presupuestos viven dentro de
  Categorías y se muestran en Reportes.

### 11.4 Reglas que las dos apps deben compartir siempre

1. Importes en centavos (`Long`), nunca `double`.
2. Formato de dinero `$ 1.500,50`; fechas `dd/MM/yyyy`; meses con locale `es-AR`.
3. Rangos `[inicio, fin)`.
4. Mismos textos de validación, palabra por palabra.
5. Mismo set de datos iniciales (categorías, cuentas, negocio "Personal" con id 1).
6. Mismos colores semánticos y misma paleta de la dona.
7. El total de una factura se recalcula en el dominio, nunca se toma de la pantalla.
8. "Lo que ya cargaste nunca se te bloquea" al vencer el plan.
9. Categorías, cuentas y metas son globales; el resto va por negocio.
10. Nunca una migración destructiva hacia adelante.

---

## 12. Documentos relacionados

En `C:\Proyectos\GP`: `SPEC.md`, `FROZEN-SPEC.md`, `ARCHITECTURE.md`, `PLANES.md`, `ROADMAP.md`,
`MASTER-ROADMAP.md`, `AUDIT-FINAL.md`, `UI-ESCRITORIO-ANALISIS-Y-ROADMAP-MOVIL.md` (el análisis en el
sentido inverso), `RELEASE.md`, `PUBLICAR.md`.

En `C:\Proyectos\MisFinanzasDesktop`: `RESUMEN-APP.md`, `ARQUITECTURA-Y-ROADMAP.md`, `MEJORAS-FUTURAS.md`,
`docs/fases/FASE-*.md`.
