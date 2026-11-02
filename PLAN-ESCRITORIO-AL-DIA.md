# Plan de trabajo — Poner la app de escritorio al día con Android

> Backlog ejecutable para `C:\Proyectos\MisFinanzasDesktop`, derivado de
> `ANALISIS-ANDROID-PARA-ESCRITORIO.md` y `ANALISIS-ESCRITORIO-PARA-ANDROID.md`.
>
> Punto de partida: escritorio `1.0.2`, `BaseDatos.SchemaVersion = 6`. Android esquema Room **v15**.
> Las migraciones nuevas del escritorio arrancan en **7**.
>
> Cada punto es una unidad de trabajo cerrable por separado, con archivos, SQL, criterio de aceptación y
> tests. Se trabajan en orden salvo aviso; las dependencias están anotadas.

---

## Cómo trabajamos cada punto

1. **Una migración por punto.** Subir `SchemaVersion` de uno en uno y agregar el `case` en
   `AplicarMigracion`. El migrador ya crea un backup `pre-migration` y corre cada migración en su
   transacción: no hay que tocar esa mecánica.
2. **Las tablas nuevas también van al bloque base** (`Inicializar`, con `CREATE TABLE IF NOT EXISTS`), para
   que una instalación nueva no dependa de las migraciones. Mismo problema que resuelve
   `DatosIniciales.kt` en Android.
3. **Un test por migración** en `tests/MisFinanzasDesktop.Tests/BaseDatosTests.cs`: que la tabla exista,
   que `user_version` quede en el número nuevo y que el caso de datos viejos se preserve.
4. **Convenciones del escritorio que no se cambian en estos puntos:** `fecha` como `TEXT` ISO
   `yyyy-MM-dd`, `createdAt` en epoch millis, montos en centavos (`long`), sin foreign keys declaradas.
   La unificación con Android de esos formatos es el bloque **F**, al final.
5. Al cerrar un punto, **actualizar los dos `.md` de análisis** (secciones de paridad) para que no queden
   desfasados.
6. Verificación mínima antes de cerrar: `dotnet build -c Release` y
   `dotnet test tests/MisFinanzasDesktop.Tests -c Release` en verde.

---

## Bloque A — Bloqueante de correctitud (hacer primero)

### A1. Unificar el parseo y el formato de montos — ✅ HECHO (2026-10-03)

- [x] **Problema:** `"1.500"` valía **1500** en escritorio y **1,50** en Android.
- [x] **Regla decidida** (implementada igual en las dos apps):
  1. Se quitan espacios (incluido el no-rompible).
  2. Dos o más comas → inválido.
  3. **Con coma:** la coma es el decimal y los puntos son separadores de miles.
  4. **Sin coma:** el punto es decimal, **salvo** que el texto tenga forma de miles — parte entera de 1 a 3
     dígitos que **no empieza con 0** y todos los grupos siguientes de **exactamente 3 dígitos**. Varios
     puntos que no cumplen eso → inválido.
  5. Se valida la forma final con `^-?(\d+(\.\d*)?|\.\d+)$` (fuera exponentes, `Infinity`, `NaN`, hex) y
     se redondea `valor * 100`. Negativos → inválido.
  - Casos de referencia: `"1.500"` = 1500 · `"1500.50"` = 1500,50 · `"1.500,50"` = 1500,50 ·
    `"1.5"` = 1,50 · `"0.999"` = 1,00 · `"12.345.678"` = 12.345.678 · `"1 500,50"` = 1500,50.
- [x] **Escritorio:** `Services/Dinero.cs` — `ParsearACentavos` reescrito + `EsSeparadorDeMiles` +
  `FormaNumerica`.
- [x] **Android:** `core/util/Dinero.kt` — `parsearACentavos` reescrito + `esSeparadorDeMiles` +
  `FORMA_NUMERICA`.
- [x] **`CentavosAEntrada` queda distinto a propósito:** escritorio devuelve `"1500,50"` (el usuario ve ese
  texto en el `TextBox`), Android devuelve `"1500.50"` porque es el **valor crudo** de `CampoMonto`, que ya
  lo muestra con coma y miles por `VisualTransformation`. Meter una coma en ese valor crudo rompe el mapeo
  del cursor. El parseo acepta las dos formas, así que la paridad se cumple igual.
- [x] **Tests:** `tests/MisFinanzasDesktop.Tests/PuraTests.cs` (14 casos válidos + 8 inválidos + ida y
  vuelta) y `app/src/test/.../DineroTest.kt` (los mismos casos).
- [x] **Verificado:** escritorio `dotnet test -c Release` → **43/43**. Android
  `:app:testDebugUnitTest --tests DineroTest` → **8/8**.
- [x] Secciones de paridad actualizadas en los dos `.md` de análisis.

> Nota de build: el `bin\Release` del escritorio estaba bloqueado por la app en ejecución
> (`MSB3027 ... El archivo se ha bloqueado por: "Mis Finanzas"`). Los tests se corrieron con
> `-p:BaseOutputPath=<temp>`. Para compilar normal, cerrar la app primero.

---

## Bloque B — Clientes y proveedores (migración 7)

> Es la base de Facturas (B necesita cerrarse antes de D). Equivale a la migración 14→15 de Android.

### B1. Migración 7: recrear `contacto` como ficha por negocio — ✅ HECHO (2026-10-03)

