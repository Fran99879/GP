# Roadmap — Clientes, Proveedores y copia en la nube

> Plan de trabajo. **Fase 1 hecha el 29/09/2026** (ver al final de cada fase); las fases 2
> y 3 siguen pendientes. Las decisiones tomadas acá son las que se implementaron.
>
> Otros roadmaps del repo (`AUDITORIA-Y-ROADMAP-MOVIL.md`,
> `UI-ESCRITORIO-ANALISIS-Y-ROADMAP-MOVIL.md`) están cerrados y son de otra etapa.

---

## 0. Prioridad actual (01/10/2026)

**Primero va la interfaz, no las funciones nuevas.** Decidido el 01/10/2026: antes de la
Fase 2 (historial y cuenta corriente) se hace el trabajo de las secciones **12 — responsive**
y **13 — fluidez**. Razón: la app ya se puede instalar en tablet y en Chromebook, y una
función más no sirve de nada en una pantalla donde el texto cruza de punta a punta o el
listado de movimientos se traba.

Orden de trabajo, los primeros tres en una sola tanda:

| # | Trabajo | Sección | Estado |
|---|---|---|---|
| 1 | Ancho máximo del contenido en las 23 pantallas | 12, Paso 1 | ✅ hecho el 01/10/2026 |
| 2 | Diálogos con scroll | 12, transversal | ✅ hecho el 01/10/2026 |
| 3 | `LazyColumn` en Finanzas | 13, Paso 1 | ✅ hecho el 01/10/2026 |
| 4 | Revisión con el texto del sistema al 200 % | 12, transversal | ⛔ falta: pide emulador |
| 5 | Baseline Profile | 13, Paso 2 | ⛔ configuración |
| 6 | `WindowSizeClass` y grillas | 12, Paso 2 | ⛔ diseño por pantalla |
| 7 | Medición (`macrobenchmark`, estabilidad de Compose) | 13, Paso 3 | ⛔ sesión propia |
| 8 | Navegación adaptativa (`NavigationRail`) | 12, Paso 3 | ⛔ último: es el que rompe la navegación |

Los puntos 1 a 3 **compilan pero todavía no se vieron corriendo**: no hay emulador levantado.
Antes de seguir con el 5 hay que abrir la app en un emulador de tablet y en un teléfono.

Las secciones 9, 10 y 11 (Drive, códigos promocionales, modo viaje) quedan **detrás** de
esto. La Fase 2 arranca cuando los puntos 1 a 5 estén probados en emulador.

---

## 1. Por qué

De las cuatro funciones que sostienen la suscripción (`PLANES.md`), tres ya están:

| Roadmap Pro | Estado |
|---|---|
| Control de stock | ✅ hecho con el catálogo de productos |
| **Clientes** | ⛔ este documento |
| **Proveedores** | ⛔ este documento |
| Estadísticas avanzadas | ⛔ depende de las dos anteriores |

Clientes y proveedores son la **base de las estadísticas**: sin ellos no hay "mejor
cliente", ni "cuánto le compro a cada proveedor", ni cuenta corriente. Son las dos piezas
que faltan para que Pro deje de ser "pagar por más negocios".

---

## 2. Con qué se arranca (lo que ya existe)

| Pieza | Hoy | Qué implica |
|---|---|---|
| Tabla `contacto` | `id`, `nombre`, `createdAt`. Índice **único por nombre**, **sin `negocioId`** | Es la semilla, pero el único global choca con multi-negocio: dos negocios no pueden tener un "Juan" cada uno |
| `deuda.nombre` | Texto libre, autocompletado desde `contacto` | Hay que poder asociarla a un cliente sin romper las deudas viejas |
| `factura.cliente` / `factura.documento` | Texto **congelado** al emitir | Se conserva tal cual; el vínculo se agrega aparte |
| `egreso` | No tiene proveedor | Hay que agregarlo opcional |
| Patrón de capa | Entity → Dao → Mapper → Repository(+Impl con `flatMapLatest` sobre `NegocioActual`) → UseCase → ViewModel → Screen, más el alta en `AppContainer` | Copiar el de productos; ver `ProductoRepositoryImpl` y `ProductoUseCases` |
| Bloque Pro del menú | `DESTINOS_PRO` en `DestinosNav.kt`, con candado si el plan es Gratis | Las dos pantallas nuevas se suman a esa lista y heredan el gate |

