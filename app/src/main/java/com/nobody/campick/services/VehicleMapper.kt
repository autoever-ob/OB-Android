package com.nobody.campick.services

import com.nobody.campick.models.vehicle.Vehicle
import com.nobody.campick.models.vehicle.VehicleDetailViewData
import com.nobody.campick.models.vehicle.VehicleStatus
import com.nobody.campick.models.vehicle.Seller
import com.nobody.campick.services.network.ProductItemDTO
import com.nobody.campick.services.network.ProductDetailDTO
import com.nobody.campick.services.network.ProductSellerDTO

object VehicleMapper {

    /**
     * ProductItemDTO를 Vehicle로 변환
     */
    fun mapToVehicle(dto: ProductItemDTO): Vehicle {
        return Vehicle(
            id = dto.productId.toString(),
            imageName = mapImageName(dto.thumbNail),
            thumbnailURL = dto.thumbNail,
            title = dto.title,
            price = dto.price,
            year = dto.generation?.toString() ?: "미상",
            mileage = dto.mileage,
            fuelType = dto.fuelType,
            transmission = dto.transmission,
            location = dto.location,
            status = mapVehicleStatus(dto.status),
            postedDate = dto.createdAt,
            isOnSale = dto.status.uppercase() != "SOLD",
            isFavorite = dto.isLiked,
            likeCount = dto.likeCount
        )
    }

    /**
     * ProductDetailDTO를 VehicleDetailViewData로 변환
     */
    fun mapToVehicleDetailViewData(dto: ProductDetailDTO): VehicleDetailViewData {
        return VehicleDetailViewData(
            id = dto.productId.toString(),
            title = dto.title,
            priceText = dto.price,
            yearText = dto.generation?.toString() ?: "미상",
            mileageText = dto.mileage,
            typeText = mapVehicleType(dto.fuelType),
            location = dto.location,
            description = dto.description,
            images = dto.images.map { mapImageName(it) },
            features = dto.options.filter { it.isInclude }.map { it.optionName },
            isLiked = dto.isLiked,
            likeCount = dto.likeCount,
            seller = mapToSeller(dto.seller)
        )
    }

    /**
     * ProductSellerDTO를 Seller로 변환
     */
    private fun mapToSeller(dto: ProductSellerDTO): Seller {
        return Seller(
            id = dto.userId.toString(),
            name = dto.nickName,
            avatar = "testImage1", // API에서 제공되지 않으므로 기본 이미지 사용
            totalListings = dto.sellingCount,
            totalSales = dto.completeCount,
            rating = dto.rating,
            isDealer = dto.role.equals("DEALER", ignoreCase = true)
        )
    }

    /**
     * 이미지 이름 매핑 (API URL을 앱에서 사용하는 이미지 이름으로 변환)
     */
    private fun mapImageName(imageUrl: String): String {
        // 실제 구현에서는 이미지 URL을 적절히 처리
        // 지금은 테스트용 이미지로 매핑
        return when {
            imageUrl.contains("1") || imageUrl.isEmpty() -> "testImage1"
            imageUrl.contains("2") -> "testImage2"
            imageUrl.contains("3") -> "testImage3"
            else -> "testImage1"
        }
    }

    /**
     * 연료타입을 차량타입으로 매핑
     */
    private fun mapVehicleType(fuelType: String): String {
        return when (fuelType.lowercase()) {
            "가솔린", "gasoline" -> "모터홈"
            "디젤", "diesel" -> "트레일러"
            "lpg" -> "픽업캠퍼"
            "하이브리드", "hybrid" -> "캠핑밴"
            "전기", "electric" -> "모터홈"
            else -> "모터홈"
        }
    }

    /**
     * 상태 문자열을 VehicleStatus로 매핑
     */
    private fun mapVehicleStatus(status: String): VehicleStatus {
        return when (status.uppercase()) {
            "SELLING", "판매중", "ACTIVE" -> VehicleStatus.ACTIVE
            "SOLD", "판매완료" -> VehicleStatus.SOLD
            "RESERVED", "예약중" -> VehicleStatus.RESERVED
            else -> VehicleStatus.ACTIVE
        }
    }
}