- [x] `Services/BaseDatos.cs`: `SchemaVersion = 7`, tabla nueva en el bloque base y `case 7` con el
  `DROP TABLE IF EXISTS contacto_new` → `CREATE` → `INSERT ... SELECT` → `DROP` → `RENAME` →
  `CREATE UNIQUE INDEX ix_contacto_negocio_nombre ON contacto(negocioId, nombre COLLATE NOCASE)`.
- [x] Los contactos existentes pasan al primer negocio con `tipo = 'cliente'`, conservando `id`, `nombre`
  y `createdAt`.
- [x] **El índice único va solo en la migración, no en el bloque base.** El `CREATE TABLE IF NOT EXISTS`
  es no-op sobre una base vieja, así que el `CREATE INDEX` del bloque base fallaba con
  `SQLite Error 1: 'no such column: negocioId'`. Lo encontró el test de migración, no la ejecución normal.
- [x] API mínima para que la app siga funcionando (el CRUD completo es B2):
  `NombresContactos(negocioId?, tipo?)` reemplaza a `Contactos()`, y `AsegurarContacto(negocioId, nombre,
  tipo)` reemplaza a `AgregarContacto(nombre)` devolviendo el id (reusa la ficha existente, con trim y
  `COLLATE NOCASE`). `tipo = 'ambos'` entra en los filtros de cliente y de proveedor.
- [x] Call sites adaptados: `Views/DeudaDialog.xaml.cs` (contactos del negocio actual) y
  `Views/RemitoWindow.xaml.cs` (solo clientes).
- [x] **Tests nuevos** en `BaseDatosTests.cs`: forma de la tabla y existencia del índice; dos negocios con
  el mismo "Juan" sin duplicar; filtro por tipo con `'ambos'` en los dos; y
  `Migracion_7_conserva_los_contactos_viejos`, que arma una base con el esquema viejo
  (`user_version = 1`) y verifica que los nombres sobreviven como clientes y que queda en `user_version = 7`.
  `Migracion_crea_tablas_y_user_version_6` pasó a `..._7`.
- [x] **Verificado:** `dotnet test -c Release` → **47/47**.

### B2. Modelo y API de datos — ✅ HECHO (2026-10-03)

- [x] `Models/Modelos.cs`: `TipoContacto` (`Cliente` / `Proveedor` / `Ambos`, con `Etiqueta` e `Icono`
  🧑 🚚 🔁, port de `TipoContacto` de Kotlin), clase `Contacto` con los 9 campos de la ficha más
  `EsDe(tipo)`, `Detalle` (documento · teléfono · email, salteando vacíos) y `Display`.
- [x] `ResultadoContacto` (`Guardado` / `NombreVacio` / `NombreRepetido`) y
  `GuardarContactoResultado(Estado, Id, Existente)`, al estilo de `AuthResultado`.
- [x] `Services/BaseDatos.cs`:
  - `Contactos(negocioId?, tipo?, busqueda)` — con tipo, `'ambos'` entra en las dos listas; la búsqueda
    filtra por nombre, documento o teléfono (igual que `ContactoDao.buscarPorTipo` de Android).
  - `NombresContactos(negocioId?, tipo?)` ahora se apoya en `Contactos(...)`.
  - `ObtenerContacto(id)`, `ContactoPorNombre(negocioId, nombre)` (`COLLATE NOCASE`).
  - `CrearContacto`, `ActualizarContacto`, `GuardarContacto(x)` (recorta todos los campos y avisa
    `NombreRepetido` devolviendo la ficha existente en vez de reventar contra el índice único).
  - `AsegurarContacto(negocioId, nombre, tipo)` **promueve a `'ambos'`** si la ficha existía con el otro
    tipo — mismo criterio que `AgregarContactoUseCase` de Android.
  - `EliminarContacto(id)`: en una transacción pone en NULL `egreso.proveedorId`, `deuda.contactoId` y
    `factura.clienteId` **solo si esas columnas ya existen** (llegan en B4 y D1), y después borra la ficha.
    El historial conserva el nombre que ya tenía guardado.
  - Helpers nuevos: `TablaExiste`, `ColumnasContacto`, `LeerContacto`, `LlenarParametrosContacto`.
- [x] **Sin gate de licencia todavía**: el candado de las herramientas comerciales es el punto E1.
- [x] **Tests nuevos** (7): ficha completa con recorte de espacios y `Detalle`; `'ambos'` en las dos
  listas; nombre vacío y nombre repetido (incluido editar la propia ficha sin chocar consigo misma);
  búsqueda por nombre, documento y teléfono; promoción a `'ambos'`; nombre vacío en `AsegurarContacto`;
  y borrado que no toca el movimiento asociado.
- [x] **Verificado:** `dotnet build -c Release` sin errores y `dotnet test` → **54/54**.

### B3. Pantallas de Clientes y Proveedores — ✅ HECHO (2026-10-03)

- [x] `Views/ContactosView.xaml(.cs)`: **una sola vista parametrizada** por tipo
  (`Mostrar(TipoContacto.Cliente | TipoContacto.Proveedor)`). Cambian el título, el subtítulo, el texto
  del botón de alta y el mensaje de lista vacía; el resto es el mismo contenido, igual que la
  `ContactosScreen` de Android.
