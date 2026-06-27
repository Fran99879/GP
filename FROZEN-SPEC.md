# 🧊 FROZEN SPECIFICATION — TallerApp

**Versión:** 1.0-FROZEN (MVP)
**Plataforma:** Android
**Estado:** 🟢 SPEC FROZEN
**Reemplaza a:** `SPEC.md` (borrador auditado)
**Regla de cambios:** A partir de este documento, la especificación está **CERRADA**. Ninguna funcionalidad nueva, cambio de alcance o reinterpretación es válida sin un **Change Request (CR)** aprobado y registrado en la sección 14.

> Este documento es la **única fuente de verdad** durante todo el desarrollo. Si un comportamiento no está aquí, **no existe** en el MVP. Si algo parece faltar, no se improvisa: se levanta un CR.

---

## 1. Objetivo

Aplicación Android para la gestión diaria de un taller mecánico, optimizada para **mínimo tiempo de uso** (pocos toques, sin formularios extensos). Permite:

1. Registrar trabajos (vehículos que ingresan al taller).
2. Controlar ingresos y egresos.
3. Conocer la ganancia del día y del mes.

No es un ERP ni un sistema de facturación/administración.

---

## 2. Alcance

### 2.1 Dentro de alcance (MVP)
Gestión de trabajos, estados de trabajo y de cobro, finanzas (ingresos/egresos), cobros, dashboard, reportes diarios y mensuales, exportación PDF/Excel, y búsqueda por patente y cliente.

### 2.2 Fuera de alcance (MVP)
Todo lo no listado en 2.1. Persistencia **únicamente local** en el dispositivo (sin nube, sin sincronización, sin backup automático).

### 2.3 Supuestos de plataforma y datos congelados
| Tema | Decisión congelada |
|------|--------------------|
| Plataforma | Android, monodispositivo. |
| Usuario | **Un único usuario** sin login (sin multiusuario, sin roles). |
| Persistencia | **Local en el dispositivo.** Si el dispositivo se pierde, los datos se pierden (riesgo aceptado para el MVP — ver 13.1). |
| Conectividad | Funciona 100 % offline. |
| Moneda | **Una sola moneda** (peso local del taller). Sin multimoneda. Montos con 2 decimales. |
| Fecha/hora | Zona horaria del dispositivo. Fecha de ingreso = fecha del sistema al crear. |
| Identidad de Cliente | **Texto libre** por trabajo. No existe entidad "Cliente" reutilizable en el MVP (ver 13.2). |

---

## 3. Funcionalidades incluidas

1. Dashboard con indicadores del día/mes y accesos rápidos.
2. Alta, consulta y edición de trabajos.
3. Gestión de estado de reparación y estado de cobro de cada trabajo.
4. Registro de ingresos manuales (no ligados a trabajos).
5. Registro de egresos.
6. Cobro de un trabajo (genera ingreso automático).
7. Reportes diarios y mensuales.
8. Exportación de reportes a PDF y Excel.
9. Búsqueda de trabajos por patente y por cliente.

---

## 4. Funcionalidades excluidas (prohibidas en MVP)

Control de stock · Facturación electrónica · Multiusuario · Roles y permisos · Agenda · Recordatorios automáticos · Notificaciones · Integraciones con servicios externos · Fotografías/adjuntos · Modo oscuro · Copias de seguridad · Sincronización en la nube · Rentabilidad por trabajo · Historial por cliente · Cuentas corrientes / deudas con saldo.

> Nota: el estado de cobro (sección 8) indica **si un trabajo fue cobrado o no**, pero el MVP **no** lleva cuenta corriente, ni saldos parciales acumulados por cliente, ni seguimiento de deuda más allá del flag por trabajo.

---

## 5. Modelo de datos (entidades y campos)

### 5.1 Entidad: Trabajo

