# Novedades de la versión 1.4 (versionCode 9)

Qué recibe quien hoy tiene la **1.2** (`versionCode 6`), que es la que está publicada en la prueba
cerrada. Documento de trabajo para preparar la subida al Play Console.

> Estado al 05/10/2026: el código está listo y compila. **Todavía no se subió nada.** Los arreglos
> de interfaz del 03 y el 05/10 están en el árbol de trabajo sin commitear (ver sección 6).

---

## 1. Lo que más se va a notar

### Textos que se leían en vertical

En teléfonos con el **tamaño de pantalla** grande de Android (Ajustes → Pantalla → Tamaño de
pantalla), los nombres de las listas se partían letra por letra: un contacto llamado "Hermano" se
leía `Her / man / o`, y la fecha `04/08/2026` aparecía cortada en cinco renglones.

Pasaba en todas las listas y se vio en un teléfono real. Ahora el texto se corta con `…` cuando no
entra, y el importe y los botones de cada fila van en una columna que ocupa lo que el más ancho de
los dos, en vez de la suma.

Pantallas corregidas: Quién me debe · Movimientos · Productos · Clientes y proveedores · Facturas ·
Recurrentes · Cuentas · Categorías · Negocios · Agenda.

### Texto del sistema al 200 %

Con la letra grande de Android:

- El título de cada pantalla se iba a dos renglones y el segundo quedaba tapado por la barra: se
  leía "Movimient" y nada más. En las 23 pantallas.
- Las etiquetas de la barra inferior se partían: `Movimi / entos`.
- Las fechas del filtro de Movimientos se cortaban a mitad de número: `01/10/20` y abajo `26`, que
  se lee como otra fecha.

Todo eso quedó resuelto.

### Importes pegados

Pegar un importe copiado de un mensaje o de un resumen bancario guardaba mal el monto: `1.500`
quedaba como **$1,50**, y `1.200.000` como **$1,20**. Escribirlo a mano siempre funcionó bien; el
problema era solo al pegar. Ya está corregido, con la misma regla que usa la app de escritorio.

> **Ojo:** lo que ya se guardó mal sigue mal. La actualización no corrige movimientos viejos; hay
> que editarlos a mano.

### Arranque más rápido

La app ahora viaja con un *Baseline Profile*: Android compila de antemano el código del arranque en
vez de interpretarlo la primera vez.

Medido en 10 arranques en frío, antes y después:

| | Mediana | Peor caso |
|---|---|---|
| Sin el perfil | 1317 ms | 3536 ms |
| Con el perfil | 1260 ms | **1620 ms** |

La mediana baja poco. Lo que cambia de verdad es el peor arranque, que pasa de 3,5 s a 1,6 s: es el
de quien recién instaló la app.

---

## 2. Funciones que la 1.2 no tenía

Estas ya estaban en el repositorio pero nunca se publicaron:

| Función | Qué hace |
|---|---|
| **Clientes y proveedores** | Ficha por negocio con tipo, documento, teléfono, correo, dirección y nota. Se dan de alta solos al facturar |
| **Catálogo de productos** | Alta por código de barras con la cámara, foto, precio con descuento en %, stock y aviso de stock bajo |
| **Facturas** | Comprobante interno en PDF, con descuento por línea y general. Al emitir puede registrar el cobro como ingreso y descontar el stock |
| **Copia de seguridad** | Exportar e importar todos los datos en un `.zip`. El destino lo elige el usuario (Drive, WhatsApp, la tarjeta SD) |
| **Herramientas comerciales en el menú** | Bloque "Tu negocio" con Productos, Clientes, Proveedores y Facturas. Con candado en el plan Gratis: se ven, no se ocultan |
| **Ancho máximo del contenido** | En tablet y en horizontal el texto ya no cruza la pantalla de punta a punta |

---

## 3. Qué entra en el build

- `versionName 1.4`, `versionCode 9`.
- Baseline Profile en `app/src/release/generated/baselineProfiles/`. Entra solo al armar release;
  verificado dentro del APK como `assets/dexopt/baseline.prof`.
- `androidx.profileinstaller:profileinstaller:1.4.1`.
- El módulo `:baselineprofile` es de prueba: **no** entra en el APK ni en el AAB.

### Antes de subir

1. **Confirmar el `versionCode` en el Play Console.** Si alguna vez se subió un 7 u 8, Play rechaza
   el 9 o lo toma como repetido. Mirar qué número figura como el más alto y subir desde ahí.
2. Commitear lo que falta (sección 6).
3. `gradlew :app:bundleRelease` con `keystore.properties` en su lugar.

---

## 4. Texto para la ficha del Play Console

Para el campo de novedades (entra en el límite de 500 caracteres):

```
• Arreglado: en teléfonos con el tamaño de pantalla grande, los nombres y las fechas de las listas
  se cortaban y se leían en vertical.
• Arreglado: con el texto del sistema grande, los títulos y las etiquetas quedaban cortados.
• Arreglado: al pegar un importe con puntos de miles se guardaba mal.
• La app abre más rápido la primera vez.
• Nuevo: clientes y proveedores, catálogo de productos, facturas en PDF y copia de seguridad.
```

---

## 5. Qué NO entra

- Copia automática a Google Drive (sección 9 del `ROADMAP.md`): frenada a pedido. La copia manual
  en `.zip` sí entra.
- Códigos promocionales para regalar Pro (sección 10): se resuelve en el Play Console, no en la app.
- Historial y cuenta corriente de clientes (Fase 2) y estadísticas avanzadas (Fase 3).
- `WindowSizeClass` y grillas para tablet (punto 6 del roadmap) y navegación adaptativa con
  `NavigationRail` (punto 8).

---

## 6. Pendiente antes de compilar

Los arreglos de interfaz del 05/10 están **sin commitear**. Archivos tocados:

```
app/src/main/java/com/tallerapp/core/ui/components/FilaLista.kt        (nuevo)
app/src/main/java/com/tallerapp/features/agenda/AgendaScreen.kt
app/src/main/java/com/tallerapp/features/categorias/CategoriasScreen.kt
app/src/main/java/com/tallerapp/features/contactos/ContactosScreen.kt
app/src/main/java/com/tallerapp/features/cuentas/CuentasScreen.kt
app/src/main/java/com/tallerapp/features/deudas/DeudasScreen.kt
app/src/main/java/com/tallerapp/features/facturas/FacturasScreen.kt
app/src/main/java/com/tallerapp/features/finanzas/components/MovimientoRow.kt
app/src/main/java/com/tallerapp/features/negocios/NegociosScreen.kt
app/src/main/java/com/tallerapp/features/productos/ProductosScreen.kt
app/src/main/java/com/tallerapp/features/recurrentes/RecurrentesScreen.kt
```

### Lo que se probó y lo que no

Probado en el emulador a densidad 560 (el caso del teléfono donde apareció el problema):
**Quién me debe**, **Movimientos** y **Agenda**. A densidad normal (420) todo queda igual que antes.
Los tests unitarios pasan.

Sin ver corriendo a 560: **Productos**, **Clientes y proveedores**, **Facturas**, **Recurrentes**,
**Cuentas**, **Categorías** y **Negocios**. Las tres primeras están con candado en el emulador de
prueba, que corre el plan Gratis. El cambio es el mismo de una línea que ya se verificó en las
otras, pero conviene mirarlas antes de publicar.

---

## 7. Documentos relacionados

`ROADMAP.md` (puntos 4, 5 y 7 cerrados el 03/10) · `PLANES.md` · `RELEASE.md` · `PUBLICAR.md`
