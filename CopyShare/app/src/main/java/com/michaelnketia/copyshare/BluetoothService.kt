package com.michaelnketia.copyshare

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.os.Handler
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.io.IOException

/**
 * BluetoothService manages three threads:
 *  - AcceptThread  : listens for incoming connections (server role)
 *  - ConnectThread : initiates an outgoing connection (client role)
 *  - ConnectedThread: handles the live socket — sends and receives files
 *
 * File transfer protocol (binary, no framing bugs):
 *   SENDER  → "CSFILE\n"  (7 bytes ASCII header)
 *           → Long         (8 bytes, big-endian — file byte count)
 *           → ByteArray    (N bytes — UTF-8 text file content)
 *
 *   RECEIVER reads header, reads Long, calls readFully(N bytes), writes
 *   temp file, reads text, posts MSG_FILE_RECEIVED to the UI handler.
 */
@SuppressLint("MissingPermission")
class BluetoothService(
    private val context: Context,
    private val handler: Handler,
    private val adapter: BluetoothAdapter
) {

    private var state: Int = Constants.STATE_NONE

    private var acceptThread: AcceptThread? = null
    private var connectThread: ConnectThread? = null
    private var connectedThread: ConnectedThread? = null

    // ── Public API ────────────────────────────────────────────────────────────

    @Synchronized
    fun getState(): Int = state

    /** Start listening for incoming connections (server mode). */
    @Synchronized
    fun startListening() {
        cancelConnectThread()
        cancelConnectedThread()

        if (acceptThread == null) {
            acceptThread = AcceptThread().also { it.start() }
        }
        setState(Constants.STATE_LISTEN)
    }

    /** Connect to a remote device (client mode). */
    @Synchronized
    fun connect(device: BluetoothDevice) {
        if (state == Constants.STATE_CONNECTING) cancelConnectThread()
        cancelConnectedThread()

        connectThread = ConnectThread(device).also { it.start() }
        setState(Constants.STATE_CONNECTING)
    }

    /** Called once a raw socket is established (from either thread). */
    @Synchronized
    fun onSocketConnected(socket: BluetoothSocket, device: BluetoothDevice) {
        cancelConnectThread()
        cancelAcceptThread()
        cancelConnectedThread()

        connectedThread = ConnectedThread(socket).also { it.start() }
        setState(Constants.STATE_CONNECTED)

        handler.obtainMessage(Constants.MSG_DEVICE_CONNECTED, device.name).sendToTarget()
        log("Connected to ${device.name}")
    }

    /** Send a text file through the live connection. */
    fun sendFile(filePath: String) {
        val thread = synchronized(this) {
            if (state != Constants.STATE_CONNECTED) {
                postToast("Not connected — cannot send.")
                return
            }
            connectedThread
        }
        thread?.sendFile(filePath)
    }

    @Synchronized
    fun stop() {
        cancelConnectThread()
        cancelConnectedThread()
        cancelAcceptThread()
        setState(Constants.STATE_NONE)
    }

    // ── Internal helpers ──────────────────────────────────────────────────────

    private fun setState(s: Int) {
        state = s
        handler.obtainMessage(Constants.MSG_STATE_CHANGE, s, -1).sendToTarget()
    }

    private fun onConnectionFailed(reason: String) {
        postToast("Connection failed: $reason")
        startListening()
    }

    private fun onConnectionLost() {
        postToast("Connection lost — listening again…")
        startListening()
    }

    private fun postToast(msg: String) =
        handler.obtainMessage(Constants.MSG_TOAST, msg).sendToTarget()

    private fun log(msg: String) =
        handler.obtainMessage(Constants.MSG_LOG, msg).sendToTarget()

    private fun cancelAcceptThread() {
        acceptThread?.cancel(); acceptThread = null
    }

    private fun cancelConnectThread() {
        connectThread?.cancel(); connectThread = null
    }

    private fun cancelConnectedThread() {
        connectedThread?.cancel(); connectedThread = null
    }

    // ── AcceptThread ──────────────────────────────────────────────────────────

    private inner class AcceptThread : Thread("BT-Accept") {

        private val serverSocket: BluetoothServerSocket? = runCatching {
            adapter.listenUsingRfcommWithServiceRecord(
                Constants.BT_SERVICE_NAME, Constants.BT_UUID
            )
        }.getOrNull()

        override fun run() {
            log("Listening for connections…")
            var socket: BluetoothSocket?
            while (state != Constants.STATE_CONNECTED) {
                socket = try {
                    serverSocket?.accept() ?: break
                } catch (e: IOException) {
                    break
                }
                synchronized(this@BluetoothService) {
                    when (state) {
                        Constants.STATE_LISTEN,
                        Constants.STATE_CONNECTING -> onSocketConnected(socket, socket.remoteDevice)
                        Constants.STATE_NONE,
                        Constants.STATE_CONNECTED -> runCatching { socket.close() }
                    }
                }
            }
        }

        fun cancel() = runCatching { serverSocket?.close() }
    }

    // ── ConnectThread ─────────────────────────────────────────────────────────

    private inner class ConnectThread(private val device: BluetoothDevice) : Thread("BT-Connect") {

        private val socket: BluetoothSocket? = runCatching {
            device.createRfcommSocketToServiceRecord(Constants.BT_UUID)
        }.getOrNull()

        override fun run() {
            adapter.cancelDiscovery()
            if (socket == null) {
                onConnectionFailed("Could not create socket")
                return
            }
            try {
                socket.connect()
            } catch (e: IOException) {
                runCatching { socket.close() }
                onConnectionFailed(e.message ?: "IO error")
                return
            }
            synchronized(this@BluetoothService) { connectThread = null }
            onSocketConnected(socket, device)
        }

        fun cancel() = runCatching { socket?.close() }
    }

    // ── ConnectedThread ───────────────────────────────────────────────────────

    private inner class ConnectedThread(private val socket: BluetoothSocket) : Thread("BT-Connected") {

        private val dis = DataInputStream(socket.inputStream.buffered(8192))
        private val dos = DataOutputStream(socket.outputStream.buffered(8192))

        // ── Receive loop ──────────────────────────────────────────────────────

        override fun run() {
            log("Transfer channel open.")
            try {
                while (true) {
                    val header = readAsciiLine()   // blocks until "\n" or EOF
                    when (header) {
                        "CSFILE" -> receiveFile()
                        else     -> log("Unknown header: '$header' — ignoring.")
                    }
                }
            } catch (e: IOException) {
                onConnectionLost()
            }
        }

        /**
         * Read ASCII bytes until '\n' (consumed but not included) or EOF.
         * Safe for headers; never used for binary payload.
         */
        private fun readAsciiLine(): String {
            val sb = StringBuilder()
            while (true) {
                val b = dis.read()
                if (b == -1) throw IOException("Stream closed")
                if (b == '\n'.code) return sb.toString()
                sb.append(b.toChar())
            }
        }

        /**
         * Protocol after "CSFILE\n" is confirmed:
         *  1. Read 8-byte Long  → fileSize
         *  2. readFully(fileSize bytes) → raw UTF-8 content
         *  3. Write temp file, extract text, post to UI handler.
         */
        private fun receiveFile() {
            log("Receiving file…")
            val fileSize = dis.readLong()

            if (fileSize <= 0 || fileSize > Constants.BT_MAX_FILE_BYTES) {
                log("Rejected: invalid file size ($fileSize bytes).")
                return
            }

            val bytes = ByteArray(fileSize.toInt())
            dis.readFully(bytes)           // guaranteed to read exactly N bytes

            val tempFile = File(context.cacheDir, Constants.RECEIVED_FILE_NAME)
            tempFile.writeBytes(bytes)

            val text = tempFile.readText(Charsets.UTF_8)
            log("File received — ${bytes.size} bytes.")
            handler.obtainMessage(Constants.MSG_FILE_RECEIVED, text).sendToTarget()
        }

        // ── Send ──────────────────────────────────────────────────────────────

        fun sendFile(filePath: String) {
            try {
                val file = File(filePath)
                if (!file.exists()) {
                    postToast("Temp file missing — aborting.")
                    return
                }
                val bytes = file.readBytes()

                log("Sending ${bytes.size} bytes…")

                // Header
                dos.writeBytes(Constants.PROTOCOL_HEADER)   // "CSFILE\n"
                // Size
                dos.writeLong(bytes.size.toLong())
                // Payload
                dos.write(bytes)
                dos.flush()

                log("File sent successfully (${bytes.size} bytes).")
                handler.obtainMessage(Constants.MSG_FILE_SENT).sendToTarget()

            } catch (e: IOException) {
                postToast("Send error: ${e.message}")
                log("Send failed: ${e.message}")
            }
        }

        fun cancel() = runCatching { socket.close() }
    }
}
