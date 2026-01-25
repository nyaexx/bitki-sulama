package gndsalih.nyaexx.bitkisulama

import android.app.Application
import android.content.Context
import android.os.Build
import com.google.android.material.color.DynamicColors

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        val sharedPref = getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val isDynamicEnabled = sharedPref.getBoolean("dynamic_colors", true)

        // Eğer Android 12+ ise VE kullanıcı ayarı kapatmadıysa uygula
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && isDynamicEnabled) {
            DynamicColors.applyToActivitiesIfAvailable(this)
        }
    }
}