- [x] **Es una página dentro de la ventana principal, no una ventana aparte** (corregido tras tu
  observación): se muestra en el `Host` como Inicio, Movimientos o Reportes. La **calculadora es la única
  que se abre aparte**, porque se usa mientras se mira otra pantalla.
- [x] Lista con ícono por tipo (🧑 🚚 🔁), nombre, `Detalle` (se oculta solo cuando no hay datos, con un
  `DataTrigger`) y la etiqueta del tipo. Editar y Eliminar por fila.
- [x] Buscador en vivo (`TextChanged`) por nombre, documento o teléfono, con botón **Limpiar** que
  aparece solo mientras hay texto.
- [x] `Views/ContactoDialog.xaml(.cs)`: nombre obligatorio (marcado con `Validacion.Marcar` y foco), tipo
  en `ComboBox` con `Opcion`, y documento, teléfono, correo, dirección y nota opcionales. Conserva `Id`,
  `NegocioId` y `CreatedAt` al editar.
- [x] **Nombre repetido**: no se deja fallar contra el índice único — se avisa y se ofrece **abrir la
  ficha que ya existe** (usa `GuardarContactoResultado.Existente`).
- [x] **Eliminar** avisa que los gastos, deudas y remitos que mencionan al contacto **no se tocan**.
- [x] **Accesos en la barra lateral, no en Movimientos** (corregido tras comparar con el menú de la app
  Android): bloque propio al final del menú con el encabezado **TU NEGOCIO · PRO** y las entradas
  **Clientes** y **Proveedores**, cada una con **🔒** cuando la licencia no está activa. Se abren como
  ventana (son listados de gestión, igual que Categorías o Cuentas) y, si está bloqueado, el clic manda a
  Configuración → Licencia hasta que exista la pantalla de Planes (E1).
- [x] De paso, la barra lateral quedó con el **mismo orden y agrupación que el menú de Android**:
  Inicio · Movimientos · Quién me debe · Reportes — Agenda · Negocios · Calculadora — Configuración —
  TU NEGOCIO · PRO.
- [x] **Verificado:** `dotnet build -c Release` sin errores (un `MC3024` por `Style` duplicado en el
  `TextBlock` de `Detalle`, corregido) y `dotnet test` → **54/54**.
- [ ] **Pendiente de revisión visual tuya**: no pude abrir la ventana (la app estaba corriendo y pide
  login). Mirá que los dos botones nuevos entren bien en la fila de herramientas de Movimientos.

### B4. Vínculos `proveedorId` y `contactoId` — ✅ HECHO (2026-10-03)

- [x] Columnas nuevas: `egreso.proveedorId INTEGER` y `deuda.contactoId INTEGER`, ambas opcionales y en
  NULL para todo lo ya cargado.
- [x] **No van en el `switch` de versiones** sino en el bloque condicional de `Inicializar`, con
  `ColumnaExiste`, igual que `negocioId`, `ingreso.categoria`, `deuda.fechaLimite` y `cuenta`. Motivo: una
  base que ya llegó a `user_version = 7` con una build intermedia nunca volvería a correr la migración 7 y
  se quedaría sin las columnas. Así el arreglo es idempotente y no depende de la versión.
- [x] `Models/Modelos.cs`: `Egreso.ProveedorId` y `Deuda.ContactoId` (`long?`).
- [x] `BaseDatos`: las dos columnas entran en el INSERT, el UPDATE y el SELECT de `egreso` y de `deuda`
  (`CrearEgreso`, `ActualizarEgreso`, `EgresosRango`, `CrearDeuda`, `ActualizarDeuda`, `Deudas`).
- [x] `EgresoDialog`: `ComboBox` **"Proveedor (opcional)"** con `(ninguno)` primero y los proveedores del
  negocio; guarda `ProveedorId` solo si se eligió uno. Al editar, si el proveedor fue borrado el gasto
  queda sin vínculo pero conserva su concepto.
- [x] `DeudaDialog`: el alta rápida ya devolvía el id, así que la deuda ahora guarda `ContactoId`; el
  nombre se sigue guardando como texto para que el historial no cambie si después se borra la ficha.
- [x] `EliminarContacto` (de B2) ya pone en NULL las dos columnas: ahora que existen, el camino queda
  cubierto de punta a punta.
- [x] **Tests nuevos** (4): gasto con proveedor y desvinculado al editar; gasto sin proveedor en NULL;
  deuda con contacto; y borrar el contacto deja los dos vínculos en NULL conservando concepto y nombre.
- [x] **Verificado:** `dotnet build -c Release` sin errores y `dotnet test` → **58/58**.

> Bloque B completo. El escritorio ya tiene clientes y proveedores con ficha, por negocio, vinculados a
> gastos y deudas: es la base de Facturas (bloque D).

## Bloque C — Catálogo de productos (migración 8)

### C1. Migración 8: tabla `producto` — ✅ HECHO (2026-10-03)

- [x] `SchemaVersion = 8` y `case 8` en `AplicarMigracion` con el `CREATE TABLE` + los índices
  `ix_producto_negocio` e `ix_producto_codigo`.
- [x] La misma DDL va en el **bloque base** (`CREATE TABLE IF NOT EXISTS`), para que una instalación nueva
  no dependa de las migraciones.
