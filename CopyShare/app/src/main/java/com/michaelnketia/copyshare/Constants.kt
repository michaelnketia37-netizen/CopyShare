package com.michaelnketia.copyshare

import java.util.UUID

object Constants {

    // Handler message types
    const val MSG_STATE_CHANGE = 1
    const val MSG_FILE_RECEIVED = 2
    const val MSG_FILE_SENT = 3
    const val MSG_DEVICE_CONNECTED = 4
    const val MSG_TOAST = 5
    const val MSG_LOG = 6

    // Connection states
    const val STATE_NONE = 0
    const val STATE_LISTEN = 1
    const val STATE_CONNECTING = 2
    const val STATE_CONNECTED = 3

    // Protocol
    // "CSFILE\n" header (7 bytes) + 8-byte Long (file size) + N bytes (file content)
    const val PROTOCOL_HEADER = "CSFILE\n"
    const val TEMP_FILE_NAME = "copyshare_send.txt"
    const val RECEIVED_FILE_NAME = "copyshare_received.txt"

    // Bluetooth
    val BT_UUID: UUID = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66")
    const val BT_SERVICE_NAME = "CopyShare"
    const val BT_MAX_FILE_BYTES = 5 * 1024 * 1024 // 5 MB safety cap
}