---

## 3. Decisiones ya tomadas

**Una sola tabla, no dos.** `contacto` pasa a tener un campo `tipo`
(`cliente` | `proveedor` | `ambos`). En un kiosco o un taller, el mismo tipo te compra y te
vende: con dos tablas habría que cargarlo dos veces y las estadísticas nunca cerrarían. Las
pantallas sí son dos, filtrando por `tipo`.

**El histórico no se toca.** Las facturas ya emitidas siguen guardando el nombre y el
documento del cliente como texto. El vínculo (`clienteId`) es **opcional y nuevo**: sirve
para el historial de acá en adelante, no para reescribir el pasado. Mismo criterio que con
los productos borrados.

**Cuenta corriente derivada, no una tabla de saldos.** El saldo de un cliente se **calcula**
(facturas a crédito − pagos recibidos). Un campo `saldo` guardado se desincroniza con la
primera factura que se borre; ya se pagó ese error con otros totales.

**El contacto pasa a ser por negocio.** Se agrega `negocioId` y se cambia el único global
por un índice único `(negocioId, nombre)`. Sin esto, el contacto de un negocio aparece en
el selector de otro.

**Gate Pro en el caso de uso, no en la pantalla.** Igual que `GuardarProductoUseCase`: la
pantalla se ve, el candado salta al guardar y ofrece ir a Planes.

---

## 4. Esquema propuesto (base v15)

`MIGRATION_14_15`, migración real (nunca destructiva). SQLite < 3.35 no tiene `DROP
COLUMN` y `minSdk` es 26: para cambiar el índice único de `contacto` hay que **recrear la
tabla**, como se hizo en `MIGRATION_12_13`.

```
contacto  (recreada)
  id, negocioId, nombre, tipo,            -- "cliente" | "proveedor" | "ambos"
  documento, telefono, email, direccion,  -- todos opcionales, "" por defecto
  nota, createdAt
  índice único (negocioId, nombre)  ->  ix_contacto_negocio_nombre

factura   (columna nueva)
  clienteId INTEGER NULL

egreso    (columna nueva)
  proveedorId INTEGER NULL

deuda     (columna nueva)
  contactoId INTEGER NULL

pago_contacto  (tabla nueva, fase 2)
  id, negocioId, contactoId, montoCentavos, fecha, cuenta, nota, createdAt
  índice (contactoId) -> ix_pago_contacto_contacto
```

Los datos de la `contacto` actual se copian con `tipo = 'cliente'` y `negocioId = 1`: hoy
solo se usan para autocompletar deudas a cobrar, que son clientes.

⚠️ **Room valida los índices al abrir.** El nombre del índice en la entidad tiene que ser
idéntico al de la migración o la app crashea al arrancar, y el compilador no lo detecta
(Lecciones, punto 1).

---

## 5. Fases

Cada fase se termina, se prueba **en emulador** (no solo compila) y recién ahí empieza la
siguiente.

### Fase 1 — Ficha y listado ✅ (29/09/2026)

- Entidad, DAO, mapper, repositorio y casos de uso de `contacto`, copiando el vertical de
  productos.
- `MIGRATION_14_15` con la recreación de `contacto` y las tres columnas nuevas.
- Pantallas `ClientesScreen` y `ProveedoresScreen`: buscador, alta, edición, borrado.
  Una sola pantalla parametrizada por `tipo` si el contenido termina siendo el mismo.
- Alta desde donde ya se escribe el nombre a mano: factura y deuda.
- Rutas nuevas en `Destination` y alta en `DESTINOS_PRO` (heredan el candado).
- Cascada al borrar un negocio: `NegocioDao.eliminarConDatos` tiene que llevarse también
  los contactos y sus pagos.

**Hecho cuando**: se carga un cliente, se lo elige al facturar, y la factura queda
vinculada; borrar el negocio se lleva todo.

**Cómo quedó.** Base **v15** con `MIGRATION_14_15`: `contacto` recreada (negocio, tipo y
datos de contacto, único por `(negocioId, nombre)`), más `factura.clienteId`,
`egreso.proveedorId` y `deuda.contactoId`, los tres opcionales y en NULL. Los contactos que
había pasaron al negocio 1 como clientes.