- [x] Tabla nueva: **no se toca ningún dato existente**.
- [x] **El índice del código de barras no es único, a propósito**: el código repetido se avisa y decide el
  usuario, igual que `GuardarProductoUseCase` de Android. El test lo verifica leyendo
  `pragma_index_list('producto')`, para que nadie lo "arregle" después poniéndole `UNIQUE`.
- [x] **Test nuevo**: tabla, las 10 columnas, los dos índices y que `ix_producto_codigo` no sea único. Los
  tests de versión pasaron de 7 a 8.
- [x] **Verificado:** `dotnet test` → **59/59**.

### C2. Modelo, cálculo de precios y API — ✅ HECHO (2026-10-03)

- [x] `Models/Modelos.cs`: clase `Producto` con `DescuentoCentavos`, `PrecioFinalCentavos`,
  `TieneDescuento` y `StockBajo` (`StockMinimo > 0 && Stock <= StockMinimo`), más `ResultadoProducto`
  (`Guardado` / `NombreVacio` / `CodigoRepetido`) y `GuardarProductoResultado(Estado, Id, Existente)`.
- [x] **`Services/Precios.cs`** nuevo, port literal del objeto `Precios` de Kotlin: `Descuento`
  (porcentaje recortado a 100), `ConDescuento` y `Subtotal` (se redondea el bruto `cantidad × precio` y
  **después** se aplica el descuento — ese orden no se puede "simplificar").
- [x] **Redondeo: medio hacia arriba**, con `Math.Floor(valor + 0.5)`. `Math.Round` usa redondeo bancario
  (2,5 → 2) y Kotlin `roundToLong` da 3: el mismo cálculo habría dado un centavo distinto en cada app.
- [x] **Secuela de A1**: `Dinero.ParsearACentavos` también usaba `Math.Round`, así que `"0,125"` daba 12
  centavos en el escritorio y 13 en Android. Ahora usa `Precios.RedondearACentavos`, con test.
- [x] `BaseDatos`: `Productos(negocioId, busqueda)` (nombre, código o descripción), `ObtenerProducto`,
  `ProductoPorCodigo` (el código vacío nunca coincide), `ContarProductos`, `CrearProducto`,
  `ActualizarProducto`, `GuardarProducto` (recorta campos, limita `DescuentoPct` a `0..100` y **avisa** el
  código repetido devolviendo el existente), `EliminarProducto` y
  `DescontarStock` con `MAX(0, stock - $c)` — **nunca baja de 0**, igual que el DAO de Android.
- [x] **Tests nuevos** (22 en total): `PreciosTests` con los mismos casos que Android, el del redondeo
  bancario, y 7 de la API (alta con precio/descuento/stock, código repetido, nombre vacío y descuento
  recortado, búsqueda, stock bajo, descuento de stock con piso en 0, separación por negocio y borrado).
- [x] **Paridad en Android**: se agregó `app/src/test/.../domain/model/PreciosTest.kt` con los mismos
  casos, para que el día que alguien toque el cálculo en una app, falle en la otra. **4/4 en verde.**
- [x] **Verificado:** escritorio `dotnet test` → **81/81**; Android `:app:testDebugUnitTest` → PreciosTest
  **4/4**.

> Nota de build: apareció un `error BG1002: no se encuentra el archivo ... MainWindow.baml` por el `obj`
> de Release desincronizado (efecto de compilar con `BaseOutputPath` redirigido mientras la app corre).
> Se resuelve borrando `obj/Release/net10.0-windows` y recompilando.

### C3. Pantalla de Productos — ✅ HECHO (2026-10-03)

- [x] `Views/ProductosView.xaml(.cs)` — **página dentro de la ventana principal**, no una ventana aparte
  (corregido tras tu observación): lista con foto (o 📦 cuando no hay), nombre, código y
  descripción, **precio de lista tachado** cuando hay descuento con el final al lado, y stock en rojo
  con ⚠ cuando está bajo. El stock se muestra sin decimales inútiles (12, no 12,00).
- [x] Buscador en vivo por nombre, código o descripción, con **Limpiar** condicional, y mensajes de lista
  vacía distintos según si se está buscando.
- [x] `Views/ProductoDialog.xaml(.cs)`: nombre obligatorio, código, descripción, precio, descuento %,
  stock, stock mínimo y foto. **Vista previa del precio final** en vivo mientras se escribe, usando
  `Precios.Descuento`. Los números sueltos (porcentaje, stock) aceptan coma y punto.
