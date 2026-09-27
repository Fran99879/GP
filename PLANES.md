# Planes Gratis / Pro / Premium — esquema propuesto

Documento de decisión para la monetización de **Mis Finanzas** en Google Play.
Nada de esto está implementado todavía: es la propuesta para que la revises y ajustes.

Contexto: la app ya está **completa a nivel funcional** (ver `UI-ESCRITORIO-ANALISIS-Y-ROADMAP-MOVIL.md`).
El sistema de licencias Ed25519 se retiró del móvil; queda solo en escritorio.

---

## 1. Principio: qué se cobra y qué no

La regla que propongo, y que conviene no romper:

> **Lo que ya cargaste nunca se te bloquea.** Se cobra por *capacidad* y *herramientas
> de negocio*, no por el acceso a tus propios datos.

Esto importa por dos motivos:

1. **Confianza.** Es una app de finanzas personales. Si alguien carga seis meses de
   movimientos y al vencer la suscripción no los puede ver, no vuelve nunca más — y lo
   cuenta en las reseñas.
2. **Política de Play.** Retener datos del usuario como rehén para forzar un pago es un
   camino rápido a una suspensión.

Traducción concreta: si alguien tiene 4 negocios en Pro y deja de pagar, **los 4 siguen
visibles y exportables**; simplemente no puede crear un quinto ni seguir cargando en los
que exceden el límite gratuito. Nunca se borra nada.

---

## 2. Los tres planes

### 🆓 Gratis — "que sirva de verdad"

El plan gratuito tiene que ser **usable como app completa de finanzas personales**, no un
demo recortado. Si no sirve, nadie llega a considerar pagar.

| Incluye | Detalle |
|---|---|
| **1 negocio** | Suficiente para uso personal, que es la mayoría de los usuarios |
| Ingresos y gastos | Sin límite de cantidad |
| Categorías y cuentas | Personalizables, con ícono |
| Quién me debe | Deudas con fecha límite y recordatorio |
| Metas de ahorro | Sin límite |
| Agenda | Tareas, turnos y productos |
| Reportes del mes | Balance, dona por categoría, barras, evolución 12 meses |
| Presupuestos | Por categoría |
| Calculadora | — |
| Temas y moneda | Claro/oscuro, color de acento, multi-moneda |

### ⭐ Pro — "para el que tiene un negocio"

El salto natural: **dejás de manejar solo tu plata y empezás a manejar la de un negocio.**

| Suma sobre Gratis | Por qué acá |
|---|---|
| **Negocios ilimitados** | Es *el* límite que empuja la conversión: separar personal de negocio, o tener dos locales |
| **Comparativa entre negocios** | Solo tiene sentido con más de uno; ya está construida |
| **Vista combinada** en Reportes | Ídem |
| **Remitos PDF** | Herramienta de trabajo, no de finanzas personales |
| **Movimientos recurrentes** | Sueldo, alquiler, suscripciones |
| **Exportar PDF / Excel** | Para el contador |

### 💎 Premium — "tranquilidad"

Pro resuelve *hoy*. Premium resuelve el **miedo a perder los datos**, que es la
preocupación real de quien lleva la contabilidad de su negocio en el teléfono.

| Suma sobre Pro | Estado |
|---|---|
| **Backup automático en la nube** | ⛔ No existe — hay que construirlo |
| **Sincronizar entre dispositivos** (teléfono ↔ escritorio) | ⛔ No existe — hay que construirlo |
| **Historial de respaldos y restauración** | ⛔ No existe |
| Soporte prioritario | Organizativo |

> ⚠️ **Premium todavía no se puede vender.** Su valor depende de features que no existen.
> Lanzarlo ahora sería cobrar por una promesa. La recomendación es **salir con Gratis + Pro**
> y agregar Premium cuando el backup en la nube esté andando.

---

## 3. Qué hay que construir

| Plan | Features listas | Por construir |
|---|---|---|
| Gratis | **Todo** ✅ | Nada |
| Pro | **Todo** ✅ (multi-negocio, comparativa, remitos, recurrentes, export) | Solo el *gate* de pago |
| Premium | Nada | Backup nube, sync, historial |

Dicho de otra forma: **para vender Pro solo falta la integración de cobro.** Todo lo que
incluye ya está hecho y verificado.

---

## 4. Precio — decisión tuya

