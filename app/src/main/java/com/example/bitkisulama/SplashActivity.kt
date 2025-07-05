package com.example.bitkisulama

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.os.Handler

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)  // Burada splash ekranı layout'unu ekleyeceğiz

        // Handler kullanarak 700 ms sonra MainActivity'ye geçiş
        Handler().postDelayed({
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()  // Splash screen kapatılır
        }, 1000)  // bekleme süresi (ms)
    }
}
