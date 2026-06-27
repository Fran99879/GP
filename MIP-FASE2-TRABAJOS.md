# 🧩 MODULE IMPLEMENTATION PLAN (MIP) — Módulo Trabajos (Fase 2)

**Proyecto:** TallerApp — v1.0 (MVP)
**Módulo:** Gestión de Trabajos
**Fase del Roadmap:** 2
**Documentos base (MDD):** [FROZEN-SPEC.md](FROZEN-SPEC.md) §5–§9, §11 · [ARCHITECTURE.md](ARCHITECTURE.md) §1–§9 · [MASTER-ROADMAP.md](MASTER-ROADMAP.md) Fase 2
**Naturaleza:** Plan técnico previo a programar. **No contiene código.** Un solo módulo. No avanza a Finanzas/Cobros.

---

## Resumen ejecutivo

Este MIP convierte el diseño del módulo **Trabajos** en un plan ejecutable paso a paso. Trabajos es el **núcleo del sistema**: registra el ingreso de un vehículo, administra su ciclo de reparación mediante una máquina de estados, y permite consultarlo, editarlo, eliminarlo y buscarlo. Es la primera fase que introduce las tres capas reales (dominio, datos, presentación) sobre el esqueleto navegable de la Fase 1.

**Límite de alcance estricto:** este módulo **no** cobra, **no** registra ingresos/egresos y **no** calcula dinero agregado. Crea el `Precio del trabajo` y el `Estado de cobro = Pendiente de cobro`, pero el flujo de cobro real pertenece a la Fase 4. Esa frontera es la regla más importante a no cruzar.

**Estado al cierre del plan:** 🟢 (ver sección Definition of Ready).

---

## 1. Resumen del módulo

- **Objetivo:** permitir al taller registrar y administrar los vehículos/trabajos y su estado de reparación, con búsqueda por patente y cliente.
- **Alcance (incluido):** alta, edición, eliminación, lista con tarjetas, detalle, máquina de estados con cambio de un toque, validaciones V-1/V-4/V-8/V-9, búsqueda (RN-10), persistencia local de la entidad Trabajo.
- **Alcance (excluido, no cruzar):** cobro (9.3/9.4 → Fase 4), ingresos/egresos (Fase 3), indicadores del dashboard (Fase 5), reportes (Fase 6), exportación (Fase 7). El `Estado de cobro` solo se **muestra** y se inicializa; no se opera.
- **Funcionalidad principal:** CRUD de Trabajo + transiciones de estado válidas + búsqueda.

---

## 2. Dependencias técnicas

- **Módulos previos terminados:** Fase 1 (navegación, `core/ui`, `core/navigation`, tema). Aprobada.
- **Componentes reutilizados:** `PrimaryButton`, `PlaceholderScaffold` (se conserva para barras con título/atrás), tema y tipografía de `core/ui/theme`, rutas de `Destination`.
- **Entidades utilizadas:** **Trabajo** (única entidad de esta fase). Enums: Estado de reparación, Estado de cobro, Servicio realizado. No usa Ingreso ni Egreso.
- **Servicios consumidos:** ninguno externo. Crea los suyos: repositorio de Trabajos, casos de uso, validadores, máquina de estados.
- **Información compartida (hacia futuras fases):** la interfaz `TrabajoRepository` y la entidad `Trabajo` quedarán disponibles para que Cobros (Fase 4) y Dashboard (Fase 5) las consuman en **solo lectura**. Este módulo es el **dueño** de la entidad Trabajo.

---

## 3. Plan de implementación (tareas ordenadas)

> Orden lógico: dominio → datos → presentación. Cada tarea es pequeña, verificable y depende de la anterior.