No me meto a fijar precios, pero sí las variables que importan:

- **Play se queda el 15%** del primer millón de dólares anuales (30% después). Sobre una
  suscripción chica, ese 15% es menos de lo que parece en valores absolutos.
- **Suscripción mensual vs pago único.** La mensual da ingreso sostenido, pero te obliga a
  seguir entregando valor todos los meses o la gente cancela. El pago único convierte mucho
  mejor pero no financia el mantenimiento.
- **Sugerencia**: Pro como **suscripción mensual con opción anual** (la anual con descuento
  mejora mucho la retención y cobra por adelantado), más **prueba gratis de 7 o 14 días**,
  que Play soporta nativamente.
- En Argentina conviene mirar el precio en pesos y que Play ajusta por país: definilo en
  moneda local, no convertido del dólar.

---

## 5. Implementación técnica

### Productos a crear en Play Console

| ID sugerido | Tipo | Notas |
|---|---|---|
| `pro_mensual` | Suscripción (base plan mensual) | Con período de prueba |
| `pro_anual` | Suscripción (base plan anual) | Mismo producto, otro base plan |

Play modela esto como **un producto "Pro" con dos base plans**, no dos productos separados.

### En la app

1. **Dependencia**: `com.android.billingclient:billing-ktx`.
2. **`EstadoPlan`** (objeto global, como `NegocioActual`): expone
   `StateFlow<Plan>` con `GRATIS` / `PRO`. Las pantallas lo observan y se actualizan solas.
3. **Gates**: envolver los puntos de venta —
   - `CrearNegocioUseCase`: si es Gratis y ya hay 1, devolver un resultado que la UI traduzca
     en la pantalla de upgrade (ya hay precedente: `EliminarNegocioUseCase` devuelve `Boolean`).
   - Botón de Remito, export y comparativa: mostrar el candado en vez de ocultarlos —
     **la gente no compra lo que no sabe que existe**.
4. **Pantalla de planes**: comparativa de los tres, con el precio que trae Play (nunca
   hardcodeado: Play devuelve el precio localizado del usuario).
5. **Restaurar compras**: obligatorio. Al reinstalar o cambiar de teléfono, `queryPurchases`
   al iniciar.
6. **Compras pendientes**: Play exige manejarlas (pago en efectivo en Argentina puede quedar
   pendiente días). No entregar Pro hasta `PURCHASED`.

### Degradación (lo más delicado)

Cuando una suscripción vence, con el principio de la sección 1:

- Los negocios que excedan el límite pasan a **solo lectura**: se ven, se exportan, no se
  cargan movimientos nuevos.
- **Nunca** se borran datos ni se ocultan.
- Un aviso claro y no agresivo explicando qué se recuperaría al renovar.

### Validación

Para una app **100% local** como esta, lo pragmático es confiar en el estado que cachea la
librería de Play. Un backend de validación solo se justifica si más adelante hay sync
(Premium), porque ahí ya vas a tener servidor.

---

## 6. Lo que no se puede probar en el emulador

A diferencia de todo lo demás que verificamos, **Play Billing no se puede probar localmente**.
Requiere:

1. La app **subida a Play** (alcanza con testing interno).
2. Los productos **creados y activos** en Play Console.
3. Una **cuenta de tester con licencia** configurada.

Es decir: la secuencia obligada es **cuenta de desarrollador → subir a testing interno →
recién ahí implementar y probar el cobro**. No se puede adelantar.

---

## 7. Recomendación

1. **Salir ya con Gratis + Pro.** Todo lo de Pro está construido; falta solo el cobro.
2. **Premium después**, cuando exista el backup en la nube. No vender promesas.
3. Primera subida a **testing interno**, no producción: te deja probar el cobro de verdad
   sin exponer la app.
4. Definir vos: **precios** y si querés prueba gratis (yo la pondría).

### Decisiones pendientes

- [ ] ¿El límite del plan Gratis es **1 negocio** o 2? (el escritorio usa 2)
- [ ] ¿Los **recurrentes** van en Gratis o en Pro? Los puse en Pro, pero son útiles también
      para uso personal — es discutible.
- [ ] Precio de Pro mensual y anual.
- [ ] ¿Prueba gratis? ¿De cuántos días?
- [ ] ¿Lanzamos Premium más adelante o lo dejamos fuera del roadmap por ahora?
