package com.michaelnketia.copyshare

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.michaelnketia.copyshare.databinding.ActivityMainBinding
import java.io.File

@SuppressLint("MissingPermission")
class MainActivity : AppCompatActivity() {

    // ── ViewBinding ───────────────────────────────────────────────────────────
    private lateinit var binding: ActivityMainBinding

    // ── Bluetooth ─────────────────────────────────────────────────────────────
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothService: BluetoothService? = null

    // ── Permissions ───────────────────────────────────────────────────────────
    private val permissionsToRequest: Array<String>
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN
            )
        } else {
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
            val allGranted = results.values.all { it }
            if (allGranted) initBluetooth()
            else showToast("Bluetooth permissions are required for CopyShare to work.")
        }

    private val enableBtLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) startBluetoothService()
            else showToast("Bluetooth must be enabled to use CopyShare.")
        }

    // ── Handler (main-thread) ─────────────────────────────────────────────────
    private val uiHandler = Handler(Looper.getMainLooper()) { msg ->
        when (msg.what) {
            Constants.MSG_STATE_CHANGE -> onStateChange(msg.arg1)
            Constants.MSG_FILE_RECEIVED -> onFileReceived(msg.obj as String)
            Constants.MSG_FILE_SENT -> onFileSent()
            Constants.MSG_DEVICE_CONNECTED -> onDeviceConnected(msg.obj as String)
            Constants.MSG_TOAST -> showToast(msg.obj as String)
            Constants.MSG_LOG -> appendLog(msg.obj as String)
        }
        true
    }

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        checkPermissionsAndInit()
    }

    override fun onResume() {
        super.onResume()
        if (bluetoothService?.getState() == Constants.STATE_NONE) {
            startBluetoothService()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        bluetoothService?.stop()
    }

    // ── UI Setup ──────────────────────────────────────────────────────────────

    private fun setupUI() {
        // Paste from clipboard
        binding.btnPaste.setOnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = clipboard.primaryClip
            if (clip != null && clip.itemCount > 0) {
                val text = clip.getItemAt(0).coerceToText(this).toString()
                binding.etText.setText(text)
                binding.etText.setSelection(text.length)
                appendLog("Pasted ${text.length} characters from clipboard.")
            } else {
                showToast("Clipboard is empty.")
            }
        }

        // Clear text
        binding.btnClear.setOnClickListener {
            binding.etText.text?.clear()
            appendLog("Text cleared.")
        }

        // Connect to a device
        binding.btnConnect.setOnClickListener {
            showDevicePicker()
        }

        // Send
        binding.btnSend.setOnClickListener {
            val text = binding.etText.text.toString().trim()
            if (text.isEmpty()) {
                showToast("Paste or type some text first.")
                return@setOnClickListener
            }
            if (bluetoothService?.getState() != Constants.STATE_CONNECTED) {
                showToast("Connect to a device first.")
                return@setOnClickListener
            }
            sendTextAsFile(text)
        }

        // Copy received text
        binding.btnCopyReceived.setOnClickListener {
            val text = binding.tvReceivedText.text.toString()
            if (text.isNotBlank()) {
                copyToClipboard(text)
                showToast("✓ Copied! Now paste wherever you need it.")
            }
        }

        // Clear log
        binding.btnClearLog.setOnClickListener {
            binding.tvLog.text = ""
        }

        updateConnectionState(Constants.STATE_NONE)
    }

    // ── Permissions & BT init ─────────────────────────────────────────────────

    private fun checkPermissionsAndInit() {
        val missing = permissionsToRequest.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (missing.isEmpty()) initBluetooth()
        else permissionLauncher.launch(missing.toTypedArray())
    }

    private fun initBluetooth() {
        val manager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = manager.adapter

        if (bluetoothAdapter == null) {
            showToast("This device does not support Bluetooth.")
            return
        }

        if (!bluetoothAdapter!!.isEnabled) {
            enableBtLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
        } else {
            startBluetoothService()
        }
    }

    private fun startBluetoothService() {
        bluetoothService?.stop()
        bluetoothService = BluetoothService(this, uiHandler, bluetoothAdapter!!)
        bluetoothService?.startListening()
        appendLog("Listening for incoming connections…")
    }

    // ── Device Picker ─────────────────────────────────────────────────────────

    private fun showDevicePicker() {
        val adapter = bluetoothAdapter ?: run {
            showToast("Bluetooth not ready.")
            return
        }

        val paired: Set<BluetoothDevice> = adapter.bondedDevices ?: emptySet()
        if (paired.isEmpty()) {
            AlertDialog.Builder(this)
                .setTitle("No Paired Devices")
                .setMessage(
                    "CopyShare works with already-paired devices.\n\n" +
                    "Go to Settings → Bluetooth and pair the target device first, " +
                    "then come back here to connect."
                )
                .setPositiveButton("OK", null)
                .show()
            return
        }

        val names = paired.map { "${it.name}\n${it.address}" }.toTypedArray()
        val devices = paired.toList()

        AlertDialog.Builder(this)
            .setTitle("Select Paired Device")
            .setItems(names) { _, which ->
                val device = devices[which]
                appendLog("Connecting to ${device.name}…")
                bluetoothService?.connect(device)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // ── Send Logic ────────────────────────────────────────────────────────────

    private fun sendTextAsFile(text: String) {
        // Write text to a temp .txt file in cacheDir
        val tempFile = File(cacheDir, Constants.TEMP_FILE_NAME)
        tempFile.writeText(text, Charsets.UTF_8)

        appendLog("Created temp file: ${tempFile.name} (${tempFile.length()} bytes)")
        appendLog("Sending to connected device…")

        binding.btnSend.isEnabled = false
        binding.progressSend.visibility = View.VISIBLE

        bluetoothService?.sendFile(tempFile.absolutePath)
    }

    // ── Handler callbacks ─────────────────────────────────────────────────────

    private fun onStateChange(newState: Int) {
        updateConnectionState(newState)
    }

    private fun onDeviceConnected(deviceName: String) {
        appendLog("✓ Connected to $deviceName")
    }

    private fun onFileSent() {
        binding.btnSend.isEnabled = true
        binding.progressSend.visibility = View.GONE
        appendLog("✓ File delivered successfully.")
        showToast("Text sent!")
    }

    private fun onFileReceived(text: String) {
        // Show the received text
        binding.cardReceived.visibility = View.VISIBLE
        binding.tvReceivedText.text = text

        // Auto-copy to clipboard
        copyToClipboard(text)

        // Alert the user (API 10+ blocks silent clipboard writes in some contexts,
        // so we always prompt explicitly)
        appendLog("✓ Text received (${text.length} chars) — copied to clipboard.")
        showPasteDialog(text)
    }

    // ── Clipboard ─────────────────────────────────────────────────────────────

    private fun copyToClipboard(text: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("CopyShare Transfer", text)
        clipboard.setPrimaryClip(clip)
    }

    private fun showPasteDialog(text: String) {
        AlertDialog.Builder(this)
            .setTitle("📋 Text Received!")
            .setMessage(
                "The transferred text has been copied to your clipboard.\n\n" +
                "Go to any app and long-press → Paste to use it.\n\n" +
                "Preview: \"${text.take(120)}${if (text.length > 120) "…" else ""}\""
            )
            .setPositiveButton("Got it — I'll Paste") { _, _ -> }
            .setNeutralButton("Copy Again") { _, _ ->
                copyToClipboard(text)
                showToast("Copied again!")
            }
            .show()
    }

    // ── UI state helpers ──────────────────────────────────────────────────────

    private fun updateConnectionState(state: Int) {
        val (label, color, connectEnabled, sendEnabled) = when (state) {
            Constants.STATE_NONE       -> Quad("● Bluetooth Off",      R.color.status_off,         true,  false)
            Constants.STATE_LISTEN     -> Quad("◎ Listening…",         R.color.status_listening,   true,  false)
            Constants.STATE_CONNECTING -> Quad("⟳ Connecting…",        R.color.status_connecting,  false, false)
            Constants.STATE_CONNECTED  -> Quad("✓ Connected",           R.color.status_connected,   true,  true)
            else                       -> Quad("● Unknown",             R.color.status_off,         true,  false)
        }
        binding.tvStatus.text = label
        binding.tvStatus.setTextColor(ContextCompat.getColor(this, color))
        binding.btnConnect.isEnabled = connectEnabled
        binding.btnSend.isEnabled = sendEnabled

        if (state != Constants.STATE_CONNECTED) {
            binding.progressSend.visibility = View.GONE
        }
    }

    private fun appendLog(msg: String) {
        val current = binding.tvLog.text.toString()
        val timestamp = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
            .format(java.util.Date())
        val newLine = "[$timestamp] $msg"
        binding.tvLog.text = if (current.isEmpty()) newLine else "$current\n$newLine"
        // Auto-scroll
        binding.scrollLog.post { binding.scrollLog.fullScroll(View.FOCUS_DOWN) }
    }

    private fun showToast(msg: String) =
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()

    /** Tiny helper to return four values from a when expression. */
    private data class Quad(
        val label: String,
        val colorRes: Int,
        val connectEnabled: Boolean,
        val sendEnabled: Boolean
    )
}
