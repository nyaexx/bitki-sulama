package gndsalih.nyaexx.bitkisulama

import android.Manifest
import android.app.Dialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageButton
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import gndsalih.nyaexx.bitkisulama.MonitoringActivity
import gndsalih.nyaexx.bitkisulama.R

class MainActivity : AppCompatActivity() {

    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var deviceListView: ListView
    private lateinit var connectButton: Button
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
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

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
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val githubButton = findViewById<ImageButton>(R.id.github_button)
        githubButton.setOnClickListener {
            showAboutDialog()
        }

        val shareButton = findViewById<ImageButton>(R.id.share_button)
        shareButton.setOnClickListener {
            shareApp()
        }

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        deviceListView = findViewById(R.id.deviceListView)
        connectButton = findViewById(R.id.connectButton)

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, deviceList)
        deviceListView.adapter = adapter

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

        deviceListView.setOnItemClickListener { _, _, position, _ ->
            val device = bluetoothDevices[position]
            try {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                    val bondState = device.bondState
                    if (bondState == BluetoothDevice.BOND_BONDED) {
                        val intent = Intent(this, MonitoringActivity::class.java)
                        intent.putExtra("device_address", device.address)
                        startActivity(intent)
                    } else {
                        Toast.makeText(this, "Cihaz aktif değil veya eşleşme yapılmamış", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Bluetooth izinleri eksik", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this, "Cihaza erişilemiyor: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("MainActivity", "Cihaz erişim hatası: ${e.message}")
            }
        }
    }

    private fun showAboutDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_about)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val versionText = dialog.findViewById<TextView>(R.id.version_text)
        val githubLink = dialog.findViewById<TextView>(R.id.github_link)

        val versionName = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0)).versionName
            } else {
                @Suppress("DEPRECATION")
                packageManager.getPackageInfo(packageName, 0).versionName
            }
        } catch (e: Exception) {
            "Bilinmiyor"
        }
        versionText.text = "v$versionName"

        githubLink.setOnClickListener {
            val intent =
                Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/nyaexx/bitki-sulama"))
            startActivity(intent)
            dialog.dismiss()
        }

        dialog.show()
    }




    private fun openGitHubPage() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(GITHUB_URL))
        startActivity(intent)
    }

    private fun shareApp() {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "text/plain"
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name))

        val shareMessage = "Bitki Sulama uygulamamızı deneyin: " +
                "https://github.com/nyaexx/bitki-sulama/releases/latest"

        shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
        startActivity(Intent.createChooser(shareIntent, "Paylaş"))
    }

    private fun checkBluetoothPermissionsAndLoadDevices() {
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.BLUETOOTH
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
            permissions.add(Manifest.permission.BLUETOOTH_SCAN)
        } else {
            permissions.add(Manifest.permission.BLUETOOTH_ADMIN)
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
        val hasPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
        } else {
            ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED
        }

        if (!hasPermission) {
            Toast.makeText(this, "Bluetooth izni verilmedi", Toast.LENGTH_SHORT).show()
            return
        }

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
                val addressText = view.findViewById<TextView>(R.id.deviceAddress)
                addressText.text = bluetoothDevices[position].address
                return view
            }
        }
        deviceListView.adapter = customAdapter
    }
}