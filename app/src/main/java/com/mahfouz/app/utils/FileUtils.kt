package com.mahfouz.app.utils

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FileUtils {

    fun createTempImageFile(context: Context): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = File(context.cacheDir, "camera_photos")
        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
    }

    fun persistImageToInternalStorage(context: Context, sourceUri: Uri): String? {
        return try {
            val invoicesDir = File(context.filesDir, "invoices")
            if (!invoicesDir.exists()) {
                invoicesDir.mkdirs()
            }
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.getDefault()).format(Date())
            val targetFile = File(invoicesDir, "invoice_$timeStamp.jpg")

            context.contentResolver.openInputStream(sourceUri)?.use { input: InputStream ->
                FileOutputStream(targetFile).use { output: FileOutputStream ->
                    input.copyTo(output)
                }
            }
            Uri.fromFile(targetFile).toString()
        } catch (e: Exception) {
            e.printStackTrace()
            sourceUri.toString()
        }
    }
}
