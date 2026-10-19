# JSch rules
-keep class com.jcraft.jsch.** { *; }
-dontwarn com.jcraft.jsch.**

# Tink rules
-keep class com.google.crypto.tink.** { *; }
-dontwarn com.google.crypto.tink.**

# Hilt rules
-keep,allowobfuscation,allowshrinking interface dagger.hilt.internal.GeneratedEntryPoint
-keep,allowobfuscation,allowshrinking interface dagger.hilt.internal.GeneratedComponent

# Room
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**

# Apache Commons Net
-keep class org.apache.commons.net.** { *; }
-dontwarn org.apache.commons.net.**
