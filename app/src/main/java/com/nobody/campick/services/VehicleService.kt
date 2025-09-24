package com.nobody.campick.services

import com.nobody.campick.models.vehicle.VehicleDetailViewData
import com.nobody.campick.models.vehicle.VehicleRegistrationRequest
import com.nobody.campick.models.vehicle.ProductInfoResponse
import com.nobody.campick.services.network.APIService
import com.nobody.campick.services.network.ApiResult
import com.nobody.campick.services.network.Endpoint

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
    suspend fun registerVehicle(request: VehicleRegistrationRequest): ApiResult<Unit> {
        return APIService.post<Unit>(
            endpoint = Endpoint.RegisterProduct,
            body = request
        )
    }

    /**
     * 이미지 업로드
     */
    suspend fun uploadImage(imageData: ByteArray): ApiResult<String> {
        return APIService.multipart<String>(
            endpoint = Endpoint.UploadImage,
            parts = mapOf("image" to imageData)
        )
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
    suspend fun toggleLike(productId: String): ApiResult<Unit> {
        return APIService.patch<Unit>(
            endpoint = Endpoint.ProductLike(productId), // like 전용 엔드포인트
        )
    }

}