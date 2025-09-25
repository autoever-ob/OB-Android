package com.nobody.campick.services

import com.nobody.campick.models.vehicle.VehicleDetailViewData
import com.nobody.campick.models.vehicle.VehicleRegistrationRequest
import com.nobody.campick.models.vehicle.ProductInfoResponse
import com.nobody.campick.models.product.ProductFilterRequest
import com.nobody.campick.models.product.ProductPageData
import com.nobody.campick.models.product.ProductDetailDTO
import com.nobody.campick.models.product.ProductLikeResponse
import com.nobody.campick.models.product.ProductSort
import com.nobody.campick.models.ImageUploadResponse
import com.nobody.campick.services.network.APIService
import com.nobody.campick.services.network.ApiResult
import com.nobody.campick.services.network.Endpoint
import com.nobody.campick.utils.ImageCompressor
import kotlinx.serialization.builtins.serializer

object VehicleService {

    /**
     * 매물 상세 정보 조회
     */
    suspend fun fetchVehicleDetail(productId: String): ApiResult<VehicleDetailViewData> {
        return APIService.get<VehicleDetailViewData>(
            endpoint = Endpoint.ProductDetail(productId)
        )
    }

    /**
     * 매물 등록
     */
    suspend fun registerVehicle(request: VehicleRegistrationRequest): ApiResult<Int> {
        val jsonBody = kotlinx.serialization.json.Json.encodeToString(
            VehicleRegistrationRequest.serializer(),
            request
        )
        return APIService.post<Int>(
            endpoint = Endpoint.RegisterProduct,
            body = jsonBody
        )
    }

    /**
     * 매물 수정 (PATCH /api/product/{productId})
     */
    suspend fun updateProduct(productId: String, request: VehicleRegistrationRequest): ApiResult<Int> {
        val jsonBody = kotlinx.serialization.json.Json.encodeToString(
            VehicleRegistrationRequest.serializer(),
            request
        )
        return APIService.patch<Int>(
            endpoint = Endpoint.ProductDetail(productId),
            body = jsonBody
        )
    }

    /**
     * 다중 이미지 업로드 (iOS 스타일)
     */
    suspend fun uploadImages(images: List<ByteArray>): ApiResult<List<String>> {
        if (images.isEmpty()) {
            return ApiResult.Success(emptyList())
        }

        val compressedImages = mutableListOf<Pair<ByteArray, String>>()
        images.forEachIndexed { index, imageData ->
            val compressed = ImageCompressor.compressImage(imageData, 1.0)
            if (compressed != null) {
                compressedImages.add(compressed to "image_$index.jpg")
            } else {
                return ApiResult.Error("이미지 압축 실패")
            }
        }

        return when (val result = APIService.uploadFiles<ImageUploadResponse>(
            endpoint = Endpoint.UploadImage,
            files = compressedImages,
            fieldName = "files"
        )) {
            is ApiResult.Success -> {
                val urls = result.data.data?.map { it.productImageUrl } ?: emptyList()
                ApiResult.Success(urls)
            }
            is ApiResult.Error -> ApiResult.Error(result.message)
        }
    }

    /**
     * 단일 이미지 업로드
     */
    suspend fun uploadImage(imageData: ByteArray): ApiResult<String> {
        return when (val result = uploadImages(listOf(imageData))) {
            is ApiResult.Success -> {
                if (result.data.isNotEmpty()) {
                    ApiResult.Success(result.data.first())
                } else {
                    ApiResult.Error("이미지 URL을 받지 못했습니다")
                }
            }
            is ApiResult.Error -> ApiResult.Error(result.message)
        }
    }

    /**
     * 차량 추천 목록 조회
     */
    suspend fun fetchCarRecommendations(): ApiResult<List<String>> {
        return APIService.get<List<String>>(
            endpoint = Endpoint.CarRecommend
        )
    }

    /**
     * 상품 정보 조회 (차량 종류, 모델, 옵션)
     */
    suspend fun fetchProductInfo(): ApiResult<ProductInfoResponse> {
        return APIService.get<ProductInfoResponse>(
            endpoint = Endpoint.ProductInfo
        )
    }

