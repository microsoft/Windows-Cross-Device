-verbose

# -libraryjars ../app/libs
# -dontpreverify
# -dontobfuscate
# -dontshrink
# -dontoptimize

# keep class BuildConfig
-keep public class **.BuildConfig { *; }

# keep class members of R
-keepclassmembers class **.R$* {public static <fields>;}

# keep all public and protected method names,
# which could be used by Java reflection.
-keepclassmembernames class * {
    public protected <methods>;
}