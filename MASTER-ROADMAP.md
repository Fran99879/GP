# 🗺️ MASTER EXECUTION ROADMAP — TallerApp

**Proyecto:** TallerApp — Gestión Simple para Talleres
**Versión:** 1.0 (MVP)
**Fuente de verdad:** [FROZEN-SPEC.md](FROZEN-SPEC.md) (🟢 SPEC FROZEN, v1.0-FROZEN)
**Naturaleza de este documento:** Plan de ejecución. No contiene código, diseño de base de datos, arquitectura técnica ni elección de tecnologías.

---

## Cómo leer este roadmap

- Las fases están pensadas para ser **pequeñas, secuenciales y auditables**: cada una se desarrolla, se prueba y se cierra antes de empezar la siguiente.
- Toda referencia `RN-x`, `V-x`, `CA-x`, `9.x` remite a la sección correspondiente del Frozen Spec.
- Una fase **no avanza** hasta cumplir su sección *"Qué debe quedar terminado antes de avanzar"*.
- Regla transversal: ninguna fase puede introducir funcionalidad fuera del alcance (sección 4 del Frozen Spec). Cualquier desvío exige un Change Request.

---

# FASE 1 — Fundación del Proyecto

**Objetivo**
Dejar una aplicación **navegable** de extremo a extremo (esqueleto), con todas las pantallas accesibles y el flujo general recorrible, aún sin funcionalidad real.

**Alcance**
- Organización inicial del proyecto y convenciones de trabajo.
- Navegación principal entre los 5 módulos (Dashboard, Trabajos, Finanzas, Reportes, Búsqueda integrada).
- Estructura de pantallas vacías: Dashboard, Lista de Trabajos, Alta/Detalle de Trabajo, Ingresos, Egresos, Reportes.
- Definición del flujo general de navegación y de los accesos rápidos del Dashboard (aún no funcionales).

**Módulos involucrados**
Todos (a nivel cascarón/navegación). Ninguno con lógica.

**Funcionalidades incluidas**
- Acceso a cada pantalla.
- Botones de navegación y accesos rápidos enlazados a sus pantallas destino.
- Estados visuales vacíos (placeholders).

**Dependencias**
Ninguna. Es la base del proyecto.

**Riesgos**
- Definir una navegación que luego no soporte los flujos de cobro/edición → revisar contra secciones 6 y 9 del Frozen Spec antes de cerrar.
- Sobrediseñar pantallas que cambiarán en fases siguientes.

**Checklist de implementación**
- [ ] Estructura del proyecto creada y documentada.
- [ ] Mapa de navegación entre todas las pantallas del MVP.
- [ ] Pantallas placeholder para los 6 destinos principales.
- [ ] Accesos rápidos del Dashboard enlazados (Nuevo Trabajo / Nuevo Ingreso / Nuevo Gasto).
- [ ] Recorrido completo ida y vuelta sin callejones sin salida.

**Checklist de pruebas**
- [ ] Se puede llegar a cada pantalla desde el Dashboard.
- [ ] Se puede volver atrás desde cada pantalla.
- [ ] No hay enlaces rotos ni pantallas inalcanzables.

**Criterios de aceptación**
- App navegable completa; cada acceso lleva a su pantalla; sin lógica de negocio aún.

**Qué debe quedar terminado antes de avanzar**
Navegación estable y mapa de pantallas aprobado contra el Frozen Spec. No se toca lógica de datos hasta cerrar esto.

---

# FASE 2 — Gestión de Trabajos

**Objetivo**
Construir el módulo principal: registrar y administrar vehículos/trabajos con sus estados y validaciones.

**Alcance**
- Alta de trabajo (campos sección 5.1, obligatorios V-1).
- Edición (respetando reglas de bloqueo: Precio bloqueado si `Cobrado`, fecha de ingreso no editable).
- Eliminación de trabajo.
- Lista de trabajos con tarjetas (Patente o `Marca+Modelo+Cliente` si no hay patente — C5).
- Detalle de trabajo.
- Estado de reparación y **máquina de transiciones** (sección 7) con cambio de un solo toque.
- Estado de cobro inicial `Pendiente de cobro` (visible, aún sin flujo de cobro → Fase 4).
- Búsqueda por cliente y por patente (sección 11, RN-10).
- Validaciones V-1, V-4, V-8, V-9.

