# Mis Finanzas (Escritorio) — Análisis de la app de Windows (referencia para Android)

> Documento de paridad, espejo de `ANALISIS-ANDROID-PARA-ESCRITORIO.md`. Describe **qué hace y cómo está
> hecha la app de escritorio** (`C:\Proyectos\MisFinanzasDesktop`, C#/.NET 10/WPF) con el detalle
> necesario para que la app Android (`C:\Proyectos\GP`, `com.tallerapp`) pueda replicar lo que falta y las
> dos versiones avancen juntas.
>
> Estado al momento de escribirlo: escritorio `Version 1.0.2` (en `.csproj`), esquema
> `PRAGMA user_version` objetivo **6** (`BaseDatos.SchemaVersion`). Android `1.4` / `versionCode 9`,
> esquema Room **v15**.
>
> Mantenimiento: cada cambio de función o de modelo en cualquiera de las dos apps se refleja en la
> sección 13 (paridad) y, si toca datos, en la sección 4.

---

## 1. Qué es la app

App de escritorio para **Windows** que combina **finanzas personales + gestión de varios negocios**,
100 % local (offline-first, sin servidor). Misma marca, misma paleta y misma nomenclatura que la app
Android.

Diferencias de enfoque respecto de Android:

- Es un **producto instalable que se vende**: licencia firmada con Ed25519, instalador Inno Setup,
  actualizador propio y CI/CD con GitHub Actions.
- Tiene **perfil local con contraseña** (login al abrir), porque una PC es compartida.
- Está pensada para pantalla grande: barra lateral de navegación, selector de negocio arriba, ventanas de
  diálogo, atajos de teclado y calculadora acoplada al costado de la ventana.

---

## 2. Stack, build y distribución

| | Escritorio | Android |
|---|---|---|
| Lenguaje / plataforma | C# sobre **.NET 10** (`net10.0-windows`), `Nullable` e `ImplicitUsings` activados | Kotlin |
| UI | **WPF** (XAML), sin MVVM framework: cada vista es `UserControl`/`Window` con code-behind | Compose + Material 3 |
| Base de datos | SQLite con `Microsoft.Data.Sqlite` 10.0.11 y **SQL propio** | Room 2.7.2 |
| PDF | **QuestPDF** 2026.8.0 (licencia Community, seteada en el arranque) | `android.graphics.pdf` propio |
| Gráficos | Canvas/Path de WPF dibujados a mano (dona, barras, evolución) | Canvas de Compose |
| Criptografía | **BouncyCastle** 2.7.0 (Ed25519), **Argon2id** (`Isopoh.Cryptography.Argon2`), DPAPI (`System.Security.Cryptography.ProtectedData`) | Play Billing |
| Tests | **xUnit** en `tests/MisFinanzasDesktop.Tests` (excluido del proyecto de la app) | JUnit4 |

Build y publicación:

```
dotnet build                                   # compilar
dotnet test tests/MisFinanzasDesktop.Tests     # tests
scripts/publish.ps1 -Version X.Y.Z             # .exe self-contained single-file win-x64 + SHA-256 + update.json
scripts/sign.ps1 -File publish\...exe          # firma (si hay certificado)
iscc /DMyAppVersion=X.Y.Z installer\MisFinanzas.iss
```

- Fuente única de versión: `<Version>` del `.csproj`; CI lo sobreescribe con `-p:Version=`.
- `.github/workflows/ci.yml`: restore + build Release + test en cada push a `main` y cada PR.
- `.github/workflows/release.yml`: se dispara **solo** con tags `vX.Y.Z` (valida el formato), corre tests,
  publica, firma si están los secrets, y crea la GitHub Release.
- Instalador: **Inno Setup**, instalación por usuario (`PrivilegesRequired=lowest`), `CloseApplications` +
  `RestartApplications` para que el auto-updater pueda cerrar y reabrir la app. **Los datos del usuario en
  `%APPDATA%\MisFinanzas` no se tocan al instalar ni al desinstalar.**

---

## 3. Arquitectura

Arquitectura simple y plana (no hay Clean Architecture como en Android):

```
App.xaml.cs           arranque: tema, base, licencia, auth, recurrentes, auto-backup, errores globales
MainWindow            marco: barra lateral de navegación, selector de negocio, perfil, barra de licencia
Views/                vistas (UserControl) y diálogos (Window), con code-behind
Models/Modelos.cs     POCOs + enums + etiquetas
Services/             BaseDatos, Dinero, Fechas, Exportar, ExportarDatos, Backup, Tema, Log, Rutas, Online/
Licensing/            ServicioLicencia, LicenciaModelos (payload + canonicalizador), DeviceId
Utilities/            PasswordHasher (Argon2id), ProtectedStore (DPAPI)
Themes/Colors.xaml    paleta, estilos (Card, Primary, Ghost, Nav, H1, H2, Muted) e íconos de línea
```

**Estado global** (`App`): `Db` (`BaseDatos`), `Licencia` (`ServicioLicencia`), `Auth`
(`AuthenticationService`), `NegocioActualId`. Equivale a `AppContainer` + `NegocioActual` + `EstadoPlan`
de Android, pero sin Flows: las vistas tienen un método `Refrescar()` que se llama a mano al navegar o
al cambiar de negocio.

### 3.1 Secuencia de arranque (`App.OnStartup`)

1. `ShutdownMode = OnExplicitShutdown` y licencia Community de QuestPDF.
2. Handlers globales de excepciones: `DispatcherUnhandledException` (muestra un mensaje genérico, registra
   y **marca `Handled`** para no cerrar la app), `AppDomain.UnhandledException` y
   `TaskScheduler.UnobservedTaskException`.
3. `Tema.Cargar()` **antes** de crear ventanas, para que el acento ya esté aplicado.
4. `new BaseDatos()` → crea/migra el esquema.
5. `ServicioLicencia(LicenciaConfig.Cargar())` y `AuthenticationService`.
6. `NegocioActualId` = primer negocio.
7. `Db.IntegridadOk()` (`PRAGMA integrity_check`): si falla, avisa que puede restaurar una copia desde
   Configuración → Datos.
8. Login: `WelcomeWindow` si no hay perfil, `LoginWindow` si ya existe. Si no autentica, cierra.
9. Si `RequiereLicencia && !Habilitada` → `ActivationWindow`; si no activa, cierra.
10. `Db.GenerarRecurrentes()` (envuelto en try/catch, nunca rompe el arranque).
11. `BackupService.EjecutarAutoSiCorresponde(alCerrar: false)`.
12. `MainWindow.Show()` y `ShutdownMode = OnMainWindowClose`.

Al salir (`OnExit`) se ejecuta la copia automática "al cerrar", también a prueba de excepciones.

### 3.2 Rutas de datos (`Services/Rutas.cs`)

Todo bajo `%APPDATA%\MisFinanzas\`:

| Ruta | Contenido |
|---|---|
| `misfinanzas.db` | Base SQLite |
| `Logs\` | Registros con rotación diaria |
| `Backups\` | Copias locales |
| `Exports\` | Exportaciones CSV/PDF |
| `Profile\` | Avatar del perfil |
| `prefs.json` | Apariencia, moneda, onboarding |
| `backup-config.json` | Configuración de copia automática |
| `license.dat`, `clock.dat` | Licencia y protección anti-retroceso de reloj |

---

## 4. Modelo de datos (SQLite, `user_version` objetivo 6)

Conexión: `PRAGMA foreign_keys = ON; PRAGMA journal_mode = WAL; PRAGMA synchronous = NORMAL;`

**Fecha contable: `TEXT` ISO `"yyyy-MM-dd"`.** `createdAt` es epoch millis UTC. *(Android usa epoch
millis para la fecha; ver sección 13.)*

### 4.1 Tablas

| Tabla | Columnas |
|---|---|
| `negocio` | `id`, `nombre`, `createdAt` |
| `ingreso` | `id`, `montoCentavos`, `concepto`, `metodo`, `fecha` (ISO), `createdAt`, + por migración `negocioId`, `categoria` (def. `'Otros'`), `cuenta` (def. `'Efectivo'`) — índice `ix_ingreso_fecha` |
| `egreso` | `id`, `montoCentavos`, `categoria`, `concepto`, `fecha` (ISO), `createdAt`, + por migración `negocioId`, `cuenta` — índice `ix_egreso_fecha` |
| `deuda` | `id`, `nombre`, `montoCentavos`, `fecha`, `nota`, `cobrada`, `fechaCobro`, `createdAt`, + por migración `negocioId`, `fechaLimite` |
| `agenda` | `id`, `negocioId`, `tipo`, `titulo`, `descripcion`, `fecha`, `hora`, `hecho`, `createdAt` — índice `ix_agenda_fecha` |
| `categoria` | `id`, `tipo`, `nombre`, `color`, `icono`, `orden`, `activo`, `presupuestoCentavos` (v4) |
| `recurrente` | `id`, `negocioId`, `tipo`, `montoCentavos`, `concepto`, `categoria`, `metodo`, `diaMes`, `activo`, `ultimoGenerado`, `createdAt` |
| `meta` | `id`, `nombre`, `objetivoCentavos`, `actualCentavos`, `fechaObjetivo`, `createdAt` |
| `cuenta` | `id`, `nombre`, `icono`, `saldoInicialCentavos`, `orden`, `activo` |
| `contacto` | `id`, `nombre` (**UNIQUE COLLATE NOCASE**, global), `createdAt` |
| `user_profile` (v2) | `id`, `username` (UNIQUE), `displayName`, `passwordHash`, `avatarPath`, `createdAt`, `updatedAt`, `lastLoginAt` |

Globales (sin `negocioId`): `categoria`, `cuenta`, `meta`, `contacto`, `user_profile`. Igual criterio que
Android, salvo `contacto` (en Android ya es por negocio y con ficha completa).

### 4.2 Esquema base + migraciones versionadas

El bloque base se crea siempre con `CREATE TABLE IF NOT EXISTS`. Después:

- Se siembra el negocio **"Personal"** si no hay ninguno.
- Se agrega `negocioId` a `ingreso`, `egreso` y `deuda` si falta, **con backfill** al negocio Personal.
- Se agrega `ingreso.categoria`, `deuda.fechaLimite` y `cuenta` en ingreso/egreso si faltan.

Migraciones por `PRAGMA user_version` (`MigrarEsquema`):

| v | Cambio |
|---|---|
| 1 | Baseline (el esquema base ya quedó creado) |
| 2 | `user_profile` |
| 3 | Siembra de categorías por defecto (solo si la tabla está vacía) |
| 4 | `categoria.presupuestoCentavos` |
| 5 | No-op (`meta` ya se crea en el bloque base) |
| 6 | Siembra de cuentas por defecto + migración de `ingreso.metodo` a `cuenta` (`MercadoPago`/`Efectivo` tal cual, el resto a `Banco`) |

Garantías del migrador, a conservar:

- **Antes** de migrar, `BackupService.Crear("pre-migration")` (solo en la base principal, no en tests).
- Cada migración corre **en su propia transacción**; si falla, `Rollback` + log `Critical` y se propaga.
- `user_version` se escribe recién después de aplicar la migración.

### 4.3 Datos iniciales

Idénticos a Android: mismas 7 categorías de egreso y 4 de ingreso con los mismos colores y emojis, y las
cuentas Efectivo 💵 / Banco 🏦 / MercadoPago 💳.

---

## 5. Lógica de negocio

### 5.1 Dinero (`Services/Dinero.cs`)

Centavos (`long`) en todo el sistema, cultura `es-AR`.

- `Formatear(centavos)` → `$ 1.500,50` (vía `ToString("N2", es-AR)`).
- `ParsearACentavos(texto)`: regla **unificada con Android** (punto A1 del plan, ya aplicado). La coma es
  siempre el decimal y entonces los puntos son miles; sin coma, el punto es decimal salvo que el texto
  tenga forma de miles (parte entera de 1 a 3 dígitos que no empieza con 0 y grupos siguientes de
  exactamente 3 dígitos), así `"1.500"` = 1500 y `"1500.50"` = 1500,50. Rechaza vacío, no numérico,
  negativos, dos comas, varios puntos que no son miles, exponentes e `Infinity`.
- `CentavosAEntrada(centavos)` → `1500,50` (coma).
- `Equivalente(centavos)` → `≈ US$ 12,50`, solo con `MostrarEquivalente` y `Tasa > 0`.
- Estado estático: `Simbolo`, `SimboloSecundario`, `Tasa`, `MostrarEquivalente`, seteado desde `Tema`.

### 5.2 Fechas (`Services/Fechas.cs`)

- Guardado: ISO `yyyy-MM-dd` (`HoyIso()`, `Iso(DateTime)`).
- Visualización: `dd/MM/yyyy` (`Mostrar(iso)`, tolerante: si no parsea devuelve el original).
- Rangos `[inicio, fin)` con `InicioDeMes(año, mes)` / `InicioDeMesSiguiente(año, mes)`.
- `EtiquetaMes(año, mes)` → `"Agosto 2026"` con locale `es-AR`.

### 5.3 Recurrentes (`BaseDatos.GenerarRecurrentes`)

Misma regla que Android: activos, `UltimoGenerado != "yyyy-MM"` y `DiaMes <= día de hoy`; la fecha usa
`min(DiaMes, díasDelMes)`; marca el mes y devuelve cuántos creó. **Idempotente por mes.** Se dispara en el
arranque.

### 5.4 Saldo de cuentas

`SaldoCentavos = SaldoInicialCentavos + Σ ingresos − Σ egresos` agrupados por **nombre** de cuenta
(comparación `OrdinalIgnoreCase`), **sobre todos los negocios**. Igual que Android.

### 5.5 Integridad referencial por nombre (reglas a replicar)

- **Renombrar** una categoría propaga el nuevo nombre a los movimientos (`UPDATE ... SET categoria=...`).
- **Eliminar** una categoría **reasigna** sus movimientos a `"Otros"`; la categoría `"Otros"` **no se puede
  eliminar**.
- **Eliminar** una cuenta reasigna los movimientos a `"Efectivo"`; `"Efectivo"` **no se puede eliminar**.
- **Renombrar** una cuenta propaga el nombre a ingresos y egresos.

### 5.6 Deudas

Además de pendientes (suma y cantidad), el escritorio cuenta **deudas vencidas** por `fechaLimite`
(`DeudasVencidasCount`) y la fila muestra un aviso en rojo.

---

## 6. Navegación y pantallas

`MainWindow`: barra lateral izquierda + barra superior (avatar y saludo, barra de días de licencia,
selector de negocio) + `ContentControl` host + toast de actualización disponible.

**Barra lateral:** Inicio · Movimientos · Quién te debe · Agenda · Reportes · Negocios · Calculadora ·
Configuración.

**Atajos de teclado:** `Ctrl+I` nuevo ingreso · `Ctrl+G` nuevo gasto · `Ctrl+B` buscar · `Ctrl+1..7`
secciones. *(Android no tiene equivalente; no aplica.)*

El plan **Personal** (tope 1 negocio) **oculta** la sección Negocios y el selector de la barra superior.

| Pantalla | Qué hace |
|---|---|
| **Inicio** (`DashboardView`) | 4 tarjetas: Ingresos, Gastos, Balance (en Tostado) y "Me deben". Metas de ahorro con "Gestionar metas". Accesos rápidos: ＋ Ingreso, － Gasto, 👥 Quién me debe, 📅 Agenda, 📊 Reportes, 🏪 Negocios |
| **Movimientos** (`FinanzasView`) | Rango **Desde / Hasta** con `DatePicker`, búsqueda por concepto o categoría, botones Buscar / Hoy / Este mes; totales Ingresos / Gastos / Balance; `+ Ingreso` y `− Gasto`; accesos a 🏦 Cuentas, 🏷️ Categorías, 🎯 Presupuestos, 🔁 Recurrentes, 🧾 Remito; listas con Editar / Eliminar |
| **Quién te debe** (`DeudasView`) | Total pendiente de cobro, alta/edición/eliminación, marcar cobrada, aviso de vencida por `fechaLimite` |
| **Agenda** (`AgendaView`) | **Tres niveles de zoom**: `Anio` (12 meses en miniatura), `Mes` (grilla lunes→domingo) y `Dia` (detalle). Flechas ◀ ▶ mueven según el modo. Entradas Tarea / Turno / Producto con hora opcional y "hecho" |
| **Reportes** (`ReportesView`) | Mes navegable ◀ ▶; "Ver todos los negocios (combinado)"; Balance del mes; **dona** de gastos por categoría; **evolución de 12 meses** con leyenda Ingresos/Gastos; comparativa entre negocios; presupuestos del mes ("Cargalos en Configuración → Categorías"); **Exportar PDF** y **Exportar Excel (CSV)** |
| **Negocios** (`NegociosView`) | Crear, renombrar, eliminar; estado de la licencia y botón para activarla |
| **Calculadora** (`CalculadoraWindow`) | Ventana **no modal acoplada** al costado de la ventana principal, que la sigue al mover o redimensionar; reutiliza la instancia si ya está abierta |
| **Configuración** (`SettingsView`) | Grupos en MAYÚSCULAS: **PERFIL** (avatar con cambiar/quitar foto, nombre, usuario), **APARIENCIA** (Claro/Oscuro, 6 acentos, moneda, equivalente + tasa), **SEGURIDAD** (cambio de contraseña), **DATOS** (crear copia, exportar CSV, restaurar), **COPIA AUTOMÁTICA A LA NUBE** (carpeta + frecuencia + copiar ahora), **LICENCIA** (administrar), **APLICACIÓN** (base local, offline-first, buscar actualizaciones, guía rápida, lista de atajos) |
| **Ventanas de gestión** | `CategoriasWindow` + `CategoriaDialog`, `CuentasWindow` + `CuentaDialog`, `MetasWindow` + `MetaDialog`, `RecurrentesWindow` + `RecurrenteDialog`, `PresupuestosWindow` (ventana dedicada), `NegocioDialog`, `AgendaDialog`, `IngresoDialog`, `EgresoDialog`, `DeudaDialog` |
| **Otras** | `RemitoWindow` (remito PDF con logo opcional), `CropWindow` (recorte del avatar), `WelcomeWindow` (primer inicio), `LoginWindow`, `OnboardingWindow` (guía rápida), `ActivationWindow` (licencia), `UpdateWindow` (descarga e instalación) |

Helpers de UI: `Views/Filas.cs` (filas de lista para binding), `Views/Opcion.cs` (opciones de combos),
`Views/Validacion.cs` (`Marcar` pone borde rojo + foco, `Limpiar` lo quita).

---

## 7. Licencias (`Licensing/`)

Equivalente funcional del plan Pro de Android, pero con firma propia.

**Configuración** (`licensing.local.json`, fuera de git, con defaults en código):

| Clave | Default | Significado |
|---|---|---|
| `ProductId` | `mis_finanzas_desktop` | Tiene que coincidir con el del payload |
| `PublicKeyHex` | clave Ed25519 del License Manager | Verificación de firma |
| `DevBypass` | `false` | Todo habilitado, negocios ilimitados |
| `RequireLicense` | `true` | `true` = la app se **bloquea** hasta activar |
| `FreeNegociosMax` | `2` | Negocios sin licencia (solo si `RequireLicense = false`) |

**Payload** (base64 de un JSON): `v`, `product_id`, `plan`, `device_id`, `issued_at`, `expires_at`,
`features[]`, `signature`. El `Canonicalizer` serializa **con claves en orden alfabético y sin espacios**,
sin el campo `signature`, exactamente como el backend y el SDK Android: ese string es lo que se firma y
se verifica con **Ed25519**.

**Validación**, en este orden: firma → `product_id` → `device_id` → reloj → vencimiento. Estados:
`Valida`, `Vencida`, `FirmaInvalida`, `OtroDispositivo`, `OtroProducto`, `RelojAlterado`.

- `DeviceId.Actual()` = `sha256(<hex>)` de `MachineGuid` del registro + `MachineName` (mismo formato que
  el SDK Android). Si no se puede leer el registro, cae a `MachineName`.
- **Anti-retroceso de reloj** (`clock.dat`): si el reloj retrocede más de 1 hora respecto del último
  registrado → `RelojAlterado`. Si la protección falla por I/O, **no** bloquea.
- `expires_at` vacío = licencia **de por vida**.
- Tope de negocios: `DevBypass` = ilimitado; feature `"negocios:N"` = N; sin feature = 1 (plan personal);
  sin licencia = `FreeNegociosMax`.
- La barra superior muestra **días restantes** y una fracción de progreso, y cambia de color al acercarse
  el vencimiento (se oculta si es de por vida).

---

## 8. Perfil y autenticación

- `PasswordHasher`: **Argon2id**, `t=3`, `m=65536` KB (64 MB), `p=1`; hash codificado en un solo string
  (`argon2id$v=19$m=65536,t=3,p=1$<salt>$<hash>`).
- `AuthenticationService`: `Autenticar(password)` devuelve `AuthResultado(Estado, IntentosRestantes,
  SegundosBloqueo)`. **5 intentos** y después **30 segundos** de bloqueo. También `CrearPerfilInicial(...)`
  y `CambiarPassword(actual, nueva)` (exige la actual).
- `ProtectedStore`: DPAPI del usuario de Windows para cualquier secreto local (previsto para futuros
  tokens de nube). No hay criptografía casera.
- Avatar: se guarda en `%APPDATA%\MisFinanzas\Profile\` y se recorta con `CropWindow`.

---

## 9. Copias de seguridad (`Services/BackupService.cs`)

Formato: **copia del archivo SQLite**, no un `.zip` (a diferencia de Android).

- `Crear(motivo)` usa la **Backup API de SQLite** (copia consistente de una base viva), valida el
  resultado y aplica retención de **14** archivos locales.
- `Validar(path)`: comprueba que el archivo sea una base SQLite íntegra.
- `Restaurar(backupPath)`: **primero** crea una copia del estado actual; si la restauración falla, intenta
  volver al estado previo; devuelve `true` solo si la base quedó íntegra.
- `CopiarAExterno(carpeta)`: copia a una carpeta ya sincronizada con Google Drive / OneDrive (o un USB),
  con retención de **10**.
- `EjecutarAutoSiCorresponde(alCerrar)`: según `backup-config.json`
  (`CarpetaExterna`, `Frecuencia` ∈ `Off` / `AlCerrar` / `Diaria` / `Semanal`, `UltimoAutoMs`). Se llama al
  iniciar y al cerrar, y **nunca** rompe ninguno de los dos.
- Se crea un backup automático `pre-migration` antes de cualquier migración de esquema.

---

## 10. Exportación

- `Exportar.Pdf(ReporteMes, path)` y `Exportar.Csv(ReporteMes, path)`: reporte mensual (ingresos, gastos,
  balance, gastos por categoría). `ReporteMes.BalanceCentavos` se calcula, no se guarda.
- `Exportar.RemitoPdf(RemitoDoc, path)`: remito/factura simple con logo opcional del negocio;
  `RemitoItem.SubtotalCentavos = round(Cantidad * PrecioUnitCentavos)` y
  `RemitoDoc.TotalCentavos = Σ subtotales`.
- `ExportarDatos.ExportarCsv(db, destinoBase)`: **exportación completa** de la base a CSV, en una
  subcarpeta con timestamp. Solo lectura, no modifica nada. *(Android no tiene equivalente: ver 13.3.)*

---

## 11. Apariencia (`Services/Tema.cs`, `Themes/Colors.xaml`)

Paleta base idéntica a Android: Navy `#02223A`, Azul `#023A5D`, Marrón `#98643A`, Tostado `#C99A6D`,
Tostado claro `#EDDFCD`. Semánticos **Ingreso `#1E9E5B`**, **Gasto `#D1493F`**, **Deuda `#B8791F`** — y el
comentario del código es explícito: el verde de ingresos y el rojo de gastos **no se tocan** al cambiar de
acento. Superficies: `AppBg #F5F1EA`, `Surface #FFFFFF`, `TextMain #14202B`, `TextMuted #6B6256`,
`Outline #E2DACB`.

Cada acento es una **paleta de 4 colores** (`Accent`, `Dark`, `Container`, `OnDark`), no un color solo:

| Nombre | Accent | Dark | Container | OnDark |
|---|---|---|---|---|
| Azul | `#023A5D` | `#02223A` | `#EDDFCD` | `#C99A6D` |
| Rosa | `#C25E7A` | `#3E2230` | `#F6E3EA` | `#EAC0CD` |
| Verde | `#3E8E6E` | `#1E3A30` | `#E1EFE8` | `#AFD8C6` |
| Violeta | `#7A5AA6` | `#2C2340` | `#ECE5F6` | `#CBBBE8` |
| Rojo | `#C0544B` | `#3A211E` | `#F7E2DF` | `#E8B4AF` |
| Gris | `#5F6B76` | `#242A30` | `#E8EBEE` | `#C6CDD4` |

Todos los brushes se consumen como `DynamicResource`, así que el cambio de acento o de modo se ve en vivo
sin reiniciar. Estilos compartidos: `Card`, `Primary`, `Ghost`, `Nav`, `H1`, `H2`, `Muted`, `NavIcon`.
Íconos: `StreamGeometry` de trazo (Home, Coin, Users, Calendar, Chart, Store, Settings, Pencil, Trash),
los mismos que Android replica en `IconosApp`.

Preferencias en `prefs.json`: `Accent`, `Modo` (`"Claro"` / `"Oscuro"`), `Moneda`, `MonedaSec`, `Tasa`,
`MostrarEquivalente`, `OnboardingVisto`. Monedas disponibles: `$`, `US$`, `€`, `R$`, `Gs` — idénticas a
Android.

> El escritorio tiene **Claro / Oscuro**; Android además tiene **"Seguir al sistema"**.

---

## 12. Robustez, logging y online

- `Log`: niveles `Info` / `Warn` / `Error` / `Critical`, con componente (`"DB"`, `"Backup"`, …), archivo
  con rotación diaria y retención de **30 días**. **Nunca lanza excepciones** y no registra datos
  sensibles.
- Errores de UI: mensaje genérico ("Tus datos no fueron modificados"), sin stack traces, y la app sigue
  viva.
- `IntegridadOk()` al iniciar, con mensaje que apunta a Configuración → Datos.
- `OnlineConfig` (`online.local.json`, gitignored): `Enabled` y `CheckUpdates` en `true` por defecto,
  **`TelemetryEnabled` apagado**, `UpdateManifestUrl` apuntando al manifiesto público en R2.
- `UpdateService.ComprobarAsync`: lee el manifiesto (`productId`, `version`, `downloadUrl`, `sha256`,
  `signature`, `notes`) y devuelve `NoConfigurado` / `Actualizado` / `HayActualizacion` / `SinConexion` /
  `Error`. **Nunca bloquea la app**: sin Internet degrada en silencio.
- `UpdateInstaller`: descarga a `%TEMP%` con progreso, **verifica SHA-256** antes de ejecutar, y lanza el
  instalador de Inno Setup (que cierra la app, actualiza y la reabre). No reemplaza archivos por su cuenta.
- Toast de actualización en `MainWindow` con "Más tarde" / "Actualizar", y confirmación al cerrar la app
  (salvo durante una actualización, vía `App.CierreSinConfirmar`).

Tests (xUnit): migración crea tablas y deja `user_version = 6`; saldo de cuentas; metas; renombrar
categoría propaga y eliminar reasigna; integridad al iniciar; recurrentes una vez por mes; negocio
Personal por defecto; perfil; ingresos suman y filtran por negocio; deudas pendientes y vencidas; parseo de
dinero en formato argentino y rechazo de inválidos; Argon2 + bloqueo tras 5 intentos.

---

## 13. Paridad Escritorio ↔ Android

### 13.1 Lo que el escritorio tiene y a Android **le falta**

| Función del escritorio | Qué habría que hacer en Android |
|---|---|
| **Agenda con zoom Año / Mes / Día** | Android solo tiene Mes + Día. Agregar la vista anual con 12 meses en miniatura y días marcados |
| **Exportación completa de la base a CSV** | `ExportarDatos.ExportarCsv` saca todas las tablas a una carpeta con timestamp. Android solo exporta el reporte mensual |
| **Copia automática a carpeta externa** | Frecuencia `Off` / `AlCerrar` / `Diaria` / `Semanal` + retención. En Android el equivalente sería una copia programada a una carpeta o a Drive |
| **Retención de copias** | 14 locales / 10 externas. La copia de Android es manual, una por vez |
| **Backup previo a migrar** (`pre-migration`) | Android confía en las migraciones de Room; una copia automática antes de migrar sería barata y salva un caso catastrófico |
| **`PRAGMA integrity_check` al iniciar** | Android no lo hace; Room no lo trae gratis |
| **Logging a archivo con rotación y retención** | Android usa `Log.w` del sistema. Un archivo de registro ayuda al soporte |
| **Ventana dedicada de Presupuestos** | En Android viven dentro de Categorías |
| **Deudas vencidas contadas aparte** | Android guarda `fechaLimite` pero no muestra un conteo de vencidas |
| **Filtro por rango Desde/Hasta con dos `DatePicker`** | Android tiene búsqueda y "Este mes"; conviene revisar que el rango libre esté igual de accesible |
| **Perfil con contraseña y bloqueo tras intentos** | Decisión consciente de **no** replicarlo en Android (el teléfono ya tiene bloqueo). Se documenta para que no se "arregle" por error |
| **Atajos de teclado** | No aplica en teléfono |

### 13.2 Divergencias que hay que resolver (riesgo de datos)

| | Escritorio | Android | Acción sugerida |
|---|---|---|---|
| **Parseo de montos** | ~~`"1.500"` = 1500~~ | ~~`"1.500"` = 1,50~~ | ✅ **RESUELTO (A1)**: regla única en las dos apps, con los mismos casos de test en `PuraTests.cs` y `DineroTest.kt` |
| **Texto editable del monto** | `"1500,50"` (coma) | `"1500.50"` (punto) | **Divergencia intencional**: en Android ese texto es el valor *crudo* de `CampoMonto`, que lo muestra con coma y miles por `VisualTransformation`; en escritorio el usuario ve el texto tal cual. El parseo acepta las dos formas |
| `fecha` de movimientos y agenda | `TEXT` ISO `yyyy-MM-dd` | `INTEGER` epoch millis (inicio de día local) | Elegir una (recomendado: epoch millis) y migrar |
| Marca de creación | `createdAt` | `fechaRegistro` | Unificar el nombre |
| `ingreso.metodo` | Existe (`MetodoPago`) | **Retirado** en v13 (duplicaba `cuenta`) | Retirarlo en escritorio: el monto se conserva y el método pasa a `cuenta` |
| `ingreso.categoria` | Existe (def. `'Otros'`) | **No existe** | Decidir: se agrega en Android o se deja de usar en escritorio |
| `recurrente.metodo` | `metodo` | `cuenta` | Unificar a `cuenta` |
| `contacto` | `(id, nombre UNIQUE NOCASE, createdAt)`, global | Ficha por negocio con tipo, documento, teléfono, correo, dirección y nota | Portar al escritorio la migración 14→15 de Android |
| Modo de tema | Claro / Oscuro | Sistema / Claro / Oscuro | Agregar "Seguir al sistema" en escritorio |
| Versión de esquema | `user_version` 6 | Room 15 | Mantener una tabla de equivalencias cuando el backup sea común |

### 13.3 Lo que Android tiene y al escritorio **le falta**

Resumen (el detalle está en `ANALISIS-ANDROID-PARA-ESCRITORIO.md`, sección 11.1): catálogo de
**productos**, **clientes y proveedores** con ficha, **facturas** con ítems y emisión (descuento de stock
y registro en caja opcionales), los **vínculos por id** entre esas entidades y los movimientos, la
pantalla de **Planes**, y el **backup en `.zip` con manifiesto** (formato más portable que la copia cruda).

### 13.4 Reglas que las dos apps deben compartir siempre

1. Importes en centavos (`long`), nunca `double`.
2. Formato de dinero `$ 1.500,50`; fechas `dd/MM/yyyy`; meses con locale `es-AR`.
3. Rangos `[inicio, fin)`.
4. Mismos textos de validación, palabra por palabra.
5. Mismo set de datos iniciales (categorías, cuentas, negocio "Personal").
6. Mismos colores semánticos (Ingreso / Gasto / Deuda) y misma paleta de la dona, sin alterarlos al
   cambiar de acento.
7. Eliminar una categoría reasigna a `"Otros"`; eliminar una cuenta reasigna a `"Efectivo"`; ninguna de
   las dos se puede borrar.
8. Renombrar categoría o cuenta propaga el nombre a los movimientos.
9. Categorías, cuentas y metas son globales; movimientos, deudas, agenda y recurrentes van por negocio.
10. Nunca una migración destructiva hacia adelante; copia de seguridad antes de migrar.
11. El total de un comprobante se recalcula en el dominio, nunca se toma de la pantalla.
12. Vencer el plan o la licencia **no** oculta ni borra datos ya cargados.

---

## 14. Hoja de ruta propia del escritorio

`docs/fases/` (todas marcadas **Planificación**, pero con buena parte ya implementada):

| Fase | Tema |
|---|---|
| 1 | Producto / Experiencia de usuario |
| 2 | Seguridad y confiabilidad (Argon2, DPAPI, logging, manejo de errores) |
| 3 | Distribución, CI/CD y actualizaciones (publish, firma, Inno Setup, GitHub Actions) |
| 4 | Robustez, base de datos y recuperación (migraciones versionadas, backups, integridad) |
| 5 | Online opcional (updates, telemetría apagada por defecto) |
| 6 | Cloud + Sync + Multiplataforma — **diseñar ahora, no implementar**: la regla es no tomar decisiones que impidan evolucionar de local-first a cloud sin tirar abajo SQLite |

La Fase 6 es justamente donde las divergencias de la sección 13.2 (fechas, nombres de columnas, `metodo`,
`contacto`) se vuelven caras: conviene cerrarlas antes de empezar cualquier sincronización.

---

## 15. Documentos relacionados

En `C:\Proyectos\MisFinanzasDesktop`: `RESUMEN-APP.md`, `ARQUITECTURA-Y-ROADMAP.md`, `MEJORAS-FUTURAS.md`,
`README.md`, `docs/BUILD.md`, `docs/RELEASE.md`, `docs/UPDATES.md`, `docs/VERSIONING.md`,
`docs/CODE-SIGNING.md`, `docs/fases/FASE-*.md`.

En `C:\Proyectos\GP`: `ANALISIS-ANDROID-PARA-ESCRITORIO.md` (el espejo de este documento), `SPEC.md`,
`FROZEN-SPEC.md`, `ARCHITECTURE.md`, `PLANES.md`, `ROADMAP.md`,
`UI-ESCRITORIO-ANALISIS-Y-ROADMAP-MOVIL.md`.
