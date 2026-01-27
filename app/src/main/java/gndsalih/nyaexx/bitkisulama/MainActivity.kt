package gndsalih.nyaexx.bitkisulama

import android.Manifest
import android.app.Dialog
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
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
import androidx.core.content.ContextCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.gson.Gson

class MainActivity : AppCompatActivity() {

    private lateinit var deviceListView: ListView
    private lateinit var emptyLayout: LinearLayout
    private val savedDevices = mutableListOf<SavedDevice>()
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

    // Bluetooth açıldıktan sonra gidilecek cihaz bilgilerini tutar
    private var pendingDeviceAddress: String? = null
    private var pendingDeviceName: String? = null

    private val GITHUB_URL = "https://github.com/nyaexx/bitki-sulama"

    // 1. Bluetooth İzin Launcher'ı
    private val bluetoothPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions.all { it.value }) {
                // İzinler verildi, Bluetooth kontrolüne devam et
                checkBluetoothAndProceed()
            } else {
                Toast.makeText(this, "Bluetooth izinleri reddedildi.", Toast.LENGTH_LONG).show()
            }
        }

    // 2. Bluetooth Açma (Enable) Launcher'ı
    private val enableBtLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (bluetoothAdapter?.isEnabled == true) {
            // Bluetooth açıldı, bekleyen cihaza git
            proceedToMonitoring()
        } else {
            Toast.makeText(this, "Bağlantı kurmak için Bluetooth'u açmalısınız.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Geçiş Animasyonları
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            overrideActivityTransition(OVERRIDE_TRANSITION_OPEN, android.R.anim.fade_in, android.R.anim.fade_out)
        } else {
            @Suppress("DEPRECATION")
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // UI Init
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        deviceListView = findViewById(R.id.deviceListView)
        emptyLayout = findViewById(R.id.emptyLayout)
        val fabAdd = findViewById<FloatingActionButton>(R.id.fabAdd)

        setupStatusBarContrast()

        findViewById<ImageButton>(R.id.github_button).setOnClickListener { showAboutDialog() }
        findViewById<ImageButton>(R.id.settings_button).setOnClickListener { showSettingsDialog() }

        fabAdd.setOnClickListener {
            startActivity(Intent(this, AddDeviceActivity::class.java))
        }

        // Listeye Tıklama (Bluetooth Kontrollü)
        deviceListView.setOnItemClickListener { _, _, position, _ ->
            val device = savedDevices[position]
            pendingDeviceAddress = device.address
            pendingDeviceName = device.name

            checkBluetoothAndProceed()
        }

        deviceListView.setOnItemLongClickListener { _, _, position, _ ->
            val device = savedDevices[position]
            showOptionsDialog(device, position)
            true
        }

        loadSavedDevices()
    }

    override fun onResume() {
        super.onResume()
        loadSavedDevices()
    }

    private fun checkBluetoothAndProceed() {
        val permissions = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
            permissions.add(Manifest.permission.BLUETOOTH_SCAN)
        } else {
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        val needed = permissions.filter { ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED }

        if (needed.isNotEmpty()) {
            bluetoothPermissionLauncher.launch(needed.toTypedArray())
        } else {
            // İzinler var, Bluetooth açık mı?
            if (bluetoothAdapter?.isEnabled == false) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                enableBtLauncher.launch(enableBtIntent)
            } else {
                // Her şey hazır, geçiş yap
                proceedToMonitoring()
            }
        }
    }

    private fun proceedToMonitoring() {
        if (pendingDeviceAddress != null) {
            val intent = Intent(this, MonitoringActivity::class.java).apply {
                putExtra("device_address", pendingDeviceAddress)
                putExtra("device_name", pendingDeviceName)
            }
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)

            // Verileri temizle
            pendingDeviceAddress = null
            pendingDeviceName = null
        }
    }

    private fun loadSavedDevices() {
        val sharedPref = getSharedPreferences("saved_devices_pref", Context.MODE_PRIVATE)
        val json = sharedPref.getString("devices_list", null)

        savedDevices.clear()
        if (json != null) {
            try {
                val array = Gson().fromJson(json, Array<SavedDevice>::class.java)
                savedDevices.addAll(array)
            } catch (e: Exception) {
                Log.e("MainActivity", "Veri okuma hatası: ${e.message}")
            }
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
                val view = layoutInflater.inflate(R.layout.device_item, parent, false)
                val nameText = view.findViewById<TextView>(R.id.deviceName)
                val addressText = view.findViewById<TextView>(R.id.deviceAddress)
                val icon = view.findViewById<ImageView>(R.id.deviceIcon)

                val device = getItem(position)
                nameText.text = device?.name
                addressText.text = device?.address
                icon.setImageResource(R.drawable.ic_leaf)

                return view
            }
        }
        deviceListView.adapter = adapter
    }
    private fun showOptionsDialog(device: SavedDevice, position: Int) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_device_options)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialog.findViewById<View>(R.id.btnRenameOption).setOnClickListener {
            dialog.dismiss()
            showRenameDialog(device, position)
        }

        dialog.findViewById<View>(R.id.btnDeleteOption).setOnClickListener {
            dialog.dismiss()
            showDeleteDialog(device) // Mevcut silme diyaloğun
        }

        dialog.show()
    }
    private fun showRenameDialog(device: SavedDevice, position: Int) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_device_name) // Cihaz eklemedeki diyaloğu kullanıyoruz
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val input = dialog.findViewById<EditText>(R.id.deviceNameInput)
        val btnSave = dialog.findViewById<Button>(R.id.btnAdd)
        btnSave.text = "Güncelle"
        input.setText(device.name)

        btnSave.setOnClickListener {
            val newName = input.text.toString()
            if (newName.isNotEmpty()) {
                // Listeyi güncelle
                savedDevices[position] = device.copy(name = newName)

                // SharedPreferences'a kaydet
                val json = Gson().toJson(savedDevices)
                getSharedPreferences("saved_devices_pref", Context.MODE_PRIVATE)
                    .edit().putString("devices_list", json).apply()

                loadSavedDevices()
                dialog.dismiss()
                Toast.makeText(this, "İsim güncellendi", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.findViewById<Button>(R.id.btnCancel)?.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun showDeleteDialog(device: SavedDevice) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_delete_confirm)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialog.findViewById<TextView>(R.id.deleteTitle).text = "${device.name} cihazını silmek istediğinize emin misiniz?"

        dialog.findViewById<Button>(R.id.btnDeleteConfirm).setOnClickListener {
            deleteDevice(device)
            dialog.dismiss()
        }
        dialog.findViewById<Button>(R.id.btnDeleteCancel).setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun deleteDevice(device: SavedDevice) {
        savedDevices.remove(device)
        val json = Gson().toJson(savedDevices)
        getSharedPreferences("saved_devices_pref", Context.MODE_PRIVATE).edit()
            .putString("devices_list", json).apply()
        loadSavedDevices()
        Toast.makeText(this, "Cihaz silindi", Toast.LENGTH_SHORT).show()
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
}