- [x] **Código repetido**: se avisa y se ofrece abrir el producto que ya lo usa. No se bloquea.
- [x] **Foto**: `Services/ImagenProducto.cs` nuevo, con la misma política que el helper de Android —
  copia a `%APPDATA%\MisFinanzas\Productos\` (nuevo `Rutas.Productos`), reescalado a 1024 px y JPEG de
  calidad 85, nombre por GUID. `Borrar` solo toca archivos de esa carpeta, nunca el original del usuario.
  Se limpia la copia huérfana al reemplazar la foto, al cancelar el diálogo y al eliminar el producto.
- [x] **Desvío del plan, a propósito**: no se reusa `CropWindow`. El avatar se recorta a un cuadrado, pero
  un producto se fotografía como entra en la caja y recortarlo suele cortar justo lo que lo identifica.
  Android tampoco lo recorta: copia y reescala. Se siguió a Android.
- [x] **Eliminar** avisa que las facturas ya emitidas no se tocan (conservan descripción y precio).
- [x] **Entrada Productos en la barra lateral**, arriba de Clientes dentro de **TU NEGOCIO · PRO**, con
  candado 🔒. El candado se unificó en `MainWindow.PermiteUsoComercial(que)`, que es el único lugar a
  cambiar cuando exista la pantalla de Planes (E1). Si el candado corta la navegación, la sección activa
  del menú **vuelve a la anterior** en vez de quedar marcada en una página que no se abrió.
- [x] **Verificado:** `dotnet build -c Release` sin errores y `dotnet test` → **81/81**.
- [ ] **Pendiente de revisión visual tuya**: la ventana y el diálogo no los pude abrir (la app estaba
  corriendo y pide login). Mirá sobre todo la fila del catálogo con foto y con descuento.

> Bloque C completo: el escritorio ya tiene catálogo de productos con precio, descuento, stock y foto.
> Queda el bloque D (facturas), que usa esto y los contactos del bloque B.

## Bloque D — Facturas (migración 9) — depende de B y C

### D1. Migración 9: `factura` y `factura_item` — ✅ HECHO (2026-10-03)

- [x] `SchemaVersion = 9` y `case 9` con las dos tablas y los índices `ix_factura_negocio`,
  `ix_factura_fecha` e `ix_factura_item_factura`. La misma DDL va en el **bloque base**
  (`CREATE TABLE IF NOT EXISTS`) para las instalaciones nuevas.
- [x] Tablas nuevas: **no se toca ningún dato existente**.
- [x] `factura.fecha` queda como **TEXT ISO**, la convención del escritorio. Unificarla con los millis de
  Android es el punto **F4**, que toca todas las tablas a la vez: mezclar formatos ahora haría ese trabajo
  más difícil, no menos.
- [x] `ingresoId`, `clienteId` y `factura_item.productoId` son **opcionales (NULL)**: una factura puede ser
  solo un documento, y una línea puede no venir del catálogo.
- [x] **Test nuevo**: las dos tablas, las 13 columnas de `factura`, las 7 de `factura_item`, los tres
  índices y que los tres vínculos acepten NULL (`pragma_table_info."notnull" = 0`).
- [x] **Verificado:** `dotnet test` → **82/82**.

### D2. Emisión de factura — ✅ HECHO (2026-10-03)

- [x] `Models/Modelos.cs`: `ItemFactura` (con `SubtotalCentavos` **calculado** siempre con
  `Precios.Subtotal`, nunca leído de la pantalla), `Factura` (con `DescuentoCentavos` y
  `RegistradaEnCaja`), `ResultadoFactura` (`Emitida` / `SinItems` / `SinCliente`) y
  `EmitirFacturaResultado(Estado, Id, IngresoId)`.
- [x] `BaseDatos`: `Facturas(negocioId)` (sin líneas, de la más reciente a la más vieja),
  `ObtenerFactura(id)` (con líneas), `ContarFacturas`, `SiguienteNumeroFactura` (`"0001"`, **sugerencia
  editable**), `EmitirFactura(...)`, `AsociarIngreso` y `EliminarFactura`.
- [x] **`EmitirFactura`, en el mismo orden que `EmitirFacturaUseCase` de Android:**
  1. Rechaza sin ítems y sin cliente.
  2. `AsegurarContacto` da de alta al cliente por nombre y reusa la ficha si ya existía.
  3. **Recalcula** `subtotal = Σ líneas` y `total = subtotal − descuento general`. El total que venga de
     la pantalla se descarta.
  4. Cabecera + líneas **en una transacción**.
  5. Si se pide, descuenta stock solo de las líneas que vienen del catálogo.
  6. Si se pide y el total es > 0, crea el ingreso `Factura N° <numero> — <cliente>` en la cuenta elegida
     (o `Efectivo`) y lo vincula.
- [x] `EliminarFactura` borra cabecera y líneas en una transacción, pero **no** revierte el stock ni borra
  el ingreso: eso ya pasó en la realidad y se corrige desde Movimientos.
- [x] **Sin gate de licencia en la capa de datos**: el candado va en la UI con
  `MainWindow.PermiteUsoComercial`, igual que Productos y Contactos (en Android el gate está en el
  use case porque ahí no hay otra capa).
- [x] **Tests nuevos** (9): el total recalculado **ignorando valores mentirosos** de entrada; sin ítems y
  sin cliente no guardan nada; el cliente se da de alta una sola vez al facturar dos veces; stock
  descontado solo si se pide y solo para líneas del catálogo; ingreso creado solo si se pide, con
  concepto, cuenta y fecha correctos; cuenta vacía cae en `Efectivo`; numeración correlativa **por
  negocio** y respetando el número que escriba el usuario; eliminar no revierte stock ni ingreso;
  separación por negocio.
- [x] **Verificado:** `dotnet build -c Release` sin errores y `dotnet test` → **91/91**.

### D3. Pantallas de Facturas — ✅ HECHO (2026-10-06)

- [x] `Views/FacturasView.xaml(.cs)`: **página** dentro de la ventana principal (no una ventana aparte).
  Lista del negocio con `N° — cliente`, fecha, documento y cuenta, total, el subtotal y el descuento
  cuando hay, y una chapita **"En caja"** en las que generaron ingreso. Resumen arriba con la cantidad
  emitida y el total acumulado.
- [x] **Entrada Facturas** al final del bloque **TU NEGOCIO · PRO** de la barra lateral, con candado 🔒
  (orden del menú de Android: Productos · Clientes · Proveedores · Facturas).
- [x] `Views/FacturaDialog.xaml(.cs)`: cabecera (cliente con autocompletado de contactos, documento,
  número sugerido, fecha), alta de líneas y `DataGrid` editable con descripción, cantidad, precio,
  descuento % y subtotal calculado.
- [x] **Agregar del catálogo**: toma precio y descuento del producto, y **agregar el mismo artículo otra
  vez suma cantidad** en vez de repetir la línea — equivalente a volver a escanear en Android. Las líneas
  libres (flete, mano de obra) se agregan con su propio botón.
- [x] Totales en vivo: subtotal, descuento general y total, recalculados en cada tecla con
  `Precios.Subtotal` y `Precios.ConDescuento`.
- [x] Dos casillas al emitir: **descontar stock** (apagada por defecto, porque no todos inventarían) y
  **registrar el cobro** (encendida). Con las dos apagadas la factura es solo un documento.
- [x] Las líneas sin descripción, sin cantidad o sin precio **se descartan** al emitir en vez de facturar
  un vacío.
- [x] **Ver una factura emitida abre el mismo diálogo en solo lectura**: campos deshabilitados, grilla
  bloqueada y sin botón de emitir. El historial no se reescribe.
- [x] **Eliminar** avisa, con el texto ajustado según haya generado ingreso o no, que no se devuelve el
  stock ni se borra el cobro.
- [x] **Verificado:** `dotnet build -c Release` sin errores y `dotnet test` → **91/91**.
- [ ] **Pendiente de revisión visual tuya**: el `DataGrid` de líneas y el ancho del diálogo (820×680).

### D4. PDF del comprobante — ✅ HECHO (2026-10-06)

- [x] `Services/Exportar.cs`: `FacturaPdf(factura, negocio, emisor, logoPath, path)` con QuestPDF,
  tomando `RemitoPdf` como base para que los dos papeles se vean de la misma familia (misma cabecera con
  logo opcional, misma tabla, mismos colores de marca `#023A5D` / `#E2DACB`).
