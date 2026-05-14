package com.michaelnketia.copyshare.code

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object ZipUtils {

    fun zipProject(folder: File, zipFile: File) {

        ZipOutputStream(FileOutputStream(zipFile)).use { zipOut ->

            folder.walkTopDown().forEach { file ->

                if (file.isFile) {

                    val entryName = file.relativeTo(folder).path

                    zipOut.putNextEntry(ZipEntry(entryName))

                    FileInputStream(file).use {
                        it.copyTo(zipOut)
                    }

                    zipOut.closeEntry()
                }
            }
        }
    }
}