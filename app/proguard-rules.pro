# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /home/work/adt/android-sdks/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

#org apache
-dontwarn org.apache.commons.**
-keep class org.apache.http.** { *; }
-dontwarn org.apache.http.**

#crashanalytics
-keepattributes SourceFile,LineNumberTable
-keep public @com.google.android.gms.common.util.DynamiteApi class * { *; }