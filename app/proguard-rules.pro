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

# 保留应用主类
-keep class com.ljs.location2mqtt.MainActivity { *; }
-keep class com.ljs.location2mqtt.ltmService { *; }
-keep class com.ljs.location2mqtt.LocationForcegroundService { *; }
-keep class com.ljs.location2mqtt.BootBroadcastReceiver { *; }
-keep class com.ljs.location2mqtt.CheckPermissionsActivity { *; }
-keep class com.ljs.location2mqtt.DataBaseOpenHelper { *; }
-keep class com.ljs.location2mqtt.LogAdapter { *; }
-keep class com.ljs.location2mqtt.Contant { *; }
-keep class com.ljs.location2mqtt.Utils { *; }
-keep class com.ljs.location2mqtt.FlowLayout { *; }

# 保留Android组件
-keep class * extends android.app.Activity { *; }
-keep class * extends android.app.Service { *; }
-keep class * extends android.content.BroadcastReceiver { *; }
-keep class * extends android.app.Application { *; }

# 保留布局和资源
-keep class **.R$* { *; }

# 保留JSON相关
-keep class org.json.** { *; }

# 保留OkHttp相关
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

# 移除未使用的代码
-dontwarn android.support.**
-dontwarn androidx.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

# 优化
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-optimizationpasses 5
-allowaccessmodification