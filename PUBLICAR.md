# Publicar una versión en Google Play

Procedimiento completo, de código a Play Console. Para el keystore y la configuración de
firma, ver `RELEASE.md`. Para el estado del proyecto, `README-CONTEXTO.md`.

---

## 1. Antes de compilar

```bash
git status --short          # el árbol debería estar limpio
```

Si hay cambios sin commitear, el AAB sale de ellos y el versionCode queda sin un commit que
lo respalde. Commitear primero.

Revisar si cambió el esquema de la base:

```bash
grep -n "version = " app/src/main/java/com/tallerapp/data/local/TallerDatabase.kt
```

Si subió respecto de la versión publicada, **tiene que haber una migración nueva** en
`Migraciones.kt` y estar registrada en `addMigrations(...)`. Room no avisa en tiempo de
compilación: la app crashea al abrir.

`TallerDatabase.VERSION_ESQUEMA` tiene que acompañar a `version`, porque la copia de
seguridad lo guarda en el archivo exportado.

## 2. Subir la versión

En `app/build.gradle.kts`:

```kotlin
versionCode = 9       // +1 SIEMPRE, aunque la anterior se haya rechazado
versionName = "1.4"   // cambia solo si el usuario ve algo nuevo
```

**El versionCode se quema aunque Play rechace el AAB.** Si subís el 9 y falla la validación,
el siguiente es 10, no 9 de nuevo.

`versionName` es lo que se ve en Ajustes de Android. Subirlo cuando hay funciones nuevas;
dejarlo igual si fue solo un arreglo interno.

## 3. Compilar

```bash
JAVA_HOME="C:/Program Files/Android/Android Studio/jbr" ./gradlew :app:testDebugUnitTest :app:bundleRelease --no-daemon
```

Los tests van en el mismo comando a propósito: `assembleDebug` solo no los corre, y ya pasó
que `Fakes.kt` quedara sin compilar durante varias tandas sin que nadie se enterara.

Sale en `app/build/outputs/bundle/release/app-release.aab`.

Verificar qué quedó adentro:

```bash
unzip -p app/build/outputs/bundle/release/app-release.aab BUNDLE-METADATA/com.android.tools.build.libraries/dependencies.pb | grep -a -o "billingclient[^ ]*"
```

## 4. Probar antes de subir

Compilar no es probar. Esta app ya acumuló seis tandas "que compilaban" y no abrían.

```bash
export PATH="$PATH:/c/Users/Fran9/AppData/Local/Android/Sdk/platform-tools"
JAVA_HOME="C:/Program Files/Android/Android Studio/jbr" ./gradlew :app:assembleDebug --no-daemon
adb -s emulator-5554 install -r app/build/outputs/apk/debug/app-debug.apk
adb -s emulator-5554 logcat -c
adb -s emulator-5554 shell "monkey -p com.tallerapp -c android.intent.category.LAUNCHER 1"
adb -s emulator-5554 logcat -d -t 300 | grep -iE "FATAL|Room|IllegalState"
```

Suele haber dos dispositivos conectados, por eso el `-s`. En Git Bash, las rutas del
dispositivo necesitan `MSYS_NO_PATHCONV=1` adelante.

### Si cambió el esquema de la base

Instalar encima de la versión anterior no alcanza si la base está vacía: hay que migrar
**con datos**. El camino que funcionó:

```bash
# 1. Sacar la base actual del dispositivo
adb -s emulator-5554 exec-out run-as com.tallerapp cat databases/tallerapp.db > "C:/Users/Fran9/AppData/Local/Temp/db.sqlite"

# 2. (opcional) agregarle filas del caso raro con sqlite3 desde Python

# 3. Devolverla
adb -s emulator-5554 shell am force-stop com.tallerapp
adb -s emulator-5554 push "C:/Users/Fran9/AppData/Local/Temp/db.sqlite" /data/local/tmp/db.sqlite
adb -s emulator-5554 shell "run-as com.tallerapp sh -c 'rm -f databases/tallerapp.db-wal databases/tallerapp.db-shm; cp /data/local/tmp/db.sqlite databases/tallerapp.db'"

# 4. Instalar la nueva, abrir, y volver a sacarla para comparar
```

