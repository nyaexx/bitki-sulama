package gndsalih.nyaexx.bitkisulama

import android.Manifest
import android.app.Dialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.materialswitch.MaterialSwitch

class MainActivity : AppCompatActivity() {

    private var bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private lateinit var deviceListView: ListView
    private lateinit var connectButton: Button
    private lateinit var settingsButton: Button
    private val deviceList = mutableListOf<String>()
    private val bluetoothDevices = mutableListOf<BluetoothDevice>()

    private val GITHUB_URL = "https://github.com/nyaexx/bitki-sulama"
    private val GITHUB_URL_LATEST = "https://github.com/nyaexx/bitki-sulama/releases/latest"

    // Bluetooth durum değişikliklerini dinleyen alıcı
    private val bluetoothStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
                when (state) {
                    BluetoothAdapter.STATE_ON -> {
                        // Bluetooth açıldığı an listeyi doldurmayı dene
                        setupBluetooth()
                    }
                    BluetoothAdapter.STATE_OFF -> {
                        clearDeviceList()
                    }
                }
            }
        }
    }

    private val bluetoothPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val allGranted = permissions.all { it.value }
            if (allGranted) {
                // İzinler verildi, şimdi Bluetooth'u kontrol et ve cihazları yükle
                setupBluetooth()
            } else {
                Toast.makeText(this, "Bluetooth ve Konum izinleri gerekli.", Toast.LENGTH_LONG).show()
            }
        }

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
        connectButton = findViewById(R.id.connectButton)
        settingsButton = findViewById(R.id.settingsButton)

        setupStatusBarContrast()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            settingsButton.visibility = View.VISIBLE
            settingsButton.setOnClickListener { showSettingsDialog() }
        }

        // 3. Bluetooth Dinleyici Kaydı
        val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        registerReceiver(bluetoothStateReceiver, filter)

        // 4. Click Listeners
        findViewById<ImageButton>(R.id.github_button).setOnClickListener { showAboutDialog() }
        findViewById<ImageButton>(R.id.share_button).setOnClickListener { shareApp() }

        // ANA TETİKLEYİCİ: Çökme riskine karşı önce izinleri check eden fonksiyonu çağırıyoruz
        connectButton.setOnClickListener {
            checkBluetoothPermissionsAndLoadDevices()
        }

        deviceListView.setOnItemClickListener { _, _, position, _ ->
            val device = bluetoothDevices[position]
            // Cihaza bağlanmadan önce tekrar izin kontrolü (Android 12 güvenlik kuralı)
            if (hasBluetoothConnectPermission()) {
                val intent = Intent(this, MonitoringActivity::class.java)
                intent.putExtra("device_address", device.address)
                startActivity(intent)
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            } else {
                checkBluetoothPermissionsAndLoadDevices()
            }
        }

        // Başlangıçta Bluetooth zaten açıksa ve izinler tamamsa listeyi doldur
        if (hasBluetoothConnectPermission() && bluetoothAdapter?.isEnabled == true) {
            setupBluetooth()
        }
    }

    private fun checkBluetoothPermissionsAndLoadDevices() {
        val permissions = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(Manifest.permission.BLUETOOTH_SCAN)
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        val needed = permissions.filter { ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED }

        if (needed.isNotEmpty()) {
            bluetoothPermissionLauncher.launch(needed.toTypedArray())
        } else {
            // İzinler zaten var, Bluetooth'u kurmaya geç
            setupBluetooth()
        }
    }

    private fun hasBluetoothConnectPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Eski sürümlerde bu izne gerek yok
        }
    }

    private fun setupBluetooth() {
        val adapter = bluetoothAdapter
        if (adapter == null) {
            Toast.makeText(this, "Bluetooth bu cihazda desteklenmiyor.", Toast.LENGTH_SHORT).show()
            return
        }

        // Android 12+ için izin kontrolü yapmadan isEnabled sorgusu bile crash yapabilir
        if (!hasBluetoothConnectPermission()) {
            checkBluetoothPermissionsAndLoadDevices()
            return
        }

        if (!adapter.isEnabled) {
            // Bluetooth kapalıysa açılmasını iste
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            // Not: Crash'i önleyen en önemli nokta buraya izinsiz girmemekti.
            startActivity(enableBtIntent)
            return
        }

        // Buraya gelindiyse hem izinler var hem de Bluetooth açık
        loadPairedDevices()
    }

    private fun loadPairedDevices() {
        // Tekrar izin check (Lint uyarısını engellemek ve güvenlik için)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return
        }

        val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter?.bondedDevices
        deviceList.clear()
        bluetoothDevices.clear()

        pairedDevices?.forEach { device ->
            deviceList.add(device.name ?: "Bilinmeyen Cihaz")
            bluetoothDevices.add(device)
        }

        if (deviceList.isEmpty()) {
            Toast.makeText(this, "Eşleşmiş cihaz bulunamadı. Lütfen önce telefon ayarlarından cihazı eşleştirin.", Toast.LENGTH_LONG).show()
        }

        val customAdapter = object : ArrayAdapter<String>(this, R.layout.device_item, R.id.deviceName, deviceList) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                val addressText = view.findViewById<TextView>(R.id.deviceAddress)
                if (position < bluetoothDevices.size) {
                    addressText.text = bluetoothDevices[position].address
                }
                return view
            }
        }
        deviceListView.adapter = customAdapter
    }

    private fun clearDeviceList() {
        deviceList.clear()
        bluetoothDevices.clear()
        (deviceListView.adapter as? ArrayAdapter<*>)?.notifyDataSetChanged()
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
                packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0)).versionName
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

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(bluetoothStateReceiver)
        } catch (e: Exception) {
            Log.e("MainActivity", "Receiver kaydı bulunamadı.")
        }
    }
}