Una sola pantalla (`features/contactos/`) para las dos listas, con el tipo como parámetro
del ViewModel. En el menú, Clientes y Proveedores entraron al bloque Pro, que además ahora
scrollea: con cuatro filas el menú ya no entra en un teléfono y Facturas quedaba fuera.

El **alta automática** quedó en `EmitirFacturaUseCase`: al emitir se crea la ficha con el
nombre escrito y la factura guarda su `clienteId`. Si el nombre ya existía se reusa, y si
estaba cargado como proveedor pasa a "ambos" en vez de duplicarse. Esa alta rápida **no**
pide Pro (anotar a quién le fiaste es parte del uso personal); el candado está en la ficha
completa.

Borrar un contacto deja los vínculos en NULL y no toca el historial: la factura sigue
mostrando el nombre con el que se emitió.

Probado en emulador: migración sobre la base con datos, alta y borrado de ficha, y una
factura que creó sola el cliente "Panaderia-Ana".

### Fase 2 — Historial y cuenta corriente

- Ficha con historial: facturas del cliente / compras al proveedor, ordenadas por fecha.
- Facturas a crédito: campo "pagada" o cobro parcial, con la tabla `pago_contacto`.
- Saldo calculado y semáforo en el listado (quién debe, cuánto, desde cuándo).
- Deudas a pagar del proveedor, reusando lo que ya hace "Quién me debe" pero al revés.

**Hecho cuando**: el saldo de un cliente coincide con las facturas impagas menos los pagos,
y sigue coincidiendo después de borrar una factura.

### Fase 3 — Estadísticas (el cuarto punto del roadmap Pro)

- Mejor cliente del período, proveedor con más compras, producto más vendido.
- Rentabilidad: precio de venta contra costo. **Requiere un campo de costo en el producto**,
  que hoy no existe: entra acá o en una fase propia.

---

## 6. Dónde toca cada cosa

| Archivo | Qué cambia |
|---|---|
| `data/local/ContactoEntity.kt` / `ContactoDao.kt` | Reescritura completa (hoy el DAO solo devuelve nombres) |
| `data/local/Migraciones.kt` | `MIGRATION_14_15` |
| `data/local/TallerDatabase.kt` | Versión 15, entidades y DAOs nuevos |
| `data/local/NegocioDao.kt` | Cascada: contactos y pagos |
| `data/local/FacturaEntity.kt`, `EgresoEntity.kt`, `DeudaEntity.kt` | Columna de vínculo |
| `domain/model/`, `domain/repository/`, `domain/usecase/` | Vertical nuevo, con gate Pro en los casos de uso de guardado |
| `core/di/AppContainer.kt` | Repositorios y casos de uso |
| `core/navigation/Destination.kt`, `DestinosNav.kt`, `TallerApp.kt` | Rutas y bloque Pro |
| `core/ui/icons/IconosApp.kt` | Dos íconos Tabler nuevos (`users` y `truck`) |
| `features/clientes/`, `features/proveedores/` | Pantallas y ViewModels |
| `features/facturas/`, `features/deudas/` | Selector de contacto en vez de texto libre |
| `PLANES.md`, `README-CONTEXTO.md` | Actualizar al cerrar cada fase |

---

## 7. Fuera de alcance

Para que el trabajo termine: **no** entran en estas fases la sincronización en la nube, el
envío de la factura por correo o WhatsApp desde la ficha, los recordatorios automáticos de
cobro, ni la importación de contactos del teléfono. La agenda del teléfono además pediría
un permiso nuevo y cambiaría la declaración de "no recopila datos" de Play, que hoy está
enviada así.

---

## 8. Riesgos

1. **El índice único de `contacto`.** Es el único cambio que toca datos existentes. Recrear
   la tabla y probar la migración con una base que ya tenga contactos, no con una vacía.
2. **Contactos repetidos.** Al pasar de texto libre a ficha, el mismo cliente puede estar
   escrito de tres formas en deudas viejas. No se hace merge automático: se ofrece elegir
   uno existente al cargar, y listo.
3. **Rentabilidad sin costo.** La fase 3 no se puede cerrar sin agregar el costo al
   producto; conviene decidirlo antes de empezar la fase 2 para no migrar dos veces.
4. **Alcance.** Clientes y proveedores son la puerta a un CRM entero. Lo que sostiene la
   suscripción es saber a quién le vendés y cuánto te debe, no un embudo de ventas.

