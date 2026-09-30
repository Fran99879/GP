# Roadmap — Clientes, Proveedores y copia en la nube

> Plan de trabajo. **Fase 1 hecha el 29/09/2026** (ver al final de cada fase); las fases 2
> y 3 siguen pendientes. Las decisiones tomadas acá son las que se implementaron.
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
