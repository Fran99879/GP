# Spec-Driven Development — TallerApp

**Versión:** 1.0 (MVP)
**Plataforma:** Android
**Tipo:** Gestión simple para talleres mecánicos

---

> ⚠️ **DOCUMENTO SUPERADO — NO USAR PARA DESARROLLAR.**
> Esta es la especificación funcional original (borrador). Fue **auditada** y **corregida**.
> La **única fuente de verdad** para el desarrollo es **[FROZEN-SPEC.md](FROZEN-SPEC.md)** (🟢 SPEC FROZEN),
> que incorpora las correcciones C1–C15 listadas abajo. Cualquier cambio posterior requiere un **Change Request**.

### Correcciones aceptadas y aplicadas en la Frozen Spec

| # | Tema | Resolución |
|---|------|-----------|
| C1 | Fórmula de Ganancia | `Ganancia = Ingresos − Egresos`, criterio de **caja** (cash-basis): cada movimiento se imputa a su fecha. |
| C2 | Doble conteo dinero | El **Cobro** es la **única** vía por la que un trabajo entra a Ingresos. El Ingreso manual es solo para conceptos **no** ligados a trabajos. |
| C3 | Entrega sin cobro (fiado) | Se separa el **estado de cobro** (`Pendiente de cobro` / `Cobrado`) del estado de reparación. |
| C4 | Transiciones de estado | Se define máquina de estados con transiciones permitidas. |
| C5 | Patente opcional vs identificador | La tarjeta/búsqueda usan Patente si existe; si no, `Marca + Modelo + Cliente`. |
| C6 | Obligatorios vs opcionales | Tabla explícita de validaciones por campo. |
| C7 | Pago mixto | Modelado como reparto de monto entre métodos (la suma debe igualar el total). |
| C8 | "Costo del trabajo" | Renombrado a **"Precio del trabajo"** (importe a cobrar). |
| C9 | Corrección de movimientos | Se permite editar/anular movimientos de finanzas del día en curso. |
| C10 | "Caja del día" | Definido = total de Ingresos del día (todos los métodos). |
| C11 | Categoría "Servicios" | Renombrada a **"Servicios e impuestos"** (luz, agua, alquiler) para no solapar con "Servicio realizado". |
| C12 | Trabajo cancelado | Se añade estado **Cancelado** (sin cobro). |
| C13 | Cliente texto libre | Se documenta como texto libre y su limitación de búsqueda (sin entidad Cliente en MVP). |
| C14 | Definiciones de período/conteo | "del mes" = mes calendario; se definen "vehículos atendidos" y "trabajos realizados". |
| C15 | Persistencia | Datos **locales en el dispositivo**; se documenta el riesgo (sin nube en MVP). |

---

## 1. Objetivo

Desarrollar una aplicación Android sencilla para la gestión diaria de un taller mecánico.

La prioridad es **minimizar el tiempo** que el usuario dedica a utilizar la aplicación. Todas las acciones deben poder realizarse con **pocos toques** y **sin formularios extensos**.

La aplicación **no** busca reemplazar un sistema ERP ni un software administrativo completo. Su objetivo es:

- Registrar trabajos.
- Controlar ingresos y egresos.
- Conocer la ganancia del taller.

---

## 2. Público objetivo

- Pequeños talleres mecánicos.
- Talleres familiares.
- Talleres de motos.
- Electricidad del automóvil.
- Mecánicos independientes.

---

## 3. Principios del sistema

| Principio | Descripción |
|-----------|-------------|
| Interfaz simple | Diseño minimalista, sin ruido visual. |
| Botones grandes | Fáciles de pulsar incluso con las manos sucias o con guantes. |
| Pocas pantallas | Flujos cortos, sin navegación profunda. |
| Carga rápida de información | Menos campos, valores automáticos donde sea posible. |
| Sin funciones innecesarias | Solo lo que aporta valor diario. |
| Información clara | Datos legibles de un vistazo. |
| Navegación intuitiva | Sin necesidad de aprendizaje previo. |

---

## 4. Dashboard (pantalla principal)

Al abrir la aplicación se mostrará:

- **Caja del día**
- **Ganancia del mes**
- **Vehículos actualmente en el taller**
- **Vehículos esperando repuestos**
- **Trabajos pendientes**
- **Trabajos entregados durante el día**

### Accesos rápidos

- ➕ Nuevo Trabajo
- 💰 Nuevo Ingreso
- 🧾 Nuevo Gasto

---

## 5. Gestión de Trabajos

Módulo principal de la aplicación. Cada **trabajo** representa el ingreso de un vehículo al taller.

### Campos

| Campo | Obligatorio | Notas |
|-------|-------------|-------|
| Cliente | Sí | — |
| Teléfono | No | Opcional |
| Patente | No | Opcional |
| Marca | Sí | — |
| Modelo | Sí | — |
| Fecha de ingreso | Automática | Se asigna al crear |
| Estado | Sí | Ver sección 6 |
| Servicio realizado | Sí | *Ver sección 5.1* |
| Problema informado | No | Opcional |
| Diagnóstico | No | Opcional |
| Costo del trabajo | Sí | — |
| Método de pago | Solo al entregar | Ver sección 8 |