| Campo | Tipo | Obligatorio | Editable tras alta | Reglas |
|-------|------|-------------|--------------------|--------|
| ID | interno | Sí (auto) | No | Identificador único interno. |
| Cliente | texto | **Sí** | Sí | Texto libre. No vacío. |
| Teléfono | texto | No | Sí | Solo dígitos y símbolos `+ - ( ) espacio`. |
| Patente | texto | No | Sí | Se normaliza a MAYÚSCULAS, sin espacios. |
| Marca | texto | **Sí** | Sí | No vacío. |
| Modelo | texto | **Sí** | Sí | No vacío. |
| Servicio realizado | selección | **Sí** | Sí | Lista cerrada (ver 5.4). |
| Fecha de ingreso | fecha | Sí (auto) | **No** | Fecha del sistema al crear. |
| Estado de reparación | enum | Sí | Sí (toque) | Ver sección 7. Valor inicial: `Pendiente`. |
| Estado de cobro | enum | Sí (auto) | Indirecto | Ver sección 8. Valor inicial: `Pendiente de cobro`. |
| Problema informado | texto | No | Sí | Libre. |
| Diagnóstico | texto | No | Sí | Libre. |
| Precio del trabajo | monto | **Sí** | Sí (mientras no esté `Cobrado`) | ≥ 0. (Antes "Costo del trabajo" — C8.) |
| Cobro asociado | ref | No | No | Se completa al cobrar (ver 9). |

### 5.2 Entidad: Ingreso

| Campo | Tipo | Obligatorio | Reglas |
|-------|------|-------------|--------|
| ID | interno | Sí (auto) | — |
| Monto | monto | Sí | > 0. |
| Concepto | texto | Sí | No vacío. |
| Método de pago | enum | Sí | Ver 5.5. |
| Fecha | fecha | Sí | Por defecto fecha del sistema; editable. |
| Origen | enum | Sí (auto) | `Manual` o `Cobro de trabajo`. |
| Trabajo asociado | ref | Solo si Origen=`Cobro` | Vincula al trabajo cobrado. |

### 5.3 Entidad: Egreso

| Campo | Tipo | Obligatorio | Reglas |
|-------|------|-------------|--------|
| ID | interno | Sí (auto) | — |
| Monto | monto | Sí | > 0. |
| Categoría | enum | Sí | Ver 5.6. |
| Concepto | texto | Sí | No vacío. |
| Fecha | fecha | Sí | Por defecto fecha del sistema; editable. |

### 5.4 Lista cerrada: Servicio realizado
`Cambio de aceite` · `Frenos` · `Embrague` · `Distribución` · `Electricidad` · `Otros`.

> "Otros" permite registrar un trabajo cuyo servicio no está en la lista. El reporte "Servicios más realizados" agrupa por estos valores.

### 5.5 Lista cerrada: Método de pago
`Efectivo` · `Transferencia` · `Tarjeta` · `Mercado Pago` · `Pago mixto`.

- **Pago mixto** (C7): obliga a indicar **dos o más** importes por método (Efectivo / Transferencia / Tarjeta / Mercado Pago). La **suma de los importes debe igualar exactamente** el monto total del ingreso/cobro. No se permite confirmar si no cuadra.

### 5.6 Lista cerrada: Categoría de egreso
`Repuestos` · `Herramientas` · `Combustible` · `Servicios e impuestos` · `Otros`.

> "Servicios e impuestos" (C11) = luz, agua, alquiler, etc. No confundir con "Servicio realizado" del trabajo.

---

## 6. Módulos

| Módulo | Responsabilidad | Pantallas |
|--------|-----------------|-----------|
| Dashboard | Mostrar indicadores y accesos rápidos. | 1 |
| Trabajos | Lista, alta, detalle/edición, cambio de estado, cobro. | Lista + Detalle + Alta |
| Finanzas | Registro de ingresos y egresos; edición/anulación del día. | Ingresos + Egresos |
| Reportes | Cálculo y visualización de métricas; exportación. | 1 |
| Búsqueda | Filtro de trabajos por patente/cliente. | Integrada en lista |

---

## 7. Estados de reparación y máquina de transiciones

### 7.1 Estados
`Pendiente` → `En reparación` → `Esperando repuestos` → `Terminado` → `Entregado` · y `Cancelado` (C12).

### 7.2 Transiciones permitidas (C4)
El cambio se hace **con un solo toque**, sin formulario.

| Desde | Transiciones permitidas |
|-------|-------------------------|
| Pendiente | En reparación, Esperando repuestos, Cancelado |
| En reparación | Esperando repuestos, Terminado, Cancelado |
| Esperando repuestos | En reparación, Terminado, Cancelado |
| Terminado | Entregado, En reparación |
| Entregado | *(sin transiciones de reparación; ver 7.3)* |
| Cancelado | *(terminal)* |

