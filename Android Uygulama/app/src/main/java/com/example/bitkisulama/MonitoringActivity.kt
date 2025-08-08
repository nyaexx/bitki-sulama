package com.example.bitkisulama

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
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
import java.util.*

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

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false) // Varsayılan başlığı gizle

        toolbar.setNavigationOnClickListener {
            disconnectAndGoBack()
        }

        temperatureTextView = findViewById(R.id.temperatureTextView)
        humidityTextView = findViewById(R.id.humidityTextView)
        soilMoistureTextView = findViewById(R.id.soilMoistureTextView)
        manualWateringButton = findViewById(R.id.manualWateringButton)
        disconnectButton = findViewById(R.id.disconnectButton)

        temperatureTextView.text = "Sıcaklık: --°C"
        humidityTextView.text = "Nem: --%"
        soilMoistureTextView.text = "Toprak Nemi: --"

        manualWateringButton.setOnClickListener {
            sendWateringCommand()
        }

        disconnectButton.setOnClickListener {
            disconnectAndGoBack()
        }

        val deviceAddress = intent.getStringExtra("device_address")
        if (deviceAddress != null) {
            val device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(deviceAddress)
            connectToDevice(device)
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
                Toast.makeText(this, "Sulama komutu gönderildi", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "Sulama komutu gönderildi")
            } catch (e: Exception) {
                Toast.makeText(this, "Komut gönderilemedi: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e(TAG, "Komut gönderme hatası: ${e.message}")
            }
        } else {
            Toast.makeText(this, "Bluetooth bağlantısı yok", Toast.LENGTH_SHORT).show()
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
        try {
            val uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB") // SPP UUID
            bluetoothSocket = device.createRfcommSocketToServiceRecord(uuid)
            bluetoothSocket?.connect()

            if (bluetoothSocket?.isConnected == true) {
                Toast.makeText(this, "${device.name} bağlantı başarılı!", Toast.LENGTH_SHORT).show()
                inputStream = bluetoothSocket?.inputStream!!
                outputStream = bluetoothSocket?.outputStream!!
                startReadingData()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Bağlantı hatası: ${e.message}")
            Toast.makeText(this, "Bağlantı kurulamadı: ${e.message}", Toast.LENGTH_SHORT).show()
            finish()
        }
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
                        temperatureTextView.text = "Sıcaklık: $temp °C"
                        Log.d(TAG, "Sıcaklık güncellendi: $temp")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Sıcaklık ayrıştırma hatası: ${e.message}")
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
                        soilMoistureTextView.text = "Toprak Nemi: $moisture"
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
                    val humidityLine = data.substring(startIndex, endIndex)
                    handler.post {
                        humidityTextView.text = "Nem: " + humidityLine.substringAfter("Nem:").trim()
                        Log.d(TAG, "Nem güncellendi: " + humidityLine.substringAfter("Nem:").trim())
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