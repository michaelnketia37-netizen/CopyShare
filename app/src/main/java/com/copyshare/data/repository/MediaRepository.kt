package com.copyshare.data.repository

import android.content.Context
import android.provider.MediaStore
import com.copyshare.data.model.MediaFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class MediaRepository(private val context: Context) {

    suspend fun getAllMedia(): List<MediaFile> = withContext(Dispatchers.IO) {
        val mediaList = mutableListOf<MediaFile>()
        mediaList.addAll(getImages())
        mediaList.addAll(getVideos())
        mediaList.addAll(getAudio())
        mediaList.addAll(getDocuments())
        mediaList.addAll(getApkFiles())
        mediaList.sortByDescending { it.dateModified }
        mediaList
    }

    suspend fun getImages(): List<MediaFile> = withContext(Dispatchers.IO) {
        val images = mutableListOf<MediaFile>()
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.MIME_TYPE,
            MediaStore.Images.Media.DATE_MODIFIED
        )

        val cursor = context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            "${MediaStore.Images.Media.DATE_MODIFIED} DESC"
        )

        cursor?.use {
            val idColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val nameColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val dataColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            val sizeColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
            val mimeColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE)
            val dateColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED)

            while (it.moveToNext()) {
                val id = it.getLong(idColumn)
                val name = it.getString(nameColumn)
                val path = it.getString(dataColumn)
                val size = it.getLong(sizeColumn)
                val mime = it.getString(mimeColumn)
                val date = it.getLong(dateColumn) * 1000

                images.add(
                    MediaFile(
                        id = id,
                        name = name,
                        path = path,
                        size = size,
                        mimeType = mime,
                        dateModified = date,
                        mediaType = MediaFile.MediaType.IMAGE
                    )
                )
            }
        }
        images
    }

    suspend fun getVideos(): List<MediaFile> = withContext(Dispatchers.IO) {
        val videos = mutableListOf<MediaFile>()
        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.DATA,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.MIME_TYPE,
            MediaStore.Video.Media.DATE_MODIFIED
        )

        val cursor = context.contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            "${MediaStore.Video.Media.DATE_MODIFIED} DESC"
        )

        cursor?.use {
            val idColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val nameColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
            val dataColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
            val sizeColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
            val mimeColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE)
            val dateColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_MODIFIED)

            while (it.moveToNext()) {
                val id = it.getLong(idColumn)
                val name = it.getString(nameColumn)
                val path = it.getString(dataColumn)
                val size = it.getLong(sizeColumn)
                val mime = it.getString(mimeColumn)
                val date = it.getLong(dateColumn) * 1000

                videos.add(
                    MediaFile(
                        id = id,
                        name = name,
                        path = path,
                        size = size,
                        mimeType = mime,
                        dateModified = date,
                        mediaType = MediaFile.MediaType.VIDEO
                    )
                )
            }
        }
        videos
    }

    suspend fun getAudio(): List<MediaFile> = withContext(Dispatchers.IO) {
        val audio = mutableListOf<MediaFile>()
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.MIME_TYPE,
            MediaStore.Audio.Media.DATE_MODIFIED
        )

        val cursor = context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            "${MediaStore.Audio.Media.DATE_MODIFIED} DESC"
        )

        cursor?.use {
            val idColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val nameColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
            val dataColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val sizeColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
            val mimeColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE)
            val dateColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_MODIFIED)

            while (it.moveToNext()) {
                val id = it.getLong(idColumn)
                val name = it.getString(nameColumn)
                val path = it.getString(dataColumn)
                val size = it.getLong(sizeColumn)
                val mime = it.getString(mimeColumn)
                val date = it.getLong(dateColumn) * 1000

                audio.add(
                    MediaFile(
                        id = id,
                        name = name,
                        path = path,
                        size = size,
                        mimeType = mime,
                        dateModified = date,
                        mediaType = MediaFile.MediaType.AUDIO
                    )
                )
            }
        }
        audio
    }

    suspend fun getDocuments(): List<MediaFile> = withContext(Dispatchers.IO) {
        val documents = mutableListOf<MediaFile>()
        val downloadsDir = context.getExternalFilesDir("Downloads")
        val documentsDir = context.getExternalFilesDir("Documents")

        listOf(downloadsDir, documentsDir).filterNotNull().forEach { dir ->
            dir.listFiles()?.forEach { file ->
                if (isDocument(file.name)) {
                    documents.add(
                        MediaFile(
                            id = file.hashCode().toLong(),
                            name = file.name,
                            path = file.absolutePath,
                            size = file.length(),
                            mimeType = getMimeType(file.name),
                            dateModified = file.lastModified(),
                            mediaType = MediaFile.MediaType.DOCUMENT
                        )
                    )
                }
            }
        }
        documents.sortByDescending { it.dateModified }
        documents
    }

    suspend fun getApkFiles(): List<MediaFile> = withContext(Dispatchers.IO) {
        val apks = mutableListOf<MediaFile>()
        val downloadsDir = context.getExternalFilesDir("Downloads")

        downloadsDir?.listFiles()?.forEach { file ->
            if (file.extension == "apk") {
                apks.add(
                    MediaFile(
                        id = file.hashCode().toLong(),
                        name = file.name,
                        path = file.absolutePath,
                        size = file.length(),
                        mimeType = "application/vnd.android.package-archive",
                        dateModified = file.lastModified(),
                        mediaType = MediaFile.MediaType.APK
                    )
                )
            }
        }
        apks.sortByDescending { it.dateModified }
        apks
    }

    private fun isDocument(fileName: String): Boolean {
        val docExtensions = listOf("pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt")
        return docExtensions.any { fileName.endsWith(it, ignoreCase = true) }
    }

    private fun getMimeType(fileName: String): String {
        return when {
            fileName.endsWith(".pdf") -> "application/pdf"
            fileName.endsWith(".doc") || fileName.endsWith(".docx") -> "application/msword"
            fileName.endsWith(".xls") || fileName.endsWith(".xlsx") -> "application/vnd.ms-excel"
            fileName.endsWith(".txt") -> "text/plain"
            else -> "*/*"
        }
    }
}