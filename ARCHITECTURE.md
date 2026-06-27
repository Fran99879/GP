# 🏛️ TECHNICAL ARCHITECTURE DESIGN — TallerApp

**Proyecto:** TallerApp — Gestión Simple para Talleres
**Versión:** 1.0 (MVP)
**Fuente de verdad funcional:** [FROZEN-SPEC.md](FROZEN-SPEC.md) (🟢 SPEC FROZEN)
**Plan de ejecución:** [MASTER-ROADMAP.md](MASTER-ROADMAP.md)
**Naturaleza:** Diseño de arquitectura técnica. **No contiene código.** Las elecciones de tecnología son **propuestas justificadas**, sujetas a aprobación antes de la Fase 1.

> Principio rector: **simplicidad y mantenibilidad por encima de la sobreingeniería.** Toda decisión se mide contra el alcance del MVP. Si un patrón no resuelve un problema real del Frozen Spec, no entra.

---

## 0. Resumen de decisiones (Decision Record)

| ID | Decisión | Elección propuesta | Por qué |
|----|----------|--------------------|---------|
| AD-1 | Plataforma | Android nativo (Kotlin) | Frozen Spec fija Android monodispositivo; nativo da mejor UX/rendimiento con un solo target. |
| AD-2 | UI | Jetpack Compose | Menos código declarativo, formularios y listas rápidas, alineado con metas de UX. |
| AD-3 | Arquitectura | **Clean Architecture pragmática + MVVM** en 3 capas | Separa dominio de la UI y de la persistencia; testeable; sin sobrecapas. |
| AD-4 | Persistencia | Base de datos local relacional (Room/SQLite) | Datos estructurados, relaciones, consultas de reportes; local por requisito 13.1. |
| AD-5 | Estado UI | Patrón unidireccional (estado inmutable + eventos) por pantalla | Predecible, evita estados inconsistentes (riesgo R5). |
| AD-6 | Navegación | Un único grafo de navegación con destinos por módulo | Centraliza el flujo de la sección 9 del Frozen Spec. |
| AD-7 | Dinero | Tipo entero en **centavos** + una sola moneda | Evita errores de redondeo (RN-1, V-10); crítico para R1. |
| AD-8 | Exportación | Generadores PDF/Excel que consumen el **mismo modelo de reporte** que la UI | Garantiza CA-12 (export = pantalla). |

> AD-1/AD-2/AD-4 son recomendaciones; pueden sustituirse por equivalentes (ej. multiplataforma) sin alterar las capas AD-3.

---

## 1. Arquitectura general

### 1.1 Arquitectura propuesta
**Clean Architecture en 3 capas + MVVM en la capa de presentación.**

```
┌──────────────────────────────────────────────┐
│  PRESENTACIÓN (UI)                             │
│  Pantallas + ViewModels + Estado de UI         │
│  (Dashboard, Trabajos, Finanzas, Reportes…)    │
└───────────────▲───────────────┬────────────────┘
                │ estado/eventos │ llama casos de uso
┌───────────────┴───────────────▼────────────────┐
│  DOMINIO (lógica de negocio, pura)             │
│  Entidades + Reglas (RN-x) + Casos de uso       │
│  Interfaces de repositorio (contratos)          │
└───────────────▲───────────────┬────────────────┘
                │ implementa     │ usa contratos
┌───────────────┴───────────────▼────────────────┐
│  DATOS                                          │
│  Repositorios (implementación) + DAOs + DB local│
│  Mappers entidad↔persistencia                   │
└─────────────────────────────────────────────────┘
```

### 1.2 Justificación
- **Por qué Clean + MVVM:** el núcleo de riesgo del proyecto es la **integridad del dinero y de los estados** (R1, R5). Aislar las reglas de negocio (RN-1…RN-11) en una capa de dominio **independiente de la UI y de la base de datos** permite probarlas de forma unitaria y garantizar que Dashboard, Reportes y Exportación calculen lo mismo (consistencia exigida por CA-10/CA-12).
- **Por qué pragmática (no purista):** el MVP es pequeño. Se evita multiplicar capas (sin DTOs redundantes, sin abstracciones especulativas). Se usan solo: Entidades, Casos de uso, Repositorios e interfaces.
- **Por qué la regla de dependencia:** las capas externas dependen de las internas, nunca al revés. El dominio no conoce Android ni la base de datos → portable y testeable.

