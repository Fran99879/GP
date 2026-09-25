# Mis Finanzas — Auditoría del escritorio y roadmap de la app móvil

> Documento guía para llevar el diseño y las funcionalidades de la app de **escritorio**
> (`C:\Proyectos\MisFinanzasDesktop`, C#/.NET/WPF) a la app **móvil**
> (`C:\Proyectos\GP`, Kotlin/Jetpack Compose/Room). Fecha: 2026-09.

---

## 1) Auditoría del escritorio (cómo está diseñado y funciona)

### Navegación y estructura
- **Barra lateral fija** a la izquierda (Navy oscuro) con el título "Mis Finanzas / Finanzas + Negocios" y los ítems:
  **Inicio · Movimientos · Quién me debe · Agenda · Reportes · Negocios · —— · Configuración**.
  El ítem activo se resalta con el color de acento.
- **Barra superior**: a la izquierda el **avatar + "Hola, {nombre}"**; a la derecha el **selector de negocio** (oculto en plan Personal) y la **barra de días de licencia** (semáforo).
- Cada sección es una vista dentro del área de contenido (no ventanas nuevas), salvo diálogos modales.

### Pantallas principales
- **Inicio (Dashboard)**: tarjetas de resumen (Ingresos/Gastos/Balance/Me deben), metas de ahorro con barra, accesos rápidos, aviso de deudas vencidas.
- **Movimientos**: filtros (Desde/Hasta + búsqueda), resumen del período, listas de ingresos y gastos con **ícono de categoría**, y una fila de accesos: **🏦 Cuentas · 🏷️ Categorías · 🎯 Presupuestos · 🔁 Recurrentes · 🧾 Remito**.
- **Quién me debe**: total pendiente, lista con recordatorios (fecha de cobro, vencidas en rojo).
- **Reportes**: selector de mes, balance, dona de gastos por categoría (colores propios), evolución 12 meses + comparativa, presupuestos del mes (semáforo), comparativa entre negocios, exportar PDF/CSV.
- **Negocios**: multi-negocio (según licencia).
- **Configuración**: tarjetas separadas → **Perfil** (nombre, foto) · **Apariencia** (color de acento, modo claro/oscuro, moneda + conversión) · **Seguridad** (cambiar contraseña) · **Datos** (backups locales + copia automática a la nube) · **Licencia** (plan, actualizar plan) · **Aplicación** (versión, buscar actualizaciones, guía rápida, atajos).

### Sistema de diseño
- Paleta de acento configurable (Azul/Rosa/Verde/Violeta/Rojo/Gris) + **modo claro/oscuro**; neutros y contenedores por DynamicResource.
- Componentes: tarjetas (Card), botones Primary/Ghost, íconos vectoriales (Tabler), diálogos con validación (campo inválido resaltado), recorte de foto circular.
- Dinero en **centavos** (entero) con formato `es-AR`, símbolo configurable + equivalente en 2ª moneda.

### Datos / dominio
- SQLite con **migraciones versionadas** (PRAGMA user_version). Tablas: negocio, ingreso, egreso, deuda, agenda, categoria, cuenta, meta, recurrente, user_profile.
- Categorías personalizables (color/ícono/presupuesto) globales; cuentas con saldo; recurrentes que se generan solos; metas; recordatorios de deuda.
- Licencias **Ed25519 offline** (plan personal/negocio con feature `negocios:N`), verificación firma→producto→dispositivo→reloj→vencimiento.
- Backups (SQLite Backup API, integrity_check, retención) + copia automática a carpeta sincronizada con la nube.
- Auto-updater propio: descarga instalador desde Cloudflare R2, verifica SHA-256, instala con consentimiento.

---

## 2) Estado actual de la app móvil

**Arquitectura:** Clean/MVVM — `domain` (model/repository/usecase/validation), `data` (local Room + mapper + repository), `features` (Compose + ViewModel), DI manual (`AppContainer`). Jetpack Compose + Material 3. Room v7.

**Ya portado:**
- ✅ Menú lateral (Navigation Drawer) estilo escritorio: Inicio · Movimientos · Quién me debe · Reportes · Configuración.
- ✅ Modo oscuro (toggle Sistema/Claro/Oscuro, persistido).
- ✅ Categorías personalizables (color + ícono) + usadas en el form de gasto.
- ✅ Cuentas / medios de pago (saldos) + selector en ingreso y gasto.
- ✅ Presupuestos por categoría (tope + sección en Reportes con semáforo).
- ✅ Metas de ahorro (objetivo + progreso).
- ✅ Perfil (👤) en la barra superior del Inicio (abre Configuración; a futuro pantalla propia).

**Base ya existente:** Dashboard, Finanzas (ingreso/egreso), Deudas, Reportes (dona, export PDF/CSV), Licencia (activación).

---

## 3) Diseño objetivo para móvil (cómo acomodarlo mejor)

Adaptar el escritorio al teléfono (no copiar 1:1). Guías:

1. **Navegación**: mantener el **drawer** (☰ a la izquierda) como la barra lateral del escritorio. Evaluar además una **bottom bar** con los 3-4 destinos más usados (Inicio, Movimientos, Reportes) para acceso de un toque.
2. **Barra superior**: título a la izquierda + **avatar/perfil a la derecha** (ya hecho en Inicio). El perfil debe abrir una **pantalla de Perfil** (nombre, foto) — hoy abre Configuración como placeholder.
3. **Configuración organizada en secciones** como el escritorio (tarjetas agrupadas):
   - **Perfil** (nombre, foto)
   - **Apariencia** (tema claro/oscuro ✅, color de acento ⏳, moneda ⏳)
   - **Categorías ✅ · Cuentas ✅ · Presupuestos ⏳(vía categorías) · Recurrentes ⏳ · Metas ✅**
   - **Datos** (exportar/compartir; backup a nube ⏳ o backup del sistema)
   - **Cuenta/Plan** (según monetización — ver sección 4)
   - **Aplicación** (versión, novedades)
4. **Movimientos**: sumar la **fila de accesos** (Cuentas/Categorías/Presupuestos/Recurrentes) arriba de las listas, como el escritorio, y **filtros/búsqueda**.
5. **Listas**: mostrar el **ícono de la categoría** (ya en escritorio; en móvil falta en la lista) y la **cuenta**.
6. **Componentes**: unificar tarjetas, colores por rol (Ingreso verde / Gasto rojo / Deuda ámbar), y respetar el modo oscuro en todas las pantallas nuevas.

---

## 4) Distribución y monetización en el teléfono (recomendación)

### ¿Se puede actualizar la app del teléfono?
Sí, de dos formas — **elegí una según cómo distribuyas**:

- **A) Google Play Store (recomendado):** Google entrega e **actualiza la app automáticamente**. No hace falta el auto-updater propio. Es lo estándar, da confianza y alcance.
- **B) APK directo (como compartís el .exe):** podés hacer un **updater propio** (descarga APK desde tu servidor/R2 + pide instalar), igual que el escritorio. El usuario debe permitir "instalar apps de fuentes desconocidas". Funciona pero es más engorroso y no escala.