### 7.3 Reglas
- **Solo** un trabajo en estado `Terminado` puede pasar a `Entregado`.
- Al pasar a `Entregado` se dispara el flujo de cobro (sección 9).
- Un trabajo `Entregado` **no cobrado** permanece visible como pendiente de cobro hasta que se registre el cobro.
- `Cancelado` es terminal y **no** genera cobro ni ingreso. Sale del conteo de trabajos en taller.
- **Revertir desde `Entregado`:** no permitido en el MVP una vez registrado el cobro. Si el cobro fue un error, se **anula el movimiento de cobro** (sección 9.4), lo que devuelve el trabajo a `Estado de cobro = Pendiente de cobro` manteniendo `Estado de reparación = Entregado`.

---

## 8. Estado de cobro (independiente del estado de reparación) — C3

| Valor | Significado |
|-------|-------------|
| `Pendiente de cobro` | Aún no se registró el cobro del trabajo. Valor inicial. |
| `Cobrado` | Se registró el cobro; existe un Ingreso asociado. |

- Es **independiente** del estado de reparación: un trabajo puede estar `Entregado` + `Pendiente de cobro` (fiado).
- Pasa a `Cobrado` **únicamente** al confirmar el flujo de cobro (sección 9).
- Mientras está `Pendiente de cobro`, el campo `Precio del trabajo` es editable; una vez `Cobrado`, queda bloqueado.

---

## 9. Flujos

### 9.1 Flujo — Nuevo trabajo (meta: < 1 min)
1. Dashboard → "Nuevo Trabajo".
2. Completar obligatorios: Cliente, Marca, Modelo, Servicio realizado, Precio del trabajo. (Teléfono, Patente, Problema, Diagnóstico opcionales.)
3. Guardar → estado de reparación `Pendiente`, estado de cobro `Pendiente de cobro`, fecha de ingreso = hoy.

### 9.2 Flujo — Cambio de estado de reparación
1. Lista de Trabajos → tocar tarjeta → Detalle.
2. Tocar el estado destino permitido (7.2). Cambio inmediato, sin formulario.

### 9.3 Flujo — Cobro de un trabajo (C2)
1. El trabajo debe estar en `Entregado`.
2. Acción "Registrar cobro" → elegir Método de pago (5.5). Si `Pago mixto`, repartir importes (suma = Precio del trabajo).
3. Confirmar →
   - Se crea **un Ingreso** con `Origen = Cobro de trabajo`, `Trabajo asociado` = este trabajo, Monto = Precio del trabajo, Fecha = fecha del cobro.
   - El trabajo pasa a `Estado de cobro = Cobrado` y se bloquea su Precio.
4. **Regla anti-doble-conteo:** el cobro de un trabajo **solo** puede ingresar dinero por esta vía. El registro manual de Ingresos (9.5) **no** debe usarse para cobros de trabajos.

### 9.4 Flujo — Anulación de cobro
1. Desde el detalle del trabajo `Cobrado`, acción "Anular cobro" (permitida solo el mismo día del cobro — ver 9.6).
2. Confirmar → se elimina el Ingreso asociado y el trabajo vuelve a `Pendiente de cobro`.

### 9.5 Flujo — Nuevo ingreso manual (meta: < 30 s)
1. Dashboard → "Nuevo Ingreso".
2. Completar Monto, Concepto, Método de pago, Fecha (default hoy). `Origen = Manual`.
3. Guardar. **Uso permitido:** conceptos **no** ligados a un trabajo (p. ej. venta suelta). **No** para cobrar trabajos.

### 9.6 Flujo — Nuevo egreso (meta: < 30 s)
1. Dashboard → "Nuevo Gasto".
2. Completar Monto, Categoría, Concepto, Fecha (default hoy).
3. Guardar.

### 9.7 Edición/anulación de movimientos (C9)
- Ingresos y Egresos pueden **editarse o anularse únicamente dentro del día en que fueron registrados** (`Fecha de registro = hoy`). Pasado ese día, quedan inmutables en el MVP.
- La anulación de un Ingreso con `Origen = Cobro` equivale a "Anular cobro" (9.4).

---

## 10. Reglas de negocio