### 1.3 Flujo de datos (unidireccional)
1. La **UI** emite un **evento** (ej. "guardar trabajo").
2. El **ViewModel** invoca un **caso de uso** del dominio.
3. El **caso de uso** aplica reglas (validaciones, RN-x) y usa una **interfaz de repositorio**.
4. El **repositorio** (capa de datos) lee/escribe en la **base local** y devuelve entidades.
5. El resultado vuelve como **nuevo estado inmutable**; la UI se redibuja.

> No hay escritura directa UI→DB. Todo dinero/estado pasa por casos de uso. Esto cierra la puerta al doble conteo (RN-3) y a transiciones inválidas (V-9).

---

## 2. Estructura del proyecto (carpetas y responsabilidades)

> Estructura conceptual **por capas con sub-organización por módulo** (feature-first dentro de presentación, layer-first en el núcleo). No es código; es el mapa de responsabilidades.

```
/app
├── /core                      → Transversal, sin lógica de negocio de un módulo
│   ├── /ui                    → Tema, componentes compartidos, formato de moneda/fecha
│   ├── /navigation            → Grafo de navegación y destinos
│   ├── /error                 → Tipos de error y resultado, mensajes
│   └── /util                  → Utilidades puras (fechas, dinero en centavos, formato)
│
├── /domain                    → Lógica de negocio PURA (sin Android, sin DB)
│   ├── /model                 → Entidades: Trabajo, Ingreso, Egreso (+ enums/estados)
│   ├── /rules                 → Reglas RN-x y máquina de estados (sección 7)
│   ├── /validation            → Validaciones V-1..V-10
│   ├── /repository            → Interfaces (contratos) de repositorios
│   └── /usecase               → Casos de uso por operación de negocio
│
├── /data                      → Implementación de persistencia
│   ├── /local                 → Base de datos, DAOs, esquema
│   ├── /repository            → Implementación de las interfaces de /domain
│   └── /mapper                → Conversión entidad de dominio ↔ registro de DB
│
└── /features                  → Presentación (UI + ViewModel) por módulo
    ├── /dashboard
    ├── /trabajos              → lista, alta, detalle, cambio de estado
    ├── /finanzas              → ingresos, egresos
    ├── /cobros                → flujo de cobro/anulación (integra trabajos+finanzas)
    ├── /reportes
    └── /exportacion           → generación PDF/Excel
```

| Carpeta | Responsabilidad |
|---------|-----------------|
| `/core` | Lo que comparten todos los módulos sin pertenecer a ninguno: tema, navegación, manejo de errores, utilidades (dinero, fechas). |
| `/domain` | Corazón del sistema. Entidades, reglas, validaciones, casos de uso y contratos. **No depende de nada externo.** |
| `/data` | Cómo se guardan y leen los datos. Implementa los contratos del dominio. Único lugar que conoce la base local. |
| `/features` | Lo que el usuario ve y toca. Cada módulo tiene sus pantallas y su ViewModel; no contiene reglas de negocio (delega en casos de uso). |

---

## 3. Organización por módulos

| Módulo | Capa principal | Responsabilidad | Depende de |
|--------|----------------|-----------------|------------|
| **Dashboard** | features + usecases de lectura | Mostrar indicadores 12.1 (RN-4, RN-7, RN-8). | Trabajos, Finanzas, Cobros (lectura). |
| **Trabajos** | features + domain | CRUD, estados (sección 7), búsqueda (RN-10). | core, domain. |
| **Finanzas** | features + domain | Ingresos/Egresos, caja (RN-4), edición del día (V-7). | core, domain. |
| **Cobros** | features + domain (caso de uso integrador) | Cobro/anulación (9.3/9.4), anti-doble-conteo (RN-3). | Trabajos + Finanzas. |
| **Reportes** | features + usecases de agregación | Métricas 12.2/12.3 (RN-1, RN-2, RN-9). | Trabajos, Finanzas. |
| **Exportaciones** | features/exportacion | PDF/Excel del modelo de reporte (CA-12). | Reportes. |
| **Componentes compartidos** | core/ui | Botonera grande, campos, tarjetas, diálogos de confirmación. | — |
| **Utilidades** | core/util | Dinero (centavos), fechas/períodos (mes calendario), formato. | — |

