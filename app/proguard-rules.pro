# ==============================================================================
# ProGuard / R8 Optimization & Obfuscation Rules
# ==============================================================================

# Preserve source file & line numbers for de-obfuscation in crash reports / stack traces
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Preserve standard reflection and annotation attributes
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod,Exceptions

# ------------------------------------------------------------------------------
# Kotlin Coroutines & Standard Library
# ------------------------------------------------------------------------------
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.coroutines.** {
    volatile <fields>;
}

# ------------------------------------------------------------------------------
# Jetpack Compose Internals
# ------------------------------------------------------------------------------
-keep class androidx.compose.runtime.** { *; }
-keepclassmembers class * {
    @androidx.compose.runtime.Composable *;
}

# ------------------------------------------------------------------------------
# Moshi & Retrofit Serialization (Reflection & Codegen)
# ------------------------------------------------------------------------------
-keepclasseswithmembers class * {
    @com.squareup.moshi.Json <fields>;
}
-keepclasseswithmembers class * {
    @com.squareup.moshi.JsonQualifier <fields>;
}
-keep class * extends com.squareup.moshi.JsonAdapter { *; }
-keepclassmembers enum * { *; }

-keepattributes EnclosingMethod
-keepclassmembers,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# ------------------------------------------------------------------------------
# Room Database
# ------------------------------------------------------------------------------
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }
-dontwarn androidx.room.paging.**

# ------------------------------------------------------------------------------
# Firebase & Google Play Services
# ------------------------------------------------------------------------------
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**

