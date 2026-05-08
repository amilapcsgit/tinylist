# Keep data classes
-keep class com.cyberlist.neonlist.data.** { *; }

# Keep serialization
-keepattributes *Annotation*, InnerClasses
-keep,includedescriptorclasses class com.cyberlist.neonlist.**$$serializer { *; }

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *

# Remove logs in release
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
}
