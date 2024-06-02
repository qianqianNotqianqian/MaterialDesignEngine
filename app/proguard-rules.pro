
-dontskipnonpubliclibraryclassmembers
-ignorewarnings

-keep class androidx.appcompat.widget.SearchView { *; }

-keep class androidx.appcompat.widget.Toolbar { *** mMenuView; }
-keep class androidx.appcompat.widget.ActionMenuView { *** mPresenter; }
-keep class androidx.appcompat.widget.ActionMenuPresenter { *** mOverflowButton; }

-keep public class * extends androidx.core.view.ActionProvider {
    public <init>(android.content.Context);
}

-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

-keep class kotlin.** { *; }
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**
-keepclassmembers class **$WhenMappings {
    <fields>;
}
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}
-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
    static void checkParameterIsNotNull(java.lang.Object, java.lang.String);
}

-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}
# OkHttp3
-keep class okhttp3.** { *; }
-dontwarn okhttp3.**

-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}

# Keep the MPAndroidChart library classes
-keep class com.github.mikephil.charting.** { *; }

# Keep the MPAndroidChart Renderer classes
-keep class com.github.mikephil.charting.renderer.** { *; }

# Keep the MPAndroidChart Utils classes
-keep class com.github.mikephil.charting.utils.** { *; }

# Keep the MPAndroidChart Highlight classes
-keep class com.github.mikephil.charting.highlight.** { *; }

# Keep the MPAndroidChart Formatters classes
-keep class com.github.mikephil.charting.formatter.** { *; }

# Keep the MPAndroidChart Interfaces
-keep interface com.github.mikephil.charting.interfaces.datasets.** { *; }

# Keep the Lottie library classes
-keep class com.airbnb.lottie.** { *; }

# Keep the Lottie network classes
-keep class com.airbnb.lottie.network.** { *; }

# Keep the Lottie parser classes
-keep class com.airbnb.lottie.parser.** { *; }

# Keep the Lottie utils classes
-keep class com.airbnb.lottie.utils.** { *; }

# Keep the Lottie model classes
-keep class com.airbnb.lottie.model.** { *; }

# XUI 库
-keep class com.xuexiang.xui.widget.** { *; }
-keep class com.xuexiang.xui.adapter.** { *; }
-keep class com.xuexiang.xui.utils.** { *; }

# RxJava 和 RxAndroid 库
-keep class io.reactivex.** { *; }
-dontwarn io.reactivex.**

# RxBinding 库
-keep class com.jakewharton.rxbinding2.** { *; }
-dontwarn com.jakewharton.rxbinding2.**

# RxUtil2 库
-keep class com.xuexiang.xutil.** { *; }

# 方法名等混淆指定配置
#-obfuscationdictionary proguard-dict.txt
# 类名混淆指定配置
#-classobfuscationdictionary proguard-dict.txt
# 包名混淆指定配置
#-packageobfuscationdictionary proguard-dict.txt
