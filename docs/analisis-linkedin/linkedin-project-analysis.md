# Mis Finanzas — Análisis del proyecto para LinkedIn y portfolio

> Documento de análisis y documentación. No modifica código, configuración ni datos.
> Fecha del análisis: 12/10/2026. Commit de referencia: `b0f9279`, con cambios sin commitear en
> `core/backup/EstadoBase.kt` y `core/ui/components/AvisoBaseDanada.kt` (archivos nuevos) más 6
> archivos modificados.
> Toda afirmación lleva su evidencia en el repositorio. Lo que no pude comprobar está marcado
> como **[NO VERIFICADO]**.

---

## 1. Resumen ejecutivo del producto

**Nombre:** Mis Finanzas (`applicationId = com.tallerapp`, `versionName = "1.4"`,
`versionCode = 10`). Evidencia: `app/build.gradle.kts:16-20`.

**Qué es:** una aplicación Android nativa para llevar las cuentas personales y las de un negocio
chico, que funciona enteramente en el teléfono, sin cuenta de usuario y sin conexión a internet.

**El problema que ataca:** casi todas las apps de finanzas piden crear una cuenta, sincronizan
contra un servidor y, en varios casos, piden credenciales bancarias. Para un monotributista,
comerciante o trabajador independiente que solo quiere saber cuánto entró, cuánto salió y quién le
debe, ese costo de confianza y de fricción es alto. Esta app elimina las dos cosas: los datos no
salen del dispositivo.

**La evidencia más fuerte de esa promesa** está en `app/src/main/AndroidManifest.xml`: el
manifiesto **no declara ni un solo permiso**. No hay `INTERNET`, no hay `CAMERA`, no hay
almacenamiento. No es una declaración de marketing: es una restricción verificable a nivel de
sistema operativo.

**Público objetivo:** trabajadores independientes, comerciantes y pymes unipersonales de Argentina
y la región (la app implementa la convención de miles y decimales es-AR, ver 2.4), más cualquier
persona que quiera llevar sus gastos sin registrarse en ningún lado.

**Qué lo diferencia de un CRUD:** cuatro cosas que un ABM genérico no tiene.
1. Aritmética de dinero en enteros (centavos), nunca en punto flotante.
2. 14 migraciones de base de datos reales, versionadas, que preservan los datos del usuario.
3. Una copia de seguridad que hace checkpoint de WAL antes de copiar y se niega a restaurar una
   copia más nueva que la app.
4. Monetización real con Google Play Billing, con la frontera de plan aplicada en la capa de
   dominio y no en la UI.

**Estado real:** publicada en Google Play en **prueba cerrada** con testers. Evidencia:
`NOVEDADES-1.4.md:4` indica que la `1.2` (`versionCode 6`) "está publicada en la prueba cerrada", y
que la `1.4` todavía no se subió. **[NO VERIFICADO desde el repo]** la cantidad de testers, las
métricas de uso y la fecha de salida a producción. El autor indica que la prueba cerrada termina en
3 días (al 12/10/2026).

---

## 2. Auditoría técnica con evidencias

### 2.1 Tamaño y forma del proyecto

| Métrica | Valor | Cómo se obtuvo |
|---|---|---|
| Archivos Kotlin (`app/src/main`) | 210 | `find app/src/main/java -name "*.kt" \| wc -l` |
| Líneas de Kotlin (`app/src/main`) | 15.311 | `find ... -exec cat {} + \| wc -l` |
| Pantallas Compose (`*Screen*.kt`) | 23 | `find app/src/main -name "*Screen*.kt"` |
| ViewModels | 17 | `find app/src/main -name "*ViewModel*.kt"` |
| Módulos de feature | 23 paquetes bajo `features/` | listado de directorios |
| Entidades Room | 13 | `data/local/TallerDatabase.kt:18` |
| Casos de uso | 35 archivos en `domain/usecase/` | listado de directorio |
| Commits | 35, entre 27/06/2026 y 05/10/2026 | `git log` |
| Autor en git | `Fran` (35 de 35 commits) | `git log --format="%an"` |

### 2.2 Stack verificado

| Tecnología | Versión | Para qué se usa | Evidencia |
|---|---|---|---|
| Kotlin + Jetpack Compose | BOM `2025.06.01` | Toda la UI, sin layouts XML | `app/build.gradle.kts:80-91` |
| Material 3 | vía BOM | Sistema de diseño y theming | `app/build.gradle.kts:90` |
| Navigation Compose | 2.7.7 | 27 destinos en un único `NavHost` | `core/navigation/TallerApp.kt` |
| Room + KSP | 2.7.2 | Persistencia local, 13 entidades, esquema v15 | `data/local/` |
| Coroutines + Flow | vía lifecycle 2.8.7 | Todo el estado reactivo | `DashboardViewModel.kt:20-30` |
| Google Play Billing | 9.1.0 | Suscripción al plan Pro | `core/billing/FacturacionPlay.kt` |
| Play In-App Updates | 2.1.0 | Aviso de versión nueva | `core/actualizacion/ActualizacionApp.kt` |
| Play Services Code Scanner | 16.1.0 | Escaneo de códigos **sin permiso de cámara** | `core/scan/EscanerCodigo.kt` |
| Baseline Profiles | 1.4.1 + módulo propio | Arranque en frío más rápido | `baselineprofile/`, `app/build.gradle.kts:110-116` |
| `android.graphics.pdf` | API nativa | PDF sin librería externa | `core/export/PdfExporter.kt` |
| JUnit 4 + coroutines-test | 4.13.2 / 1.8.1 | Tests unitarios JVM del dominio | `app/build.gradle.kts:122-124` |

**Dependencias que NO están y vale la pena notar:** no hay Hilt/Dagger, no hay Retrofit ni OkHttp,
no hay Firebase, no hay librería de gráficos, no hay librería de PDF. Cada ausencia es una decisión
documentada, no un olvido (ver 2.3, 2.8, 2.9).

### 2.3 Arquitectura

Clean Architecture por capas, con separación real y no solo nombres de carpeta:

```
features/<feature>/   → Screen (Compose) + ViewModel
domain/usecase/       → casos de uso, una clase por operación, operator fun invoke()
domain/repository/    → interfaces
domain/model/         → modelos de dominio puros (sin anotaciones de Room)
domain/validation/    → validadores de negocio
data/repository/      → implementaciones de las interfaces de dominio
data/mapper/          → Entity ↔ Modelo
data/local/           → Room: Entity, Dao, Migraciones, Database
core/                 → billing, backup, export, scan, navigation, ui, util, di
```

**Lo que lo hace verificable:** los modelos de `domain/model/` no importan nada de Room, y existe
una capa `data/mapper/` dedicada a traducir. El dominio compila sin Android, y por eso los tests
corren en JVM puro (`testImplementation("junit:junit")`, sin Robolectric y sin `androidTest`).

