package com.nobody.campick.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.compose.ui.graphics.toArgb
import com.bumptech.glide.Glide
import com.nobody.campick.R
import com.nobody.campick.resources.theme.AppColors
import com.nobody.campick.databinding.ItemEmptyProductsBinding
import com.nobody.campick.databinding.ItemProductCardBinding
import com.nobody.campick.databinding.ItemLoadMoreBinding
import com.nobody.campick.models.Product
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class ProfileProductAdapter(
    private val onProductClick: (Product) -> Unit,
    private val onLoadMore: () -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val items = mutableListOf<ProfileProductItem>()

    companion object {
        const val TYPE_EMPTY = 0
        const val TYPE_PRODUCT = 1
        const val TYPE_LOAD_MORE = 2
    }

    sealed class ProfileProductItem {
        object Empty : ProfileProductItem()
        data class ProductItem(val product: Product) : ProfileProductItem()
        object LoadMore : ProfileProductItem()
    }

    fun submitList(products: List<Product>, hasMore: Boolean = false) {
        items.clear()

        if (products.isEmpty()) {
            items.add(ProfileProductItem.Empty)
        } else {
            items.addAll(products.map { ProfileProductItem.ProductItem(it) })
            if (hasMore) {
                items.add(ProfileProductItem.LoadMore)
            }
        }

        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is ProfileProductItem.Empty -> TYPE_EMPTY
            is ProfileProductItem.ProductItem -> TYPE_PRODUCT
            is ProfileProductItem.LoadMore -> TYPE_LOAD_MORE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_EMPTY -> {
                val binding = ItemEmptyProductsBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                EmptyViewHolder(binding)
            }
            TYPE_PRODUCT -> {
                val binding = ItemProductCardBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                ProductViewHolder(binding)
            }
            TYPE_LOAD_MORE -> {
                val binding = ItemLoadMoreBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                LoadMoreViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ProductViewHolder -> {
                val item = items[position] as ProfileProductItem.ProductItem
                holder.bind(item.product)
            }
            is LoadMoreViewHolder -> {
                holder.bind()
            }
            // EmptyViewHolder는 바인딩할 데이터가 없음
        }
    }

    override fun getItemCount(): Int = items.size

    inner class ProductViewHolder(
        private val binding: ItemProductCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(product: Product) {
            binding.apply {
                // 제품 이미지
                if (!product.thumbNailUrl.isNullOrEmpty()) {
                    Glide.with(itemView.context)
                        .load(product.thumbNailUrl)
                        .placeholder(R.drawable.ic_car)
                        .error(R.drawable.ic_car)
                        .into(imageViewProduct)
                } else {
                    imageViewProduct.setImageResource(R.drawable.ic_car)
                }

                // 제품 정보 (Swift와 동일하게)
                textViewTitle.text = product.title
                textViewPrice.text = formatCost(product.cost)
                textViewGeneration.text = product.generation.toString()
                textViewMileage.text = formatMileage(product.mileage) + "km"
                textViewLocation.text = shortLocation(product.location)
                textViewDate.text = formatDate(product.createdAtString)

                // 상태 설정
                setStatusChip(product.status)

                // 클릭 리스너
                root.setOnClickListener {
                    onProductClick(product)
                }
            }
        }

        private fun setStatusChip(status: String) {
            val (text, color, backgroundRes) = when (status.uppercase()) {
                "AVAILABLE", "ACTIVE" -> Triple("판매중", AppColors.brandLightGreen, R.drawable.status_chip_active)
                "RESERVE", "RESERVED" -> Triple("예약중", AppColors.brandOrange, R.drawable.status_chip_reserved)
                "SOLD" -> Triple("판매완료", AppColors.brandWhite60, R.drawable.status_chip_sold)
                else -> Triple(status, AppColors.brandOrange, R.drawable.status_chip_background)
            }

            binding.textViewStatus.apply {
                this.text = text
                setTextColor(color.toArgb())
                setBackgroundResource(backgroundRes)
            }
        }

        private fun formatCost(cost: Int): String {
            return try {
                val formatter = NumberFormat.getNumberInstance(Locale.getDefault())
                "${formatter.format(cost)}만원"
            } catch (e: Exception) {
                "${cost}만원"
            }
        }

        private fun formatMileage(mileage: Int): String {
            // Swift 로직과 동일: 10만 이상은 12.3만, 1만 이상은 1.2만, 1만 미만은 1,234
            return when {
                mileage >= 100000 -> {
                    val value = mileage / 10000.0
                    String.format(Locale.getDefault(), "%.1f만", value)
                }
                mileage >= 10000 -> {
                    val value = mileage / 10000.0
                    String.format(Locale.getDefault(), "%.1f만", value)
                }
                else -> {
                    NumberFormat.getNumberInstance(Locale.getDefault()).format(mileage)
                }
            }
        }

        private fun shortLocation(fullLocation: String): String {
            // Swift 로직: "경상북도 구미시" -> "구미시", "서울특별시 강남구" -> "강남구"
            val parts = fullLocation.split(Regex("\\s+")).filter { it.isNotEmpty() }
            return parts.lastOrNull() ?: fullLocation
        }

        private fun formatDate(dateString: String): String {
            return try {
                // ISO8601 날짜 문자열을 파싱해서 MM.dd 형식으로 변환
                val inputFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                val outputFormatter = SimpleDateFormat("MM.dd", Locale.getDefault())
                val date = inputFormatter.parse(dateString)
                outputFormatter.format(date ?: Date())
            } catch (e: Exception) {
                // 파싱 실패 시 현재 날짜로 대체
                val formatter = SimpleDateFormat("MM.dd", Locale.getDefault())
                formatter.format(Date())
            }
        }
    }

    inner class EmptyViewHolder(
        binding: ItemEmptyProductsBinding
    ) : RecyclerView.ViewHolder(binding.root)

    inner class LoadMoreViewHolder(
        private val binding: ItemLoadMoreBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind() {
            binding.root.setOnClickListener {
                onLoadMore()
            }
        }
    }
}