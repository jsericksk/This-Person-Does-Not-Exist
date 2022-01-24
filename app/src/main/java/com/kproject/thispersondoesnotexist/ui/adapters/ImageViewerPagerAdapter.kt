package com.kproject.thispersondoesnotexist.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.kproject.thispersondoesnotexist.databinding.PagerImageBinding
import com.kproject.thispersondoesnotexist.models.SavedImage

class ImageViewerPagerAdapter(
    private val imageList: List<SavedImage>
) : RecyclerView.Adapter<ImageViewerPagerAdapter.ImageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val binding = PagerImageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ImageViewHolder(binding)
    }

    override fun onBindViewHolder(viewHolder: ImageViewHolder, position: Int) {
        viewHolder.bindView(imageList[position])
    }

    override fun getItemCount(): Int = imageList.size

    inner class ImageViewHolder(
        private val binding: PagerImageBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bindView(image: SavedImage) {
            with (binding) {
                ivImage.load(image.contentUri)
            }
        }
    }
}