- [x] Contenido: negocio y emisor, **FACTURA** con número y fecha, cliente y documento, tabla con
  descripción, cantidad, precio, **descuento por línea** y subtotal, el bloque de totales y las notas.
- [x] El descuento general **solo aparece si lo hubo**: una factura sin descuento no muestra una línea en
  cero que invite a preguntar qué es.
- [x] **Los importes salen de lo guardado, no de un recálculo**: reimprimir una factura dentro de un año
  tiene que dar exactamente el mismo papel que el día que se emitió, aunque el precio del producto haya
  cambiado diez veces desde entonces.
- [x] UI: botón **PDF** en cada fila de `FacturasView`, y al emitir se ofrece generarlo en el momento. El
  archivo va a `Rutas.Exports` como `factura-<número>.pdf` y se abre con el visor del sistema. El número
  lo escribe el usuario, así que los caracteres inválidos para un nombre de archivo se reemplazan por `-`.
- [x] Fallo controlado: si la generación falla, queda registrado en el log y el usuario ve un aviso, no un
  stack trace.
- [x] **Tests nuevos** (2): el PDF se genera, pesa más de 1 KB y arranca con la firma `%PDF`; y una
  factura sin descuento ni notas también sale (son las ramas opcionales del layout). Los tests fijan
  `QuestPDF.Settings.License`, que en la app la setea `App.OnStartup`.
- [x] **Verificado:** `dotnet test` → **93/93**, y se generó un PDF de muestra para revisarlo a ojo.

> Bloque D completo. El escritorio ya factura: catálogo, líneas libres, descuentos por línea y general,
> stock, caja y PDF.

## Bloque E — Paridad de producto (sin riesgo de datos)

### E1. Pantalla de Planes / licencia visible — ✅ HECHO (2026-10-06)

- [x] `Views/PlanesView.xaml(.cs)`: **página** con el estado actual de la licencia (plan, tope de
  negocios, días restantes o "sin vencimiento"), botón **Activar licencia / Actualizar plan** que abre
  `ActivationWindow`, y la comparativa en dos columnas — **Gratis / Tus finanzas personales** contra
  **Con licencia / Tu negocio** — con ✓ y 🔒.
- [x] Entrada **Planes** 👑 en la barra lateral, entre Calculadora y Configuración, que es donde está en
  el menú de Android.
- [x] Al activar desde ahí se llama a `MainWindow.RefrescarPlan()`, así el menú, los candados y la barra
  de días quedan al día sin reiniciar.
- [x] **Cambio de criterio importante, para cumplir la regla de `PLANES.md`:** antes el candado cortaba
  la **navegación** y no se podía ni entrar a Productos, Clientes, Proveedores o Facturas sin licencia.
  Eso rompía *"lo que ya cargaste nunca se te bloquea"*. Ahora **siempre se entra**: lo cargado se ve, se
  imprime y se exporta igual. Lo que pide licencia es **guardar algo nuevo**.
