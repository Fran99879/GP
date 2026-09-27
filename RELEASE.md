# Build de release firmado (Play Store)

La configuración de firma **ya está lista** en `app/build.gradle.kts`. Falta un único paso
manual: generar tu keystore. Lo hacés vos porque implica elegir contraseñas — no deben pasar
por el chat ni quedar en el repo.

---

## 1. Generar el keystore (una sola vez, y guardalo bien)

Desde `C:\Proyectos\GP`:

```bash
"C:\Program Files\Android\Android Studio\jbr\bin\keytool.exe" -genkeypair -v -keystore mis-finanzas-release.jks -keyalg RSA -keysize 2048 -validity 10000 -alias mis-finanzas
```

Te va a pedir una contraseña y algunos datos (nombre, organización, país). Anotá la
contraseña y el alias.

> ⚠️ **Si perdés este archivo o la contraseña, no podés volver a publicar actualizaciones**
> de la app con la misma identidad en Play. Guardá una copia fuera de la máquina (pendrive,
> nube privada). No lo subas al repo: `.gitignore` ya bloquea `*.jks`.

## 2. Crear `keystore.properties`

Copiá `keystore.properties.ejemplo` como `keystore.properties` (en la raíz del proyecto) y
completalo con tus datos:

```properties
storeFile=mis-finanzas-release.jks
storePassword=<la que pusiste>
keyAlias=mis-finanzas
keyPassword=<la de la clave>
```

Ese archivo está en `.gitignore`, así que no se sube. Si no existe, el proyecto **igual
compila** (genera un APK sin firmar), así que nadie queda bloqueado.

## 3. Generar el artefacto

Para **Play Store** se sube un **AAB** (Android App Bundle), no un APK:

```bash
./gradlew :app:bundleRelease
```

Queda en `app/build/outputs/bundle/release/app-release.aab`.

Para probar en un teléfono por fuera de Play (APK instalable):

```bash
./gradlew :app:assembleRelease
```

Queda en `app/build/outputs/apk/release/`.

---

## Pendientes antes de subir

- [x] **`targetSdk` = 36** (era 34, que bloqueaba la subida). Al pasar a 35+ Android fuerza
  *edge-to-edge*: la app dibuja debajo de las barras del sistema. Se agregó `enableEdgeToEdge()`
  y el control de apariencia de las barras en `TallerAppTheme` (sin eso los íconos de la barra
  de estado quedaban invisibles). Revisadas todas las pantallas en emulador, sin recortes.
  Confirmar en Play Console el nivel exigido hoy por si pidiera 37.
- [ ] **`versionCode`** se incrementa en **cada** subida a Play (hoy está en 1). El
  `versionName` es el que ve el usuario ("1.0").
- [x] **R8 activado** (`isMinifyEnabled` + `isShrinkResources`) con reglas en
  `app/proguard-rules.pro` (se conservan entidades Room y modelos de dominio, que se leen
  por reflexión). Probado en emulador: onboarding, alta de movimiento, Movimientos, Reportes,
  Me deben, Agenda, Negocios, Configuración y Calculadora, sin crashes.
- [x] **Licencias retiradas de la app móvil**: se eliminó el módulo `:licensesdk`, la pantalla
  de activación y los `BuildConfig` de licencia. La app abre siempre. El sistema Ed25519 queda
  solo para escritorio; en móvil la monetización va por **Play Billing** (Pro/Premium).
- [ ] Ficha de Play: ícono 512×512, gráfico de cabecera, capturas, descripción, política de
  privacidad (obligatoria) y el cuestionario de seguridad de datos.

## Estado actual verificado

- `:app:assembleRelease` ✅ → APK sin firmar **1,65 MB** (era 10,5 MB antes de R8)
- `:app:bundleRelease` ✅ → `app-release.aab` **3,9 MB** (era 10,2 MB), targetSdk 36 + R8

Ambos generados sin keystore, que es lo esperado hasta que completes el paso 2.
