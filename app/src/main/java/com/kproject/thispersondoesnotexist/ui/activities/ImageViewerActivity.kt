package com.kproject.thispersondoesnotexist.ui.activities

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.kproject.thispersondoesnotexist.R
import com.kproject.thispersondoesnotexist.databinding.ActivityImageViewerBinding
import com.kproject.thispersondoesnotexist.models.SavedImage
import com.kproject.thispersondoesnotexist.ui.adapters.ImageViewerPagerAdapter
import com.kproject.thispersondoesnotexist.ui.viewmodels.SavedImagesViewModel
import com.kproject.thispersondoesnotexist.utils.Utils

class ImageViewerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityImageViewerBinding
    private lateinit var deleteImageLauncher: ActivityResultLauncher<IntentSenderRequest>

    private val savedImagesViewModel: SavedImagesViewModel by viewModels()
    private var imageList: List<SavedImage>? = null
    private var currentImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImageViewerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val initialImagePosition = intent.extras?.getInt("imagePosition")
        binding.vpImagePager.registerOnPageChangeCallback(onPageChangeListener)

        savedImagesViewModel.savedImageList.observe(this) { list ->
            list?.let { imageList ->
                if (imageList.isEmpty()) {
                    finish()
                }

                this.imageList = imageList
                val adapter = ImageViewerPagerAdapter(imageList)
                binding.vpImagePager.adapter = adapter
                binding.vpImagePager.setCurrentItem(initialImagePosition!!, false)
            }
        }

        // Only used on Android 10 or higher
        deleteImageLauncher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
            if (it.resultCode == RESULT_OK) {
                if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
                    currentImageUri?.let { savedImagesViewModel.deleteCacheInAndroidQ(it) }
                } else {
                    Utils.showToast(this, getString(R.string.image_successfully_deleted))
                }
            } else {
                Utils.showToast(this, getString(R.string.error_deleting_image))
            }
        }
    }

    private fun showImageDeleteConfirmationDialog(imageUri: Uri) {
        AlertDialog.Builder(this)
            .setTitle(R.string.dialog_delete_image)
            .setMessage(R.string.dialog_delete_image_message)
            .setPositiveButton("OK") { dialogInterface, position ->
                setResult(RESULT_OK)
                savedImagesViewModel.deleteImage(imageUri, deleteImageLauncher)
                dialogInterface.dismiss()
            }
            .setNegativeButton(R.string.button_cancel) { dialogInterface, position ->
                dialogInterface.dismiss()
            }
            .show()
    }

    private val onPageChangeListener = object : ViewPager2.OnPageChangeCallback() {

        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            val imageUri = imageList!![position].contentUri
            supportActionBar?.title = imageList!![position].name

            binding.ibShare.setOnClickListener {
                val intent = Intent(Intent.ACTION_SEND)
                intent.type = "image/*"
                intent.putExtra(Intent.EXTRA_STREAM, imageUri)
                startActivity(
                    Intent.createChooser(intent, resources.getString(R.string.share_image))
                )
            }

            binding.ibDelete.setOnClickListener {
                showImageDeleteConfirmationDialog(imageUri)
            }
        }
    }
}