**Inyección de dependencias manual.** `core/di/AppContainer.kt` (12.573 bytes) construye a mano la
base, los repositorios y los 35+ casos de uso, y los expone a Compose por un `CompositionLocal`
(`AppContainerCompose.kt`). *Decisión:* evita el procesador de Hilt y su costo de compilación en un
proyecto de un solo módulo de app. *Limitación honesta:* es el archivo que más crece con cada
feature nueva, y a cierto tamaño un contenedor manual deja de pagar. A 15 KLOC todavía paga.

### 2.4 Dinero: la decisión técnica más importante del proyecto

Todo importe se maneja como `Long` en **centavos**. Evidencia: `core/util/Dinero.kt:7-8` ("El
importe se maneja en centavos (entero) en todo el sistema para evitar errores de redondeo"), y los
campos `Long` en `domain/model/Ingreso.kt`, `Egreso.kt` y `Factura.kt`.

Lo interesante no es la decisión —es estándar— sino el **parser**. `Dinero.parsearACentavos()`
(`core/util/Dinero.kt:35-58`) resuelve un problema concreto del español rioplatense: `1.500` son
mil quinientos pesos, no uno con cinco. La regla implementada:

- Si hay coma, la coma es el decimal y todo punto es separador de miles.
- Sin coma, un punto es decimal **salvo** que el texto tenga forma de miles: parte entera de 1 a 3
  dígitos que no arranca con `0`, y todos los grupos siguientes de exactamente 3 dígitos. Así
  `1.500` = 1500 y `1500.50` = 1500,50.
- Dos o más comas, o puntos que no son miles, se rechazan.

Un detalle que delata criterio: después de normalizar, el texto se valida contra
`Regex("""^-?(\d+(\.\d*)?|\.\d+)$""")` **antes** de llamar a `toDoubleOrNull()`, con el comentario
`// toDoubleOrNull aceptaría "1e5", "Infinity" o hexadecimales: acá solo dígitos y un punto.`
(`Dinero.kt:54-55`). Eso es haber leído qué acepta realmente `toDouble` en Kotlin.

**El parser nació de un bug real.** `NOVEDADES-1.4.md:38-43` lo documenta: pegar un importe copiado
de un mensaje guardaba `1.500` como `$1,50` y `1.200.000` como `$1,20`. Escribirlo a mano siempre
anduvo; el problema era solo al pegar. Y el documento aclara, con honestidad poco frecuente: *"lo
que ya se guardó mal sigue mal. La actualización no corrige movimientos viejos"*.

Cubierto por tests: `core/util/DineroTest.kt` (8 casos) y `core/ui/components/CampoMontoTest.kt`
(8 casos).

La misma regla está escrita en la app de escritorio hermana, y el comentario lo dice explícitamente:
*"Regla única compartida con la app de escritorio (`Dinero.ParsearACentavos`), porque la misma
cadena tiene que valer lo mismo en las dos"* (`Dinero.kt:24-26`).

### 2.5 Base de datos y migraciones

`TallerDatabase` está en **versión 15** con 13 entidades (`data/local/TallerDatabase.kt:18-21`).

Hay **14 migraciones escritas a mano** (`MIGRATION_1_2` … `MIGRATION_14_15`, 19.634 bytes en
`data/local/Migraciones.kt`). No hay `fallbackToDestructiveMigration()`: lo único declarado es
`fallbackToDestructiveMigrationOnDowngrade()` (`TallerDatabase.kt:54`). En criollo: **ninguna
actualización de la app le borra los datos al usuario**, y está garantizado por configuración.

Las migraciones incluyen cambios destructivos reales y no solo `ADD COLUMN`: la v4 retira la tabla
de trabajos del taller, la v13 retira el método de pago del ingreso porque "la cuenta ya respondía
lo mismo", la v15 convierte los contactos en clientes y proveedores con ficha propia
(`TallerDatabase.kt:9-15`). El proyecto arrancó como app de taller mecánico y se reconvirtió en app
de finanzas sin que nadie perdiera datos. Eso se lee en el historial de migraciones.

### 2.6 Copia de seguridad y recuperación — lo mejor del proyecto

`core/backup/CopiaSeguridad.kt` exporta todos los datos a un único `.zip`. Las decisiones están
documentadas en el código (`CopiaSeguridad.kt:17-32`) y cada una tiene su porqué:

1. **Se copia el archivo de base, no un volcado tabla por tabla.** El razonamiento textual: *"no hay
   forma de que una tabla nueva quede afuera de la copia por olvido: el día que se agregue una, ya
   está adentro"*. Es elegir una clase entera de bug imposible por sobre escribir menos código.
2. **Checkpoint de WAL antes de copiar** (`PRAGMA wal_checkpoint(FULL)`, `CopiaSeguridad.kt:60`),
   porque si no los últimos movimientos podrían estar todavía en el `-wal` y la copia saldría
   incompleta.
3. **El zip lleva un `manifiesto.json` con la versión de esquema** (`CopiaSeguridad.kt:68-71`), y la
   restauración **se niega** a abrir una copia más nueva que la app instalada: Room haría un
   downgrade destructivo, que es justo lo que la función viene a evitar.
4. **`TallerDatabase.cerrar()`** existe solo para la restauración: suelta el singleton y cierra la
   base antes de pisar el archivo, porque con la base abierta SQLite seguiría escribiendo sobre lo
   viejo (`TallerDatabase.kt:62-72`).
5. **Limitación declarada:** las fotos de productos no entran en la copia (son archivos aparte y
   pesan). Tras restaurar quedan con ícono genérico y el resto de la ficha entera.

Encima de eso, `core/backup/EstadoBase.kt` (archivo nuevo, **sin commitear** al 12/10/2026): al
arrancar, fuera del hilo principal, corre un control de integridad de la base y publica el resultado
en un `StateFlow` global que las pantallas observan. Si la base está dañada, la app avisa
(`core/ui/components/AvisoBaseDanada.kt`) y no hace nada más. El comentario explica por qué: *"una
base corrupta no se repara sola, y borrarla por nuestra cuenta sería tomar la decisión más
destructiva posible en lugar del usuario"* (`EstadoBase.kt:13-16`).

### 2.7 Facturación, stock y frontera de plan

`domain/usecase/FacturaUseCases.kt` (5.067 bytes) contiene `EmitirFacturaUseCase`, que guarda
cabecera e ítems y, **opcionalmente**, descuenta stock y registra el cobro como ingreso. Los dos
efectos son banderas separadas a propósito: *"hay quien factura para imprimir nada más, y hay quien
todavía no inventarió el stock"* (`FacturaUseCases.kt:45-49`).

Dos detalles de criterio:

- **El total se recalcula en el caso de uso, no se confía en el que manda la pantalla**
  (`FacturaUseCases.kt:51-53`): el descuento general se aplica sobre la suma de ítems que ya traen
  el suyo. Es no confiar en el cliente, aplicado aun cuando el "cliente" es tu propia UI.
- **La frontera de plan vive en el dominio.** `ResultadoEmitirFactura` incluye el caso `RequierePro`
  como variante del sealed interface, junto a `SinItems` y `SinCliente`
  (`FacturaUseCases.kt:33-40`). El límite comercial es un resultado tipado que el compilador obliga
  a manejar, no un `if` escondido en un `@Composable`.

El modelo de planes es explícito y chico: `Plan.GRATIS` / `Plan.PRO`, con
`LimitesPlan.NEGOCIOS_GRATIS = 1` (`core/billing/Plan.kt`). La regla comercial declarada: uso
personal gratis, uso comercial pago.

`core/billing/FacturacionPlay.kt` implementa Play Billing con `queryProductDetails`,
`queryPurchasesAsync`, `acknowledgePurchase` y un `StateFlow` de estado. Expone un
`data class OfertaPro` con el precio ya formateado por Play en la moneda del usuario, días de
prueba y duración del período. **[NO VERIFICADO]** si hubo compras reales: eso vive en Play Console,
no en el repositorio.

### 2.8 Gráficos y PDF propios

`core/ui/components/Charts.kt` dibuja los gráficos con `androidx.compose.foundation.Canvas` y
primitivas (`Offset`, `Size`, `Stroke`, `StrokeCap`) — sin ninguna librería de charts en
`build.gradle.kts`. Torta de gastos por categoría, ingresos contra gastos y evolución de 12 meses,
todo a mano. *Beneficio:* cero peso agregado al APK y control total del estilo. *Costo:* cada tipo
de gráfico nuevo se paga en código.

Mismo criterio en PDF: `core/export/PdfExporter.kt` usa `android.graphics.pdf.PdfDocument` y dibuja
A4 en puntos a 72 dpi (595 × 842), sin iText ni PDFBox (`PdfExporter.kt:12-19`). Hay tres
generadores —reporte, factura (`FacturaPdf.kt`) y remito (`RemitoPdf.kt`)— más `CsvExporter.kt`.

### 2.9 Privacidad y seguridad

| Aspecto | Estado | Evidencia |
|---|---|---|
| Permisos declarados | **Ninguno** | `AndroidManifest.xml` completo, sin `<uses-permission>` |
| Red | No hay cliente HTTP en dependencias | `app/build.gradle.kts` |
| Cuenta de usuario / login | No existe | no hay feature de auth en `features/` |
| `allowBackup` | `false` (la copia la maneja la app) | `AndroidManifest.xml:7` |
| Compartir archivos | `FileProvider` con `exported="false"` y `grantUriPermissions` | `AndroidManifest.xml:28-36` |
| Escaneo de códigos | Play Services Code Scanner: no pide `CAMERA`, el módulo se baja a demanda | `app/build.gradle.kts:104-106` |
| Release ofuscado | `isMinifyEnabled = true`, `isShrinkResources = true`, R8 con `proguard-android-optimize` | `app/build.gradle.kts:44-48` |
| Firma | Credenciales en `keystore.properties`, fuera de git; si falta, igual compila sin firmar | `app/build.gradle.kts:24-39` |
| Secretos en el repo | **Ninguno trackeado.** `git ls-files` no devuelve `keystore`, `*.jks`, `local.properties` ni `licensing.properties` | `.gitignore` + `git ls-files` |

No hice auditoría ofensiva ni pruebas destructivas. Esto es inspección estática.

### 2.10 Accesibilidad y adaptación de pantalla

Es el área donde el proyecto muestra haber salido al mundo real y vuelto con cicatrices.

- **`core/ui/components/Responsive.kt`**: limita el contenido a `ANCHO_MAXIMO_CONTENIDO = 600.dp`
  (el límite "compact" de Material 3) y lo centra. En teléfono no cambia nada; en tablet,
  Chromebook o teléfono acostado evita líneas de texto que cruzan la pantalla de punta a punta.
- **`core/ui/components/EscalaTexto.kt`**: define `ESCALA_APILADO = 1.3f` como umbral único de toda
  la app y una función `conTopeDeEscala()` para los textos encerrados en cajas de alto fijo (título
  de la barra superior, etiquetas de la barra inferior). El resto del texto escala entero, *"que es
  lo que el usuario pidió en Ajustes de Android"*.
- **El bug que lo originó** está en `NOVEDADES-1.4.md:14-35`: con el tamaño de pantalla grande de
  Android, un contacto llamado "Hermano" se leía `Her / man / o`, y la fecha `04/08/2026` salía en
  cinco renglones. Con la letra al 200 %, el título decía "Movimient" y nada más, en las 23
  pantallas. Se detectó en un teléfono real y se corrigió en 10 pantallas de listas.

### 2.11 Rendimiento

- Módulo `:baselineprofile` propio que genera el perfil, más `profileinstaller:1.4.1` en la app para
  instalarlo al arrancar. El comentario explica el porqué fino: *"sin esto el perfil que viaja en el
  AAB no se aplica en Android 7..8 ni cuando Play todavía no mandó el perfil en la nube"*
  (`app/build.gradle.kts:110-116`).
- Hook opcional de informes de estabilidad del compilador de Compose, detrás de una property:
  `gradlew :app:assembleRelease -Pcompose.informes=true` (`app/build.gradle.kts:68-76`). Sirve para
  encontrar clases de estado inestables que fuerzan recomposiciones de más.
- Estado expuesto con `stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ...)`
  (`DashboardViewModel.kt:20-30`): el flujo se corta 5 segundos después de que la pantalla deja de
  observarlo, en vez de seguir consultando la base.

**[NO VERIFICADO]** no hay mediciones de arranque, de jank ni benchmarks guardados en el repo. El
perfil está configurado; los números no existen.

### 2.12 Testing — lo más flojo, dicho sin maquillar

| Archivo | Casos `@Test` |
|---|---|
| `core/util/DineroTest.kt` | 8 |
| `core/ui/components/CampoMontoTest.kt` | 8 |
| `domain/model/PreciosTest.kt` | 4 |
| `domain/validation/IngresoValidatorTest.kt` | 3 |
| **Total** | **23** |

Hay un `testutil/Fakes.kt` y un directorio `domain/usecase/` **vacío** dentro de los tests. No hay
tests instrumentados (`app/src/androidTest` no existe), no hay tests de migración de Room —la
omisión más cara, dado que hay 14 migraciones a mano—, no hay tests de `CopiaSeguridad`, no hay
tests de `EmitirFacturaUseCase`, no hay cobertura medida y no hay CI.

Los 23 tests que existen están bien elegidos: cubren la aritmética de dinero, que es donde un error
cuesta plata. Pero no alcanzan para llamar al proyecto "testeado".

### 2.13 CI/CD y despliegue

| Aspecto | Estado |
|---|---|
| CI (GitHub Actions u otro) | **No existe** — no hay `.github/`, ni `.gitlab-ci.yml`, ni `Jenkinsfile` |
| Docker | No aplica (app Android) |
| Build de release | Local, firmado con `keystore.properties`, R8 activo |
| Distribución | Google Play, **prueba cerrada** |
| Actualizaciones | In-App Updates de Play (`core/actualizacion/ActualizacionApp.kt`) |
| Política de privacidad | Página propia en `docs/index.html` (lista para GitHub Pages) + `POLITICA-PRIVACIDAD.md` |
| Material de ficha | `FICHA-PLAY.md` con los textos; `play-assets/` con icono 512, banner 1024×500 y 8 capturas (no trackeado en git) |

### 2.14 Documentación

20 archivos `.md` en la raíz (`ARCHITECTURE.md`, `FROZEN-SPEC.md`, `ROADMAP.md` de 32 KB,
`MASTER-ROADMAP.md`, `PUBLICAR.md`, `RELEASE.md`, análisis cruzados con la versión de escritorio).
El código referencia esos documentos desde los comentarios ("Frozen Spec 6 / Arquitectura AD-4",
"ROADMAP sección 12, paso 1"), lo que indica que la documentación se usa y no solo se escribe.

**Observación 1:** el proyecto **no tiene `README.md`**. Para un repo de portfolio es lo primero que
falta. No lo creo sin tu autorización, según la regla 4 del pedido.

**Observación 2:** el `namespace` y el `applicationId` siguen siendo `com.tallerapp`, herencia de
cuando el proyecto era una app de taller. No es un problema funcional —cambiarlo después de
publicar es imposible sin perder la ficha— pero un revisor técnico lo va a notar, y conviene contar
la historia en vez de esconderla.

---

## 3. Funcionalidades: qué está hecho y qué no

### Implementado y verificado en código

**Finanzas personales**
- Ingresos y egresos con categoría, cuenta y negocio (`features/finanzas/`, `RegistrarIngresoUseCase`, `RegistrarEgresoUseCase`, más editar y eliminar).
- Cuentas y medios de pago con saldo (`features/cuentas/`, `CuentaUseCases.kt`).
- Categorías personalizables con ícono (`features/categorias/`, `CategoriaUseCases.kt`).
- Deudas por cobrar con fecha límite y marcado de cobrada (`features/deudas/`, `MarcarDeudaCobradaUseCase.kt`).
- Metas de ahorro (`features/metas/`, `MetaUseCases.kt`).
- Movimientos recurrentes (`features/recurrentes/`, `RecurrenteUseCases.kt`).
- Dashboard con indicadores en vivo (`features/dashboard/`, `ObservarDashboardUseCase.kt`).
- Calculadora integrada (`features/calculadora/`).
- Agenda de tareas y turnos (`features/agenda/`, `AgendaUseCases.kt`).
- Reportes: torta por categoría, ingresos vs. gastos, evolución de 12 meses (`features/reportes/`, `ObservarReporteMensualUseCase.kt`, `ObservarEvolucionUseCase.kt`, `Charts.kt`).
- Exportación a PDF y CSV, compartida vía `FileProvider` (`core/export/`).

**Negocio (plan Pro)**
- Múltiples negocios con cambio de contexto (`features/negocios/`, `NegocioUseCases.kt`).
- Comparativa entre negocios (`ObservarComparativaNegociosUseCase.kt`).
- Catálogo de productos con foto, precio con descuento y stock (`features/productos/`, `ProductoUseCases.kt`, `core/util/ImagenProducto.kt`).
- Escaneo de código de barras sin permiso de cámara (`core/scan/EscanerCodigo.kt`).
- Facturas en PDF que descuentan stock y registran el cobro (`FacturaUseCases.kt`, `FacturaPdf.kt`).
- Remitos en PDF (`features/remito/`, `RemitoPdf.kt`).
- Clientes y proveedores con ficha, alta automática al facturar (`features/contactos/`, `ContactoUseCases.kt`, `AgregarContactoUseCase`).

**Plataforma**
- Copia de seguridad y restauración en `.zip` con manifiesto de esquema (`core/backup/CopiaSeguridad.kt`).
- Detección de base dañada al arrancar (`core/backup/EstadoBase.kt`) — **sin commitear** al 12/10/2026.
- Suscripción Pro con Play Billing (`core/billing/FacturacionPlay.kt`).
- Aviso de actualización disponible (`core/actualizacion/ActualizacionApp.kt`).
- Onboarding (`features/onboarding/`), perfil (`features/perfil/`), ajustes (`features/ajustes/`).
- Moneda secundaria con tasa configurable (`Dinero.simboloSecundario`, `Dinero.tasa`).

### Parcial
- **Testing**: 23 casos unitarios sobre dinero y validación; el resto del dominio sin cubrir.
- **Copia de seguridad**: no incluye las fotos de productos (limitación conocida y documentada).
- **Moneda secundaria**: la tasa se carga a mano, no se actualiza sola (no hay red, por diseño).

### Planificado o inexistente
- CI/CD automatizado.
- Tests de migración de Room y tests instrumentados.
- Sincronización entre dispositivos: incompatible con el diseño sin red; hoy el puente es el `.zip`.
- Play Integrity API: todo en "Desactivado" en Play Console y **no hay código que la llame**. No es
  deuda técnica, es que la app no la necesita.
- La versión 1.4 todavía **no está subida** al Play Console (`NOVEDADES-1.4.md:7`).

### Contexto a declarar
Existe una aplicación de escritorio hermana para Windows (C#/WPF), fuera de este repositorio, con la
que se comparten reglas de negocio: el comentario de `Dinero.parsearACentavos` menciona
explícitamente `Dinero.ParsearACentavos` del escritorio *"porque la misma cadena tiene que valer lo
mismo en las dos"* (`Dinero.kt:24-26`). Hay documentos de análisis cruzado en la raíz
(`ANALISIS-ANDROID-PARA-ESCRITORIO.md`, `ANALISIS-ESCRITORIO-PARA-ANDROID.md`,
`PLAN-ESCRITORIO-AL-DIA.md`). **[NO VERIFICADO]** el estado de esa app: su código no está acá.

### Sobre la autoría
Los 35 commits figuran a nombre de `Fran`. **Dos de ellos** llevan la línea
`Co-Authored-By: Claude`, lo que indica uso de asistencia de IA en parte del desarrollo. Las
publicaciones de la sección 6 están redactadas para no afirmar que todo se hizo sin ninguna
herramienta de asistencia.

---

## 4. Qué vale la pena mostrar en LinkedIn

De todo lo anterior, estos cinco puntos son los que hacen que un técnico piense "este tipo sabe lo
que hace". El resto es ruido.

**1. Cero permisos, verificable en el manifiesto.**
El `AndroidManifest.xml` no declara ni uno. Es la diferencia entre decir "respetamos tu privacidad"
y que el sistema operativo lo garantice. Y el escáner de códigos de barras funciona igual, vía Play
Services Code Scanner, que corre fuera de la app.

**2. El parser de importes en español rioplatense.**
`1.500` son mil quinientos pesos. `1500.50` son mil quinientos con cincuenta. La misma cadena, dos
significados, y la app tiene que acertar siempre. Salió de un bug real: pegar un importe copiado de
un mensaje guardaba `1.200.000` como `$1,20`. Incluye el detalle de validar con regex antes de
`toDoubleOrNull()`, porque `toDouble` acepta `"1e5"` e `"Infinity"`.

**3. Copia de seguridad con criterio de ingeniería.**
Checkpoint de WAL antes de copiar, porque si no los últimos movimientos quedan en el `-wal`.
Manifiesto con versión de esquema. Negarse a restaurar una copia más nueva que la app, porque Room
haría un downgrade destructivo. Y copiar el archivo entero en vez de volcar tabla por tabla, para
que una tabla nueva no pueda quedar afuera por olvido. Cuatro decisiones, cuatro porqués escritos
al lado.

**4. 14 migraciones a mano, cero `fallbackToDestructiveMigration`.**
La base va por la versión 15. El proyecto arrancó como app de taller mecánico y terminó siendo app
de finanzas: la v4 sacó la tabla de trabajos, la v15 convirtió contactos en clientes y proveedores.
Nadie perdió un dato, y está garantizado por configuración.

**5. Accesibilidad encontrada en un teléfono real, no en el emulador.**
Con el tamaño de pantalla grande de Android, un contacto llamado "Hermano" se leía `Her / man / o`.
Con la letra al 200 %, el título de las 23 pantallas decía "Movimient". Se corrigió con un umbral
único (`ESCALA_APILADO = 1.3f`) compartido por toda la app, en vez de parchear pantalla por
pantalla.

**Lo que NO conviene presentar como logro:** usar Compose, usar Room, tener carpetas `domain/` y
`data/`, usar MVVM. Es el piso de la plataforma en 2026, y presentarlo como mérito le baja
credibilidad a todo lo demás.

---

## 5. Plan de capturas de pantalla

**Buena noticia:** ya existen 8 capturas en `play-assets/capturas/` (no trackeadas en git):
`1-inicio.png`, `2-movimientos.png`, `3-reportes.png`, `4-comparativa.png`, `5-deudas.png`,
`6-agenda.png`, `7-productos.png`, `8-facturas.png`. Hay además `banner-1024x500.png`,
`icono-512.png` y una base de datos de demo (`datos-demo.db`, `datos-demo.sql`), o sea que ya tenés
un set de datos ficticios preparado. **Revisalas antes de reusarlas:** se hicieron para la ficha de
Play y podrían corresponder a la 1.2.

**No pude ejecutar la app ni tomar capturas en este análisis**: es un entorno de línea de comandos
sin emulador conectado, y además hubiera requerido tu autorización. Las instrucciones para
regenerarlas están abajo.

### Capturas priorizadas para la publicación

| # | Archivo sugerido | Qué muestra | Qué debe verse | Qué ocultar | Mensaje al lector | Formato |
|---|---|---|---|---|---|---|
| 1 | `01-dashboard.png` | Dashboard con datos de demo | Balance del mes, indicadores en vivo, meta de ahorro | Todo dato real: usar `datos-demo.db` | "Esto es un producto terminado, no una demo" | **Principal, sola** |
| 2 | `02-reportes.png` | Reportes: torta por categoría y evolución 12 meses | Gráficos con datos creíbles | — | "Gráficos dibujados en Canvas, sin librería" | Sola o en collage |
| 3 | `03-factura-pdf.png` | PDF de factura abierto en el visor | El PDF real con ítems, descuento y total | Datos de clientes reales | "Genera documentos, no solo los lista" | Collage |
| 4 | `04-copia-seguridad.png` | Pantalla de copia con el aviso de reemplazo | El texto que advierte antes de restaurar | Rutas con tu usuario | "Pensó en el peor caso" | Collage |
| 5 | `05-escala-texto.png` | **Comparación lado a lado**: misma pantalla con letra normal y al 200 % | Que el título se lee entero en las dos | — | "Probó accesibilidad en serio" | **Compuesta, la más fuerte** |
| 6 | `06-arquitectura.png` | Diagrama de capas (no es captura) | Las 4 capas y la dirección de dependencias | — | Solo si queda prolijo | Opcional |

**Recomendación:** publicá 4 imágenes, no 6. LinkedIn muestra bien hasta 4 en el carrusel. Orden
sugerido: `01-dashboard` (portada), `05-escala-texto` (la que genera comentarios), `02-reportes`,
`03-factura-pdf`.

La #5 es la apuesta diferencial: casi nadie muestra accesibilidad en un posteo de portfolio, y es
exactamente lo que registra un líder técnico con experiencia.

### Para regenerar capturas (instrucciones, no las ejecuté)

1. Levantá el emulador `Pixel_7_gratis` (según tus notas, el `Pixel_7` no se toca).
2. Compilá e instalá el debug con tu procedimiento habitual de build e install.
3. Cargá los datos de demo desde `play-assets/datos-demo.sql` para no exponer datos reales.
4. Para la #5: Ajustes de Android → Pantalla → tamaño de letra al máximo, capturá; volvé a normal,
   capturá la misma pantalla; armá la composición lado a lado.
5. Revisá cada captura antes de subir: sin nombres reales, sin montos reales, sin notificaciones en
   la barra de estado. Poné el dispositivo en modo avión y silenciá notificaciones antes de empezar.

---

## 6. Tres versiones de la publicación

### Versión A — Profesional y técnica

> `1.500` son mil quinientos pesos. `1500.50` son mil quinientos con cincuenta.
>
> La misma cadena, dos significados, y mi app tenía que acertar siempre. No acertaba: pegar un
> importe copiado de un mensaje guardaba `1.200.000` como `$1,20`. Escribirlo a mano andaba bien; el
> problema era solo al pegar.
>
> Así arrancó la versión 1.4 de **Mis Finanzas**, una app Android para llevar las cuentas personales
> y las de un negocio chico, hoy en prueba cerrada en Google Play.
>
> Tres cosas que me llevó tiempo hacer bien:
>
> **1. Aritmética de dinero en enteros.** Todo importe se guarda como `Long` en centavos, nunca en
> `Double`. El parser implementa la convención es-AR: con coma, la coma es el decimal y los puntos
> son miles; sin coma, un punto es decimal salvo que el texto tenga forma de miles. Y valida contra
> una regex antes de llamar a `toDoubleOrNull()`, porque `toDouble` en Kotlin te acepta `"1e5"` y
> `"Infinity"` sin chistar.
>
> **2. Copia de seguridad que no miente.** Checkpoint de WAL antes de copiar el archivo de SQLite,
> porque si no los últimos movimientos todavía están en el `-wal` y la copia sale incompleta. El
> `.zip` lleva un manifiesto con la versión de esquema, y la restauración se niega a abrir una copia
> más nueva que la app instalada: Room haría un downgrade destructivo, que es justo lo que la
> función viene a evitar.
>
> **3. 14 migraciones de Room escritas a mano, cero `fallbackToDestructiveMigration`.** La base va
> por la versión 15. El proyecto arrancó siendo otra cosa y se reconvirtió en el camino. Nadie
> perdió un dato.
>
> Stack: Kotlin, Jetpack Compose, Material 3, Room, Coroutines/Flow, Play Billing. Clean
> Architecture con el dominio sin dependencias de Android, así que los tests corren en JVM puro. DI
> manual en vez de Hilt, porque es un solo módulo. Gráficos dibujados con Canvas y PDFs con la API
> nativa de Android: cero librerías de terceros para las dos cosas.
>
> Y un dato que me gusta más que todo lo anterior: **el manifiesto no declara ni un permiso.** Ni
> `INTERNET`, ni `CAMERA`, ni almacenamiento. No es una promesa de privacidad, es una restricción del
> sistema operativo. El escáner de códigos de barras anda igual, vía Play Services Code Scanner.
>
> Estado real: 15.000 líneas de Kotlin, 23 pantallas, versión 1.4 lista para subir, prueba cerrada
> con testers en curso. Lo que falta: la cobertura de tests es de 23 casos y no hay CI. Lo sé, y está
> en la lista.
>
> ¿Alguien más peleó con el parsing de importes en locales latinoamericanos? Me interesa saber cómo
> lo resolvieron.
>
> #AndroidDev #Kotlin #JetpackCompose #CleanArchitecture #SQLite #DesarrolloDeSoftware

---

### Versión B — Presentación del producto

> Casi todas las apps de finanzas te piden lo mismo antes de dejarte anotar un gasto: creá una
> cuenta, aceptá los términos, dejanos sincronizar tus datos. Algunas te piden las credenciales del
> banco.
>
> Hice **Mis Finanzas** sin nada de eso.
>
> Es una app Android para llevar las cuentas de tu casa y las de tu negocio. La instalás y anotás.
> No hay registro, no hay servidor, no hay sincronización. **El manifiesto de la app no declara ni
> un permiso**: no puede acceder a internet aunque quisiera. Tus datos quedan en tu teléfono y
> punto.
>
> Qué hace hoy:
>
> → Ingresos y gastos con categoría y cuenta (efectivo, banco, tarjeta, Mercado Pago)
> → Quién te debe, con fecha límite y total pendiente siempre visible
> → Reportes con gráfico de torta por categoría y evolución de los últimos 12 meses
> → Exportación a PDF y Excel para mandarle al contador
> → Copia de seguridad en un solo archivo, para cuando cambiás de teléfono
>
> Y para quien tiene un negocio: catálogo de productos con escáner de código de barras, facturas en
> PDF que descuentan stock y registran el cobro, remitos, clientes y proveedores, y comparativa
> entre varios negocios.
>
> La división es simple: **uso personal gratis, uso comercial pago.** Sin publicidad en ninguno de
> los dos.
>
> Está en prueba cerrada en Google Play y la versión 1.4 está lista para subir. 15.000 líneas de
> Kotlin, 23 pantallas, 100 % offline.
>
> La hice porque yo necesitaba esto y no encontré una que no me pidiera registrarme. Si llevás las
> cuentas de un negocio chico, me interesa saber qué te faltó de las apps que probaste.
>
> #Emprendedores #Pymes #Android #FinanzasPersonales #Privacidad

---

### Versión C — Desarrollo y aprendizaje

> El bug que más me enseñó este año no estaba en el código. Estaba en un teléfono prestado.
>
> Alguien abrió **Mis Finanzas** —mi app de finanzas para Android— en un teléfono con el tamaño de
> pantalla grande de Android. Un contacto llamado "Hermano" se leía así:
>
> `Her`
> `man`
> `o`
>
> Y la fecha `04/08/2026` salía cortada en cinco renglones. Con la letra del sistema al 200 %, el
> título de las 23 pantallas decía "Movimient" y nada más: el segundo renglón quedaba tapado por la
> barra.
>
> En mi emulador nunca pasó. Obvio: yo nunca toqué esas opciones.
>
> Lo que aprendí arreglándolo:
>
> **Un umbral, no veinte parches.** La tentación era ir pantalla por pantalla. Terminé definiendo
> una sola constante —la escala de texto a partir de la cual una caja de alto fijo se rompe— y
> usándola en todos lados. El día que el número esté mal, lo cambio en un solo lugar.
>
> **El texto grande es una opción que el usuario eligió.** Mi primer instinto fue topear el tamaño y
> listo. Está mal: si alguien puso la letra al 200 % es porque la necesita. Lo único que topeo es el
> texto encerrado en cajas que no puedo agrandar (título de barra, etiquetas de navegación). El
> resto escala entero.
>
> **Probar en un dispositivo real no es opcional.** No hay sustituto. El emulador te miente por
> omisión: te muestra la configuración que vos elegiste.
>
> Esa misma versión arregló otro bug que también salió del mundo real: pegar un importe copiado de
> un mensaje guardaba `1.500` como `$1,50`. Escribirlo a mano siempre anduvo.
>
> Y acá va lo más incómodo de escribir: **la actualización no corrige los movimientos que ya se
> guardaron mal.** Lo puse tal cual en las notas de la versión. Mentirle al usuario sobre el alcance
> de un fix es peor que el fix que falta.
>
> El proyecto arrancó siendo otra cosa —una app para un taller— y terminó siendo una app de
> finanzas. La base de datos va por la versión 15 con 14 migraciones escritas a mano, y en el camino
> nadie perdió un dato. Esa cadena de migraciones es el diario del proyecto.
>
> Kotlin, Jetpack Compose, Room, Clean Architecture. Hoy en prueba cerrada en Google Play. Usé
> asistencia de IA en partes del desarrollo, como cualquiera en 2026.
>
> ¿Cuál fue el bug que más te enseñó y no estaba en el código?
>
> #DesarrolloDeSoftware #Android #Kotlin #Accesibilidad #Aprendizaje

---

## 7. Cuál publicaría

**La C, "Desarrollo y aprendizaje".**

Razones:

1. **Arranca con una imagen concreta, no con una lista de tecnologías.** `Her / man / o` es visual y
   se entiende en dos segundos. Las publicaciones que arrancan con "Les presento mi nuevo proyecto
   desarrollado con Kotlin y Jetpack Compose" se saltean.
2. **La accesibilidad diferencia.** Hay miles de posteos de apps de finanzas; casi ninguno habla de
   probar con la letra al 200 %. Un líder técnico con experiencia lo registra al instante, porque
   sabe lo que cuesta.
3. **Admitir que el fix no corrige los datos viejos genera más confianza que cualquier lista de
   features.** Es la señal más barata y más creíble de criterio profesional.
4. **Cierra con una pregunta abierta y genérica**, que es lo que mueve el alcance en LinkedIn. La
   versión A cierra con una pregunta muy técnica que contesta menos gente.

La A guardala para cuando postees sobre el motor de la app (el parser, la copia de seguridad) ante
una audiencia más técnica. La B va casi sin cambios como texto de la ficha de Play y para un posteo
dirigido a emprendedores, no a developers.

**Secuencia sugerida:** publicá la C ahora, con la imagen comparativa de escala de texto. Dos
semanas después, la A. Un mes después, la B, cuando salgas de prueba cerrada a producción: ahí
tenés novedad real que contar.

---

## 8. Material complementario para el portfolio

### 8.1 Descripción corta (30-50 palabras)

> Aplicación Android nativa de finanzas personales y de negocio que funciona 100 % sin conexión y
> sin declarar un solo permiso. Kotlin, Jetpack Compose y Room sobre Clean Architecture, con
> facturación, control de stock, reportes en PDF y suscripción vía Google Play Billing. En prueba
> cerrada en Play.

*(47 palabras)*

### 8.2 Descripción extensa (100-150 palabras)

> **Mis Finanzas** es una aplicación Android para llevar las cuentas personales y las de un negocio
> chico sin crear cuenta, sin servidor y sin conexión a internet: el manifiesto no declara ningún
> permiso, por lo que la privacidad la garantiza el sistema operativo y no una promesa.
>
> Incluye ingresos y gastos categorizados, deudas por cobrar, metas, movimientos recurrentes,
> reportes con gráficos dibujados en Canvas y exportación a PDF y CSV. El plan Pro agrega múltiples
> negocios con comparativa, catálogo de productos con escáner de códigos de barras, facturación que
> descuenta stock y registra el cobro, remitos y fichas de clientes y proveedores.
>
> Construida en Kotlin con Jetpack Compose, Material 3 y Room sobre Clean Architecture: dominio sin
> dependencias de Android, importes en centavos enteros, 14 migraciones de base escritas a mano y
> copia de seguridad con checkpoint de WAL y verificación de esquema. 15.000 líneas, 23 pantallas,
> en prueba cerrada en Google Play.

*(149 palabras)*

### 8.3 Stack tecnológico

| Tecnología | Función en el proyecto | Evidencia |
|---|---|---|
| Kotlin | Lenguaje único, JVM target 17 | `app/build.gradle.kts:60-66` |
| Jetpack Compose + Material 3 | 23 pantallas, sin layouts XML | `features/*/*Screen.kt` |
| Navigation Compose 2.7.7 | 27 destinos en un `NavHost` | `core/navigation/TallerApp.kt` |
| Room 2.7.2 + KSP | 13 entidades, esquema v15, 14 migraciones | `data/local/` |
| Coroutines + Flow | Estado reactivo, `stateIn` + `WhileSubscribed` | `features/*/*ViewModel.kt` |
| Play Billing 9.1.0 | Suscripción Pro con precio local y período de prueba | `core/billing/FacturacionPlay.kt` |
| Play In-App Updates 2.1.0 | Aviso de versión nueva | `core/actualizacion/ActualizacionApp.kt` |
| Play Code Scanner 16.1.0 | Escaneo de códigos sin permiso de cámara | `core/scan/EscanerCodigo.kt` |
| Baseline Profiles + ProfileInstaller | Arranque en frío más rápido | `baselineprofile/`, `app/build.gradle.kts:110` |
| `android.graphics.pdf` | PDF de reportes, facturas y remitos | `core/export/PdfExporter.kt` |
| Compose Canvas | Torta, barras y evolución, sin librería de charts | `core/ui/components/Charts.kt` |
| R8 | `minify` + `shrinkResources` en release | `app/build.gradle.kts:44-48` |
| JUnit 4 + coroutines-test | 23 tests unitarios en JVM puro | `app/src/test/` |

### 8.4 Resumen técnico (arquitectura y flujo)

Capas: `features` (Compose + ViewModel) → `domain` (casos de uso, modelos, validadores, interfaces
de repositorio) → `data` (implementaciones, mappers, Room). Las dependencias apuntan siempre hacia
el dominio; los modelos de dominio no conocen Room, y por eso los tests corren sin Android.

El estado fluye en una sola dirección: los DAO exponen `Flow`, los repositorios los mapean a
modelos de dominio, los casos de uso los combinan, y el ViewModel los convierte en `StateFlow` con
`WhileSubscribed(5_000)` para que la consulta se corte cuando la pantalla deja de mirar. La UI es
una función del estado.

Las dependencias se arman a mano en `core/di/AppContainer.kt` y se entregan a Compose por un
`CompositionLocal`. El negocio activo, el plan del usuario y la salud de la base son tres
`StateFlow` globales (`NegocioActual`, `EstadoPlan`, `EstadoBase`) que cualquier pantalla observa.

### 8.5 Principales desafíos y cómo se resolvieron

| Desafío | Solución | Evidencia |
|---|---|---|
| `1.500` vale 1500 o 1,5 según el contexto | Parser con regla de forma-de-miles más validación por regex antes de `toDoubleOrNull()` | `core/util/Dinero.kt:35-58` |
| Copia de seguridad incompleta por el WAL | `PRAGMA wal_checkpoint(FULL)` antes de copiar el archivo | `core/backup/CopiaSeguridad.kt:60` |
| Restaurar una copia más nueva borraría datos | Manifiesto con versión de esquema y rechazo explícito | `core/backup/CopiaSeguridad.kt:17-28` |
| Reconvertir la app sin perder datos de usuarios | 14 migraciones a mano, sin fallback destructivo | `data/local/Migraciones.kt` |
| Texto del sistema al 200 % rompía las 23 pantallas | Umbral único `ESCALA_APILADO` más `conTopeDeEscala()` | `core/ui/components/EscalaTexto.kt` |
| Escanear códigos sin pedir permiso de cámara | Play Services Code Scanner, que corre fuera de la app | `core/scan/EscanerCodigo.kt` |
| Base corrupta al arrancar | Chequeo de integridad fuera del hilo principal y aviso, sin borrar nada | `core/backup/EstadoBase.kt` |

### 8.6 Estado y próximos pasos

**Terminado:** las 23 pantallas, las dos líneas de producto (personal y negocio), copia de seguridad
y restauración, facturación Play, exportación PDF/CSV y las correcciones de accesibilidad de la 1.4.

**Pendiente inmediato (quedan 3 días de prueba cerrada):**
1. Subir el AAB de la 1.4 (`versionCode 10`) al Play Console: todavía no se subió.
2. Commitear `EstadoBase.kt` y `AvisoBaseDanada.kt`, que están sin versionar.
3. Completar Contenido de la app en Play: política de privacidad (la página ya existe en
   `docs/index.html`), seguridad de los datos, acceso a la app, anuncios, clasificación de
   contenido y público objetivo.
4. Revisar si la app entra en la categoría de finanzas que exige declaración adicional.

**Pendiente de ingeniería (post-lanzamiento):**
1. Tests de migración de Room: la omisión más cara con 14 migraciones a mano.
2. Tests de `EmitirFacturaUseCase` y de `CopiaSeguridad`.
3. CI con GitHub Actions: build más tests en cada push.
4. `README.md` del repositorio, que hoy no existe.
5. Incluir las fotos de productos en la copia de seguridad.

### 8.7 Guion para video de 45-60 segundos

| Tiempo | Pantalla | Qué decís |
|---|---|---|
| 0:00-0:05 | Dashboard con datos de demo | "Mis Finanzas: las cuentas de tu casa y de tu negocio, en tu teléfono." |
| 0:05-0:12 | Cargar un gasto: monto, categoría, cuenta, guardar | "Anotar un gasto son cuatro toques. Sin registrarte, sin internet." |
| 0:12-0:20 | Reportes: torta y evolución de 12 meses | "Reportes que se entienden: en qué se te va la plata y cómo venís mes a mes." |
| 0:20-0:28 | Quién me debe, marcar una como cobrada | "Lo que te deben, con fecha. Un toque cuando te pagan." |
| 0:28-0:40 | Escanear código → producto → emitir factura → PDF | "Para tu negocio: escaneás, facturás, y el PDF sale con el stock ya descontado." |
| 0:40-0:50 | Ajustes → Copia de seguridad → archivo generado | "Todo en un archivo, para cuando cambiás de teléfono." |
| 0:50-0:58 | Ajustes de Android mostrando permisos: ninguno | "Y esto: la app no pide ni un permiso. No puede mandar tus datos a ningún lado." |

El cierre es el golpe. Mostrá la pantalla de permisos de Android vacía, en primer plano, sin
locución encima. Esa imagen vale más que los 50 segundos anteriores.

### 8.8 Texto para la sección Proyectos de LinkedIn

> **Mis Finanzas — Aplicación Android de finanzas personales y de negocio**
> *jun. 2026 – actualidad · Proyecto personal*
>
> App Android nativa para llevar las cuentas personales y las de un negocio chico, 100 % offline y
> sin declarar ningún permiso del sistema. Incluye ingresos y gastos categorizados, deudas por
> cobrar, reportes con gráficos propios, exportación a PDF/CSV, catálogo de productos con escáner de
> códigos, facturación con descuento de stock y suscripción vía Google Play Billing.
>
> Kotlin · Jetpack Compose · Material 3 · Room · Coroutines/Flow · Clean Architecture · Google Play
> Billing · Baseline Profiles
>
> 15.000 líneas de Kotlin, 23 pantallas, 13 entidades y 14 migraciones de base de datos. En prueba
> cerrada en Google Play.
>
> **[COMPLETAR]** Enlace a la ficha de Play cuando salga a producción. Enlace al repositorio si lo
> hacés público.

### 8.9 Checklist antes de publicar

**Contenido**
- [ ] Ninguna captura muestra nombres, montos o teléfonos reales: usá `play-assets/datos-demo.db`
- [ ] Ninguna captura muestra notificaciones personales en la barra de estado (modo avión antes de capturar)
- [ ] Las capturas corresponden a la 1.4, no a la 1.2
- [ ] No hay rutas con tu nombre de usuario de Windows visibles en ninguna imagen
- [ ] Ningún número de usuarios, descargas o ingresos sin dato real que lo respalde

**Exactitud**
- [ ] Decís "prueba cerrada en Google Play", no "disponible en Google Play", mientras siga en beta
- [ ] No afirmás tener usuarios activos ni métricas que no podés mostrar
- [ ] Si mencionás la app de escritorio, aclarás que es un proyecto hermano
- [ ] Mencionás el uso de asistencia de IA si alguien pregunta por el proceso (dos commits la declaran)

**Forma**
- [ ] El primer renglón funciona solo: LinkedIn corta el resto detrás de "ver más"
- [ ] Párrafos de 1 a 3 renglones, con línea en blanco entre cada uno
- [ ] Entre 4 y 6 hashtags, no más
- [ ] La pregunta final es genérica y fácil de contestar
- [ ] Publicás martes, miércoles o jueves, entre 8 y 10 de la mañana

**Antes de que alguien entre al repo**
- [ ] Existe un `README.md` (hoy no existe)
- [ ] `EstadoBase.kt` y `AvisoBaseDanada.kt` commiteados
- [ ] `git ls-files | grep -iE "keystore|\.jks|local.properties"` sigue devolviendo vacío

---

## 9. Lo que todavía necesitás verificar vos

Lo que este documento menciona pero **no se puede comprobar desde el repositorio**:

1. **Estado exacto en Play Console.** El repo dice que la 1.2 está en prueba cerrada y que la 1.4 no
   se subió. Confirmalo en la consola antes de publicar nada.
2. **Cantidad de testers y feedback recibido.** Un número concreto ("12 testers") es mucho mejor que
   "prueba cerrada" a secas. Si no lo tenés, no lo inventes.
3. **Si hubo compras del plan Pro.** No hay forma de saberlo desde el código.
4. **Estado real de la app de escritorio hermana.** Su código no está en este repositorio.
5. **Si el repositorio va a ser público.** Si sí: `README.md` primero, y verificá que
   `README-CONTEXTO.md` siga en `.gitignore` (hoy lo está).
6. **Si las capturas de `play-assets/capturas/` son de la 1.4.** Las correcciones de accesibilidad
   son de esta versión; capturas viejas mostrarían justo los bugs que arreglaste.
7. **Fecha exacta de inicio del proyecto** para la sección Proyectos. El primer commit es del
   27/06/2026, pero el trabajo puede haber empezado antes.
8. **Métricas de rendimiento.** El Baseline Profile está configurado, pero no hay mediciones
   guardadas. No afirmes mejoras de arranque sin medirlas.
