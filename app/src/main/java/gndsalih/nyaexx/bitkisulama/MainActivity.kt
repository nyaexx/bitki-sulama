package gndsalih.nyaexx.bitkisulama

import android.Manifest
import android.app.Dialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Window
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.materialswitch.MaterialSwitch
import gndsalih.nyaexx.bitkisulama.MonitoringActivity
import gndsalih.nyaexx.bitkisulama.R

class MainActivity : AppCompatActivity() {

    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var deviceListView: ListView
    private lateinit var connectButton: Button
    private lateinit var settingsButton: Button
    private val deviceList = mutableListOf<String>()
    private val bluetoothDevices = mutableListOf<BluetoothDevice>()
    private lateinit var adapter: ArrayAdapter<String>

    private val GITHUB_URL = "https://github.com/nyaexx/bitki-sulama"

    private val bluetoothPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val granted = permissions.all { it.value }
            if (granted) {
                setupBluetooth()
            } else {
                Toast.makeText(this, "Bluetooth ve konum izinleri gerekli", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        // 1. Önce Animasyon (Varsa) ve Tema Hazırlıkları
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            overrideActivityTransition(OVERRIDE_TRANSITION_OPEN, android.R.anim.fade_in, android.R.anim.fade_out)
        } else {
            @Suppress("DEPRECATION")
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        super.onCreate(savedInstanceState)

        // 2. MUTLAKA ÖNCE LAYOUT'U SET ET
        setContentView(R.layout.activity_main)

        // 3. UI ELEMANLARINI BAĞLA (Init)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // Lateinit özelliklerini burada başlatıyoruz
        deviceListView = findViewById(R.id.deviceListView)
        connectButton = findViewById(R.id.connectButton)
        settingsButton = findViewById(R.id.settingsButton)

        // 4. GÖRÜNÜM AYARLARI
        setupStatusBarContrast()

        // Android 12+ ise Ayarlar Butonunu Göster
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            settingsButton.visibility = android.view.View.VISIBLE
            settingsButton.setOnClickListener { showSettingsDialog() }
        }

        // 5. CLICK LISTENER'LAR
        findViewById<ImageButton>(R.id.github_button).setOnClickListener { showAboutDialog() }
        findViewById<ImageButton>(R.id.share_button).setOnClickListener { shareApp() }

        // 6. BLUETOOTH İŞLEMLERİ
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        connectButton.setOnClickListener {
            if (bluetoothAdapter == null) {
                Toast.makeText(this, "Bluetooth desteklenmiyor", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!bluetoothAdapter.isEnabled) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivity(enableBtIntent)
            } else {
                checkBluetoothPermissionsAndLoadDevices()
            }
        }

        // Cihaz Seçimi ve Geçiş
        deviceListView.setOnItemClickListener { _, _, position, _ ->
            val device = bluetoothDevices[position]
            try {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                    val intent = Intent(this, MonitoringActivity::class.java)
                    intent.putExtra("device_address", device.address)
                    startActivity(intent)
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                } else {
                    Toast.makeText(this, "Bluetooth izinleri eksik", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Hata: ${e.message}")
            }
        }
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

    private fun showSettingsDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_settings)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val dynamicSwitch = dialog.findViewById<MaterialSwitch>(R.id.dynamicSwitch)
        val sharedPref = getSharedPreferences("app_settings", Context.MODE_PRIVATE)

        val isDynamicEnabled = sharedPref.getBoolean("dynamic_colors", true)
        dynamicSwitch.isChecked = isDynamicEnabled

        dynamicSwitch.setOnCheckedChangeListener { _, isChecked ->
            sharedPref.edit().putBoolean("dynamic_colors", isChecked).apply()

            Toast.makeText(this, "Tema uygulanıyor...", Toast.LENGTH_SHORT).show()

            Handler(Looper.getMainLooper()).postDelayed({
                // Uygulamayı tamamen kapat ve en baştan (Splash'ten) başlat
                val packageManager = packageManager
                val intent = packageManager.getLaunchIntentForPackage(packageName)
                val componentName = intent?.component
                val mainIntent = Intent.makeRestartActivityTask(componentName)
                startActivity(mainIntent)
                Runtime.getRuntime().exit(0) // Uygulama sürecini (process) tamamen bitir
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

        // HATALI YER BURASIYDI: TextView yerine View veya LinearLayout yapıyoruz
        val githubLink = dialog.findViewById<android.view.View>(R.id.github_link)

        val versionName = try {
            packageManager.getPackageInfo(packageName, 0).versionName
        } catch (e: Exception) { "Bilinmiyor" }

        versionText.text = "n$versionName"

        githubLink.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(GITHUB_URL)))
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun shareApp() {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name))
            putExtra(Intent.EXTRA_TEXT, "Bitki Sulama uygulamamızı deneyin: $GITHUB_URL")
        }
        startActivity(Intent.createChooser(shareIntent, "Paylaş"))
    }

    private fun checkBluetoothPermissionsAndLoadDevices() {
        val permissions = mutableListOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.BLUETOOTH)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
            permissions.add(Manifest.permission.BLUETOOTH_SCAN)
        }

        val neededPermissions = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (neededPermissions.isNotEmpty()) {
            bluetoothPermissionLauncher.launch(neededPermissions.toTypedArray())
        } else {
            setupBluetooth()
        }
    }

    private fun setupBluetooth() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) return

        val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter.bondedDevices
        deviceList.clear()
        bluetoothDevices.clear()

        pairedDevices?.forEach { device ->
            deviceList.add(device.name ?: "Bilinmeyen Cihaz")
            bluetoothDevices.add(device)
        }

        val customAdapter = object : ArrayAdapter<String>(this, R.layout.device_item, R.id.deviceName, deviceList) {
            override fun getView(position: Int, convertView: android.view.View?, parent: android.view.ViewGroup): android.view.View {
                val view = super.getView(position, convertView, parent)
                view.findViewById<TextView>(R.id.deviceAddress).text = bluetoothDevices[position].address
                return view
            }
        }
        deviceListView.adapter = customAdapter
    }
}