| # | Tarea | Objetivo | Resultado esperado | Depende de |
|---|-------|----------|--------------------|-----------|
| T1 | Definir entidad `Trabajo` y enums en `domain/model` | Modelar el dato puro (5.1) | Entidad + enums (estados, servicio) tipados | Fase 1 |
| T2 | Definir máquina de transiciones en `domain/rules` | Implementar 7.2/7.3 | Función que dada un estado devuelve transiciones válidas | T1 |
| T3 | Definir validaciones en `domain/validation` | V-1, V-4, V-8 | Validadores puros con resultado tipado | T1 |
| T4 | Definir contrato `TrabajoRepository` en `domain/repository` | Aislar persistencia | Interfaz con operaciones CRUD + búsqueda | T1 |
| T5 | Definir casos de uso en `domain/usecase` | Orquestar reglas | Casos: crear, editar, eliminar, listar, obtener, buscar, cambiar estado | T2,T3,T4 |
| T6 | Definir esquema y DAO en `data/local` | Persistencia local (Room) | Tabla `trabajo`, DAO con consultas e índices | T1 |
| T7 | Mapper entidad↔registro en `data/mapper` | Convertir dominio↔DB | Mapeo bidireccional sin pérdida | T1,T6 |
| T8 | Implementar `TrabajoRepositoryImpl` en `data/repository` | Cumplir T4 sobre Room | Repositorio funcional | T6,T7 |
| T9 | Inicializar base de datos (provisión) | Punto único de acceso a DB | Instancia de DB disponible para repositorio | T6 |
| T10 | ViewModel + estado de UI de Lista/Búsqueda | Exponer lista observable y filtro | Estado (cargando/contenido/vacío) + eventos | T5 |
| T11 | Reemplazar placeholder `TrabajosListScreen` | Lista real con tarjetas (RN-8 visual, C5) | Tarjetas con Patente o Marca+Modelo+Cliente | T10 |
| T12 | ViewModel + pantalla de Alta (`TrabajoFormScreen`) | Crear trabajo (<1 min, 9.1) | Form con obligatorios y validación en vivo | T5 |
| T13 | ViewModel + pantalla de Detalle/Edición | Ver/editar + cambiar estado por toque | Detalle con transiciones válidas, Precio bloqueable | T5 |
| T14 | Eliminación con confirmación | Borrado seguro | Diálogo + borrado | T13 |
| T15 | Búsqueda por patente y cliente | RN-10 | Filtro en lista por texto | T10 |
| T16 | Cableado de navegación definitivo | Conectar lista↔detalle↔alta con datos reales | Rutas con id real (no "demo") | T11,T12,T13 |
| T17 | Autoauditoría + pruebas del módulo | Cerrar la fase | Checklists §14–§16 superadas | T1–T16 |

---

## 4. Archivos involucrados

> Sin código; solo responsabilidad. Rutas bajo `app/src/main/java/com/tallerapp/`.

### 4.1 Nuevos
| Archivo | Responsabilidad |
|---------|-----------------|
| `domain/model/Trabajo.kt` | Entidad pura del trabajo (campos 5.1). |
| `domain/model/EstadoReparacion.kt` | Enum de estados (7.1). |
| `domain/model/EstadoCobro.kt` | Enum `PendienteDeCobro`/`Cobrado` (8). |
| `domain/model/ServicioRealizado.kt` | Lista cerrada (5.4). |
| `domain/rules/TransicionesTrabajo.kt` | Máquina de transiciones (7.2/7.3, V-9). |
| `domain/validation/TrabajoValidator.kt` | V-1, V-4, V-8. |
| `domain/repository/TrabajoRepository.kt` | Contrato CRUD + búsqueda. |
| `domain/usecase/` (varios) | Crear/Editar/Eliminar/Listar/Obtener/Buscar/CambiarEstado. |
| `data/local/TrabajoEntity.kt` | Registro persistible. |
| `data/local/TrabajoDao.kt` | Consultas Room + índices. |
| `data/local/TallerDatabase.kt` | Definición de la base local. |
| `data/mapper/TrabajoMapper.kt` | Dominio↔persistencia. |
| `data/repository/TrabajoRepositoryImpl.kt` | Implementación del contrato. |
| `features/trabajos/list/TrabajosListViewModel.kt` | Estado de lista/búsqueda. |
| `features/trabajos/form/TrabajoFormViewModel.kt` | Estado de alta/edición. |
| `features/trabajos/detalle/TrabajoDetalleViewModel.kt` | Estado de detalle + cambio de estado. |
| `features/trabajos/components/TrabajoCard.kt` | Tarjeta de la lista (C5). |

### 4.2 Modificados
| Archivo | Cambio |
|---------|--------|
| `app/build.gradle.kts` | Agregar dependencias Room + ViewModel/Lifecycle. |
| `features/trabajos/TrabajosListScreen.kt` | Reemplazar placeholder por lista real + búsqueda. |
| `features/trabajos/TrabajoFormScreen.kt` | Reemplazar placeholder por formulario real. |
| `features/trabajos/TrabajoDetalleScreen.kt` | Reemplazar placeholder por detalle/edición. |
| `core/navigation/TallerApp.kt` | Inyectar ViewModels y pasar id real al detalle. |