### Licencias propias vs. pago mensual (Play Billing)
El sistema de **licencias Ed25519** que usás anda perfecto para **distribución directa** (vos generás y vendés licencias). Pero **no encaja bien con Play Store**:
- Play **exige Google Play Billing** para vender contenido/funciones digitales dentro de la app; no podés cobrar por tu cuenta la parte de "más negocios".
- A los usuarios de celular les cuesta pegar una "clave de licencia"; el modelo natural en móvil es **freemium + suscripción**.

### Recomendación concreta
**Modelo freemium con suscripción por Play Billing**, separando canales:

| | **Móvil (Play Store)** | **Escritorio (directo)** |
|---|---|---|
| Distribución | Play Store (auto-update) | Descarga directa + auto-updater propio (R2) |
| Monetización | **Freemium**: gratis con límite + **suscripción mensual/anual** por Play Billing para "Negocio/Pro" | Licencia Ed25519 (personal / negocio) que ya tenés |
| Qué desbloquea el pago | Multi-negocio, reportes avanzados, remitos, etc. | Igual, por `negocios:N` |

- **Gratis (móvil):** finanzas personales completas (movimientos, categorías, cuentas, presupuestos, metas, reportes básicos). Límite: **1 negocio**.
- **Pro/Negocio (móvil, suscripción):** multi-negocio, comparativa entre negocios, remitos PDF, export avanzado. Precio mensual/anual (el anual con descuento).
- **No mezclar** el candado de licencia Ed25519 en la versión de Play: en móvil el "candado" lo maneja **Play Billing** (estado de suscripción), no una clave. El código de licencias queda para el **escritorio**.