- [x] El gate quedó en un solo lugar, `MainWindow.PermiteGuardarComercial(origen, que)`, que avisa, aclara
  que lo cargado se sigue viendo y **ofrece abrir Planes**. Lo llaman:
  - `ProductosView.Guardar` (y borra la foto que se eligió en el diálogo, que ya no referencia nadie),
  - `ContactosView.Guardar`,
  - `FacturasView.Nueva_Click` (las ya emitidas se ven e imprimen siempre).
- [x] **El alta rápida de contactos sigue libre**: anotar a quién le fiaste al cargar una deuda o un
  remito es uso personal, igual que en Android (`AgregarContactoUseCase` tampoco pide Pro).
- [x] Bloque de recordatorio en la pantalla: *"Lo que ya cargaste nunca se te bloquea"*, con el motivo.
- [x] **Verificado:** `dotnet build -c Release` sin errores y `dotnet test` → **93/93**.
- [ ] **Pendiente de revisión visual tuya**: la comparativa en dos columnas y el bloque de estado.

### E2. Modo de tema "Seguir al sistema" — ✅ HECHO (2026-10-06)

- [x] `Services/Tema.cs`: `ModoActual` ahora acepta `"Sistema"` además de `"Claro"` y `"Oscuro"`.
  `EsOscuro` resuelve: con `"Sistema"` manda Windows, si no, lo que eligió el usuario.
- [x] `WindowsEnOscuro()` lee `AppsUseLightTheme` de
  `HKCU\Software\Microsoft\Windows\CurrentVersion\Themes\Personalize` (0 = oscuro). Si el registro no
  se puede leer, **asume claro**: es el default de Windows y nunca deja la app ilegible.
- [x] **Cambio en vivo**: `SystemEvents.UserPreferenceChanged` reaplica el tema cuando Windows cambia el
  suyo, sin reiniciar. Solo actúa con el modo `"Sistema"` — si el usuario eligió Claro u Oscuro a mano,
  su elección manda. La suscripción se hace en `Cargar()` con un `-=` previo, para no duplicarla.
- [x] `SettingsView`: tercer botón **🖥 Sistema** junto a ☀ Claro y 🌙 Oscuro.
- [x] El resaltado marca el modo **elegido**, no el aplicado: con `"Sistema"` puesto, el botón resaltado
  es Sistema aunque Windows esté en oscuro.
- [x] **Verificado:** `dotnet build -c Release` sin errores y `dotnet test` → **93/93**. La lectura del
  registro se comprobó contra este equipo (`AppsUseLightTheme = 0`, Windows en oscuro).
- [x] `Tema` no tiene tests unitarios porque escribe en `Application.Current.Resources`, que fuera de la
  app WPF es null. Es la misma razón por la que no los tenía antes.

### E3. Copia de seguridad en `.zip` con manifiesto — ✅ HECHO (2026-10-16)

- [x] `BackupService.ExportarZip(destino)`: arma el **mismo formato que Android** —
  `manifiesto.json` (`{app, esquema, fecha}`) + `base/misfinanzas.db` dentro de un `.zip`. La base se saca
  con la **Backup API**, no copiando el archivo vivo, así que la copia es consistente aunque la app esté
  escribiendo. Nombre sugerido `mis-finanzas-YYYY-MM-DD.zip`, igual que en el teléfono.
- [x] `BackupService.RestaurarDesdeArchivo(path)`: acepta `.zip` **y** `.db` suelto, así nadie pierde el
  respaldo que ya tenía. Del `.zip` toma cualquier entrada `.db`, de modo que también puede leer una copia
  hecha por la otra app.
- [x] **Rechaza una copia de un esquema más nuevo** que el de la build, con el mismo mensaje que Android:
  abrirla sería perder datos, que es justo lo que esta función viene a evitar.
- [x] `BaseDatos.SchemaVersion` pasó a ser **pública** (equivale a `VERSION_ESQUEMA` de Android): la
  escribe el manifiesto y la compara la restauración.
- [x] **No se tocó la copia automática a la carpeta externa (Google Drive / OneDrive)**: sigue usando el
  `.db` suelto de `Crear()`, igual que antes. El `.db` queda como formato **interno** (copia previa a
  migrar, lista de copias de esta PC, copia automática) y el `.zip` como formato **portable**.
- [x] `SettingsView` → DATOS: **Guardar una copia (.zip)** con `SaveFileDialog` y **Restaurar desde un
  archivo** con `OpenFileDialog` que acepta los dos formatos. La lista de copias automáticas de la PC
  quedó aparte, con su propio botón **Crear ahora**.
- [x] **Tests nuevos** (4): rechaza esquema más nuevo, rechaza un `.zip` sin base adentro, rechaza un
  archivo inexistente, y el nombre sugerido lleva la fecha. No se prueba `ExportarZip` entero porque
  escribiría sobre la base real de `%APPDATA%`; lo que se verifica es lo que la restauración exige, que es
  la parte que decide si el usuario recupera o pierde sus datos.
- [x] **Verificado:** `dotnet build -c Release` sin errores y `dotnet test` → **97/97**.

> Ojo con la expectativa: el **formato** ya es común, pero los **contenidos** todavía no son
> intercambiables entre las dos apps — el esquema del escritorio va por 9 y el de Android por 15, y las
> fechas se guardan distinto. Eso se cierra en el bloque F (sobre todo F4).

### E4. Decidir avatar: catálogo vs foto