> **Prohibido tocar:** archivos de Finanzas, Reportes, Dashboard (siguen como placeholders de Fase 1). Solo se modifican los listados arriba.

---

## 5. Componentes

| Componente | Responsabilidad | Recibe | Genera |
|------------|-----------------|--------|--------|
| `TrabajoCard` | Mostrar un trabajo en la lista | Datos del trabajo (id, identificación, estado, fecha) | Evento "abrir detalle" (id) |
| `TrabajosListScreen` | Render de lista + barra de búsqueda + acceso a alta | Estado de lista, texto de búsqueda | Eventos: buscar, abrir detalle, nuevo trabajo |
| `TrabajoFormScreen` | Captura de campos de alta/edición | Estado de formulario, errores | Eventos: cambios de campo, guardar |
| `TrabajoDetalleScreen` | Ver datos + cambiar estado + editar + eliminar | Estado de detalle, transiciones válidas | Eventos: cambiar estado, editar, eliminar |
| `ConfirmDialog` (reutiliza patrón) | Confirmar acción destructiva (eliminar) | Mensaje | Confirmar/Cancelar |

---

## 6. Servicios (capa dominio/datos)

| Servicio | Responsabilidad | Entradas | Salidas |
|----------|-----------------|----------|---------|
| `CrearTrabajoUseCase` | Validar (V-1, V-4) y crear | Datos del formulario | Resultado: éxito(id) / error de validación |
| `EditarTrabajoUseCase` | Validar y actualizar (respeta V-8) | Trabajo editado | Resultado éxito/error |
| `EliminarTrabajoUseCase` | Borrar un trabajo | id | Resultado éxito/error |
| `ListarTrabajosUseCase` | Exponer lista observable | — | Flujo de lista de trabajos |
| `BuscarTrabajosUseCase` | Filtrar por patente/cliente (RN-10) | texto | Lista filtrada |
| `ObtenerTrabajoUseCase` | Cargar detalle | id | Trabajo o "no encontrado" |
| `CambiarEstadoUseCase` | Aplicar transición válida (V-9, 7.2) | id, estado destino | Resultado éxito/error (transición inválida) |
| `TrabajoRepository(+Impl)` | Persistencia local | operaciones CRUD | entidades / flujos observables |
| `TransicionesTrabajo` | Calcular transiciones permitidas | estado actual | conjunto de estados destino |
| `TrabajoValidator` | Reglas de campo | datos de trabajo | lista de errores |

> Todos los servicios de escritura pasan por casos de uso; **la UI nunca escribe directo a la base** (Arquitectura §1.3).

---

## 7. Modelos involucrados

- **Trabajo** (única entidad de la fase). Campos según Frozen Spec 5.1.
- **EstadoReparacion**, **EstadoCobro**, **ServicioRealizado** (enums / objetos de valor).
- **Interacción:** un Trabajo nace con `EstadoReparacion = Pendiente` y `EstadoCobro = PendienteDeCobro`. El `EstadoReparacion` evoluciona según `TransicionesTrabajo`. El `EstadoCobro` se inicializa pero **no cambia en esta fase** (cambiará en Fase 4). El `Precio` se vuelve no editable solo si `EstadoCobro = Cobrado` (condición que aún no puede darse en Fase 2, pero la regla V-8 queda implementada y lista).
- No hay relación con Ingreso/Egreso en esta fase.

---

## 8. Flujo de datos

1. **Origen:** el usuario completa el formulario (alta) o toca un estado (detalle).
2. **Evento → ViewModel:** la pantalla emite un evento; el ViewModel llama al caso de uso correspondiente.
3. **Transformaciones:** el caso de uso ejecuta validaciones (V-1/V-4) o calcula la transición válida (V-9) antes de persistir.
4. **Persistencia:** el repositorio escribe en la base local (Room) vía DAO; el mapper convierte dominio↔registro.
5. **Actualización de UI:** la lista observa la base y se recompone automáticamente; el detalle recibe el nuevo estado; los errores se devuelven como estado y se muestran junto al campo/acción.

```
Pantalla → (evento) → ViewModel → UseCase → [Validación/Reglas] → Repository → DAO → DB local
                                   ▲                                                 │
                                   └──────────── nuevo estado / flujo observable ◄───┘
```

