# Tight R8 stripping and optimization rules for AnyRemote

# Enable aggressive code optimizations and flat package structure
-repackageclasses 'a'
-allowaccessmodification

# Keep essential attributes for Android/Kotlin/Compose runtime
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# Keep our core classes intact to avoid class verification issues
-keep class com.anyremote.remote.** { *; }

# Keep compose runtime classes to prevent layout verification issues
-keep class androidx.compose.runtime.** { *; }

# Strip all android.util.Log calls from the release build and external libraries
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
    public static int w(...);
    public static int e(...);
}

# Kotlinx Serialization Rules
-keepattributes *Annotation*, InnerClasses
-keep,allowobfuscation,allowshrinking class kotlinx.serialization.internal.**
-keep,allowobfuscation,allowshrinking class * implements kotlinx.serialization.KSerializer {
    <fields>;
    <init>(...);
    <methods>;
}
-keepclassmembers class * {
    @kotlinx.serialization.Serializable *;
}

# Keep our specific Data Classes for the RemoteDatabase
-keep class com.anyremote.remote.data.RemoteDatabase$Brand { *; }
-keep class com.anyremote.remote.data.RemoteDatabase$DeviceType { *; }
-keep class com.anyremote.remote.data.RemoteDatabase$DatabaseContainer { *; }
