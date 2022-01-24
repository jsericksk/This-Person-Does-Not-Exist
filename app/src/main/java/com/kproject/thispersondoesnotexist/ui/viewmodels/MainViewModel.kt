package com.kproject.thispersondoesnotexist.ui.viewmodels

import android.app.Application
import android.content.ContentValues
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import com.kproject.thispersondoesnotexist.utils.Constants
import com.kproject.thispersondoesnotexist.utils.Utils
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

class MainViewModel(val app: Application) : AndroidViewModel(app) {

    fun saveImage(bitmap: Bitmap) {
        var imageFile: File? = null
        val imageName = "Image ${System.currentTimeMillis()}.jpg"
        val folderToSave = Constants.APP_FOLDER
        val imageOutputStream: OutputStream?
        if (Utils.isSdk29OrAbove()) {
            val values = ContentValues()
            values.put(MediaStore.Images.Media.DISPLAY_NAME, imageName)
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            values.put(MediaStore.Images.Media.RELATIVE_PATH, folderToSave)
            val uri: Uri? =
                    app.applicationContext.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            imageOutputStream = app.applicationContext.contentResolver.openOutputStream(uri!!)
        } else {
            val imagePath =
                    Environment.getExternalStoragePublicDirectory(folderToSave).toString()
            imageFile = File(imagePath, imageName)
            if (!File(imagePath).exists()) {
                File(imagePath).mkdirs()
            }
            imageOutputStream = FileOutputStream(imageFile)
        }

        imageOutputStream.use { outputStream ->
            if (!bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)) {
                throw IOException("Unable to save image.")
            }

            /**
             * Scans the file so it can appear in the gallery right after insertion.
             */
            imageFile?.let {
                MediaScannerConnection.scanFile(
                    app.applicationContext,
                    arrayOf(it.toString()),
                    null
                ) { path, uri -> }
            }
        }
    }
}