---

## 9. Pendiente — copia automática a Google Drive

> Anotado el 30/09/2026. **No empezado.** La copia manual (`core/backup/CopiaSeguridad.kt`,
> exportar/restaurar un `.zip`) ya está y resuelve el caso grave: no perder los datos al
> reinstalar o cambiar de teléfono. Esto es el paso siguiente, que es comodidad.

### Qué sería

El usuario entra con su cuenta de Google y la app sube **el mismo `.zip` que ya genera** a
`appDataFolder`, una carpeta oculta de **su** Drive. Una vez por semana, con WorkManager.

`appDataFolder` y no una carpeta normal: no ocupa la cuota visible del usuario, no aparece
mezclada con sus archivos, y ninguna otra app puede leerla. Tampoco la vemos nosotros: no
hay servidor de por medio.

### Trabajo

| Parte | Esfuerzo |
|---|---|
| Código: sign-in, subida a `appDataFolder`, WorkManager semanal, elegir copia al restaurar | 2-3 h |
| Google Cloud Console: proyecto, pantalla de consentimiento OAuth, **SHA-1 de la firma de Play** (el de Google, no el del keystore local, porque la firma de apps está aceptada) | Trámite |
| Posible **verificación de OAuth**: `drive.appdata` puede caer como scope sensible (video de demostración, semanas de espera) | Trámite, bloquea producción |
| Play: rehacer **Seguridad de los datos** (hoy declara "no recopila nada"), sumar el permiso de red, actualizar la política de privacidad | Trámite |

El código es lo barato. Lo que puede demorar semanas son la verificación de OAuth y la
revisión de Play, y hasta que eso pase la función no puede salir a producción.

### Alternativa de bajo costo (hacer primero si hay poco tiempo)

Un **recordatorio semanal** con WorkManager: una notificación "guardá una copia" que abre
directo el selector de exportación. Sin red, sin OAuth, sin tocar ninguna declaración de
Play, y cubre el mismo riesgo para quien se acuerde de tocar la notificación. Media hora.

### Cuando se haga

- Que la subida no sea silenciosa: hay que poder ver cuándo fue la última copia y apagarla.
- Guardar dos o tres copias rotativas, no una sola: una copia semanal que pise a la anterior
  también propaga un borrado accidental.
- Si Play pide declarar recopilación de datos, decirlo con todas las letras en la ficha: el
  argumento de venta hoy es que la app no manda nada a ningún lado.

---

## 10. Pendiente — regalar la suscripción (códigos promocionales)

> Anotado el 30/09/2026. **No empezado.** Caso concreto: regalarle un año de Pro a los
> probadores y a gente cercana.

**Casi no hay código.** Esto lo resuelve Google Play, no la app: los códigos promocionales se
crean en Play Console y se canjean en la Play Store ("Canjear código", o un enlace directo).
Cuando el usuario canjea, Play le da la suscripción y la app se entera sola por el camino que
ya existe: `FacturacionPlay` consulta las compras activas al iniciar y escribe en `EstadoPlan`.
Un código canjeado es una compra más.

### Lo que hay que hacer

1. En Play Console, crear una **oferta promocional** sobre el plan base (`mensual` o `anual`)
   con el período de regalo, y generar los códigos.
2. Verificar ahí mismo los **límites vigentes**: cuántos códigos por período, si valen solo
   para quien nunca tuvo la suscripción, y si el país tiene que coincidir. Google los cambia
   seguido; no anotar acá un número que después queda viejo.
3. Repartir los códigos. Nada más.

### Lo único que podría tocarse en la app

Una fila en Planes que diga "¿Tenés un código?" y abra la pantalla de canje de Play
(`https://play.google.com/redeem?code=`). Es cosmético: sin eso, el código se canjea igual
desde la Play Store. Media hora si se quiere.

### Ojo

- Para los **probadores de la prueba cerrada** no hace falta código: la lista de *pruebas de
  licencia* de Play ya les permite comprar sin que se les cobre. El código sirve para regalar
  de verdad, fuera de la prueba.
- Un año regalado es un año que no entra. Para gente cercana está bien; como forma de
  conseguir usuarios, no.

---

## 11. Idea — modo viaje (gastos compartidos)

> Anotado el 30/09/2026. **Idea, sin decidir.** Un viaje entre amigos: cada gasto queda
> anotado con quién lo pagó y, al volver, la app dice quién le tiene que pagar a quién.

