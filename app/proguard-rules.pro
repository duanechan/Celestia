# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Keep application entry points
-keep class com.coco.celestia.** { *; }

# Jetpack Compose Rules
-keep class androidx.compose.** { *; }
-keep class kotlin.Metadata { *; }
-keepclassmembers class * {
    @androidx.compose.runtime.Composable <methods>;
}

# Firebase Rules
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**

# MPAndroidChart Rules
-keep class com.github.mikephil.charting.** { *; }
-dontwarn com.github.mikephil.charting.**

# Coil (for image loading in Jetpack Compose)
-keep class coil.** { *; }
-dontwarn coil.**

# AndroidX Lifecycle Components (ViewModel, LiveData)
-keep class androidx.lifecycle.** { *; }
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}

# Navigation Components
-keep class androidx.navigation.** { *; }
-dontwarn androidx.navigation.**

# Kotlin Reflection
-keepclassmembers class kotlin.reflect.** { *; }
-dontwarn kotlin.reflect.jvm.internal.impl.**

# Mail API Rules (javax.mail)
-keep class javax.mail.** { *; }
-keep class javax.activation.** { *; }
-dontwarn javax.mail.**
-dontwarn javax.activation.**

# Material3 UI Components
-keep class com.google.android.material.** { *; }
-dontwarn com.google.android.material.**

# General Android Rules
-keep class android.** { *; }
-dontwarn android.**

# Keep Parcelable Classes (for data transfer between Activities/Fragments)
-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# Remove Debug Information (for additional security)
-dontnote *
-dontwarn *

# Prevent logging from leaking in release builds
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
    public static int i(...);
    public static int w(...);
    public static int e(...);
}

# JavaMail and JavaBeans Activation Framework
-keep class com.sun.mail.** { *; }
-keep class com.sun.activation.** { *; }
-dontwarn com.sun.mail.**
-dontwarn com.sun.activation.**

# If using Jakarta Mail
-keep class jakarta.mail.** { *; }
-keep class jakarta.activation.** { *; }
-dontwarn jakarta.mail.**
-dontwarn jakarta.activation.**
