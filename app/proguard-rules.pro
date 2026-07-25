# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

-keep class org.drinkless.tdlib.** { *; }
-keep class org.telegram.** { *; }
-dontwarn org.drinkless.tdlib.**
-dontwarn org.telegram.**