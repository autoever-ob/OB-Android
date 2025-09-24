package com.nobody.campick.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.nobody.campick.R
import com.nobody.campick.databinding.ItemVehicleImageBinding
import com.nobody.campick.models.vehicle.VehicleImage

sealed class ImageListItem {
    object AddImageButton : ImageListItem()
    data class ImageItem(val vehicleImage: VehicleImage) : ImageListItem()
}

class VehicleImageAdapter(
    private val onAddImageClick: (View) -> Unit,
    private val onImageClick: (String) -> Unit,
    private val onImageRemove: (String) -> Unit,
    private val onSetMainImage: (String) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val items = mutableListOf<ImageListItem>()
    private val maxImages = 10

    init {
        updateItems()
    }

    fun submitList(images: List<VehicleImage>) {
        items.clear()
        items.addAll(images.map { ImageListItem.ImageItem(it) })
        updateItems()
        println("🖼️ VehicleImageAdapter: submitList called with ${images.size} images, total items: ${items.size}")
        notifyDataSetChanged()
    }

    private fun updateItems() {
        val imageCount = items.count { it is ImageListItem.ImageItem }

        // 기존 버튼들 제거
        items.removeAll { it is ImageListItem.AddImageButton }

        // 최대 10개 이미지까지 허용, 그보다 적으면 추가 버튼 표시
        if (imageCount < maxImages) {
            items.add(ImageListItem.AddImageButton)
            println("🖼️ VehicleImageAdapter: Add button added, imageCount: $imageCount, maxImages: $maxImages")
        } else {
            println("🖼️ VehicleImageAdapter: No button added, imageCount: $imageCount")
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (val item = items[position]) {
            is ImageListItem.AddImageButton -> VIEW_TYPE_ADD_IMAGE_BUTTON
            is ImageListItem.ImageItem -> VIEW_TYPE_IMAGE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_ADD_IMAGE_BUTTON -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_add_gallery_button, parent, false)
                AddImageButtonViewHolder(view)
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
            is AddImageButtonViewHolder -> holder.bind()
            is ImageViewHolder -> {
                val item = items[position] as ImageListItem.ImageItem
                holder.bind(item.vehicleImage)
            }
        }
    }

    override fun getItemCount(): Int = items.size

    inner class AddImageButtonViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind() {
            itemView.setOnClickListener {
                onAddImageClick(itemView)
            }
        }
    }

    inner class ImageViewHolder(
        private val binding: ItemVehicleImageBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: VehicleImage) {
            binding.apply {
                // 이미지 로드 (uploadedUrl 우선, 없으면 imageUri)
                val imageSource = item.uploadedUrl ?: item.imageUri
                Glide.with(itemView.context)
                    .load(imageSource)
                    .centerCrop()
                    .placeholder(R.drawable.placeholder_image)
                    .into(imageView)

                // 업로드 상태에 따른 UI 표시
                val isUploaded = item.uploadedUrl != null

                // 메인 이미지 여부에 따른 테두리 표시 (iOS와 동일)
                if (item.isMain) {
                    imageView.setBackgroundResource(R.drawable.main_image_border)
                } else {
                    imageView.setBackgroundResource(R.drawable.styled_input_background)
                }

                // 메인 라벨 표시 (iOS와 동일)
                textViewMain.isVisible = item.isMain

                // 로딩 오버레이 표시 (iOS와 동일 - 업로드 중일 때만)
                loadingOverlay.isVisible = !isUploaded

                // 삭제 버튼은 업로드된 이미지만 표시 (iOS와 동일)
                buttonRemove.isVisible = isUploaded

                // "메인으로 설정" 버튼은 사용하지 않음 (클릭으로 대체)
                buttonSetMain.isVisible = false

                buttonRemove.setOnClickListener {
                    if (isUploaded) {
                        onImageRemove(item.id)
                    }
                }

                // 이미지 클릭으로 메인 이미지 설정 (iOS와 동일)
                root.setOnClickListener {
                    if (isUploaded) {
                        onSetMainImage(item.id)
                    }
                }
            }
        }
    }


    companion object {
        private const val VIEW_TYPE_ADD_IMAGE_BUTTON = 0
        private const val VIEW_TYPE_IMAGE = 1
    }
}