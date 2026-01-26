package gndsalih.nyaexx.bitkisulama

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val window = window
            val decorView = window.decorView
            val wic = androidx.core.view.WindowInsetsControllerCompat(window, decorView)

            // Arka plan rengini kontrol et (Dinamik tema dahil)
            val typedValue = android.util.TypedValue()
            theme.resolveAttribute(com.google.android.material.R.attr.colorSurface, typedValue, true)
            val colorSurface = typedValue.data

            // Rengin aydınlık mı karanlık mı olduğunu hesapla
            val isLightBackground = androidx.core.graphics.ColorUtils.calculateLuminance(colorSurface) > 0.5

            // Eğer arka plan aydınlıksa (beyazsa) ikonları siyah yap, karanlıksa beyaz yap
            wic.isAppearanceLightStatusBars = isLightBackground
        }

        Handler().postDelayed({
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }, 600)
    }
}