### 5.1 Mejora: campo "Servicio realizado"

Se incorpora un campo **"Servicio realizado"** en la ficha del trabajo, por ejemplo:

- Cambio de aceite
- Frenos
- Embrague
- Distribución
- Electricidad
- *(otros, lista ampliable)*

**Beneficio:** permite que el reporte de **"Servicios más realizados"** (sección 9) se genere **automáticamente**, sin tener que deducirlo del diagnóstico o del problema informado.

---

## 6. Estados del trabajo

Estados disponibles:

1. **Pendiente**
2. **En reparación**
3. **Esperando repuestos**
4. **Terminado**
5. **Entregado**

**Reglas de UX:**

- El estado se modifica desde la ficha del vehículo **con un solo toque**.
- **No** se abrirán formularios para cambiar el estado.

---

## 7. Lista de Trabajos

La aplicación mostrará todos los vehículos registrados como tarjetas.

### Cada tarjeta muestra

- Patente
- Marca
- Modelo
- Estado actual
- Fecha de ingreso

**Comportamiento:**

- Al tocar una tarjeta se abre el **detalle del trabajo**.
- Desde el detalle se editan **únicamente los datos necesarios**.

---

## 8. Finanzas

La aplicación tendrá **dos registros independientes**: Ingresos y Egresos.

### 8.1 Ingresos

Registrar:

- Monto
- Concepto
- Método de pago
- Fecha

### 8.2 Egresos

Registrar:

- Monto
- Categoría
- Concepto
- Fecha

**Categorías sugeridas:**

- Repuestos
- Herramientas
- Combustible
- Servicios
- Otros

---

## 9. Cobros

Cuando un vehículo cambia al estado **"Entregado"**, podrá registrarse el cobro.

### Métodos de pago

- Efectivo
- Transferencia
- Tarjeta
- Mercado Pago
- Pago mixto

**Regla:** el ingreso queda registrado **automáticamente** dentro del módulo de Finanzas.

---

## 10. Reportes

Información resumida que mostrará la aplicación:

**Diario**
- Caja del día
- Ingresos del día
- Gastos del día
- Ganancia del día

**Mensual**
- Ingresos del mes
- Gastos del mes
- Ganancia del mes

**Operativo**
- Trabajos realizados
- Vehículos atendidos
- Servicios más realizados *(alimentado por el campo "Servicio realizado")*

### 10.1 Exportación

Los reportes podrán exportarse como:

- PDF
- Excel

---

## 11. Búsqueda

Se podrá buscar trabajos por:

- Patente
- Cliente

---

## 12. Funcionalidades opcionales (post-MVP)

No forman parte del MVP, pero podrán agregarse después:

- Guardar fotografías del vehículo.
- Adjuntar archivos.
- Modo oscuro.
- Copias de seguridad.
- Sincronización en la nube.

---

## 13. Funcionalidades excluidas

La aplicación **NO** incluirá:

- Control de stock.
- Facturación electrónica.
- Multiusuario.
- Roles y permisos.
- Agenda.
- Recordatorios automáticos.
- Notificaciones.
- Integraciones con servicios externos.

---

## 14. Requisitos de experiencia de usuario (UX)

| Requisito | Métrica |
|-----------|---------|
| Registrar un nuevo trabajo | < 1 minuto |
| Crear ingresos y gastos | < 30 segundos |
| Uso sin conocimientos técnicos | Obligatorio |
| Mínima cantidad de pantallas y toques | Prioritario |
| Simplicidad sobre cantidad de funciones | Prioritario |

---

## 15. Objetivo del MVP

Que cualquier mecánico pueda controlar, desde una **única aplicación simple, rápida y fácil de usar**:

- Qué vehículos tiene en el taller.
- En qué estado se encuentra cada trabajo.
- Cuánto dinero ingresó.
- Cuánto dinero gastó.
- Cuál fue la ganancia del día y del mes.

---

## 16. Criterios de aceptación (resumen verificable)

- [ ] El Dashboard muestra los 6 indicadores y los 3 accesos rápidos.
- [ ] Un trabajo se crea en menos de 1 minuto con los campos obligatorios.
- [ ] El estado del trabajo cambia con un solo toque, sin formulario.
- [ ] La lista de trabajos muestra tarjetas con los 5 datos definidos.
- [ ] Al pasar a "Entregado" se registra el cobro y se refleja en Finanzas automáticamente.
- [ ] Los reportes diarios y mensuales calculan ganancia (ingresos − gastos).
- [ ] "Servicios más realizados" se genera desde el campo "Servicio realizado".
- [ ] Exportación de reportes a PDF y Excel.
- [ ] Búsqueda por patente y por cliente.
