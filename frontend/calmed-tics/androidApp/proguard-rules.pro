# ProGuard / R8 rules for androidApp (CalmEd Android Application)

# ==============================================================================
# Stack trace readability
# ==============================================================================
# Retain line numbers and file names for readable stack traces in crash logs
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Retain annotations, generic signatures, inner classes, and enclosing methods
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod,Exceptions

# ==============================================================================
# kotlinx.serialization
# ==============================================================================
-keepattributes RuntimeVisibleAnnotations,AnnotationDefault
-dontnote kotlinx.serialization.**
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.calmed.**$$serializer { *; }
-keepclassmembers class com.calmed.** {
    *** Companion;
}
-keepclasseswithmembers class com.calmed.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# ==============================================================================
# Ktor
# ==============================================================================
-dontwarn io.ktor.**
-keep class io.ktor.client.engine.** { *; }

# ==============================================================================
# Koin
# ==============================================================================
-keep class org.koin.** { *; }
-dontwarn org.koin.**

# ==============================================================================
# Room
# ==============================================================================
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class * { *; }
-dontwarn androidx.room.paging.**

# ==============================================================================
# AndroidX Media3
# ==============================================================================
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**

# ==============================================================================
# Google Play Billing
# ==============================================================================
-keep class com.android.billingclient.** { *; }
-keep class com.android.vending.billing.** { *; }

# ==============================================================================
# Coil
# ==============================================================================
-dontwarn coil3.**
-keep class coil3.** { *; }

# ==============================================================================
# Google Cast Framework
# ==============================================================================
# CastOptionsProvider is loaded reflectively by the Cast Framework from the
# AndroidManifest meta-data entry OPTIONS_PROVIDER_CLASS_NAME.
-keep class com.calmed.calmedtics.cast.CastOptionsProvider { public <init>(); *; }