**Módulos involucrados**
Trabajos (núcleo). Persistencia local de la entidad Trabajo.

**Funcionalidades incluidas**
- CRUD de trabajos.
- Cambio de estado con transiciones válidas únicamente (V-9).
- Lista con identificación correcta de tarjetas.
- Búsqueda.

**Dependencias**
Fase 1 (navegación y pantallas).

**Riesgos**
- Implementar transiciones libres en lugar de la máquina de 7.2 → inconsistencias de estado.
- Permitir editar Precio tras cobro (aún no existe cobro, pero dejar la regla lista para Fase 4).
- Tarjetas sin patente mal identificadas.
- Servicio realizado cargado como texto libre en lugar de lista cerrada (5.4).

**Checklist de implementación**
- [ ] Formulario de alta con obligatorios validados (V-1).
- [ ] "Servicio realizado" como lista cerrada (5.4).
- [ ] Edición con campos bloqueados según reglas.
- [ ] Eliminación con confirmación.
- [ ] Lista con tarjetas (Patente / fallback C5).
- [ ] Detalle del trabajo.
- [ ] Cambio de estado por toque, solo transiciones válidas (7.2 / V-9).
- [ ] Estado de cobro mostrado (`Pendiente de cobro`).
- [ ] Búsqueda por cliente y patente.
- [ ] Patente normalizada (V-4).

**Checklist de pruebas**
- [ ] No se guarda un trabajo sin obligatorios.
- [ ] Transiciones inválidas no se ofrecen.
- [ ] `Cancelado` y `Entregado` son terminales según 7.2/7.3.
- [ ] Tarjeta sin patente muestra Marca+Modelo+Cliente.
- [ ] Búsqueda encuentra por patente y por cliente (texto).
- [ ] Editar un trabajo respeta campos bloqueables.

**Criterios de aceptación**
CA-2, CA-3, CA-13 (parcial: identificación de tarjetas y búsqueda), CA-14 (estado `Cancelado` no genera movimientos).

**Qué debe quedar terminado antes de avanzar**
CRUD + estados + búsqueda probados. Un trabajo puede recorrer su ciclo de reparación completo y quedar `Entregado` + `Pendiente de cobro`.

---

# FASE 3 — Finanzas

**Objetivo**
Construir el módulo financiero independiente: ingresos manuales y egresos, con cálculo de caja.

**Alcance**
- Registro de Ingreso manual (5.2, `Origen = Manual`).
- Registro de Egreso (5.3).
- Categorías de egreso (5.6) y métodos de pago (5.5, incluido el cascarón de `Pago mixto` — su validación completa se ejercita aquí).
- Cálculo de Caja del día (RN-4) e insumos para Ganancia (RN-1).
- Edición/anulación de movimientos del día (9.7, V-7).
- Validaciones V-2, V-3, V-6, V-10.

**Módulos involucrados**
Finanzas. Persistencia local de Ingreso y Egreso.

**Funcionalidades incluidas**
- Alta de ingreso y egreso (< 30 s).
- Listado de movimientos del día con edición/anulación.
- Cálculo de caja del día.

**Dependencias**
Fase 1. Independiente de Trabajos (los cobros se conectan en Fase 4).

**Riesgos**
- Implementar caja como neto (ingresos − egresos) cuando RN-4 la define como total de ingresos del día → corregir contra RN-4.
- `Pago mixto` sin validación de suma = total (V-6).
- Permitir editar movimientos de días anteriores (viola V-7).

**Checklist de implementación**
- [ ] Alta de ingreso manual con `Origen = Manual`.
- [ ] Alta de egreso con categoría obligatoria.
- [ ] Listas de métodos (5.5) y categorías (5.6) cerradas.
- [ ] `Pago mixto`: reparto con suma validada (V-6).
- [ ] Cálculo de Caja del día (RN-4).
- [ ] Edición/anulación solo de movimientos del día (V-7).
- [ ] Validaciones de monto (V-10).

**Checklist de pruebas**
- [ ] No se guardan montos ≤ 0 ni conceptos vacíos.
- [ ] Pago mixto rechaza sumas que no cuadran.
- [ ] Caja del día = total de ingresos del día (todos los métodos).
- [ ] Movimiento de ayer no es editable.
- [ ] Anular un movimiento del día lo retira de la caja.