| ID | Regla |
|----|-------|
| RN-1 | **Ganancia = Ingresos − Egresos** (C1), criterio de **caja**: cada movimiento computa en la fecha del movimiento. |
| RN-2 | Un Ingreso por cobro se imputa a la **fecha del cobro**; un Egreso a su **fecha de registro**. No existe "ganancia por trabajo" en el MVP. |
| RN-3 | El cobro de un trabajo es la **única** fuente de ingreso ligada a ese trabajo (C2). |
| RN-4 | "Caja del día" = **suma de todos los Ingresos cuya fecha = hoy** (todos los métodos) (C10). |
| RN-5 | Estado de reparación y estado de cobro son independientes (C3). |
| RN-6 | `Pago mixto`: la suma de importes por método debe igualar el total exacto (C7). |
| RN-7 | "del mes" = **mes calendario** (día 1 al último día del mes en curso) (C14). |
| RN-8 | "Vehículos en el taller" = trabajos cuyo estado de reparación ∈ {Pendiente, En reparación, Esperando repuestos, Terminado} (es decir, no `Entregado` ni `Cancelado`). |
| RN-9 | "Trabajos realizados" (reporte) = trabajos que alcanzaron `Entregado` en el período. "Vehículos atendidos" = cantidad de trabajos con fecha de ingreso dentro del período (C14). |
| RN-10 | Cliente es texto libre: dos grafías distintas son clientes distintos para la búsqueda (C13). |
| RN-11 | Un trabajo `Cancelado` no genera movimientos y no computa en métricas de dinero ni en "vehículos en taller". |

---

## 11. Validaciones

| ID | Validación |
|----|-----------|
| V-1 | Trabajo: Cliente, Marca, Modelo, Servicio realizado y Precio (≥ 0) obligatorios; no se guarda sin ellos. |
| V-2 | Ingreso: Monto > 0, Concepto no vacío, Método obligatorio. |
| V-3 | Egreso: Monto > 0, Categoría obligatoria, Concepto no vacío. |
| V-4 | Patente: si se ingresa, se normaliza (MAYÚSCULAS, sin espacios). Puede quedar vacía. |
| V-5 | Cobro: solo disponible si estado de reparación = `Entregado`. |
| V-6 | Pago mixto: no confirma si la suma de importes ≠ total. |
| V-7 | Edición de movimientos: solo si fecha de registro = hoy (V de 9.7). |
| V-8 | Precio del trabajo: bloqueado para edición cuando estado de cobro = `Cobrado`. |
| V-9 | Transición de estado: solo se ofrecen las transiciones permitidas de 7.2; el resto no se muestra. |
| V-10 | Montos: 2 decimales, no negativos (Ingreso/Egreso > 0; Precio ≥ 0). |

---

## 12. Reportes

### 12.1 Dashboard (indicadores en vivo)
- Caja del día (RN-4).
- Ganancia del mes (RN-1, mes calendario).
- Vehículos actualmente en el taller (RN-8).
- Vehículos esperando repuestos (estado = `Esperando repuestos`).
- Trabajos pendientes (estado = `Pendiente`).
- Trabajos entregados durante el día (pasaron a `Entregado` hoy).

Accesos rápidos: Nuevo Trabajo · Nuevo Ingreso · Nuevo Gasto.

### 12.2 Reporte diario
Caja del día · Ingresos del día · Gastos del día · Ganancia del día (= Ingresos − Egresos del día).

### 12.3 Reporte mensual (mes calendario)
Ingresos del mes · Gastos del mes · Ganancia del mes · Trabajos realizados (RN-9) · Vehículos atendidos (RN-9) · Servicios más realizados (conteo por valor de "Servicio realizado", solo trabajos no cancelados).

### 12.4 Exportación
Cada reporte (diario y mensual) exportable a **PDF** y **Excel**. La exportación refleja exactamente los valores mostrados en pantalla para el período seleccionado.

### 12.5 Períodos sin datos
Un reporte sin movimientos muestra todos los valores en **cero** (no error, no pantalla vacía).

---

## 13. Restricciones y riesgos aceptados

### 13.1 Persistencia local (C15)
Los datos viven **solo en el dispositivo**. No hay respaldo en el MVP. Pérdida/daño del dispositivo = pérdida de datos. **Riesgo aceptado** para el MVP; mitigación (backup/nube) queda fuera de alcance.

### 13.2 Sin entidad Cliente (C13)
Cliente es texto por trabajo. No hay historial por cliente ni autocompletado. Limita la búsqueda por cliente a coincidencia de texto. **Aceptado** para el MVP.