> **Cobros no es una capa propia de datos:** es un **caso de uso de dominio** que orquesta Trabajo + Ingreso en una sola operación transaccional. Esto evita estados intermedios inconsistentes (trabajo `Cobrado` sin ingreso, o ingreso huérfano).

---

## 4. Modelos de datos (entidades)

> Las entidades reflejan exactamente la sección 5 del Frozen Spec. Aquí se documenta responsabilidad, relaciones y restricciones — sin código.

### 4.1 Trabajo
- **Responsabilidad:** representar el ingreso de un vehículo al taller y su ciclo de reparación/cobro.
- **Relaciones:** 1 Trabajo → 0..1 Ingreso (el ingreso generado al cobrar). No tiene relación con Egresos (decisión 13.3).
- **Campos:** ID, Cliente, Teléfono?, Patente?, Marca, Modelo, Servicio realizado (enum 5.4), Fecha de ingreso (auto), Estado de reparación (enum 7.1), Estado de cobro (enum 8), Problema?, Diagnóstico?, Precio del trabajo, Ref. de cobro?.
- **Restricciones:** Cliente/Marca/Modelo/Servicio/Precio obligatorios (V-1); Precio ≥ 0 y bloqueado si `Cobrado` (V-8); Fecha de ingreso inmutable; Patente normalizada (V-4); transiciones de estado solo según 7.2 (V-9).

### 4.2 Ingreso
- **Responsabilidad:** registrar dinero que entra (manual o por cobro).
- **Relaciones:** 0..1 Ingreso ↔ 1 Trabajo, **solo** cuando `Origen = Cobro de trabajo`.
- **Campos:** ID, Monto, Concepto, Método de pago (enum 5.5; detalle de reparto si `Pago mixto`), Fecha, Origen (enum), Trabajo asociado?.
- **Restricciones:** Monto > 0 (V-2, V-10); Origen `Cobro` ⇒ Trabajo asociado obligatorio; reparto de pago mixto suma = total (V-6); editable/anulable solo el día de registro (V-7).

### 4.3 Egreso
- **Responsabilidad:** registrar dinero que sale.
- **Relaciones:** ninguna (no se vincula a trabajos — 13.3).
- **Campos:** ID, Monto, Categoría (enum 5.6), Concepto, Fecha.
- **Restricciones:** Monto > 0, Categoría y Concepto obligatorios (V-3); editable/anulable solo el día (V-7).

### 4.4 Objetos de valor / enums (no persistidos como tablas)
- **Servicio realizado**, **Método de pago**, **Categoría de egreso**, **Estado de reparación**, **Estado de cobro**, **Origen de ingreso**: listas cerradas. Se modelan como tipos enumerados del dominio para impedir valores fuera de catálogo.
- **Dinero:** valor en **centavos (entero)** + moneda única (AD-7).
- **Reporte (diario/mensual):** **modelo de solo lectura** calculado por casos de uso; no se persiste. Es la fuente única para UI y Exportación.

### 4.5 Diagrama de relaciones
```
Trabajo (1) ──genera──▶ (0..1) Ingreso[Origen=Cobro]
Ingreso[Origen=Manual]        (sin trabajo)
Egreso                        (independiente)
Reporte = f(Trabajos, Ingresos, Egresos, período)   [no persistido]
```

---

## 5. Navegación

### 5.1 Pantalla inicial
**Dashboard** (sección 12.1). Punto de entrada y centro de los accesos rápidos.