**Criterios de aceptación**
CA-6, CA-8, CA-9; insumo de CA-10 (cálculo de caja).

**Qué debe quedar terminado antes de avanzar**
Finanzas funciona de forma autónoma; la caja del día refleja correctamente ingresos y egresos del día.

---

# FASE 4 — Cobros

**Objetivo**
Vincular Trabajos con Finanzas: registrar el cobro al entregar un vehículo, generando un ingreso asociado.

**Alcance**
- Acción "Registrar cobro" disponible solo si estado de reparación = `Entregado` (V-5).
- Creación de **un único Ingreso** con `Origen = Cobro de trabajo` y `Trabajo asociado` (9.3, RN-3).
- Transición de estado de cobro a `Cobrado` y bloqueo del Precio (V-8).
- `Pago mixto` aplicado al cobro (V-6).
- Anulación de cobro el mismo día (9.4): elimina el ingreso, revierte a `Pendiente de cobro`.
- Actualización automática de caja.
- Regla anti-doble-conteo (RN-3): el cobro es la única vía de ingreso ligada a un trabajo.

**Módulos involucrados**
Trabajos + Finanzas (integración).

**Funcionalidades incluidas**
- Flujo de cobro completo (9.3) y anulación (9.4).
- Reflejo del ingreso en caja.

**Dependencias**
Fase 2 (Trabajos con estado `Entregado`) y Fase 3 (Ingresos/caja).

**Riesgos**
- **Doble conteo**: que el cobro y un ingreso manual sumen el mismo dinero (RN-3) → bloquear/educar el uso.
- Cobrar sin estar `Entregado` (V-5).
- Anular cobro sin revertir estado de cobro o sin eliminar el ingreso.
- Precio no bloqueado tras cobro (V-8).

**Checklist de implementación**
- [ ] "Registrar cobro" solo en `Entregado` (V-5).
- [ ] Cobro crea un único ingreso asociado (9.3).
- [ ] Estado de cobro → `Cobrado`, Precio bloqueado (V-8).
- [ ] Pago mixto en cobro (V-6).
- [ ] Anular cobro del día revierte ingreso y estado (9.4).
- [ ] Caja se actualiza automáticamente.

**Checklist de pruebas**
- [ ] Trabajo fiado: `Entregado` + `Pendiente de cobro` se mantiene hasta cobrar.
- [ ] Cobrar genera exactamente un ingreso (no duplica).
- [ ] Anular cobro deja el trabajo `Pendiente de cobro` y `Entregado`.
- [ ] No se puede cobrar un trabajo no entregado.
- [ ] Tras cobro, el Precio no se edita.

**Criterios de aceptación**
CA-4, CA-5, CA-7.

**Qué debe quedar terminado antes de avanzar**
El ciclo completo trabajo → entrega → cobro → caja funciona y es reversible el mismo día sin inconsistencias.

---

# FASE 5 — Dashboard

**Objetivo**
Construir la pantalla principal con indicadores en vivo alimentados por los módulos ya desarrollados.

**Alcance**
- Caja del día (RN-4).
- Ganancia del mes (RN-1, mes calendario RN-7).
- Vehículos en taller (RN-8).
- Esperando repuestos.
- Trabajos pendientes.
- Trabajos entregados hoy.
- Accesos rápidos funcionales.

**Módulos involucrados**
Dashboard (lectura), consumiendo Trabajos, Finanzas y Cobros.

**Funcionalidades incluidas**
- Los 6 indicadores de 12.1.
- Accesos rápidos enlazados (ya funcionales).

**Dependencias**
Fases 2, 3 y 4 (toda la data proviene de allí).

**Riesgos**
- Indicadores mal definidos (p. ej. contar `Entregado`/`Cancelado` como "en taller" — RN-8).
- Ganancia del mes con criterio distinto al de Reportes (debe coincidir con RN-1/RN-2).
- Desfase entre dato mostrado y dato real al crear/anular movimientos.

**Checklist de implementación**
- [ ] Caja del día (RN-4).
- [ ] Ganancia del mes (RN-1, RN-7).
- [ ] Conteo de vehículos en taller (RN-8).
- [ ] Esperando repuestos / Pendientes / Entregados hoy.
- [ ] Accesos rápidos operativos.

