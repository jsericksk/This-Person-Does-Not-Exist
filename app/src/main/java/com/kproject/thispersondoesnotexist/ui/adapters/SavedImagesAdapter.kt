package com.kproject.thispersondoesnotexist.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.size.Scale
import com.kproject.thispersondoesnotexist.databinding.RecyclerviewItemImagesBinding
import com.kproject.thispersondoesnotexist.models.SavedImage

class SavedImagesAdapter(
    private val imageList: List<SavedImage>,
    private val onItemClickListener: ((image: SavedImage) -> Unit)
) : RecyclerView.Adapter<SavedImagesAdapter.ImageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, view: Int): ImageViewHolder {
        val binding = RecyclerviewItemImagesBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        return ImageViewHolder(binding)
    }

    override fun getItemCount() = imageList.size

    override fun onBindViewHolder(viewHolder: ImageViewHolder, position: Int) {
        viewHolder.bindView(imageList[position])
    }

    inner class ImageViewHolder(
        private val binding: RecyclerviewItemImagesBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bindView(image: SavedImage) {
            with (binding) {
                ivImage.load(image.contentUri) {
                    scale(Scale.FIT)
                }
            }

            itemView.setOnClickListener {
                onItemClickListener(image)
            }
        }
    }
}