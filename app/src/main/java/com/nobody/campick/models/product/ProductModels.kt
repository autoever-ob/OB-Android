package com.nobody.campick.models.product

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * 매물 검색 필터 요청
 */
@Serializable
data class ProductFilterRequest(
    val keyword: String? = null,
    val mileageFrom: Int? = null,
    val mileageTo: Int? = null,
    val costFrom: Int? = null,
    val costTo: Int? = null,
    val generationFrom: Int? = null,
    val generationTo: Int? = null,
    val options: List<String>? = null,
    val types: List<String>? = null
)

/**
 * 매물 정렬 옵션
 */
enum class ProductSort(val queryValue: String) {
    CREATED_AT_DESC("createdAt,desc"),
    COST_ASC("cost,asc"),
    COST_DESC("cost,desc"),
    MILEAGE_ASC("mileage,asc"),
    GENERATION_DESC("generation,desc")
}

/**
 * 매물 목록 조회 응답
 */
@Serializable
data class ProductListResponse(
    val status: Int,
    val success: Boolean,
    val message: String,
    val data: ProductPageData
)

/**
 * 매물 페이지 데이터
 */
@Serializable
data class ProductPageData(
    val totalElements: Int,
    val totalPages: Int,
    val page: Int,
    val size: Int,
    val content: List<ProductItemDTO>,
    val last: Boolean
)

/**
 * 매물 목록 아이템 DTO
 */
@Serializable
data class ProductItemDTO(
    val productId: Int,
    val title: String,
    val cost: Int? = null,
    val price: String? = null,
    val generation: Int? = null,
    val fuelType: String = "",
    val transmission: String = "",
    @Serializable(with = FlexibleStringSerializer::class)
    val mileage: String = "",
    val vehicleType: String = "",
    val vehicleModel: String = "",
    val location: String = "",
    val createdAt: String,
    @SerialName("thumbNail")
    val thumbnail: String? = null,
    @SerialName("productImageUrl")
    val productImageUrl: String? = null,
    val isLiked: Boolean = false,
    val likeCount: Int = 0,
    val status: String = "AVAILABLE"
) {
    val thumbnailUrl: String?
        get() = thumbnail ?: productImageUrl

    val priceValue: String
        get() = when {
            cost != null -> cost.toString()
            price != null -> price
            else -> "0"
        }
}

/**
 * Int 또는 String을 String으로 변환하는 Serializer
 */
object FlexibleStringSerializer : KSerializer<String> {
    override val descriptor = PrimitiveSerialDescriptor("FlexibleString", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): String {
        return try {
            decoder.decodeInt().toString()
        } catch (e: Exception) {
            try {
                decoder.decodeString()
            } catch (e: Exception) {
                ""
            }
        }
    }

    override fun serialize(encoder: Encoder, value: String) {
        encoder.encodeString(value)
    }
}

/**
 * 매물 상세 조회 응답
 */
@Serializable
data class ProductDetailResponse(
    val status: Int,
    val success: Boolean,
    val message: String,
    val data: ProductDetailDTO
)

/**
 * 매물 상세 DTO
 */
@Serializable
data class ProductDetailDTO(
    val productId: Int,
    val title: String,
    val price: String,
    val generation: Int? = null,
    val fuelType: String = "",
    val transmission: String = "",
    val mileage: String = "",
    val vehicleType: String = "",
    val vehicleModel: String = "",
    val location: String = "",
    val option: List<ProductOptionDTO> = emptyList(),
    val user: ProductSellerDTO,
    val plateHash: String = "",
    val description: String = "",
    val productImageUrl: List<String> = emptyList(),
    val createdAt: String,
    val status: String = "AVAILABLE",
    val isLiked: Boolean = false,
    val likeCount: Int = 0
)

/**
 * 매물 옵션 DTO
 */
@Serializable
data class ProductOptionDTO(
    val optionName: String,
    val isInclude: Boolean
)

/**
 * 매물 판매자 DTO
 */
@Serializable
data class ProductSellerDTO(
    val nickName: String,
    val role: String,
    val rating: Double? = null,
    val sellingCount: Int,
    val completeCount: Int,
    val userId: Int
)

/**
 * 찜하기 응답
 */
@Serializable
data class ProductLikeResponse(
    val status: Int,
    val success: Boolean,
    val message: String,
    val data: String
)

/**
 * 매물 상태 변경 요청
 */
@Serializable
data class ProductStatusUpdateRequest(
    val productId: Int,
    val status: String
)

/**
 * 매물 상태 변경 응답
 */
@Serializable
data class ProductStatusUpdateResponse(
    val status: Int,
    val success: Boolean,
    val message: String,
    val data: String
)