---

## 9. Navegación

- **Acceso:** desde Dashboard ("Ver Trabajos" y "Nuevo Trabajo") y desde la propia lista.
- **Acciones del usuario:** buscar; crear; abrir detalle; editar; cambiar estado (un toque); eliminar (con confirmación).
- **Navegación saliente:** Lista → Detalle (con id real); Lista/Dashboard → Alta; Detalle → Edición (misma pantalla o modo edición). **No** navega a cobro (Fase 4). Se elimina el botón "detalle de ejemplo (demo)" de la Fase 1.

---

## 10. Validaciones

| ID | Campo/Regla | Mensaje (borrador, se afina en Fase 8) | Comportamiento |
|----|-------------|----------------------------------------|----------------|
| V-1 | Cliente, Marca, Modelo, Servicio realizado obligatorios; Precio ≥ 0 | "Completá [campo]" / "El precio no puede ser negativo" | No guarda; resalta campo. |
| V-4 | Patente (si se ingresa) se normaliza a MAYÚSCULAS sin espacios | — (transformación silenciosa) | Normaliza al guardar. |
| V-8 | Precio bloqueado si `EstadoCobro = Cobrado` | "El precio no se puede editar tras el cobro" | Campo deshabilitado. |
| V-9 | Solo transiciones válidas (7.2) | (no se ofrecen transiciones inválidas) | UI muestra solo destinos válidos. |
| — | Servicio realizado debe ser de la lista cerrada (5.4) | — | Selector, no texto libre. |

---

## 11. Manejo de errores

| Error esperado | Respuesta del sistema | Recuperación | Mensaje al usuario |
|----------------|-----------------------|--------------|--------------------|
| Validación fallida en alta/edición | No persiste; devuelve errores por campo | Usuario corrige; datos preservados | Específico por campo |
| Transición de estado inválida | Operación rechazada por caso de uso | Estado se mantiene | "No se puede pasar a ese estado" |
| Trabajo no encontrado (detalle) | Estado "no encontrado" | Volver a la lista | "El trabajo no existe" |
| Fallo de persistencia (lectura/escritura) | Operación atómica revertida | Reintentar | "No se pudo guardar, reintentá" |
| Eliminación accidental | Confirmación previa obligatoria | Cancelar en el diálogo | "¿Eliminar este trabajo?" |

---

## 12. Optimización

- **Reutilización:** `TrabajoCard` y `ConfirmDialog` como componentes; `PrimaryButton`/scaffold ya existen.
- **Reducir complejidad:** una sola pantalla de formulario para alta y edición (mismo estado, distinto modo) en lugar de duplicar.
- **Evitar duplicación:** validaciones centralizadas en `TrabajoValidator` (no repetir en UI; la UI solo refleja).
- **Mantenibilidad:** máquina de estados en un único lugar (`TransicionesTrabajo`); la UI consulta, no decide.
- **Rendimiento:** lista observable con carga perezosa; índices en DAO por estado, patente y cliente; búsqueda resuelta en consulta, no en memoria.
- **Simplicidad:** sin paginación compleja ni caché adicional en MVP (volumen bajo).

---

## 13. Riesgos técnicos

| Riesgo | Impacto | Mitigación |
|--------|---------|-----------|
| Implementar transiciones libres en vez de 7.2 (R5) | Estados inconsistentes | Toda transición pasa por `CambiarEstadoUseCase`; UI solo muestra destinos válidos. |
| Servicio realizado como texto libre | Rompe reporte futuro (Fase 6) | Selector de lista cerrada desde el inicio (5.4). |
| Tarjeta sin patente mal identificada (C5) | Confusión en la lista | Regla explícita: Patente o `Marca+Modelo+Cliente`. |
| Editar Precio tras cobro | Inconsistencia financiera futura | V-8 implementada ya, aunque el cobro llegue en Fase 4. |
| Fuga de alcance (tocar finanzas/cobro) | Rompe el orden del roadmap | Lista cerrada de archivos modificables (§4.2). |
| Definir mal el esquema de DB y migrar luego | Retrabajo en Fase 3/4 | Esquema alineado a entidades del Frozen Spec; entidad Trabajo es estable. |

---

## 14. Checklist de implementación (desarrollo)

