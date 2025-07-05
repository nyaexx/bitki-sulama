package com.example.bitkisulama

import android.app.Application
import com.google.android.material.color.DynamicColors
import android.util.Log

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Tüm uygulama için Dynamic Colors'ı etkinleştir
        DynamicColors.applyToActivitiesIfAvailable(this)

        Log.d("MyApplication", "Dynamic Colors tüm uygulama için etkinleştirildi")
    }
}