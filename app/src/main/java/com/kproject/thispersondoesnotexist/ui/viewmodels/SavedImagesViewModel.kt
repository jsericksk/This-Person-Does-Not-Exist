package com.kproject.thispersondoesnotexist.ui.viewmodels

import android.app.Application
import android.app.RecoverableSecurityException
import android.content.ContentUris
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.kproject.thispersondoesnotexist.models.SavedImage
import com.kproject.thispersondoesnotexist.utils.Constants
import com.kproject.thispersondoesnotexist.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SavedImagesViewModel(private val app: Application) : AndroidViewModel(app) {
    private val _savedImageList = MutableLiveData<List<SavedImage>>()
    val savedImageList: MutableLiveData<List<SavedImage>> = _savedImageList

    init {
        getSavedImages()
    }

    fun getSavedImages() {
        viewModelScope.launch(Dispatchers.IO) {
            val images = mutableListOf<SavedImage>()
            try {
                val collection: Uri = if (Utils.isSdk29OrAbove()) {
                    MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
                } else {
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                }
                val projection = arrayOf(
                    MediaStore.Images.Media._ID,
                    MediaStore.Images.Media.DISPLAY_NAME,
                )
                val selection = if (Utils.isSdk29OrAbove()) {
                    "${MediaStore.Images.Media.RELATIVE_PATH} LIKE ?"
                } else {
                    "${MediaStore.MediaColumns.DATA} LIKE ?"
                }
                val selectionArgs = arrayOf("%${Constants.APP_FOLDER}%")
                val order = "${MediaStore.Images.ImageColumns.DATE_MODIFIED} ASC"
                app.applicationContext.contentResolver.query(
                    collection,
                    projection,
                    selection,
                    selectionArgs,
                    order
                )?.use { cursor ->
                    val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                    val displayNameColumn =
                            cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
                    while (cursor.moveToNext()) {
                        val id = cursor.getLong(idColumn)
                        val displayName = cursor.getString(displayNameColumn)
                        val contentUri = ContentUris.withAppendedId(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            id
                        )
                        images.add(SavedImage(id, contentUri, displayName))
                    }
                    _savedImageList.postValue(images)
                }
            } catch (e: Exception) {
                Log.d("SavedImagesViewModel", "Error: getSavedImages()")
            }
        }
    }

    /**
     * @param intentSenderLauncher Used to launch an intent that will trigger the system dialog for
     * the user to confirm the deletion of the image. Will only be used on Android 10+.
     */
    fun deleteImage(
        imageUri: Uri,
        intentSenderLauncher: ActivityResultLauncher<IntentSenderRequest>
    ) {
        viewModelScope.launch {
            try {
                app.applicationContext.contentResolver.delete(imageUri, null, null)
                getSavedImages()
            } catch (e: SecurityException) {
                val intentSender = when {
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                        MediaStore.createDeleteRequest(
                            app.applicationContext.contentResolver,
                            listOf(imageUri)
                        ).intentSender
                    }
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                        val recoverableSecurityException = e as? RecoverableSecurityException
                        recoverableSecurityException?.userAction?.actionIntent?.intentSender
                    }
                    else -> null
                }

                intentSender?.let { sender ->
                    intentSenderLauncher.launch(
                        IntentSenderRequest.Builder(sender).build()
                    )
                }
            }
        }
    }

    /**
     * This method will only be used on Android 10 and in cases where the Android system throws
     * a {SecurityException} when trying to delete the image. It is a temporary solution as it
     * only erases the {MediaStore} entry but not the image file itself.
     */
    fun deleteCacheInAndroidQ(imageUri: Uri) {
        app.applicationContext.contentResolver.delete(imageUri, null, null)
        getSavedImages()
    }
}