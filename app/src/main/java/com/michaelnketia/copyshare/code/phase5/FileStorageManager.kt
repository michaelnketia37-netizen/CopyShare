package com.michaelnketia.copyshare.code.phase5

import android.content.Context
import java.io.File

object FileStorageManager {

    fun saveFile(context: Context, name: String, content: String) {
        val file = File(context.filesDir, name)
        file.writeText(content)
    }

    fun readFile(context: Context, name: String): String {
        val file = File(context.filesDir, name)
        return if(file.exists()) file.readText() else ""
    }
}