### 5.2 Grafo de navegación
```
Dashboard
 ├─▶ Nuevo Trabajo (alta)            ──▶ vuelve a Dashboard/Lista
 ├─▶ Nuevo Ingreso (alta)            ──▶ vuelve a Dashboard
 ├─▶ Nuevo Gasto (alta)              ──▶ vuelve a Dashboard
 ├─▶ Trabajos (Lista + Búsqueda)
 │     └─▶ Detalle de Trabajo
 │           ├─ cambiar estado (in-place, sin pantalla)
 │           └─▶ Registrar/Anular cobro (solo si Entregado)
 ├─▶ Finanzas (Ingresos | Egresos)
 │     └─ editar/anular movimiento del día (in-place)
 └─▶ Reportes (Diario | Mensual)
       └─▶ Exportar (PDF | Excel)
```

### 5.3 Flujo de usuario (principales)
- **Alta de trabajo (< 1 min):** Dashboard → Nuevo Trabajo → guardar → Lista. (9.1)
- **Ciclo de reparación:** Lista → Detalle → toques de estado. (9.2)
- **Cobro:** Detalle (Entregado) → Registrar cobro → método/mixto → confirma. (9.3)
- **Ingreso/Gasto (< 30 s):** Dashboard → acceso rápido → guardar. (9.5/9.6)
- **Reporte + export:** Dashboard → Reportes → seleccionar período → Exportar. (12)

> Reglas de navegación: el cambio de estado y la edición de movimientos del día ocurren **in-place** (sin abrir formularios), conforme a los principios de UX (sección 3). Acciones destructivas (eliminar trabajo, anular cobro/movimiento) interponen **diálogo de confirmación** (Fase 8).

---

## 6. Persistencia

### 6.1 Estrategia de almacenamiento
**Base de datos local relacional** en el dispositivo (Room sobre SQLite, AD-4). Justificación: los datos son estructurados y relacionales (Trabajo↔Ingreso), y los reportes requieren consultas y agregaciones por fecha/estado/categoría que una base relacional resuelve eficientemente.

### 6.2 Organización de datos
- Tablas: `trabajo`, `ingreso`, `egreso`. Enums persistidos como valores controlados.
- Dinero almacenado en **centavos enteros** (AD-7).
- Fechas almacenadas en formato ordenable; el dominio deriva "día" y "mes calendario" (RN-7).
- Índices recomendados (rendimiento): por fecha (reportes/caja), por estado de reparación (dashboard/lista), por patente y por cliente (búsqueda).

### 6.3 Integridad transaccional
- **Cobro y anulación son transacciones atómicas:** crear/borrar el Ingreso y actualizar el estado de cobro del Trabajo ocurren juntos o no ocurren. Esto protege contra R1/R5.

### 6.4 Sincronización
**No aplica en el MVP.** App offline, monodispositivo (13.4). No hay servidor ni resolución de conflictos.

### 6.5 Backups
**No aplica en el MVP** (13.1, fuera de alcance). Riesgo de pérdida de datos por pérdida/daño del dispositivo **aceptado y documentado**. La arquitectura por capas deja la puerta abierta a añadir export/import o nube **vía Change Request** sin tocar el dominio (la persistencia está aislada en `/data`).

---

## 7. Manejo de estados

### 7.1 Estado de la UI
**Patrón unidireccional con estado inmutable por pantalla** (AD-5):
- Cada pantalla tiene **un objeto de estado** (cargando / contenido / vacío / error).
- La UI solo **lee** el estado y **emite eventos**; nunca muta datos directamente.
- El ViewModel transforma eventos en llamadas a casos de uso y produce un **nuevo estado**.

### 7.2 Estado de negocio (dominio)
- **Estado de reparación** y **estado de cobro** son del dominio (entidad Trabajo), no de la UI.
- La **máquina de transiciones (7.2)** vive en `/domain/rules`; la UI solo ofrece las transiciones que el dominio declara válidas (V-9). Esto impide estados imposibles desde el origen.

### 7.3 Propagación de cambios
Las pantallas de lectura (Dashboard, Reportes) observan los datos y se recalculan ante cambios (alta, cobro, anulación), garantizando que **todas las vistas muestren la misma verdad** (mitiga R2).

---

## 8. Validaciones

