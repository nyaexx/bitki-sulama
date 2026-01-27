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
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class AddDeviceActivity : AppCompatActivity() {

    private lateinit var searchBar: EditText
    private lateinit var deviceListView: ListView
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private val fullDeviceList = mutableListOf<BluetoothDevice>()
    private var filteredList = mutableListOf<BluetoothDevice>()

    // 1. Bluetooth durum değişikliklerini dinleyen alıcı (Senin mekanizman)
    private val bluetoothStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
                if (state == BluetoothAdapter.STATE_ON) {
                    setupBluetooth()
                } else if (state == BluetoothAdapter.STATE_OFF) {
                    clearDeviceList()
                }
            }
        }
    }

    // 2. İzin isteme mekanizması (Senin mekanizman)
    private val bluetoothPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val allGranted = permissions.all { it.value }
            if (allGranted) {
                setupBluetooth()
            } else {
                Toast.makeText(this, "Bluetooth ve Konum izinleri gerekli.", Toast.LENGTH_LONG).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_device)

        // Toolbar ayarları
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false) // Başlığın göründüğünden emin ol
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        searchBar = findViewById(R.id.searchBar)
        deviceListView = findViewById(R.id.deviceListView)

        // Bluetooth dinleyici kaydı
        registerReceiver(bluetoothStateReceiver, IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED))

        // Arama/Filtreleme dinleyicisi
        searchBar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { filter(s.toString()) }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        deviceListView.setOnItemClickListener { _, _, position, _ ->
            showNameDialog(filteredList[position])
        }

        // SAYFA AÇILDIĞINDA OTOMATİK BAŞLAT (Senin istediğin can alıcı nokta)
        checkBluetoothPermissionsAndLoadDevices()
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
            setupBluetooth()
        }
    }

    private fun hasBluetoothConnectPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    private fun setupBluetooth() {
        val adapter = bluetoothAdapter
        if (adapter == null) {
            Toast.makeText(this, "Bluetooth desteklenmiyor.", Toast.LENGTH_SHORT).show()
            return
        }

        if (!hasBluetoothConnectPermission()) {
            checkBluetoothPermissionsAndLoadDevices()
            return
        }

        if (!adapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivity(enableBtIntent)
            return
        }

        loadPairedDevices()
    }

    private fun loadPairedDevices() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return
        }

        val pairedDevices = bluetoothAdapter?.bondedDevices
        fullDeviceList.clear()
        pairedDevices?.let { fullDeviceList.addAll(it) }

        // Filtreyi temizle ve listeyi güncelle
        filter(searchBar.text.toString())
    }

    private fun filter(text: String) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return
        }

        filteredList = fullDeviceList.filter {
            val name = it.name ?: "Bilinmeyen Cihaz"
            val address = it.address ?: ""
            name.contains(text, ignoreCase = true) || address.contains(text, ignoreCase = true)
        }.toMutableList()
        updateAdapter()
    }

    private fun updateAdapter() {
        val adapter = object : ArrayAdapter<BluetoothDevice>(this, R.layout.device_item, filteredList) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = layoutInflater.inflate(R.layout.device_item, parent, false)
                val deviceNameTxt = view.findViewById<TextView>(R.id.deviceName)
                val deviceAddressTxt = view.findViewById<TextView>(R.id.deviceAddress)

                val device = getItem(position)
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED || Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                    deviceNameTxt.text = device?.name ?: "Bilinmeyen Cihaz"
                } else {
                    deviceNameTxt.text = "İzin Gerekli"
                }
                deviceAddressTxt.text = device?.address
                return view
            }
        }
        deviceListView.adapter = adapter
    }

    private fun showNameDialog(device: BluetoothDevice) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return
        }

        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_device_name)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val input = dialog.findViewById<EditText>(R.id.deviceNameInput)
        val btnAdd = dialog.findViewById<Button>(R.id.btnAdd)
        val btnCancel = dialog.findViewById<Button>(R.id.btnCancel)

        input.setText(device.name ?: "Cihazım")

        btnAdd.setOnClickListener {
            val customName = if (input.text.toString().isEmpty()) (device.name ?: "Cihazım") else input.text.toString()
            saveDevice(customName, device.address, device.name ?: "Bilinmeyen")
            dialog.dismiss()
            finish()
        }

        btnCancel.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }

    private fun saveDevice(customName: String, address: String, originalName: String) {
        val sharedPref = getSharedPreferences("saved_devices_pref", Context.MODE_PRIVATE)
        val json = sharedPref.getString("devices_list", null)
        val type = object : TypeToken<MutableList<SavedDevice>>() {}.type
        val list: MutableList<SavedDevice> = if (json == null) mutableListOf() else Gson().fromJson(json, type)

        list.removeAll { it.address == address }
        list.add(SavedDevice(customName, address, originalName))

        sharedPref.edit().putString("devices_list", Gson().toJson(list)).apply()
        Toast.makeText(this, "$customName eklendi!", Toast.LENGTH_SHORT).show()
    }

    private fun clearDeviceList() {
        fullDeviceList.clear()
        filteredList.clear()
        updateAdapter()
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(bluetoothStateReceiver)
        } catch (e: Exception) { }
    }
}