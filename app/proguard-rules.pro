# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in the Android SDK tools.

# Keep Mikrotik API classes
-keep class com.mikhmon.android.core.api.** { *; }

# Keep data models for serialization
-keep class com.mikhmon.android.data.model.** { *; }

# Keep Room database classes
-keep class com.mikhmon.android.data.local.database.** { *; }

# Keep Hilt generated classes
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ComponentSupplier { *; }

# Keep Kotlinx Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

-keep,includedescriptorclasses class com.mikhmon.android.**$$serializer { *; }
-keepclassmembers class com.mikhmon.android.** {
    *** Companion;
}
-keepclasseswithmembers class com.mikhmon.android.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

# Retrofit
-dontnote retrofit2.Platform$Java8
-keepclassmembers,allowobfuscation class * {
    @retrofit2.http.* <methods>;
}