Comprobar después: `pragma user_version`, las columnas de las tablas tocadas, los nombres de
los índices (tienen que coincidir con los de las entidades) y que no se haya perdido ninguna
fila.

`adb shell cat` corrompe binarios en Windows: usar siempre `exec-out`.

El release **no es debuggable**, así que `run-as` no funciona sobre él. Todo esto va con el
build de debug.

## 5. Subir a Play Console

`Prueba y lanza → <canal> → Crear una versión`

Si el AAB ya se subió a otro canal, usar **"Agregar desde la biblioteca"** en vez de subir el
archivo de nuevo.

| Campo | Qué poner |
|---|---|
| Nombre de la versión | `9 (1.4) — <qué trae>`; no lo ve el usuario |
| Notas de la versión | Bloque `<es-419>`, máximo 500 caracteres |
| Países y regiones | Solo en cerrada, abierta y producción; la interna no los pide |

Las notas van en español neutro, orientadas a lo que el usuario nota. Nada de nombres de
clases ni de "se arregló el bug de los botones duplicados": en una ficha pública eso dice que
algo estaba roto.

Para **promover** una versión de un canal a otro: `Versiones → Promover actualización`. Se
lleva el AAB y las notas.

## 6. Canales

| Canal | Para qué | Países |
|---|---|---|
| Interna | Hasta 100 verificadores, publica en minutos | No pide |
| Cerrada (Alpha) | **Requisito para pedir producción**: 12 verificadores, 14 días | Sí |
| Producción | Todos | Sí |

Los 12 tienen que estar aceptados **de forma continua** durante los 14 días. El reloj no
corre con 11, y se interrumpe si alguien se sale. Conviene invitar 15.

Vínculo de aceptación (no es el de la ficha):

```
https://play.google.com/apps/testing/com.tallerapp
```

## 7. Suscripciones

No pasan por "enviar a revisión" y no se ven en la lista de cambios: viven en
`Monetizar con Play → Productos → Suscripciones`.

| Qué | ID |
|---|---|
| Suscripción | `pro` |
| Planes base | `mensual`, `anual` |
| Oferta en cada plan | `prueba-14-dias`, 14 días, solo usuarios nuevos |

Los IDs están **hardcodeados** en `FacturacionPlay.kt` y no se pueden cambiar después de
crearlos. Los planes base y las ofertas tienen que quedar **Activos**: en borrador,
`queryProductDetails` devuelve el producto sin ofertas y la pantalla de Planes sale vacía.

Para probar una compra sin pagar, la cuenta tiene que estar en
`Play Console → (todas las apps) → Configuración → Pruebas de licencias`. Si no está, **el
cobro es real**. En la pantalla de pago tiene que leerse "Tarjeta de prueba, siempre
aprueba".

## 8. Errores frecuentes

| Mensaje | Qué pasó |
|---|---|
| `El código de versión N ya se ha usado` | Subir el versionCode, aunque ese intento haya fallado |
| `Tu app usa la versión X de la Biblioteca de Facturación` | Play subió el mínimo; actualizar `billing-ktx` |
| `Producto pro sin ofertas` (logcat) | Plan base u oferta en borrador, o el país no está en Regiones |
| `El artículo no está disponible para comprar` | No propagó todavía, la cuenta no es verificadora, o se instaló por `adb` |
| App cierra al abrir tras actualizar | Migración faltante o índice con nombre distinto al de la entidad |

## 9. Después de publicar

Actualizar `README-CONTEXTO.md`: qué versión quedó en qué canal, qué falta y qué se aprendió.
Ese archivo es el que levanta el contexto en un chat nuevo, y está en `.gitignore`.
