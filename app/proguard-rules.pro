# Reglas de R8 para el release.
#
# La mayoría de las librerías que usa la app (Compose, Room, Navigation) ya traen sus
# propias reglas empaquetadas, así que acá solo va lo específico del proyecto.

# --- Room ---
# Las entidades y sus campos se leen por reflexión al mapear filas: si R8 les cambia el
# nombre a los campos, las consultas generadas dejan de encontrarlos.
-keep class com.tallerapp.data.local.** { *; }

# --- Modelos de dominio ---
# Se serializan/mapean por nombre; conservarlos evita sorpresas y pesan muy poco.
-keep class com.tallerapp.domain.model.** { *; }

# --- Kotlin ---
-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod

# Deja fuera los logs de debug del binario final.
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
}