**Checklist de pruebas**
- [ ] Crear/cancelar trabajos actualiza los conteos correctamente.
- [ ] Un trabajo `Entregado` o `Cancelado` no cuenta como en taller.
- [ ] Cobrar/anular actualiza caja y ganancia del mes.
- [ ] "Entregados hoy" solo cuenta los del día.

**Criterios de aceptación**
CA-1; consistencia con CA-10/CA-14.

**Qué debe quedar terminado antes de avanzar**
Todos los indicadores reflejan en tiempo real el estado real de los módulos.

---

# FASE 6 — Reportes

**Objetivo**
Construir los reportes diario y mensual con sus métricas.

**Alcance**
- Reporte diario (12.2): caja, ingresos, gastos, ganancia del día.
- Reporte mensual (12.3): ingresos, gastos, ganancia del mes; trabajos realizados (RN-9); vehículos atendidos (RN-9); servicios más realizados (conteo por "Servicio realizado", excluyendo cancelados).
- Período sin datos = ceros (12.5).

**Módulos involucrados**
Reportes (lectura sobre Trabajos y Finanzas).

**Funcionalidades incluidas**
- Cálculo de ganancia por criterio de caja (RN-1, RN-2).
- Definiciones de conteo (RN-9) aplicadas.

**Dependencias**
Fases 2, 3, 4 (datos) y conveniente tras Fase 5 (mismas definiciones que el Dashboard).

**Riesgos**
- "Trabajos realizados" vs "Vehículos atendidos" mal diferenciados (RN-9).
- "del mes" ≠ mes calendario (RN-7).
- "Servicios más realizados" contando cancelados o deduciendo de texto en vez del campo (12.3).

**Checklist de implementación**
- [ ] Reporte diario con sus 4 métricas.
- [ ] Reporte mensual con todas sus métricas.
- [ ] Servicios más realizados desde el campo (5.4), sin cancelados.
- [ ] Manejo de período sin datos (ceros).

**Checklist de pruebas**
- [ ] Ganancia diaria/mensual = ingresos − egresos del período.
- [ ] Conteos RN-9 correctos con casos mixtos.
- [ ] Mes calendario respetado en bordes de mes.
- [ ] Reporte vacío muestra ceros, no error.
- [ ] Servicios más realizados coincide con los trabajos cargados.

**Criterios de aceptación**
CA-10, CA-11, CA-15.

**Qué debe quedar terminado antes de avanzar**
Reportes consistentes con el Dashboard y con las reglas de negocio.

---

# FASE 7 — Exportaciones

**Objetivo**
Permitir compartir los reportes en PDF y Excel.

**Alcance**
- Exportación a PDF y Excel de los reportes diario y mensual (12.4).
- La exportación refleja **exactamente** lo mostrado en pantalla para el período.

**Módulos involucrados**
Reportes (salida).

**Funcionalidades incluidas**
- Generación y compartido de PDF.
- Generación y compartido de Excel.

**Dependencias**
Fase 6 (reportes calculados).

**Riesgos**
- Discrepancia entre lo exportado y lo mostrado.
- Datasets grandes sin rango claro (mitigado: solo día/mes definidos).
- Formato de montos/decimales inconsistente con la app (V-10).

**Checklist de implementación**
- [ ] Exportar reporte diario a PDF y Excel.
- [ ] Exportar reporte mensual a PDF y Excel.
- [ ] Formato de montos coherente con la app.

**Checklist de pruebas**
- [ ] Valores exportados = valores en pantalla.
- [ ] Exportación de período vacío produce documento con ceros, sin fallar.
- [ ] Archivos se generan y comparten correctamente.

**Criterios de aceptación**
CA-12.

**Qué debe quedar terminado antes de avanzar**
Ambos formatos exportan fielmente ambos reportes.

---

# FASE 8 — Pulido de UX

**Objetivo**
Llevar la experiencia a las metas de uso del Frozen Spec sin alterar funcionalidad.

**Alcance**
- Optimización de formularios (alta de trabajo < 1 min; ingreso/gasto < 30 s).
- Botones rápidos y grandes (principios sección 3).
- Confirmaciones en acciones destructivas (eliminar trabajo, anular cobro/movimiento).
- Mensajes de error claros para todas las validaciones (V-1..V-10).
- Flujo de navegación afinado.
- Rendimiento de listas y dashboard.
- Accesibilidad básica.

