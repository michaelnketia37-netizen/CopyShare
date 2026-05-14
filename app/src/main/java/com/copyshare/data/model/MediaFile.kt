package com.copyshare.data.model

import java.util.Date

data class MediaFile(
    val id: Long,
    val name: String,
    val path: String,
    val size: Long,
    val mimeType: String,
    val dateModified: Long,
    val mediaType: MediaType
) {
    enum class MediaType {
        IMAGE, VIDEO, AUDIO, DOCUMENT, APK, OTHER
    }

    fun getSizeFormatted(): String {
        return when {
            size < 1024 -> "${size}B"
            size < 1024 * 1024 -> "${size / 1024}KB"
            size < 1024 * 1024 * 1024 -> "${size / (1024 * 1024)}MB"
            else -> "${size / (1024 * 1024 * 1024)}GB"
        }
    }

    fun getDateFormatted(): String {
        val sdf = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
        return sdf.format(Date(dateModified))
    }
}