# Reglas de ProGuard/R8 para builds de release.
# Ver https://developer.android.com/build/shrink-code para la sintaxis general.

# Room: genera código en tiempo de compilación, no necesita reglas especiales
# más allá de las que trae por defecto la consumer-rules.pro de la librería.

# Moshi (reflection-based adapters de las clases generadas por moshi-kotlin-codegen)
-keepclasseswithmembers class * {
    @com.squareup.moshi.* <methods>;
}
-keep @com.squareup.moshi.JsonClass class *

# Nimbus JOSE+JWT
-keep class com.nimbusds.** { *; }
-dontwarn com.nimbusds.**
