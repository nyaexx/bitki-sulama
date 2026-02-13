package gndsalih.nyaexx.bitkisulama

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID

class MonitoringActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var temperatureTextView: TextView
    private lateinit var humidityTextView: TextView
    private lateinit var soilMoistureTextView: TextView
    private lateinit var manualWateringButton: Button
    private lateinit var disconnectButton: Button
    private var bluetoothSocket: BluetoothSocket? = null
    private lateinit var inputStream: InputStream
    private lateinit var outputStream: OutputStream
    private var readThread: Thread? = null
    private val handler = Handler(Looper.getMainLooper())
    private val buffer = StringBuilder()
    private val TAG = "MonitoringActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_monitoring)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val window = window
            val decorView = window.decorView
            val wic = androidx.core.view.WindowInsetsControllerCompat(window, decorView)

            val typedValue = android.util.TypedValue()
            theme.resolveAttribute(com.google.android.material.R.attr.colorSurface, typedValue, true)
            val colorSurface = typedValue.data

            val isLightBackground = androidx.core.graphics.ColorUtils.calculateLuminance(colorSurface) > 0.5
            wic.isAppearanceLightStatusBars = isLightBackground
        }

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(true)

        val customDeviceName = intent.getStringExtra("device_name")
        supportActionBar?.title = customDeviceName ?: getString(R.string.yonetim_paneli)
        toolbar.setNavigationOnClickListener {
            disconnectAndGoBack()
        }

        temperatureTextView = findViewById(R.id.temperatureTextView)
        humidityTextView = findViewById(R.id.humidityTextView)
        soilMoistureTextView = findViewById(R.id.soilMoistureTextView)
        manualWateringButton = findViewById(R.id.manualWateringButton)
        disconnectButton = findViewById(R.id.disconnectButton)

        temperatureTextView.text = getString(R.string.temperature)
        humidityTextView.text = getString(R.string.humidity)
        soilMoistureTextView.text = getString(R.string.soil_moisture)

        manualWateringButton.setOnClickListener {
            sendWateringCommand()
        }

        disconnectButton.setOnClickListener {
            disconnectAndGoBack()
        }

        val deviceAddress = intent.getStringExtra("device_address")
        if (deviceAddress != null) {
            try {
                val device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(deviceAddress)
                connectToDevice(device)
            } catch (e: Exception) {
                Toast.makeText(this, "Geçersiz cihaz adresi", Toast.LENGTH_SHORT).show()
                finish()
            }
        } else {
            Toast.makeText(this, "Cihaz adresi alınamadı", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun sendWateringCommand() {
        if (bluetoothSocket?.isConnected == true && ::outputStream.isInitialized) {
            try {
                val command = "WATER\n"
                outputStream.write(command.toByteArray())
                Toast.makeText(this, getString(R.string.irrigation_command_sent),Toast.LENGTH_SHORT).show()
                Log.d(TAG, "Sulama komutu gönderildi")
            } catch (e: Exception) {
                val errorMessage = e.message ?: "Unknown error"
                Toast.makeText(
                    this, getString(R.string.error_command_failed, errorMessage), Toast.LENGTH_SHORT
                ).show()
                Log.e(TAG, "Komut gönderme hatası: ${e.message}")
            }
        } else {
            Toast.makeText(this, getString(R.string.no_bluetooth_connection), Toast.LENGTH_LONG).show()
        }
    }

    private fun disconnectAndGoBack() {
        try {
            if (::inputStream.isInitialized) inputStream.close()
            if (::outputStream.isInitialized) outputStream.close()
            readThread?.interrupt()
            bluetoothSocket?.close()

            Toast.makeText(this, "Bluetooth bağlantısı kesildi", Toast.LENGTH_SHORT).show()
            Log.d(TAG, "Bluetooth bağlantısı kesildi")

            finish()
        } catch (e: Exception) {
            Toast.makeText(this, "Bağlantı kapatılırken hata: ${e.message}", Toast.LENGTH_SHORT).show()
            Log.e(TAG, "Bağlantı kapatma hatası: ${e.message}")
        }
    }

    private fun connectToDevice(device: BluetoothDevice) {
        Thread {
            try {
                if (device.bondState != BluetoothDevice.BOND_BONDED) {
                    handler.post {
                        Toast.makeText(this, getString(R.string.error_device_inactive), Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    return@Thread
                }

                val uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
                bluetoothSocket = device.createRfcommSocketToServiceRecord(uuid)

                try {
                    bluetoothSocket?.connect()
                } catch (connectException: Exception) {
                    Log.e(TAG, "İlk bağlantı denemesi başarısız: ${connectException.message}")
                    bluetoothSocket?.close()
                    handler.post {
                        Toast.makeText(this, getString(R.string.cannot_connect_to_device), Toast.LENGTH_LONG).show()
                        finish()
                    }
                    return@Thread
                }

                if (bluetoothSocket?.isConnected == true) {
                    inputStream = bluetoothSocket?.inputStream!!
                    outputStream = bluetoothSocket?.outputStream!!
                    val deviceName = device.name ?: getString(R.string.unknown_device)

                    handler.post {
                        Toast.makeText(
                            this,
                            getString(R.string.connection_success, deviceName),
                            Toast.LENGTH_SHORT
                        ).show()
                        startReadingData()
                    }
                } else {
                    handler.post {
                        Toast.makeText(
                            this,
                            getString(R.string.error_connection_no_response),
                            Toast.LENGTH_LONG
                        ).show()
                        finish()
                    }
                }
            } catch (e: SecurityException) {
                Log.e(TAG, "Güvenlik hatası: ${e.message}")
                handler.post {
                    Toast.makeText(this, "Bluetooth izinleri eksik", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Genel bağlantı hatası: ${e.message}")
                handler.post {
                    Toast.makeText(this, "Bağlantı hatası: ${e.message}", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }.start()
    }

    private fun startReadingData() {
        readThread = Thread {
            val buffer = ByteArray(1024)

            while (!Thread.currentThread().isInterrupted && bluetoothSocket?.isConnected == true) {
                try {
                    val bytesAvailable = inputStream.available()
                    if (bytesAvailable > 0) {
                        val bytes = inputStream.read(buffer)
                        val data = String(buffer, 0, bytes)
                        processReceivedData(data)
                    }
                    Thread.sleep(100)
                } catch (e: Exception) {
                    Log.e(TAG, "Veri okuma hatası: ${e.message}")
                    break
                }
            }
        }
        readThread?.start()
    }

    private fun processReceivedData(data: String) {
        buffer.append(data)
        Log.d(TAG, "Alınan ham veri: $data")
        Log.d(TAG, "Buffer durumu: $buffer")

        if (buffer.indexOf("Sıcaklık:") != -1 &&
            buffer.indexOf("Toprak Nemi:") != -1 &&
            buffer.indexOf("Nem:") != -1 &&
            buffer.lastIndexOf("\n") != -1) {

            val completeData = buffer.toString()

            findTemperature(completeData)

            findSoilMoisture(completeData)

            findHumidity(completeData)

            val lastNewline = buffer.lastIndexOf("\n")
            if (lastNewline != -1) {
                buffer.delete(0, lastNewline + 1)
            }
        }
    }

    private fun findTemperature(data: String) {
        try {
            val startIndex = data.indexOf("Sıcaklık:")
            if (startIndex != -1) {
                val endIndex = data.indexOf("\n", startIndex)
                if (endIndex != -1) {
                    val temperatureLine = data.substring(startIndex, endIndex)
                    val temp = temperatureLine.substringAfter("Sıcaklık:").trim().replace("°C", "").trim()
                    handler.post {
                        temperatureTextView.text = getString(R.string.temperature_format, temp)
                        Log.d(TAG, "Temperature updated: $temp")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Temperature separation error: ${e.message}")
        }
    }

    private fun findSoilMoisture(data: String) {
        try {
            val startIndex = data.indexOf("Toprak Nemi:")
            if (startIndex != -1) {
                val endIndex = data.indexOf("\n", startIndex)
                if (endIndex != -1) {
                    val moistureLine = data.substring(startIndex, endIndex)
                    val moisture = moistureLine.substringAfter("Toprak Nemi:").trim()
                    handler.post {
                        val translatedMoisture = when (moisture) {
                            "Kuru" -> getString(R.string.dry)
                            "Normal" -> getString(R.string.normal)
                            "Çok Islak" -> getString(R.string.very_wet)
                            else -> moisture
                        }
                        soilMoistureTextView.text = getString(R.string.soil_moisture_format, translatedMoisture)
                        Log.d(TAG, "Toprak nemi güncellendi: $moisture")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Toprak nemi ayrıştırma hatası: ${e.message}")
        }
    }

    private fun findHumidity(data: String) {
        try {
            val startIndex = data.indexOf("Nem:")
            if (startIndex != -1) {
                val endIndex = data.indexOf("\n", startIndex)
                if (endIndex != -1) {
                    val humidityValue = data.substring(startIndex, endIndex)
                        .substringAfter("Nem:")
                        .trim()

                    handler.post {
                        humidityTextView.text = getString(R.string.humidity_format, humidityValue)
                        Log.d(TAG, "Nem güncellendi: $humidityValue")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Nem ayrıştırma hatası: ${e.message}")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            readThread?.interrupt()
            if (::inputStream.isInitialized) inputStream.close()
            if (::outputStream.isInitialized) outputStream.close()
            bluetoothSocket?.close()
            Log.d(TAG, "onDestroy: Kaynaklar temizlendi")
        } catch (e: Exception) {
            Log.e(TAG, "onDestroy: Hata - ${e.message}")
            e.printStackTrace()
        }
    }

    @Suppress("DEPRECATION")
    override fun onBackPressed() {
        disconnectAndGoBack()
        super.onBackPressed()
    }
}