### Entra en la app

Sí, pero como **módulo propio**, no mezclado con los movimientos. Es finanzas personales, que
es justo la mitad gratuita de la app, y es la clase de función por la que alguien la instala
y después se queda. El riesgo es de alcance: Splitwise es una app entera. Acá tiene que ser
un viaje, sus gastos y la liquidación final; nada de cuentas permanentes entre personas, ni
notificaciones, ni pagos.

### Alcance mínimo que vale la pena

- Un **viaje** con nombre, fechas y sus participantes (nombres escritos a mano, no contactos
  del teléfono: eso pediría un permiso nuevo).
- Cada **gasto**: concepto, monto, **quién pagó** y entre quiénes se divide (todos, o algunos).
- **Liquidación**: cuánto puso cada uno, cuánto le tocaba, y las transferencias mínimas para
  saldar. El algoritmo es simple —ordenar deudores y acreedores y cruzarlos— y evita el
  "todos le pagan a todos", que es lo que hace que nadie termine de pagar.
- Compartir el resumen por WhatsApp, reusando el generador de PDF que ya existe.

### Lo que queda afuera a propósito

Que cada participante cargue desde su propio teléfono. Eso pide servidor y cuentas, y hoy la
app no tiene ni una cosa ni la otra ("no recopila datos" es un argumento de venta). Carga uno
y comparte el resultado.

### Trabajo y decisión abierta

Tablas `viaje`, `viaje_participante` y `viaje_gasto` (más una de reparto para el gasto que no
se divide entre todos), un vertical como el de productos y una pantalla con tres pestañas:
gastos, resumen y liquidación. **Un par de sesiones**, no una tarde.

Sin decidir: **gratis o Pro**. No tiene nada que ver con usar la app en un negocio, así que
la intuición dice gratis; pero es la función más vistosa que tendría y también podría ser el
gancho de Pro. Decidirlo antes de empezar.

---

## 12. Pendiente — diseño responsive (tablet, horizontal y texto grande)

> Anotado el 01/10/2026. **No empezado.** La app está hecha para un teléfono en vertical y
> hoy no se adapta a nada más.

### Cómo está hoy

Todas las pantallas son una sola columna: 157 `fillMaxWidth()` y ni un `widthIn`,
`BoxWithConstraints` o `LazyVerticalGrid` en todo el proyecto. Tampoco hay `WindowSizeClass`
(la dependencia ni está declarada) ni carpetas de recursos por ancho (`res/` solo tiene
`drawable`, `values` y `xml`). El manifiesto no fija orientación, así que el teléfono ya rota
y la app ya se abre en tablet y Chromebook: estirada, no adaptada.

El primer aviso lo dio la Fase 1 por el otro lado, el de las pantallas chicas: con cuatro
filas el bloque Pro del menú no entraba en un teléfono y hubo que hacerlo scrollear.

### Qué se rompe

| Caso | Qué pasa |
|---|---|
| Tablet o teléfono en horizontal | Las tarjetas se estiran a todo el ancho: líneas de texto de punta a punta y botones del Dashboard de medio metro |
| Dashboard (`DashboardScreen.kt`) | Los pares de botones con `weight(1f)` quedan enormes en vez de pasar a una grilla de más columnas |
| Listados (contactos, productos, finanzas) | Una fila por ítem a todo el ancho, con el texto perdido a la izquierda |
| Texto del sistema al 200 % | Sin probar. Donde el alto es fijo o el texto va en una fila con ícono, se corta |
| Diálogos (13 pantallas los usan) | Sin scroll interno: en horizontal el botón de confirmar puede quedar fuera de la pantalla |

### Pasos, del más barato al más caro

**Paso 1 — Ancho máximo del contenido.** Un contenedor en `core/ui/components/` que centre
el contenido y lo limite (`widthIn(max = 600.dp)`), aplicado dentro del `Scaffold` de cada
pantalla. En teléfono no cambia **nada**; en tablet y en horizontal arregla lo peor —el texto
infinito— sin rediseñar una sola pantalla. Es mecánico: un `import` y un wrapper por archivo.