- [ ] Android ofrece avatares de catálogo; el escritorio usa foto del disco con `CropWindow`.
- [ ] **Decisión:** o el escritorio agrega el catálogo como opción además de la foto, o se documenta como
  diferencia aceptada por plataforma. Sin trabajo de datos de por medio.

---

## Bloque F — Unificación de formatos (riesgo alto, al final)

> Estos puntos tocan datos existentes. Ninguno es urgente para la paridad de funciones, pero **todos** hay
> que cerrarlos antes de intentar sincronizar las dos apps (Fase 6 del roadmap del escritorio).

### F1. Retirar `ingreso.metodo` (migración 10)

- [ ] Android lo retiró en v13: `cuenta` ya respondía lo mismo y el formulario preguntaba dos veces.
- [ ] SQLite viejo no soporta `DROP COLUMN`: **recrear `ingreso`** copiando `id, montoCentavos, concepto,
  cuenta, categoria, negocioId, fecha, createdAt`. Se pierde solo el **detalle** del método; el monto y la
  cuenta quedan intactos.
- [ ] Quitar `MetodoPago` de `IngresoDialog` y de `Models/Modelos.cs` (`Ingreso.Metodo`).
- [ ] **Test:** ingresos previos sobreviven con su cuenta y su monto.

### F2. `recurrente.metodo` → `cuenta`

- [ ] `ALTER TABLE recurrente ADD COLUMN cuenta TEXT NOT NULL DEFAULT 'Efectivo';` + backfill desde
  `metodo` con el mismo mapeo de la migración 6 (`MercadoPago`/`Efectivo` tal cual, resto `Banco`).
- [ ] `RecurrenteDialog` pasa a elegir cuenta. `GenerarRecurrentes` crea el movimiento con `cuenta`.
- [ ] **Test:** un recurrente viejo genera un movimiento con la cuenta correcta.

### F3. Decidir `ingreso.categoria`

- [ ] Existe en escritorio (default `'Otros'`), **no existe** en Android.
- [ ] **Decisión:** o se agrega en Android (las categorías de tipo `ingreso` ya existen en las dos apps y
  hoy en Android casi no se usan), o se deja de pedir en escritorio y queda como dato histórico.
- [ ] Recomendación: **agregarla en Android** — ya hay categorías de ingreso sembradas y los reportes
  ganan un corte por categoría de ingreso.

### F4. `fecha` TEXT ISO → INTEGER epoch millis

- [ ] El punto más grande del bloque. Afecta `ingreso`, `egreso`, `deuda` (`fecha`, `fechaLimite`,
  `fechaCobro`) y `agenda`, más todas las consultas por rango y los `DatePicker`.
- [ ] Estrategia: migración que agregue columnas `fechaMs`, backfill con
  `strftime('%s', fecha) * 1000` ajustado a inicio de día local, recrear las tablas, y reemplazar los
  rangos de `Fechas` por millis.
- [ ] **No empezar** hasta que A–E estén cerrados: toca todas las vistas.
- [ ] **Test:** un movimiento cargado antes de migrar sigue cayendo en el mismo día y en el mismo mes del
  reporte.

### F5. Unificar el nombre de la marca de creación

- [ ] Escritorio `createdAt`, Android `fechaRegistro`. Elegir uno (propuesta: `createdAt`, que es el que
  ya usan las tablas nuevas de las dos apps) y renombrar en la otra.
- [ ] Hacerlo **junto con F4**, para no recrear las mismas tablas dos veces.

---

## Orden sugerido y dependencias

```
A1  (independiente, primero: es un bug de correctitud)
 └─ B1 → B2 → B3 → B4        clientes y proveedores (migración 7)
      └─ C1 → C2 → C3        productos (migración 8)
           └─ D1 → D2 → D3 → D4   facturas (migración 9)
                └─ E1        planes y candados (necesita las 3 pantallas Pro)
E2, E3, E4   en cualquier momento (no dependen de nada)
F1 … F5      al final, después de E
```

Hitos:

| Hito | Puntos | Qué se consigue |
|---|---|---|
| **M1** | A1 | Los montos se interpretan igual en las dos apps |
| **M2** | B1–B4 | Clientes y proveedores con ficha, vinculados a gastos y deudas |
| **M3** | C1–C3 | Catálogo de productos con stock y descuentos |
| **M4** | D1–D4 | Facturación completa con PDF, stock y caja |
| **M5** | E1–E4 | Paridad de producto: planes visibles, tema, backup común |
| **M6** | F1–F5 | Formatos unificados; recién acá tiene sentido pensar en sincronización |

---

## Qué NO se porta (decisiones tomadas, no son deuda)

- **Escáner de códigos de barras**: no aplica en escritorio; el código se escribe a mano.
- **Login con contraseña, licencia Ed25519, instalador y actualizador propio**: son del escritorio y no van
  a Android (el teléfono ya tiene bloqueo y Play resuelve cobro y actualización).
- **Atajos de teclado** (`Ctrl+I`, `Ctrl+G`, `Ctrl+B`, `Ctrl+1..7`): no aplican en teléfono.

## Lo que el escritorio tiene y le falta a Android (backlog inverso, para no perderlo)

Agenda con zoom Año/Mes/Día · exportación completa de la base a CSV · copia automática a carpeta externa
con retención · backup `pre-migration` · `PRAGMA integrity_check` al iniciar · log a archivo con rotación y
retención · conteo de deudas vencidas · ventana dedicada de Presupuestos.
