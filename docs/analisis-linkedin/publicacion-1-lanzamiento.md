# Publicación 1 — lanzamiento de Mis Finanzas

> Primer posteo sobre la app. Presenta qué es, para quién y dónde bajarla. La historia del bug va
> después, en `publicacion-2-bug.md`, cuando la gente ya conozca la app.

---

## Texto

Casi todas las apps de finanzas te piden lo mismo antes de dejarte anotar un gasto: creá una cuenta, aceptá los términos, dejanos sincronizar tus datos.

Yo solo quería saber cuánto entró, cuánto salió y quién me debe. Así que hice Mis Finanzas, y ya está en Google Play.

Sirve para llevar las cuentas de tu casa y las de tu negocio en el mismo lugar. No tiene registro ni servidor propio: la instalás y anotás. Tus datos quedan en tu teléfono, y si cambiás de celular los pasás con una copia de seguridad.

Qué hace:
→ Ingresos y gastos por categoría y cuenta (efectivo, banco, Mercado Pago)
→ Quién te debe, con fecha para cobrar
→ Metas de ahorro y gastos fijos que se cargan solos cada mes
→ Reportes del mes con gráficos
→ Agenda de tareas y turnos

Y si tenés un negocio: productos con escáner de código de barras, clientes y proveedores, facturas en PDF que descuentan el stock, y varios negocios por separado.

Uso personal, gratis. Uso comercial, con suscripción. Sin publicidad en ninguno de los dos.

La hice en Kotlin con Jetpack Compose, con asistencia de IA, y la probé en teléfonos reales: el primer bug importante me lo encontró un familiar. Esa historia la cuento en otro posteo.

Si la probás, me sirve muchísimo que me digas qué te faltó.

👉 https://play.google.com/store/apps/details?id=com.tallerapp

#Android #Kotlin #Emprendedores #FinanzasPersonales #Pymes

---

## Imagen

`imagenes/lanzamiento.png` (1200 × 1500): ícono, nombre y tres capturas de la 1.4.1 con datos de
demo (Inicio, Movimientos, Reportes).

## De dónde sale cada afirmación

| Afirmación | Evidencia |
|---|---|
| Sin registro ni servidor propio | No hay login ni backend; los datos viven en la base local de Room |
| Copia de seguridad para cambiar de celular | `core/backup/CopiaSeguridad.kt`, exporta e importa un `.zip` |
| Efectivo, banco, Mercado Pago | Cuentas por defecto de `DatosIniciales` |
| Gastos fijos que se cargan solos | Recurrentes, se generan al abrir la app cada mes |
| Escáner, clientes y proveedores, facturas que descuentan stock, varios negocios | Funciones Pro, ver `PLANES.md` |
| Personal gratis, comercial con suscripción | `PLANES.md` y Play Billing |
| Sin publicidad | Ninguna dependencia de anuncios en `app/build.gradle.kts` |
| En Google Play | Producción desde el 12/11/2026; 1.4.1 desde el 23/11 |

**Evitado a propósito:** "mi primera app" (no está confirmado) y cualquier frase del tipo "no usa
internet", que es falsa: la app usa internet para Play Billing y las actualizaciones.

## Sobre el link

LinkedIn suele mostrar menos los posteos con links externos; mucha gente los pone en el primer
comentario. Para un lanzamiento, donde lo que importa son las descargas, va en el texto.