**Paso 2 — Más columnas donde sobra ancho.** Sumar
`androidx.compose.material3:material3-window-size-class`, leer el tamaño una vez y bajarlo
por el árbol. Dashboard en grilla, listados con `LazyVerticalGrid(GridCells.Adaptive(...))`,
formularios con campos de a dos. Acá sí hay decisiones de diseño por pantalla.

**Paso 3 — Navegación adaptativa.** En ancho *expanded*, `NavigationRail` o
`PermanentNavigationDrawer` en vez del `ModalNavigationDrawer` + `NavigationBar` de hoy
(`TallerApp.kt`, `AppDrawer.kt`, `BarraInferior.kt`). Es el cambio más visible y el que más
fácil rompe la navegación que ya funciona: va último y solo si los dos primeros pasos
quedaron firmes.

**Transversal, hacer con el Paso 1**: pasar los diálogos a contenido scrolleable y revisar
la app entera con el texto del sistema al máximo. Es la mitad del problema de accesibilidad
y no depende del tamaño de pantalla.

**Cómo quedó el Paso 1 (01/10/2026).** `core/ui/components/Responsive.kt` con la constante
`ANCHO_MAXIMO_CONTENIDO` (600 dp, el límite de *compact* de Material 3) y el modificador
`Modifier.anchoContenido()`, que es `fillMaxWidth()` + `wrapContentWidth(CenterHorizontally)`
+ `widthIn(max = …)`: limita el contenido y lo centra en el espacio que sobra.

Aplicado al contenedor raíz de las **23 pantallas** (22 con `Scaffold` más
`OnboardingScreen`, que no tiene). Donde la pantalla scrollea va después de
`verticalScroll(...)`, así el área que scrollea sigue ocupando todo el alto y lo que se limita
es el contenido. En un teléfono no cambia nada: la pantalla mide menos que el máximo.

**Diálogos (01/10/2026).** Los cinco `AlertDialog` cuyo cuerpo es un formulario pasaron a
`Column(Modifier.verticalScroll(rememberScrollState()))`: agenda, categorías, cuentas, metas y
la línea a mano de `FacturaFormScreen`. `RecurrentesScreen` y el detalle de factura ya lo
tenían. Falta la revisión con el texto del sistema al 200 %, que necesita emulador.

**Hecho cuando**: en un emulador de tablet en horizontal, el Dashboard, un listado y un
formulario se leen sin líneas que cruzan la pantalla; al rotar no se pierde lo que estaba
escrito en un formulario; y con el texto al 200 % no hay botón ni importe cortado.

### Fuera de alcance

No entra el rediseño de las pantallas ni el patrón de dos paneles (lista + ficha al lado),
que es otra arquitectura de navegación. Tampoco el soporte de ventana flotante o pantalla
partida más allá de que no se rompa. El objetivo es que la app **se vea bien** en grande, no
que tenga una interfaz distinta para tablet.

### Riesgos

1. **Son 23 pantallas a mano.** No hay tests de interfaz, así que la única red es el
   emulador. Por eso el Paso 1 es un wrapper y no un cambio de layout: se puede hacer en
   tanda y revisar de una pasada.
2. **Probar en tablet de verdad (emulador), no solo redimensionando.** El preview de Compose
   no detecta lo que se pierde al rotar, que es estado del ViewModel.
3. **Alcance.** "Responsive" se puede estirar hasta rehacer la app. Lo que paga es el Paso 1
   más los diálogos y el texto grande; el resto es mejora, no arreglo.

---

## 13. Pendiente — fluidez y rendimiento

> Anotado el 01/10/2026. **No empezado.** Hermano de la sección 12: ahí el problema es cómo
> se ve la app en pantallas distintas, acá es cómo responde.

### Cómo está hoy

La base está sana: Kotlin 2.2.10 con el plugin de Compose (strong skipping activado por
defecto), `isMinifyEnabled` y `isShrinkResources` en release, 77 de 79 flujos leídos con
`collectAsStateWithLifecycle` (solo 2 `collectAsState()` pelados) y 23 `stateIn(...)` en los
ViewModels. No hay nada roto a nivel arquitectura.

Lo que falta es lo de abajo: listas que no recicla nadie, cero perfil de arranque y ninguna
medición. Hoy no se sabe cuánto tarda la app en abrir, porque nunca se midió.

### Lo concreto

