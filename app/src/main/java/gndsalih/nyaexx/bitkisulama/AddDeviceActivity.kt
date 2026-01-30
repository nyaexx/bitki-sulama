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
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.gson.Gson

class AddDeviceActivity : AppCompatActivity() {

    private lateinit var searchBar: EditText
    private lateinit var deviceListView: ListView
    private lateinit var scanButton: Button
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private val fullDeviceList = mutableListOf<BluetoothDevice>()
    private var filteredList = mutableListOf<BluetoothDevice>()

    private val bluetoothStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
                if (state == BluetoothAdapter.STATE_ON) {
                    loadPairedDevices()
                } else if (state == BluetoothAdapter.STATE_OFF) {
                    clearDeviceList()
                }
            }
        }
    }

    private val bluetoothPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions.all { it.value }) {
                setupBluetooth()
            } else {
                Toast.makeText(this, "Bluetooth izinleri reddedildi.", Toast.LENGTH_LONG).show()
            }
        }

    private val enableBtLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (bluetoothAdapter?.isEnabled == true) {
            loadPairedDevices()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_device)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        searchBar = findViewById(R.id.searchBar)
        deviceListView = findViewById(R.id.deviceListView)
        scanButton = findViewById(R.id.scanButton)

        registerReceiver(bluetoothStateReceiver, IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED))

        searchBar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { filter(s.toString()) }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        deviceListView.setOnItemClickListener { _, _, position, _ ->
            showNameDialog(filteredList[position])
        }

        // BUTON TIKLAMASI
        scanButton.setOnClickListener {
            checkBluetoothPermissionsAndLoadDevices()
        }

        // Otomatik Başlat
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

    private fun setupBluetooth() {
        val adapter = bluetoothAdapter
        if (adapter == null) {
            return
        }

        if (!adapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            enableBtLauncher.launch(enableBtIntent)
        } else {
            loadPairedDevices()
        }
    }

    private fun loadPairedDevices() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            checkBluetoothPermissionsAndLoadDevices()
            return
        }

        val pairedDevices = bluetoothAdapter?.bondedDevices
        fullDeviceList.clear()
        pairedDevices?.let { fullDeviceList.addAll(it) }

        filter(searchBar.text.toString())
    }

    private fun filter(text: String) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return
        }

        filteredList = fullDeviceList.filter {
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
                val icon = view.findViewById<ImageView>(R.id.deviceIcon)

                icon.setImageResource(R.drawable.outline_bluetooth_24)

                val device = getItem(position)
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED || Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                } else {
                }
                deviceAddressTxt.text = device?.address
                return view
            }
        }
        deviceListView.adapter = adapter
    }

    private fun showNameDialog(device: BluetoothDevice) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_device_name)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val input = dialog.findViewById<EditText>(R.id.deviceNameInput)
        val btnAdd = dialog.findViewById<Button>(R.id.btnAdd)
        val btnCancel = dialog.findViewById<Button>(R.id.btnCancel)

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED || Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            input.setText(device.name ?: "Cihazım")
        }

        btnAdd.setOnClickListener {
            saveDevice(customName, device.address, if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) "Bilinmeyen" else device.name ?: "Bilinmeyen")
            dialog.dismiss()
        }

        btnCancel.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun saveDevice(customName: String, address: String, originalName: String) {
        val sharedPref = getSharedPreferences("saved_devices_pref", Context.MODE_PRIVATE)
        val json = sharedPref.getString("devices_list", null)

        val list: MutableList<SavedDevice> = if (json == null) {
            mutableListOf()
        } else {
            try {
                val array = Gson().fromJson(json, Array<SavedDevice>::class.java)
                array.toMutableList()
            } catch (e: Exception) {
                mutableListOf()
            }
        }

        list.add(SavedDevice(customName, address, originalName))

        sharedPref.edit().putString("devices_list", Gson().toJson(list)).apply()
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