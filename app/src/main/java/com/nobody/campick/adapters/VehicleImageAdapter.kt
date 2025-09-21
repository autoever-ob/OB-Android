package com.nobody.campick.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.nobody.campick.R
import com.nobody.campick.databinding.ItemVehicleImageBinding
import com.nobody.campick.models.vehicle.VehicleImage

sealed class ImageListItem {
    object GalleryButton : ImageListItem()
    object CameraButton : ImageListItem()
    object MainImageButton : ImageListItem()
    data class ImageItem(val vehicleImage: VehicleImage) : ImageListItem()
}

class VehicleImageAdapter(
    private val onGalleryClick: () -> Unit,
    private val onCameraClick: () -> Unit,
    private val onMainImageClick: () -> Unit,
    private val onImageClick: (String) -> Unit,
    private val onImageRemove: (String) -> Unit,
    private val onSetMainImage: (String) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val items = mutableListOf<ImageListItem>()
    private val maxImages = 9

    init {
        updateItems()
    }

    fun submitList(images: List<VehicleImage>) {
        items.clear()
        items.addAll(images.map { ImageListItem.ImageItem(it) })
        updateItems()
        notifyDataSetChanged()
    }

    private fun updateItems() {
        val imageCount = items.count { it is ImageListItem.ImageItem }

        // 기존 버튼들 제거
        items.removeAll {
            it is ImageListItem.GalleryButton ||
            it is ImageListItem.CameraButton ||
            it is ImageListItem.MainImageButton
        }

        // Swift와 동일한 로직으로 버튼 추가
        if (imageCount < 10) {
            items.add(ImageListItem.GalleryButton)
        }

        if (imageCount < 9) {
            items.add(ImageListItem.CameraButton)
        }

        if (imageCount < 8) {
            items.add(ImageListItem.MainImageButton)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is ImageListItem.GalleryButton -> VIEW_TYPE_GALLERY_BUTTON
            is ImageListItem.CameraButton -> VIEW_TYPE_CAMERA_BUTTON
            is ImageListItem.MainImageButton -> VIEW_TYPE_MAIN_IMAGE_BUTTON
            is ImageListItem.ImageItem -> VIEW_TYPE_IMAGE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_GALLERY_BUTTON -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_add_gallery_button, parent, false)
                GalleryButtonViewHolder(view)
            }
            VIEW_TYPE_CAMERA_BUTTON -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_add_camera_button, parent, false)
                CameraButtonViewHolder(view)
            }
            VIEW_TYPE_MAIN_IMAGE_BUTTON -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_add_main_image_button, parent, false)
                MainImageButtonViewHolder(view)
            }
            VIEW_TYPE_IMAGE -> {
                val binding = ItemVehicleImageBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                ImageViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Unknown view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is GalleryButtonViewHolder -> holder.bind()
            is CameraButtonViewHolder -> holder.bind()
            is MainImageButtonViewHolder -> holder.bind()
            is ImageViewHolder -> {
                val item = items[position] as ImageListItem.ImageItem
                holder.bind(item.vehicleImage)
            }
        }
    }

    override fun getItemCount(): Int = items.size

    inner class GalleryButtonViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind() {
            itemView.setOnClickListener {
                onGalleryClick()
            }
        }
    }

    inner class CameraButtonViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind() {
            itemView.setOnClickListener {
                onCameraClick()
            }
        }
    }

    inner class MainImageButtonViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind() {
            itemView.setOnClickListener {
                onMainImageClick()
            }
        }
    }

    inner class ImageViewHolder(
        private val binding: ItemVehicleImageBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: VehicleImage) {
            binding.apply {
                Glide.with(itemView.context)
                    .load(item.imageUri)
                    .centerCrop()
                    .placeholder(R.drawable.placeholder_image)
                    .into(imageView)

                textViewMain.isVisible = item.isMain
                buttonSetMain.isVisible = !item.isMain
                buttonRemove.isVisible = true

                buttonSetMain.setOnClickListener {
                    onSetMainImage(item.id)
                }

                buttonRemove.setOnClickListener {
                    onImageRemove(item.id)
                }

                root.setOnClickListener {
                    onImageClick(item.id)
                }
            }
        }
    }

    companion object {
        private const val VIEW_TYPE_GALLERY_BUTTON = 0
        private const val VIEW_TYPE_CAMERA_BUTTON = 1
        private const val VIEW_TYPE_MAIN_IMAGE_BUTTON = 2
        private const val VIEW_TYPE_IMAGE = 3
    }
}