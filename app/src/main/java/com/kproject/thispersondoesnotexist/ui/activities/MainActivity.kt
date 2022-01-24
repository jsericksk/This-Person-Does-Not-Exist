package com.kproject.thispersondoesnotexist.ui.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import coil.ImageLoader
import coil.request.ImageRequest
import com.kproject.thispersondoesnotexist.R
import com.kproject.thispersondoesnotexist.databinding.ActivityMainBinding
import com.kproject.thispersondoesnotexist.ui.viewmodels.MainViewModel
import com.kproject.thispersondoesnotexist.utils.Constants
import com.kproject.thispersondoesnotexist.utils.Utils

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private var currentImageBitmap: Bitmap? = null

    private var readPermissionGranted = false
    private var writePermissionGranted = false
    private lateinit var permissionsLauncher: ActivityResultLauncher<Array<String>>

    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        permissionsLauncher =
                registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                    readPermissionGranted = permissions[Manifest.permission.READ_EXTERNAL_STORAGE]
                        ?: readPermissionGranted
                    writePermissionGranted = permissions[Manifest.permission.WRITE_EXTERNAL_STORAGE]
                        ?: writePermissionGranted

                    if (!readPermissionGranted) {
                        Utils.showToast(this, getString(R.string.storage_permission_info))
                    }
                }
        hasReadAndWritePermission()

        loadImage()

        setOnClicks()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_saved_images -> {
                if (hasReadAndWritePermission()) {
                    startActivity(Intent(this, SavedImagesActivity::class.java))
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setOnClicks() {
        with(binding) {
            ibReload.setOnClickListener {
                currentImageBitmap = null
                loadImage()
            }

            ibDownload.setOnClickListener {
                saveImage()
            }
        }
    }

    private fun loadImage() {
        val loader = ImageLoader(this)
        val request = ImageRequest.Builder(this)
            .data(Constants.getRandomUrl())
            .target(
                onStart = {
                    binding.ivPersonImage.setImageResource(R.drawable.placeholder_loading_image)
                },
                onError = {
                    binding.ivPersonImage.setImageResource(R.drawable.placeholder_error_loading_image)
                },
                onSuccess = { result ->
                    currentImageBitmap = (result as BitmapDrawable).bitmap
                    binding.ivPersonImage.setImageBitmap(currentImageBitmap)
                }
            )
            .build()
        loader.enqueue(request)
    }

    private fun saveImage() {
        if (hasReadAndWritePermission()) {
            currentImageBitmap?.let { bitmap ->
                mainViewModel.saveImage(bitmap)
                Utils.showSnackbar(binding.root, getString(R.string.image_saved_successfully))
            }
        }
    }

    private fun hasReadAndWritePermission(): Boolean {
        val hasReadPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
        val hasWritePermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
        val minSdk29 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

        readPermissionGranted = hasReadPermission
        writePermissionGranted = hasWritePermission || minSdk29

        val permissionsToRequest = mutableListOf<String>()
        if (!writePermissionGranted) {
            permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if (!readPermissionGranted) {
            permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        if (permissionsToRequest.isNotEmpty()) {
            permissionsLauncher.launch(permissionsToRequest.toTypedArray())
            return false
        }
        return true
    }
}