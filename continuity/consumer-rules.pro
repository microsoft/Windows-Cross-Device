-verbose

# -libraryjars ../app/libs
# -dontpreverify
# -dontobfuscate
# -dontshrink
# -dontoptimize

# keep class BuildConfig
-keep public class com.microsoft.crossdevicesdk.continuity.BuildConfig { *; }

# keep class members of R
-keepclassmembers class com.microsoft.crossdevicesdk.continuity.R$* {public static <fields>;}

# keep all public and protected method names,
# which could be used by Java reflection.
-keepclassmembernames class com.microsoft.crossdevicesdk.continuity.** {
    public protected <methods>;
}