# proguard-rules.pro
-dontobfuscate

-dontwarn kotlinx.**

-keep class org.jetbrains.skia.** { *; }
-keep class org.jetbrains.skiko.** { *; }

-dontoptimize
-dontshrink
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontpreverify
-verbose