    /**
     * 매물 목록 조회 (필터, 정렬, 페이징) - iOS 동일 로직
     */
    suspend fun fetchProducts(
        page: Int = 0,
        size: Int = 30,
        filter: ProductFilterRequest? = null,
        sort: ProductSort? = null
    ): ApiResult<ProductPageData> {
        // iOS와 동일한 방식으로 쿼리 파라미터 구성
        val queryParams = mutableMapOf<String, String>().apply {
            put("page", page.toString())
            put("size", size.toString())

            filter?.let { f ->
                // 검색 키워드 (최우선)
                f.keyword?.let {
                    if (it.isNotBlank()) {
                        put("keyword", it)
                    }
                }

                // iOS와 동일: 모든 필터 값 전송 (null이 아닌 경우)
                f.mileageFrom?.let { put("mileageFrom", it.toString()) }
                f.mileageTo?.let { put("mileageTo", it.toString()) }
                f.costFrom?.let { put("costFrom", it.toString()) }
                f.costTo?.let { put("costTo", it.toString()) }
                f.generationFrom?.let { put("generationFrom", it.toString()) }
                f.generationTo?.let { put("generationTo", it.toString()) }

                // types는 쉼표로 구분하여 전송 (서버가 배열로 파싱)
                f.types?.let { types ->
                    if (types.isNotEmpty()) {
                        put("types", types.joinToString(","))
                    }
                }
            }

            sort?.let { put("sort", it.queryValue) }
        }

        println("🌐 API Request - Endpoint: ${Endpoint.Products.url}, Params: $queryParams")

        // ApiResponse<ProductPageData>로 파싱되므로 ProductPageData 타입 사용
        return APIService.get<ProductPageData>(
            endpoint = Endpoint.Products,
            queryParams = queryParams
        )
    }

    /**
     * 매물 상세 조회 (새로운 API)
     */
    suspend fun fetchProductDetail(productId: String): ApiResult<ProductDetailDTO> {
        return APIService.get<ProductDetailDTO>(
            endpoint = Endpoint.ProductDetail(productId)
        )
    }

    /**
     * 찜하기/찜취소 (좋아요)
     */
    suspend fun toggleProductLike(productId: String): ApiResult<ProductLikeResponse> {
        return APIService.patch<ProductLikeResponse>(
            endpoint = Endpoint.ProductLike(productId)
        )
    }

    /**
     * 찜한 매물 목록 조회 (iOS와 동일)
     */
    suspend fun fetchFavorites(
        memberId: String,
        page: Int = 0,
        size: Int = 20
    ): ApiResult<com.nobody.campick.models.product.ProductPageData> {
        return APIService.get<com.nobody.campick.models.product.ProductPageData>(
            endpoint = Endpoint.Favorites(memberId),
            queryParams = mapOf(
                "page" to page.toString(),
                "size" to size.toString()
            )
        )
    }

    /**
     * 찜하기/찜취소 (기존 호환성)
     */
    suspend fun toggleLike(productId: String): ApiResult<Unit> {
        return APIService.patch<Unit>(
            endpoint = Endpoint.ProductLike(productId), // like 전용 엔드포인트
        )
    }

    /**
     * 매물 상태 변경 (PATCH /api/product/status)
     */
    suspend fun updateProductStatus(productId: String, status: com.nobody.campick.models.vehicle.VehicleStatus): ApiResult<com.nobody.campick.models.product.ProductStatusUpdateResponse> {
        val request = com.nobody.campick.models.product.ProductStatusUpdateRequest(
            productId = productId.toIntOrNull() ?: 0,
            status = status.apiValue
        )
        println("📤 Update product status request - productId: ${request.productId}, status: ${request.status}")
        val result = APIService.patch<com.nobody.campick.models.product.ProductStatusUpdateResponse>(
            endpoint = Endpoint.ProductStatus,
            body = request
        )
        println("📥 Update product status result - ${if (result is ApiResult.Success) "Success: ${result.data}" else "Error: ${(result as ApiResult.Error).message}"}")
        return result
    }
}