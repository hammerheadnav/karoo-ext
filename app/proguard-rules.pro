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

-keep class io.hammerhead.karooext.** { *; }

# Kotlin reflection (objectInstance/createInstance on Class.forName) reads Metadata
-keep class kotlin.Metadata { *; }

# Strip debug/verbose logging calls from release
-assumenosideeffects class timber.log.Timber {
    public static *** d(...);
    public static *** v(...);
}
-assumenosideeffects class timber.log.Timber$Forest {
    public *** d(...);
    public *** v(...);
}