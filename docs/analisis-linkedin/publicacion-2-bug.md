# Publicación 2 — la historia del bug (para después del lanzamiento)

> Segundo posteo, una o dos semanas después del lanzamiento. Basado en la versión C de
> `linkedin-project-analysis.md`, corregido con lo que pasó y con los datos verificados al
> 26/11/2026.

---

## Texto

Creía que tenía resuelto un problema de accesibilidad. Después vi una captura del teléfono de un familiar.

Mi app, Mis Finanzas, muestra listas: quién te debe, movimientos, productos. En ese teléfono los nombres se leían en vertical, una letra por renglón. Lo reproduje en el emulador con datos de prueba:

L
u
c
a
s

Ni el importe ni los botones llegaban a verse. Yo ya había probado con la letra del sistema al 200 % y había corregido títulos y etiquetas que se cortaban. El problema es que había medido la variable equivocada.

Ese teléfono no tenía la letra grande: tenía grande el tamaño de pantalla, otra opción de Android. En el emulador, subirlo bajó el ancho útil de 411 a 308 dp con el mismo panel. Mi código miraba la escala de la letra, así que nunca se iba a enterar.

Lo que me llevo:

→ El emulador te miente por omisión: te muestra solo la configuración que vos elegiste.

→ Antes de arreglar, reproducí con lo que tiene el usuario, no con lo que vos sospechás.

→ Arreglá el patrón, no la pantalla. En cada fila, el importe y los botones se quedaban con todo el ancho y al nombre no le quedaba nada. Lo resolví con dos piezas compartidas que usan las diez listas de la app. Y si igual no entra, el texto ahora se corta con "…" en vez de partirse letra por letra.

En la misma versión salió otro bug que solo aparece en el mundo real: pegar un importe copiado de un mensaje guardaba "1.500" como $1,50. Escribirlo a mano siempre anduvo. Y lo dije tal cual en las notas de la versión: la actualización no corrige lo que ya se había guardado mal.

Mis Finanzas está en producción en Google Play desde el 12 de noviembre. Lleva las cuentas personales y las de un negocio chico, sin cuenta y sin servidor propio: los datos quedan en el teléfono. Kotlin, Jetpack Compose y Room. La desarrollé con asistencia de IA.

¿Cuál fue el bug que más te enseñó y que nunca apareció en tu máquina?

#Android #Kotlin #JetpackCompose #Accesibilidad #DesarrolloDeSoftware

---

## Datos verificados que usa el texto

| Afirmación | De dónde sale |
|---|---|
| "L / u / c / a / s" | Captura de la 1.2 en el emulador con densidad 560 y datos de prueba (`imagenes/antes-despues.png`). No es un dato real de nadie |
| 411 dp → 308 dp | Panel de 1080 px: a 420 dpi da 411 dp; a 560 dpi, 308 dp |
| La causa era el tamaño de pantalla, no la letra | Reproducido con `font_scale 1.0` y `wm density 560` |
| Diez listas, dos piezas compartidas | `FilaLista` y `TextoFila` (`core/ui/components/FilaLista.kt`) |
| "1.500" se guardaba como $1,50 al pegar | `CampoMonto.filtrarMonto`, verificado en emulador antes y después del arreglo |
| No corrige datos viejos | Está en `NOVEDADES-1.4.md` |
| En producción desde el 12/11/2026 | Play Console: 1.4 (11) publicada el 12/11; 1.4.1 (13) el 23/11 |
| Sin cuenta y sin servidor propio | No hay login ni backend. Ojo: **sí** usa internet (ver abajo) |

## Lo que el texto evita a propósito

- **"El manifiesto no declara ni un permiso" / "no puede acceder a internet aunque quisiera"**, que
  aparece en `linkedin-project-analysis.md`. **Es falso para la app publicada**: el APK final pide
  `INTERNET`, `ACCESS_NETWORK_STATE` y `com.android.vending.BILLING`. El `AndroidManifest.xml` del
  proyecto no los declara, pero Play Billing y las actualizaciones in-app los agregan al compilar.
  Lo cierto es que los datos no salen del teléfono y la app solo habla con Google Play.
- **"Cero `fallbackToDestructiveMigration`"**: existe `fallbackToDestructiveMigrationOnDowngrade()`
  en `TallerDatabase.kt`. Para actualizaciones normales sí se usan las 14 migraciones a mano.
- **"Prueba cerrada"**: ya está en producción.
- **Nombres reales**: la captura original del teléfono tenía nombres de personas reales. No se usa.

## Para decidir antes de publicar

1. **La línea de la IA.** Dice "La desarrollé con asistencia de IA". Ajustala a cómo lo querés
   contar vos, pero no la sacaría: en el repositorio hay commits firmados como coautoría de Claude.
2. **El familiar.** No dice quién es. Si preferís "un usuario", cambialo.
3. **Imagen.** `imagenes/antes-despues.png` (1200 × 1500). Misma pantalla, mismos datos, tamaño de
   pantalla grande: a la izquierda la 1.2, a la derecha la 1.4.1. Se armó instalando la 1.2, cargando
   tres deudas de prueba y actualizando a la 1.4.1 encima, así que los datos son exactamente los
   mismos (y de paso prueba que la actualización los conserva).
