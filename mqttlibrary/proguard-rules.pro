# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in E:\DevelopTools\AndroidTools\sdk/tools/proguard/proguard-android.txt
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

-optimizationpasses 5          # 指定代码的压缩级别
-dontusemixedcaseclassnames   # 是否使用大小写混合
-dontpreverify           # 混淆时是否做预校验
-verbose                # 混淆时是否记录日志

-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*  # 混淆时所采用的算法

-dontwarn android.support.**

-dontwarn org.eclipse.paho.client.**
-keep class org.eclipse.paho.client.mqttv3.**{*;}

-keep public class android.aracy.support.mqtt.BaseRunnable {*;}

-keep public class android.aracy.support.mqtt.MQTTConstant {*;}

-keep public class android.aracy.support.mqtt.MQTTManager {*;}

-keep class *.R

-keepclasseswithmembers class **.R$* { public static <fields>;}
