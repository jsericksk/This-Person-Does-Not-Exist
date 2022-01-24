package com.kproject.thispersondoesnotexist.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.kproject.thispersondoesnotexist.databinding.ActivitySavedImagesBinding
import com.kproject.thispersondoesnotexist.models.SavedImage
import com.kproject.thispersondoesnotexist.ui.adapters.SavedImagesAdapter
import com.kproject.thispersondoesnotexist.ui.viewmodels.SavedImagesViewModel

class SavedImagesActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySavedImagesBinding

    private val savedImagesViewModel: SavedImagesViewModel by viewModels()

    /**
     * Start the activity for result to know if there was any modification in the list of
     * saved images. If the result is ok, it means that one or more images have been deleted
     * and the list must be updated.
     */
    private val startActivityForResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    savedImagesViewModel.getSavedImages()
                }
            }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySavedImagesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        savedImagesViewModel.savedImageList.observe(this) { list ->
            list?.let { imageList ->
                if (imageList.isEmpty()) {
                    binding.rvImageList.visibility = View.GONE
                    binding.llEmptyImageList.visibility = View.VISIBLE
                }
                initializeRecyclerView(imageList)
            }
        }
    }

    private fun initializeRecyclerView(imageList: List<SavedImage>) {
        val layoutManager = GridLayoutManager(this, 3)
        val adapter = SavedImagesAdapter(
            imageList, onItemClickListener = { image ->
                val intent = Intent(this, ImageViewerActivity::class.java)
                intent.putExtra("imagePosition", imageList.indexOf(image))
                startActivityForResult.launch(intent)
            }
        )
        binding.rvImageList.adapter = adapter
        binding.rvImageList.layoutManager = layoutManager
    }
}