**Módulos involucrados**
Todos (capa de presentación e interacción).

**Funcionalidades incluidas**
Ninguna nueva. Solo refinamiento. **Prohibido** agregar features (alcance sección 4).

**Dependencias**
Fases 1–7 funcionalmente completas.

**Riesgos**
- "Mejoras" que introducen funcionalidad fuera de alcance (requiere CR).
- Cambios de UX que rompen flujos ya validados.

**Checklist de implementación**
- [ ] Formularios optimizados a las metas de tiempo.
- [ ] Confirmaciones en acciones destructivas.
- [ ] Mensajes de error claros por validación.
- [ ] Botonera grande/consistente.
- [ ] Rendimiento aceptable en listas.
- [ ] Accesibilidad básica.

**Checklist de pruebas**
- [ ] Crear trabajo < 1 min; ingreso/gasto < 30 s (cronometrado).
- [ ] Cada error de validación muestra mensaje comprensible.
- [ ] Confirmaciones aparecen donde corresponde.
- [ ] No se introdujo ninguna funcionalidad nueva.

**Criterios de aceptación**
Metas de UX de la sección 16 cumplidas; cero funcionalidad fuera de alcance.

**Qué debe quedar terminado antes de avanzar**
La app cumple las metas de tiempo y claridad sin cambios funcionales.

---

# FASE 9 — Testing Integral

**Objetivo**
Verificar el funcionamiento integrado de todos los módulos y la resistencia a casos borde.

**Alcance**
- Flujo completo de un trabajo (alta → estados → entrega → cobro).
- Registro de ingresos y egresos.
- Cobros (incluido pago mixto y anulación).
- Dashboard y Reportes.
- Exportaciones.
- Casos borde y datos inválidos.
- Recuperación ante errores (incluido reinicio de app con datos locales).

**Módulos involucrados**
Todos (extremo a extremo).

**Funcionalidades incluidas**
Solo verificación. No se desarrolla funcionalidad.

**Dependencias**
Fases 1–8.

**Riesgos**
- Casos borde no contemplados afloran tarde (fiado, cancelado, anulaciones, bordes de mes).
- Persistencia local: pérdida/corrupción de datos al cerrar la app (riesgo 13.1).

**Checklist de implementación (escenarios a ejecutar)**
- [ ] Flujo completo de trabajo, incluyendo fiado y posterior cobro.
- [ ] Trabajo cancelado: sin movimientos ni conteo (RN-11).
- [ ] Pago mixto que no cuadra: rechazado (V-6).
- [ ] Cobro y luego anulación el mismo día.
- [ ] Edición de movimiento del día vs bloqueo del día anterior (V-7).
- [ ] Bordes de mes para reportes mensuales (RN-7).
- [ ] Datos inválidos en todos los formularios (V-1..V-10).
- [ ] Reinicio de app: datos locales persisten.
- [ ] Coherencia Dashboard ↔ Reportes ↔ Exportación.

**Checklist de pruebas**
- [ ] Todos los criterios CA-1..CA-16 verificados de punta a punta.
- [ ] Ningún número difiere entre Dashboard, Reportes y Exportación.

**Criterios de aceptación**
Suite CA-1..CA-16 superada en escenarios integrados; casos borde controlados.

**Qué debe quedar terminado antes de avanzar**
Sin defectos bloqueantes; todos los flujos integrados verificados.

---

# FASE 10 — Auditoría Final

**Objetivo**
Revisión integral previa al cierre del producto.

**Alcance**
- Cumplimiento del Frozen Spec (todas las secciones).
- Consistencia funcional entre módulos.
- Experiencia de usuario y navegación.
- Validaciones completas.
- Rendimiento general.
- **Ausencia de funcionalidades fuera de alcance** (sección 4).
- Calidad general.

**Módulos involucrados**
Todos.

**Funcionalidades incluidas**
Ninguna. Solo auditoría y, si corresponde, registro de CR para hallazgos fuera de alcance.

**Dependencias**
Fase 9.

**Riesgos**
- Detección tardía de desvíos del Frozen Spec → reabrir fases.
- "Funcionalidades sorpresa" añadidas en el camino sin CR.