- [ ] Entidad `Trabajo` y enums creados (5.1, 5.4, 7.1, 8).
- [ ] Máquina de transiciones `TransicionesTrabajo` (7.2/7.3).
- [ ] `TrabajoValidator` (V-1, V-4, V-8).
- [ ] Contrato `TrabajoRepository` definido.
- [ ] Casos de uso: crear, editar, eliminar, listar, obtener, buscar, cambiar estado.
- [ ] Base local, DAO (con índices) y mapper.
- [ ] `TrabajoRepositoryImpl` funcional.
- [ ] Lista con `TrabajoCard` (identificación C5) + estado vacío.
- [ ] Búsqueda por patente y cliente (RN-10).
- [ ] Formulario alta/edición con validación en vivo.
- [ ] Detalle con cambio de estado por toque (solo válidos).
- [ ] Eliminación con confirmación.
- [ ] Navegación cableada con id real (sin "demo").
- [ ] Dependencias Room/ViewModel agregadas al build.

## 15. Checklist de revisión técnica

- [ ] Consistencia con Frozen Spec §5–§9, §11.
- [ ] Respeta capas (dominio sin Android; UI sin acceso directo a DB).
- [ ] Nomenclatura coherente con la del proyecto.
- [ ] Responsabilidad única por archivo/componente.
- [ ] Bajo acoplamiento: UI depende de casos de uso, no del repositorio.
- [ ] Escalabilidad: contrato de repositorio reutilizable por Fases 4/5.
- [ ] Mantenibilidad: reglas en un solo lugar; sin duplicación.

## 16. Checklist de QA

- [ ] **Feliz:** crear trabajo con obligatorios → aparece en lista en <1 min.
- [ ] **Feliz:** recorrer estados Pendiente→…→Entregado por toques.
- [ ] **Feliz:** editar datos; buscar por patente y por cliente.
- [ ] **Borde:** trabajo sin patente → tarjeta muestra Marca+Modelo+Cliente.
- [ ] **Borde:** `Cancelado` y `Entregado` son terminales (7.2).
- [ ] **Borde:** búsqueda con texto que no coincide → lista vacía sin error.
- [ ] **Error:** guardar sin obligatorios → mensajes por campo, no persiste.
- [ ] **Error:** intentar transición inválida → rechazada.
- [ ] **Validación:** Precio negativo rechazado; patente normalizada.
- [ ] **Persistencia:** cerrar y reabrir app → trabajos persisten.
- [ ] **Integración:** navegación lista↔detalle↔alta con datos reales sin romper Fase 1.

## 17. Definition of Ready

| Requisito | Estado |
|-----------|--------|
| Arquitectura aprobada | ✅ [ARCHITECTURE.md](ARCHITECTURE.md) |
| Diseño/MDD aprobado | ✅ Frozen Spec §5–§9, §11 |
| Dependencias disponibles | ✅ Fase 1 aprobada (navegación, core) |
| Riesgos conocidos | ✅ §13 |
| Alcance congelado | ✅ Frozen Spec (CERRADO) |
| Stack confirmado | ⚠️ Pendiente confirmación explícita del stack (Kotlin/Compose/Room) — asumido aprobado en Fase 1. |

> Único punto abierto: confirmación formal del stack. No bloquea la planificación; sí debe confirmarse antes de la tarea T6 (introduce Room).

## 18. Definition of Done

El módulo Trabajos está **terminado** cuando:

- [ ] Funcionalidad completa: CRUD + estados + búsqueda operativos.
- [ ] Todas las validaciones V-1/V-4/V-8/V-9 implementadas y probadas.
- [ ] Persistencia local verificada (sobrevive reinicio).
- [ ] Checklists §14, §15, §16 superadas.
- [ ] Sin errores conocidos abiertos.
- [ ] Cumple Frozen Spec §5–§9, §11 sin desvíos de alcance.
- [ ] Cumple la arquitectura por capas (sin acceso UI→DB).
- [ ] No se modificó ningún módulo fuera de §4.2.
- [ ] Aprobación explícita para cerrar la fase.

---

# 🟢 MODULE READY FOR IMPLEMENTATION

El módulo Trabajos está completamente planificado. La única dependencia abierta es la **confirmación formal del stack** (Kotlin/Compose/Room), asumida tras la Fase 1; debe ratificarse antes de la tarea T6. No falta información de diseño ni de alcance para comenzar a programar las tareas T1–T5 de inmediato.
