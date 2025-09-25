package com.nobody.campick.models.product

import com.nobody.campick.models.vehicle.Vehicle
import com.nobody.campick.models.vehicle.VehicleStatus
import com.nobody.campick.models.vehicle.VehicleDetailViewData
import com.nobody.campick.models.vehicle.Seller
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * ProductItemDTO를 Vehicle 모델로 변환하는 유틸리티
 */
object ProductMapper {

    /**
     * ProductItemDTO를 Vehicle로 변환
     */
    fun toVehicle(dto: ProductItemDTO): Vehicle {
        return Vehicle(
            id = dto.productId.toString(),
            imageName = null,
            thumbnailURL = dto.thumbnailUrl,
            title = dto.title,
            price = formatPrice(dto.priceValue),
            year = extractYear(dto.generation, dto.title),
            mileage = formatMileage(dto.mileage),
            fuelType = dto.fuelType.ifEmpty { "-" },
            transmission = dto.transmission.ifEmpty { "-" },
            location = dto.location.ifEmpty { "-" },
            status = mapStatus(dto.status),
            postedDate = dto.createdAt,
            isOnSale = dto.status.uppercase() == "AVAILABLE",
            isFavorite = dto.isLiked
        )
    }

    /**
     * 연식 추출
     */
    private fun extractYear(generation: Int?, title: String): String {
        // 1. generation 값이 있으면 사용
        if (generation != null && generation > 0) {
            return "${generation}년"
        }

        // 2. 제목에서 연도 패턴 추출 (2000-2049년, 1900-1999년)
        val yearPattern = Regex("(20[0-4][0-9]|19[0-9]{2})")
        val matchResult = yearPattern.find(title)
        if (matchResult != null) {
            return "${matchResult.value}년"
        }

        return "-"
    }

    /**
     * 주행거리 포맷팅 (Int 입력)
     */
    private fun formatMileageFromInt(mileage: Int): String {
        return when {
            mileage >= 10000 -> {
                val 만단위 = mileage / 10000
                val 나머지 = mileage % 10000
                if (나머지 == 0) {
                    "${만단위}만km"
                } else {
                    "${만단위}.${나머지 / 1000}만km"
                }
            }
            mileage > 0 -> "${String.format("%,d", mileage)}km"
            else -> "-"
        }
    }

    /**
     * 주행거리 포맷팅 (String 입력 - 레거시)
     */
    private fun formatMileage(mileage: String): String {
        if (mileage.isEmpty()) return "-"

        // 숫자만 추출
        val digits = mileage.filter { it.isDigit() }
        if (digits.isEmpty()) return mileage

        val mileageValue = digits.toIntOrNull() ?: return mileage
        return formatMileageFromInt(mileageValue)
    }

    /**
     * 상태 매핑
     */
    private fun mapStatus(status: String): VehicleStatus {
        return when (status.uppercase()) {
            "AVAILABLE" -> VehicleStatus.ACTIVE
            "RESERVED" -> VehicleStatus.RESERVED
            "SOLD", "SOLD_OUT" -> VehicleStatus.SOLD
            else -> VehicleStatus.ACTIVE
        }
    }

    /**
     * Vehicle 리스트를 ProductItemDTO 리스트로 변환
     */
    fun toVehicleList(dtos: List<ProductItemDTO>): List<Vehicle> {
        return dtos.map { toVehicle(it) }
    }

    /**
     * ProductDetailDTO를 VehicleDetailViewData로 변환
     */
    fun toVehicleDetailViewData(dto: ProductDetailDTO): VehicleDetailViewData {
        return VehicleDetailViewData(
            id = dto.productId.toString(),
            title = dto.title,
            priceText = formatPrice(dto.price),
            yearText = extractYear(dto.generation, dto.title),
            mileageText = formatMileage(dto.mileage),
            typeText = if (dto.vehicleType.isNotEmpty()) dto.vehicleType else dto.vehicleModel,
            location = dto.location.ifEmpty { "-" },
            images = dto.productImageUrl.ifEmpty { listOf("testImage1") },
            description = dto.description.ifEmpty { "상세 설명이 없습니다." },
            features = dto.option.filter { it.isInclude }.map { it.optionName },
            seller = mapSeller(dto.user),
            isLiked = dto.isLiked,
            likeCount = dto.likeCount,
            status = VehicleStatus.fromApiValue(dto.status)
        )
    }

    /**
     * 가격 포맷팅 (String 입력 - 만원 단위 숫자에 천 단위 콤마)
     */
    private fun formatPrice(price: String): String {
        if (price.isEmpty()) return "가격 문의"

        // 숫자만 추출
        val digits = price.filter { it.isDigit() }
        if (digits.isEmpty()) return price

        val priceValue = digits.toLongOrNull() ?: return price

        return if (priceValue > 0) {
            "${NumberFormat.getNumberInstance(Locale.getDefault()).format(priceValue)}만원"
        } else {
            "가격 정보 없음"
        }
    }

    /**
     * ProductSellerDTO를 Seller로 변환
     */
    private fun mapSeller(dto: ProductSellerDTO): Seller {
        return Seller(
            id = dto.userId.toString(),
            name = dto.nickName,
            avatar = "bannerImage", // API에서 아바타 URL이 없으므로 기본값 사용
            totalListings = dto.sellingCount,
            totalSales = dto.completeCount,
            rating = dto.rating ?: 0.0, // rating이 null이면 0.0으로 기본값 설정
            isDealer = dto.role.uppercase() == "DEALER"
        )
    }
}