> Si preferís **no** pagar la comisión de Google y mantener tu sistema de licencias también en Android → distribuí el **APK directo** (opción B) y reusás las licencias Ed25519 tal cual (ya hay SDK Android de licencias en el proyecto). Es válido para un público chico/controlado; para masividad conviene Play.

**Sugerencia:** empezá por **Play Store + freemium** para el teléfono (alcance y confianza), y dejá el sistema de licencias para el escritorio. Si más adelante querés, agregás un canal APK directo con licencias para power users.

---

## 5) Roadmap del port móvil (por tandas)

### ✅ Tanda 1 — Base (hecha)
- [x] Menú lateral (drawer) · [x] Modo oscuro · [x] Categorías · [x] Cuentas/saldos · [x] Presupuestos · [x] Metas · [x] Perfil (👤) en la barra.

### ✅ Tanda 2 — Movimientos ricos (completa)
- [x] **Íconos de categoría en la lista** de movimientos.
- [x] **Cuenta** en los subtítulos de la lista (ingresos y gastos).
- [x] **Movimientos recurrentes** (tabla + generación al abrir + pantalla de gestión en Configuración).
- [x] **Recordatorios de deudas** (fecha de cobro opcional + avisos vencidas/próximas en la lista).
- [x] **Búsqueda y filtros** (Desde/Hasta + texto) en Movimientos, con atajos "Hoy" y "Este mes".

### ✅ Tanda 3 — Reportes y apariencia (hecha)
- [x] **Evolución mes a mes + comparativa** en Reportes (barras 12 meses + "gastaste X% más/menos").
- [x] **Color de acento** configurable (Azul/Rosa/Verde/Violeta/Rojo/Gris) además del modo oscuro.
- [x] **Multi-moneda** (símbolo + equivalente por tasa; equivalente bajo el balance de Reportes).
- [x] **Configuración reorganizada** en secciones (Apariencia, Moneda, Herramientas) con scroll.

### ✅ Tanda 4 — Experiencia (hecha, salvo íconos vectoriales)
- [x] **Pantalla de Perfil** propia (nombre, editable, persistido) — el 👤 del Inicio la abre. (Foto: futuro.)
- [x] **Onboarding** la primera vez (guía de bienvenida; se marca vista y no vuelve a salir).
- [x] **Clientes/proveedores frecuentes** — se guardan al cargar deudas y aparecen como chips para autocompletar.
- [ ] **Íconos** vectoriales consistentes (reemplazar emojis) — opcional, para look más pro.

### ⏳ Tanda 5 — Negocio / monetización
- [x] **Multi-negocio** en móvil (tabla `negocio` + selector en Ajustes → Negocios). Cada negocio guarda sus propios ingresos, egresos, deudas y recurrentes; el negocio activo se persiste y todas las consultas se filtran de forma reactiva. Migración v10→v11 (pendiente de probar en emulador).
- [ ] **Comparativa entre negocios** y **Remitos PDF** (dependen de multi-negocio).
- [ ] **Monetización**: integrar **Google Play Billing** (freemium + suscripción) — ver sección 4.
- [ ] Decidir distribución: **Play Store** (recomendado) vs APK directo con licencias.

### 🔒 Fuera de alcance móvil por ahora
- Auto-updater propio (si vas por Play, lo maneja Google).
- Backup a carpeta sincronizada (en Android conviene backup del sistema / export a Drive vía intent).
- Atajos de teclado (no aplican en móvil).

---

## 6) Notas técnicas del port
- Room va por **v7**; cada feature con datos agrega migración (no destructiva) y, si suma columna con default, declarar `@ColumnInfo(defaultValue=...)` para que Room no falle al abrir.
- Compilar/instalar con `JAVA_HOME` al JBR de Android Studio. Si aparece error de **cache de KSP** ("already registered"), compilar `--no-daemon` tras borrar `app/build/kspCaches`.
- Mantener el patrón: entidad+DAO → modelo → repositorio → usecase → DI → pantalla+ViewModel → ruta en `TallerApp` + acceso en Configuración/menú.
