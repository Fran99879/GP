# Roadmap — Clientes y Proveedores

> Plan de trabajo. **Todavía no está construido**: este archivo define qué se va a hacer,
> en qué orden y con qué decisiones ya tomadas, para poder arrancar sin volver a pensarlo.
>
> Otros roadmaps del repo (`AUDITORIA-Y-ROADMAP-MOVIL.md`,
> `UI-ESCRITORIO-ANALISIS-Y-ROADMAP-MOVIL.md`) están cerrados y son de otra etapa.

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

### Fase 1 — Ficha y listado

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