| Problema | Dónde | Por qué importa |
|---|---|---|
| Listas largas dibujadas enteras | `FinanzasScreen.kt:173` y `:195` recorren ingresos y egresos con `forEach` dentro de un `Column(verticalScroll)`. `ReportesScreen.kt` hace lo mismo | Sin reciclado: con 300 movimientos se componen 300 filas de una. Es el salto de scroll más probable que tiene la app |
| Solo 4 listas son `LazyColumn` | contactos, deudas, facturas y productos. Las demás son `forEach` (32 en total en `features/`) | Las cuatro que importan ya tienen `key = { it.id }`; el resto ni es lazy |
| Sin Baseline Profile | No está `androidx.profileinstaller` ni el perfil | El primer arranque corre interpretado. Es la mejora de arranque más grande por menos código que existe en Android |
| Sin medición | No hay `macrobenchmark`, ni informes del compilador de Compose, ni un número de referencia | Sin medir antes y después, "va más rápido" es una impresión |
| `SimpleDateFormat` / formato de moneda dentro de composables | 2 usos | Reinstancian el formateador en cada recomposición. Barato de arreglar |
| Cero `derivedStateOf` | Todo el proyecto | Cálculos que podrían derivarse se rehacen en cada recomposición. Puntual, no urgente |

### Pasos, del más barato al más caro

**Paso 1 — Pasar a `LazyColumn` las listas que crecen.** Finanzas primero, que es la pantalla
más usada y la que más filas acumula, después Reportes. Con `key` estable, como ya lo hacen
las otras cuatro. Es el único arreglo de esta lista que se nota a ojo desnudo.

**Cómo quedó el Paso 1 (01/10/2026).** `FinanzasScreen` pasó de
`Column(verticalScroll)` con dos `forEach` a un `LazyColumn`. La cabecera —título, tarjeta de
filtros, resumen de tres métricas, botones y la barra de herramientas— quedó dentro de un
`item { }`, porque un `LazyColumn` anidado en un `Column` con scroll no tiene alto definido.
El padding de 16 dp pasó a `contentPadding`, que es lo que corresponde en una lista. Las
claves llevan prefijo (`"ingreso-id"`, `"egreso-id"`) porque ingresos y egresos comparten la
misma lista y los id se repiten entre las dos tablas.

**`ReportesScreen` se revisó y se dejó como está.** Sus `forEach` recorren cosas acotadas: los
negocios del usuario, las categorías con presupuesto y los 12 meses del gráfico. No hay
cientos de filas, así que un `Column` con scroll alcanza y convertirlo era mover una pantalla
grande sin ganancia medible.

**Paso 2 — Baseline Profile.** Sumar `androidx.profileinstaller`, generar el perfil con el
recorrido de arranque (abrir, Dashboard, un listado) y que entre en el AAB. Es configuración,
no diseño.

**Paso 3 — Medir.** Un módulo `macrobenchmark` con arranque en frío y scroll de Finanzas, más
una corrida de los informes de estabilidad del compilador de Compose para ver qué clases de
estado no son estables. Recién con esos números tiene sentido tocar recomposiciones.

**Paso 4 — Lo puntual.** Sacar los formateadores de las composiciones, `remember` donde hace
falta y `derivedStateOf` donde el cálculo depende de otro estado.

**Hecho cuando**: Finanzas con 300 movimientos cargados scrollea sin tirones en un teléfono
de gama baja, y el arranque en frío tiene un número medido antes y después del Baseline
Profile.

### Fuera de alcance

No entra reescribir ViewModels, ni cambiar Room por otra cosa, ni paginar con `Paging 3`: los
volúmenes de un negocio chico no lo piden y la paginación arrastra la interfaz entera. Si
alguna consulta resulta lenta, se arregla la consulta, no la arquitectura.

### Riesgos

1. **Optimizar sin medir.** El Paso 3 existe para eso. Los Pasos 1 y 2 son los dos únicos que
   se pueden hacer a ciegas sin quedar debiendo una explicación.
2. ~~**`LazyColumn` dentro de un `Column(verticalScroll)` no compila bien de arriba.**~~
   Resuelto en Finanzas el 01/10/2026 moviendo la cabecera a un `item { }`. Vale igual para
   cualquier pantalla que se convierta después.
3. **Alcance.** Perseguir recomposiciones puede consumir días con ganancia cero. El techo de
   esta sección son los Pasos 1 y 2; el resto se decide con los números del Paso 3.
