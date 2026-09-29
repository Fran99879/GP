# Planes Gratis / Pro — esquema

Documento de decisión para la monetización de **Mis Finanzas** en Google Play.
Todavía sin implementar: falta la integración de cobro.

---

## 1. La división

> **Uso personal: gratis. Uso comercial: Pro.**

Quien lleva las cuentas de su casa tiene la app completa sin pagar. Quien lleva las de un
negocio paga, porque para eso la app le ahorra tiempo y plata.

Es una división que el usuario entiende sola y no necesita explicación.

### Regla que no se rompe

**Lo que ya cargaste nunca se te bloquea.** Si alguien tiene 4 negocios en Pro y deja de
pagar, los 4 **siguen visibles y exportables**: no puede crear más ni cargar movimientos
nuevos en los que exceden el límite, pero no se borra ni se esconde nada.

Dos motivos: es una app de finanzas y la confianza es todo; y retener datos del usuario para
forzar un pago puede costarte una suspensión de Play.

---

## 2. Gratis — finanzas personales completas

Tiene que servir de verdad como app de finanzas personales. Si es un demo recortado, nadie
llega a considerar pagar.

| Incluye | |
|---|---|
| **1 negocio** ("Personal") | Sin límite de movimientos |
| Ingresos y gastos | Categoría, cuenta, método de pago |
| Categorías y cuentas | Personalizables, con ícono |
| Quién me debe | Con fecha límite |
| Metas de ahorro | Sin límite |
| Movimientos recurrentes | Sueldo, alquiler, suscripciones |
| Agenda | Tareas, turnos y productos |
| Reportes del mes | Balance, dona por categoría, barras, evolución 12 meses |
| Presupuestos | Por categoría |
| Calculadora, temas, multi-moneda | |

---

## 3. Pro — para el que tiene un negocio

### Ya construido ✅

| Función | |
|---|---|
| **Negocios ilimitados** | El límite que empuja la conversión |
| **Comparativa entre negocios** | Cuál rinde más |
| **Vista combinada** en Reportes | Todos los negocios juntos |
| **Remitos PDF** | Herramienta de trabajo |
| **Exportar PDF / Excel** | Para el contador |
| **Catálogo de productos** | Alta por código de barras con la cámara, foto, precio con descuento en %, stock y aviso de stock bajo |
| **Clientes y proveedores** | Ficha con documento, teléfono, correo y dirección. Se dan de alta solos al facturar |
| **Facturas** | Comprobante interno en PDF, con descuento por línea y general; al emitir puede registrar el cobro como ingreso y descontar el stock |

### Por construir ⛔

| Función | Nota |
|---|---|
| **Historial y cuenta corriente** | Fase 2 de `ROADMAP.md`: qué le vendiste a cada cliente y cuánto te debe |
| **Estadísticas avanzadas** | Rentabilidad, producto más vendido, mejor cliente (fase 3) |

Estas dos son el argumento para que Pro **siga valiendo la suscripción mes a mes**. Sin
ellas, Pro es "pagar por más negocios" y la gente cancela después del primer mes.

> El **control de stock** quedó cubierto con el catálogo de productos: cada producto lleva
> cantidad y mínimo, y facturar descuenta lo vendido. Las **fichas** de clientes y
> proveedores están hechas (fase 1 de `ROADMAP.md`); falta el historial y la cuenta
> corriente.

> Dato útil: **clientes y proveedores** son la base de stock y de las estadísticas. Conviene
> hacerlos primero. Y ya existe la tabla `contacto` (hoy solo autocompleta deudas), así que
> hay de dónde partir.

---

## 4. Precio — a definir

Variables que importan:

- **Play se queda el 15%** del primer millón de dólares anuales (30% después).
- **Mensual vs anual**: la mensual da ingreso sostenido pero obliga a entregar valor todos
  los meses. La anual convierte peor pero cobra por adelantado y retiene mucho más.
- **Sugerencia**: suscripción **mensual + anual con descuento**. **Prueba gratis: 14 días**
  (definido por el dueño; Play la soporta nativamente).
- En Argentina, fijar el precio **en pesos**, no convertido del dólar.

### Pendiente
- [ ] Precio mensual
- [ ] Precio anual
- [x] Prueba gratis: **14 días** (alcanza a cruzar un cierre de mes sin que el usuario se olvide de que se suscribió)

---

## 5. Implementación

### Productos en Play Console

Un producto **"Pro"** con dos *base plans*: `mensual` y `anual`. No son dos productos
separados.

`Monetizar con Play → Productos → Suscripciones`

### En la app

1. **Dependencia**: `com.android.billingclient:billing-ktx`
2. **`EstadoPlan`**: objeto global con `StateFlow<Plan>` (`GRATIS` / `PRO`), mismo patrón que
   `NegocioActual`. Las pantallas lo observan y se actualizan solas.
3. **Gates**:
   - `CrearNegocioUseCase`: si es Gratis y ya hay 1, devolver un resultado que la UI traduzca
     en la pantalla de planes. Ya hay precedente: `EliminarNegocioUseCase` devuelve `Boolean`.
   - Remito, export y comparativa: **mostrar con candado, no ocultar**. Nadie compra lo que no
     sabe que existe.
4. **Pantalla de planes**: comparativa Gratis vs Pro. El precio lo trae Play ya localizado —
   **nunca hardcodearlo**.
5. **Restaurar compras**: obligatorio. `queryPurchases` al iniciar, para reinstalaciones y
   cambio de teléfono.
6. **Compras pendientes**: Play lo exige. En Argentina el pago en efectivo puede quedar
   pendiente días. No entregar Pro hasta `PURCHASED`.

### Degradación

Al vencer: los negocios que exceden el límite pasan a **solo lectura**. Se ven, se exportan,
no se cargan movimientos nuevos. Aviso claro y no agresivo de qué se recupera al renovar.

### Validación

Para una app 100% local, alcanza con el estado que cachea la librería de Play. Un backend de
validación solo se justifica si más adelante hay sincronización.

---

## 6. No se puede probar en el emulador

Play Billing requiere:

1. App **subida a Play** — ✅ ya está en prueba interna
2. Productos **creados y activos** en Play Console
3. Cuenta de **tester con licencia**

Es lo único del proyecto que no se puede verificar localmente.

---

## 7. Al activar Billing, actualizar en Play Console

Declaraciones que hoy están en "No" y pasan a "Sí":

- **Datos de inicio de sesión** → Sí (hay contenido que requiere pago)
- **Clasificación de contenido** → "¿Permite comprar contenido digital?" → Sí
- **Seguridad de los datos** → revisar: Google procesa el pago

Declarar mal es causa de suspensión.
