# ---------------------------------------------------------------
# Mailio release rules
# Libraries (Retrofit, OkHttp, kotlinx.serialization, Room, Hilt,
# Firebase, Play Services, Coil) ship their own consumer rules.
# We only keep our own code intact, so nothing reflective breaks.
# ---------------------------------------------------------------

# Keep readable stack traces (crash reports stay useful). Shrinking still removes unused code.
-dontobfuscate
-keepattributes SourceFile,LineNumberTable,Signature,InnerClasses,EnclosingMethod,*Annotation*,Exceptions

# All app classes: DTOs, Retrofit interface, workers, receivers, Hilt entry points
-keep class com.burootro.mailio.** { *; }

# kotlinx.serialization
-keepclassmembers @kotlinx.serialization.Serializable class ** {
    *** Companion;
    kotlinx.serialization.KSerializer serializer(...);
}
-dontnote kotlinx.serialization.**

# Retrofit + coroutines (suspend functions)
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

# Harmless missing optional classes
-dontwarn org.bouncycastle.**
-dontwarn org.conscrypt.**
-dontwarn org.openjsse.**
-dontwarn javax.annotation.**
-dontwarn kotlinx.serialization.internal.ClassValueReferences
-dontwarn com.google.errorprone.annotations.**