**Checklist de implementación (auditoría)**
- [ ] Trazabilidad: cada CA-x del Frozen Spec verificado.
- [ ] Cada RN-x y V-x respetado.
- [ ] Sin features fuera de la sección 4.
- [ ] Navegación y UX conforme a principios (sección 3).
- [ ] Rendimiento aceptable.
- [ ] Riesgos aceptados (13.x) documentados y vigentes.

**Checklist de pruebas**
- [ ] Recorrido de aceptación final completo.
- [ ] Verificación cruzada de cifras.
- [ ] Confirmación de alcance (nada de más, nada de menos).

**Criterios de aceptación**
Frozen Spec cumplido al 100 %; sin desvíos de alcance; producto apto para release del MVP.

**Qué debe quedar terminado antes de avanzar**
Es la fase final: su cierre = MVP listo para entrega.

---

# RESUMEN EJECUTIVO

## Orden recomendado de ejecución
1 → 2 → 3 → 4 → 5 → 6 → 7 → 8 → 9 → 10.

Las Fases 2 y 3 son relativamente independientes entre sí (Trabajos y Finanzas no se necesitan mutuamente hasta el cobro), pero se recomienda 2 antes de 3 para tener un dominio de prueba realista. La Fase 4 es el punto de integración que **requiere ambas**.

## Dependencias entre fases

| Fase | Depende de | Habilita |
|------|-----------|----------|
| 1 Fundación | — | Todas |
| 2 Trabajos | 1 | 4, 5, 6 |
| 3 Finanzas | 1 | 4, 5, 6 |
| 4 Cobros | 2, 3 | 5, 6 |
| 5 Dashboard | 2, 3, 4 | 6 (consistencia) |
| 6 Reportes | 2, 3, 4 (y 5 recomendada) | 7 |
| 7 Exportaciones | 6 | — |
| 8 Pulido UX | 1–7 | 9 |
| 9 Testing Integral | 1–8 | 10 |
| 10 Auditoría Final | 9 | Release MVP |

## Riesgos globales
- **R1 — Integridad del dinero (alto).** Doble conteo cobro/ingreso (RN-3) y criterio de caja/ganancia (RN-1/RN-2) deben ser idénticos en Cobros, Dashboard y Reportes. Mitigación: definiciones únicas verificadas en Fases 4–6 y cruzadas en Fase 9.
- **R2 — Consistencia entre vistas (medio).** Dashboard, Reportes y Exportación deben mostrar las mismas cifras. Mitigación: verificación cruzada (Fases 5, 6, 7, 9).
- **R3 — Persistencia local sin respaldo (medio, aceptado 13.1).** Pérdida de datos por pérdida/daño del dispositivo. Mitigación: fuera de alcance del MVP; documentado como riesgo aceptado.
- **R4 — Desvío de alcance (medio).** "Mejoras" añadidas sin CR, sobre todo en Fase 8. Mitigación: control de alcance en Fases 8 y 10.
- **R5 — Estados inconsistentes (medio).** Transiciones fuera de 7.2 o cobro/anulación mal revertidos. Mitigación: V-9, pruebas de Fases 2 y 4.
- **R6 — Calidad de datos de búsqueda (bajo, aceptado 13.2).** Cliente como texto libre. Aceptado en el MVP.

## Estimación relativa de complejidad

| Fase | Complejidad | Comentario |
|------|-------------|------------|
| 1 Fundación | **Baja** | Estructura y navegación. |
| 2 Gestión de Trabajos | **Alta** | Núcleo: CRUD, máquina de estados, búsqueda, validaciones. |
| 3 Finanzas | **Media** | Dos registros, validaciones, pago mixto, caja. |
| 4 Cobros | **Alta** | Integración crítica de dinero (anti-doble-conteo, anulación). |
| 5 Dashboard | **Media** | Lectura/agregación; riesgo de consistencia. |
| 6 Reportes | **Media** | Definiciones de período y conteos. |
| 7 Exportaciones | **Media** | Fidelidad de datos y formatos PDF/Excel. |
| 8 Pulido de UX | **Media** | Metas de tiempo y claridad sin tocar alcance. |
| 9 Testing Integral | **Alta** | Cobertura extremo a extremo y casos borde. |
| 10 Auditoría Final | **Media** | Verificación de cumplimiento y alcance. |

---

*Documento de planificación. No contiene código ni decisiones técnicas. Toda modificación de alcance respecto del Frozen Spec requiere Change Request (sección 14 del Frozen Spec).*
