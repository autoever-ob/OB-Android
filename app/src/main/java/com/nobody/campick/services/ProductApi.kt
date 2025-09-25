package com.nobody.campick.services

import com.nobody.campick.models.home.RecommendedVehicle
import com.nobody.campick.services.network.*

object ProductApi {

    /**
     * 상품 정보 조회 (차종, 연료, 변속기 등)
     * Swift의 fetchProductInfo()와 동일
     */
    suspend fun fetchProductInfo(): ApiResult<ProductInfoResponse> {
        return APIService.get<ProductInfoApiResponse>(Endpoint.ProductInfo)
            .map { response ->
                response.data ?: throw IllegalStateException("ProductInfo data is null")
            }
    }

    /**
     * 상품 목록 조회 (페이징)
     * Swift의 fetchProducts(page:size:)와 동일
     */
    suspend fun fetchProducts(
        page: Int? = null,
        size: Int? = null
    ): ApiResult<Page<ProductItemDTO>> {
        val queryParams = mutableMapOf<String, String>()
        page?.let { queryParams["page"] = it.toString() }
        size?.let { queryParams["size"] = it.toString() }

        return APIService.get<ApiResponse<Page<ProductItemDTO>>>(
            endpoint = Endpoint.Products,
            queryParams = queryParams
        ).map { response ->
            response.data ?: Page.empty()
        }
    }

    /**
     * 상품 상세 조회
     * Swift의 fetchProductDetail(productId:)와 동일
     */
    suspend fun fetchProductDetail(productId: String): ApiResult<ProductDetailDTO> {
        return APIService.get<ProductDetailResponse>(
            endpoint = Endpoint.ProductDetail(productId)
        ).map { response ->
            response.data ?: throw IllegalStateException("ProductDetail data is null")
        }
    }

    /**
     * 상품 등록
     */
    suspend fun registerProduct(productData: Map<String, Any>): ApiResult<String> {
        return APIService.multipart<ApiResponse<String>>(
            endpoint = Endpoint.RegisterProduct,
            parts = productData
        ).map { response ->
            response.data ?: "등록 완료"
        }
    }

    /**
     * 이미지 업로드
     */
    suspend fun uploadImage(imageData: ByteArray): ApiResult<String> {
        val parts = mapOf("image" to imageData)
        return APIService.multipart<ApiResponse<String>>(
            endpoint = Endpoint.UploadImage,
            parts = parts
        ).map { response ->
            response.data ?: ""
        }
    }

    /**
     * 추천 상품 조회
     */
    suspend fun getRecommendedVehicles(): ApiResult<List<RecommendedVehicle>> {
        return APIService.getDirect<RecommendResponse>(
            endpoint = Endpoint.CarRecommend
        ).map { response ->
            listOf(
                response.data.newVehicle,
                response.data.hotVehicle
            )
        }
    }
    /**
     * 찜하기/찜취소 (좋아요)
     */
    suspend fun toggleLike(productId: String): ApiResult<Unit> {
        return APIService.patch<Unit>(
            endpoint = Endpoint.ProductLike(productId), // like 전용 엔드포인트
        )
    }
}