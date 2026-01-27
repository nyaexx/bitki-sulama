
-dontwarn
-dontnote

-dontpreverify
-overloadaggressively


-keepattributes *Annotation*
-keep class com.google.android.material.** { *; }
-keep interface com.google.android.material.** { *; }

-keep class gndsalih.nyaexx.bitkisulama.SavedDevice { *; }


-keep class androidx.appcompat.** { *; }
-keep interface androidx.appcompat.** { *; }


-keep class androidx.constraintlayout.** { *; }


-keep class android.bluetooth.** { *; }
-keep interface android.bluetooth.** { *; }

-keep class * extends android.app.Activity { *; }
-keep class * extends android.app.Service { *; }
-keep class * extends android.content.BroadcastReceiver { *; }
-keep class * extends android.content.ContentProvider { *; }

-keep class **.R {
    <fields>;
}
-keep class **.R$* {
    <fields>;
}

-keep class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

