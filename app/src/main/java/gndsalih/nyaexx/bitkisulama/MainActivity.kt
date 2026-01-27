package gndsalih.nyaexx.bitkisulama

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


class MainActivity : AppCompatActivity() {

    private lateinit var deviceListView: ListView
    private lateinit var emptyLayout: LinearLayout
    private val savedDevices = mutableListOf<SavedDevice>()

    private val GITHUB_URL = "https://github.com/nyaexx/bitki-sulama"
    private val GITHUB_URL_LATEST = "https://github.com/nyaexx/bitki-sulama/releases/latest"

    override fun onCreate(savedInstanceState: Bundle?) {
        // 1. Geçiş Animasyonları
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            overrideActivityTransition(OVERRIDE_TRANSITION_OPEN, android.R.anim.fade_in, android.R.anim.fade_out)
        } else {
            @Suppress("DEPRECATION")
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 2. UI Init
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        deviceListView = findViewById(R.id.deviceListView)
        emptyLayout = findViewById(R.id.emptyLayout) // activity_main.xml'de tanımladığın boş ekran layout'u
        val fabAdd = findViewById<FloatingActionButton>(R.id.fabAdd)

        setupStatusBarContrast()

        // 3. Buton Dinleyicileri
        findViewById<ImageButton>(R.id.github_button).setOnClickListener { showAboutDialog() }
        findViewById<ImageButton>(R.id.settings_button).setOnClickListener { showSettingsDialog() }

        fabAdd.setOnClickListener {
            val intent = Intent(this, AddDeviceActivity::class.java)
            startActivity(intent)
        }

        // 4. Listeye Tıklama (Doğrudan İzleme Ekranına Geçiş)
        deviceListView.setOnItemClickListener { _, _, position, _ ->
            val device = savedDevices[position]
            val intent = Intent(this, MonitoringActivity::class.java)
            intent.putExtra("device_address", device.address)
            intent.putExtra("device_name", device.name)
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
        deviceListView.setOnItemLongClickListener { _, _, position, _ ->
            val deviceToDelete = savedDevices[position]

            // Silme onayı için bir diyalog göster
            val dialog = Dialog(this)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setContentView(R.layout.dialog_delete_confirm) // Birazdan bu layout'u oluşturacağız
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

            val title = dialog.findViewById<TextView>(R.id.deleteTitle)
            val btnDelete = dialog.findViewById<Button>(R.id.btnDeleteConfirm)
            val btnCancel = dialog.findViewById<Button>(R.id.btnDeleteCancel)

            title.text = "${deviceToDelete.name} cihazını silmek istediğinize emin misiniz?"

            btnDelete.setOnClickListener {
                deleteDevice(deviceToDelete)
                dialog.dismiss()
            }

            btnCancel.setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()
            true // Click olayını burada bitir (tıklama ile karışmasın)
        }

        // İlk yükleme
        loadSavedDevices()
    }

    override fun onResume() {
        super.onResume()
        // Yeni cihaz ekleyip geri dönüldüğünde listenin güncellenmesi için
        loadSavedDevices()
    }

    private fun loadSavedDevices() {
        val sharedPref = getSharedPreferences("saved_devices_pref", Context.MODE_PRIVATE)
        val json = sharedPref.getString("devices_list", null)

        savedDevices.clear()
        if (json != null) {
            val type = object : TypeToken<List<SavedDevice>>() {}.type
            val list: List<SavedDevice> = Gson().fromJson(json, type)
            savedDevices.addAll(list)
        }

        if (savedDevices.isEmpty()) {
            emptyLayout.visibility = View.VISIBLE
            deviceListView.visibility = View.GONE
        } else {
            emptyLayout.visibility = View.GONE
            deviceListView.visibility = View.VISIBLE
            setupAdapter()
        }
    }

    private fun setupAdapter() {
        val adapter = object : ArrayAdapter<SavedDevice>(this, R.layout.device_item, savedDevices) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                // Senin paylaştığın device_item.xml'i kullanıyoruz
                val view = layoutInflater.inflate(R.layout.device_item, parent, false)
                val nameText = view.findViewById<TextView>(R.id.deviceName)
                val addressText = view.findViewById<TextView>(R.id.deviceAddress)

                val device = getItem(position)
                nameText.text = device?.name
                addressText.text = device?.address

                return view
            }
        }
        deviceListView.adapter = adapter
    }

    private fun setupStatusBarContrast() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val window = window
            val wic = androidx.core.view.WindowInsetsControllerCompat(window, window.decorView)
            val typedValue = android.util.TypedValue()
            theme.resolveAttribute(com.google.android.material.R.attr.colorSurface, typedValue, true)
            val isLightBackground = androidx.core.graphics.ColorUtils.calculateLuminance(typedValue.data) > 0.5
            wic.isAppearanceLightStatusBars = isLightBackground
        }
    }
    private fun deleteDevice(device: SavedDevice) {
        val sharedPref = getSharedPreferences("saved_devices_pref", Context.MODE_PRIVATE)

        // Mevcut listeyi al
        savedDevices.remove(device)

        // Güncel listeyi tekrar kaydet
        val json = Gson().toJson(savedDevices)
        sharedPref.edit().putString("devices_list", json).apply()

        // Arayüzü güncelle
        loadSavedDevices()
        Toast.makeText(this, "Cihaz silindi", Toast.LENGTH_SHORT).show()
    }

    private fun showSettingsDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_settings)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val dynamicSwitch = dialog.findViewById<MaterialSwitch>(R.id.dynamicSwitch)
        val sharedPref = getSharedPreferences("app_settings", Context.MODE_PRIVATE)

        dynamicSwitch.isChecked = sharedPref.getBoolean("dynamic_colors", true)

        dynamicSwitch.setOnCheckedChangeListener { _, isChecked ->
            sharedPref.edit().putBoolean("dynamic_colors", isChecked).apply()
            Handler(Looper.getMainLooper()).postDelayed({
                val intent = packageManager.getLaunchIntentForPackage(packageName)
                val mainIntent = Intent.makeRestartActivityTask(intent?.component)
                startActivity(mainIntent)
                Runtime.getRuntime().exit(0)
            }, 600)
        }
        dialog.show()
    }

    private fun showAboutDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_about)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val versionText = dialog.findViewById<TextView>(R.id.version_text)
        val githubLink = dialog.findViewById<View>(R.id.github_link)

        val versionName = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getPackageInfo(packageName, android.content.pm.PackageManager.PackageInfoFlags.of(0)).versionName
            } else {
                @Suppress("DEPRECATION")
                packageManager.getPackageInfo(packageName, 0).versionName
            }
        } catch (e: Exception) { "1.0.0" }

        versionText.text = "Versiyon: $versionName"

        githubLink.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(GITHUB_URL)))
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun shareApp() {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "Bitki Sulama Uygulamamızı Deneyin: $GITHUB_URL_LATEST")
        }
        startActivity(Intent.createChooser(shareIntent, "Paylaş"))
    }
}