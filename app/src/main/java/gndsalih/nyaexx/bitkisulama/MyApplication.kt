package gndsalih.nyaexx.bitkisulama

import android.app.Application
import android.util.Log
import com.google.android.material.color.DynamicColors

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        DynamicColors.applyToActivitiesIfAvailable(this)

        Log.d("MyApplication", "Dynamic Colors tüm uygulama için etkinleştirildi")
    }
}