> Centralizadas en `/domain/validation` y ejecutadas en los casos de uso (no solo en la UI), para que ninguna ruta de escritura las eludan.

| ID | Validación | Punto de aplicación |
|----|-----------|---------------------|
| V-1 | Obligatorios de Trabajo + Precio ≥ 0 | Caso de uso alta/edición de trabajo. |
| V-2 | Ingreso: monto > 0, concepto, método | Caso de uso alta de ingreso. |
| V-3 | Egreso: monto > 0, categoría, concepto | Caso de uso alta de egreso. |
| V-4 | Patente normalizada (mayúsculas, sin espacios) | Mapper/validación de entrada. |
| V-5 | Cobro solo si `Entregado` | Caso de uso de cobro. |
| V-6 | Pago mixto: suma de reparto = total | Caso de uso de ingreso/cobro. |
| V-7 | Editar/anular solo movimientos del día | Caso de uso de edición/anulación. |
| V-8 | Precio bloqueado si `Cobrado` | Caso de uso de edición de trabajo. |
| V-9 | Solo transiciones válidas (7.2) | Máquina de estados del dominio. |
| V-10 | Montos 2 decimales / no negativos | Tipo Dinero (centavos) + validación. |

> Las validaciones de UI (deshabilitar botón "guardar", máscaras) son **espejo**, no sustituto, de las del dominio.

---

## 9. Manejo de errores

### 9.1 Estrategia
- **Resultado explícito** en casos de uso: éxito / error de validación / error de datos. Nada de fallos silenciosos.
- Tipos de error definidos en `/core/error`: *Validación*, *Regla de negocio* (ej. cobro inválido), *Persistencia* (lectura/escritura), *Estado inesperado*.

### 9.2 Mensajes
- Mensajes **claros y en lenguaje del taller** (principio sección 3 / Fase 8), no técnicos.
- Cada validación V-1..V-10 tiene su mensaje específico (ej. "El pago mixto no coincide con el total").
- Catálogo de mensajes centralizado para consistencia y futura traducción.

### 9.3 Recuperación
- **Errores de validación:** se muestran junto al campo; la operación no se ejecuta; el dato ingresado se preserva.
- **Errores de persistencia:** operación atómica revertida; mensaje "no se pudo guardar, reintentá"; estado de UI vuelve a su valor previo (sin datos a medio escribir).
- **Reinicio de app:** los datos locales persisten; la UI reconstruye estado desde la base (probado en Fase 9).
- **Acciones destructivas:** confirmación previa; anulaciones reversibles solo dentro de la ventana del día.

---

## 10. Rendimiento

- **Listas:** paginación/carga perezosa y reciclado de ítems en la Lista de Trabajos y movimientos.
- **Consultas:** índices por fecha/estado/patente/cliente (6.2); agregaciones de reportes resueltas en la base, no en memoria.
- **Dinero en enteros:** evita coste y errores de coma flotante (AD-7).
- **Estado inmutable + observabilidad:** redibujo solo de lo que cambia.
- **Exportación:** generación bajo demanda; al estar acotada a día/mes, el volumen es controlado.
- **Escalabilidad futura (sin sobreingeniería hoy):** la separación por capas permite, vía CR, agregar nube/backup, rangos de fechas personalizados o entidad Cliente sin reescribir dominio ni UI.

---

## 11. Seguridad

> App local, monousuario, sin datos sensibles de pago (no se guardan tarjetas ni credenciales). El foco es **integridad y protección básica de datos**, no autenticación.

| Aspecto | Estrategia |
|---------|-----------|
| **Protección de datos** | Almacenamiento en el sandbox privado de la app (no accesible por otras apps). Sin datos en almacenamiento compartido salvo los archivos de exportación que el usuario decida compartir. |
| **Integridad** | Operaciones atómicas (cobro/anulación), enums cerrados, validaciones en dominio, dinero en centavos. Imposible persistir un estado/valor fuera de catálogo. |
| **Validaciones** | Toda escritura pasa por casos de uso con V-1..V-10; la UI no escribe directo a la base. |
| **Exportaciones** | Los PDF/Excel se generan en almacenamiento privado y se comparten mediante el mecanismo seguro del sistema; el usuario controla el destino. Contienen solo datos del reporte, sin información sensible adicional. |
| **Superficie de ataque** | Sin red, sin servidor, sin integraciones externas (sección 4 del Frozen Spec) → superficie mínima. |