### 13.3 Sin rentabilidad por trabajo
Al no vincularse egresos a trabajos, no se calcula ganancia por trabajo/servicio. **Aceptado** para el MVP.

### 13.4 Mono-usuario / mono-dispositivo
Sin login, sin sincronización entre dispositivos. **Aceptado.**

---

## 14. Gestión de cambios (post-freeze)

Una vez congelada, toda modificación requiere un **Change Request** con: ID, descripción, módulos afectados, impacto, y aprobación. Registro:

| CR ID | Fecha | Descripción | Estado |
|-------|-------|-------------|--------|
| — | — | *(sin cambios)* | — |

---

## 15. Criterios de aceptación (MVP)

- [ ] CA-1 — Dashboard muestra los 6 indicadores (12.1) y los 3 accesos rápidos.
- [ ] CA-2 — Un trabajo se crea con los 5 campos obligatorios en < 1 min y queda `Pendiente` / `Pendiente de cobro`.
- [ ] CA-3 — El estado de reparación cambia con un solo toque y solo ofrece transiciones válidas (7.2).
- [ ] CA-4 — Un trabajo puede quedar `Entregado` + `Pendiente de cobro` (fiado) y se ve como pendiente de cobro.
- [ ] CA-5 — Registrar cobro crea un único Ingreso asociado, marca `Cobrado` y bloquea el Precio (9.3, V-8).
- [ ] CA-6 — Pago mixto exige que la suma de importes iguale el total (V-6).
- [ ] CA-7 — Anular cobro el mismo día elimina el Ingreso y revierte a `Pendiente de cobro` (9.4).
- [ ] CA-8 — Ingreso manual y Egreso se registran en < 30 s con sus validaciones (V-2, V-3).
- [ ] CA-9 — Editar/anular movimientos solo permitido el día de registro (9.7, V-7).
- [ ] CA-10 — Reporte diario y mensual calculan Ganancia = Ingresos − Egresos por criterio de caja (RN-1, RN-2).
- [ ] CA-11 — "Servicios más realizados" se genera desde el campo "Servicio realizado" (12.3).
- [ ] CA-12 — Exportación a PDF y Excel coincide con lo mostrado (12.4).
- [ ] CA-13 — Búsqueda por patente y por cliente; tarjetas sin patente se identifican por Marca+Modelo+Cliente (C5).
- [ ] CA-14 — `Cancelado` no genera movimientos ni computa en métricas (RN-11).
- [ ] CA-15 — Reporte de período sin datos muestra ceros (12.5).
- [ ] CA-16 — La app opera 100 % offline con datos locales (13.1).

---

## 16. Definición de MVP

El MVP está **completo** cuando el mecánico puede, desde una sola app offline:

1. Saber **qué vehículos** tiene en el taller y **en qué estado** está cada trabajo.
2. Registrar **cobros**, **ingresos** y **egresos** con las reglas de las secciones 9 y 10.
3. Distinguir un trabajo **entregado y cobrado** de uno **entregado y pendiente de cobro**.
4. Conocer **caja del día**, **ingresos**, **gastos** y **ganancia** del día y del mes.
5. Ver **servicios más realizados** y **exportar** los reportes.

Todo cumpliendo las metas de UX: trabajo < 1 min, ingreso/gasto < 30 s, sin conocimientos técnicos.

---

## 17. Verificación de congelamiento

| Criterio de freeze | Resultado |
|--------------------|-----------|
| Sin contradicciones | ✅ Resueltas (cobro vs ingreso, patente, "Servicios"). |
| Sin ambigüedades | ✅ Ganancia, caja, período, conteos y métodos definidos. |
| Sin decisiones abiertas | ✅ C1–C15 resueltas. |
| Todos los módulos definidos | ✅ Sección 6. |
| Todos los estados definidos | ✅ Secciones 7 y 8 con máquina de transiciones. |
| Todos los campos definidos | ✅ Sección 5 con obligatoriedad/validación. |
| Todos los flujos definidos | ✅ Sección 9. |
| Sin lugar a interpretación | ✅ Reglas RN-1..RN-11 y V-1..V-10. |

---

# 🟢 SPEC FROZEN

La especificación queda **CERRADA** en versión **1.0-FROZEN**. No existen decisiones abiertas que impidan el desarrollo. Cualquier cambio posterior debe pasar por el proceso de Change Request (sección 14).