> **No** se implementa cifrado de base ni bloqueo por PIN en el MVP (fuera de alcance); ambos quedan como posibles CR. Se documenta como decisión consciente.

---

## 12. Riesgos técnicos

| ID | Riesgo | Impacto | Mitigación arquitectónica |
|----|--------|---------|---------------------------|
| RT-1 | Doble conteo o descuadre de dinero (R1) | Alto | Dinero en centavos; cobro como caso de uso atómico; ingreso por cobro como única vía (RN-3); reglas en dominio testeable. |
| RT-2 | Inconsistencia entre Dashboard/Reportes/Export (R2) | Alto | Modelo de Reporte único consumido por UI y exportación (AD-8); cálculos centralizados en casos de uso. |
| RT-3 | Estados imposibles (R5) | Medio | Máquina de estados en dominio; UI solo ofrece transiciones válidas (V-9). |
| RT-4 | Pérdida de datos local (R3 / 13.1) | Medio (aceptado) | Persistencia aislada en `/data`; puerta abierta a backup/nube vía CR. |
| RT-5 | Fidelidad de exportación (CA-12) | Medio | Generadores consumen el mismo modelo de reporte que la pantalla. |
| RT-6 | Crecimiento de datos degrada listas/reportes | Bajo-Medio | Índices, paginación, agregación en base. |
| RT-7 | Desvío de alcance disfrazado de "decisión técnica" | Medio | Cualquier capacidad nueva exige CR (sección 14 Frozen Spec). |

---

## 13. Decisiones arquitectónicas y justificación (detalle)

| Decisión | Alternativa descartada | Justificación de la elección |
|----------|------------------------|------------------------------|
| Clean + MVVM pragmática (AD-3) | MVC simple / todo en la UI | El dinero y los estados exigen lógica testeable y aislada; MVC mezclaría reglas con UI y aumentaría R1/R5. |
| 3 capas, sin DTOs extra | Clean "purista" multicapa | El MVP es pequeño; capas extra serían sobreingeniería y costo de mantenimiento sin beneficio. |
| Base relacional local (AD-4) | Archivos/clave-valor | Relaciones (Trabajo↔Ingreso) y agregaciones de reportes se resuelven mejor en SQL; reduce código de cálculo. |
| Dinero en centavos (AD-7) | Decimal flotante | Elimina errores de redondeo; barato y seguro para toda la lógica financiera. |
| Estado unidireccional inmutable (AD-5) | Estado mutable compartido | Predecible y depurable; evita estados intermedios inconsistentes. |
| Cobro como caso de uso atómico | Escrituras separadas desde UI | Garantiza atomicidad e impide ingresos huérfanos o trabajos "cobrados" sin ingreso. |
| Modelo de reporte único (AD-8) | Cálculos duplicados en cada vista | Asegura CA-10/CA-12 y mitiga R2. |
| Sin nube/sync/backup en MVP | Incluirlos ahora | Fuera de alcance (13.x); se preserva extensibilidad por el aislamiento de `/data`. |

---

## 14. Cumplimiento del alcance

Esta arquitectura **no introduce ninguna funcionalidad fuera del Frozen Spec**. Todo lo que aparece como "extensible a futuro" (nube, backup, cifrado, entidad Cliente, rangos de fecha, rentabilidad por trabajo) está marcado explícitamente como **fuera del MVP / sujeto a Change Request**. La separación por capas existe para **no cerrar esas puertas**, no para abrirlas ahora.

---

*Documento de diseño de arquitectura. No contiene código ni implementación. Las elecciones tecnológicas son propuestas justificadas; su confirmación es el primer hito de la Fase 1 del [MASTER-ROADMAP.md](MASTER-ROADMAP.md). Cualquier cambio de alcance respecto del [FROZEN-SPEC.md](FROZEN-SPEC.md) requiere Change Request.*
