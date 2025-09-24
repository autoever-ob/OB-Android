package com.nobody.campick.models.vehicle

import kotlinx.serialization.Serializable

@Serializable
data class Seller(
    val id: String,
    val name: String,
    val avatar: String,
    val totalListings: Int,
    val totalSales: Int,
    val rating: Double,
    val isDealer: Boolean
)

@Serializable
data class VehicleDetailViewData(
    val id: String,
    val title: String,
    val priceText: String,
    val yearText: String,
    val mileageText: String,
    val typeText: String,
    val location: String,
    val images: List<String>,
    val description: String,
    val features: List<String>,
    val seller: Seller,
    val isLiked: Boolean,
    val likeCount: Int,
    val status: VehicleStatus = VehicleStatus.ACTIVE
) {
    companion object {
        fun createMockData(vehicleId: String): VehicleDetailViewData {
            val mockSeller = Seller(
                id = "seller1",
                name = "김판매자",
                avatar = "bannerImage",
                totalListings = 12,
                totalSales = 8,
                rating = 4.5,
                isDealer = false
            )

            return when (vehicleId) {
                "1", "f1" -> VehicleDetailViewData(
                    id = vehicleId,
                    title = "현대 포레스트 프리미엄",
                    priceText = "8,900만원",
                    yearText = "2022년",
                    mileageText = "15,000km",
                    typeText = "모터홈",
                    location = "서울 강남구",
                    images = listOf("testImage1", "testImage2", "testImage3"),
                    description = "상태 매우 좋은 현대 포레스트입니다. 정기적인 점검을 받았으며, 내부 시설도 깨끗하게 관리되었습니다. 캠핑 가기에 완벽한 상태입니다.",
                    features = listOf("화장실", "샤워실", "침대", "주방", "에어컨", "난방", "냉장고", "전자레인지", "소파베드", "테이블", "가스레인지", "오디오", "TV", "수납공간", "외부 차양"),
                    seller = mockSeller,
                    isLiked = false,
                    likeCount = 23
                )
                "2", "f2" -> VehicleDetailViewData(
                    id = vehicleId,
                    title = "기아 봉고 캠퍼",
                    priceText = "4,200만원",
                    yearText = "2021년",
                    mileageText = "32,000km",
                    typeText = "캠핑밴",
                    location = "부산 해운대구",
                    images = listOf("testImage2", "testImage1", "testImage3"),
                    description = "기아 봉고 기반의 캠퍼밴입니다. 소형이지만 알찬 구성으로 2인 캠핑에 최적화되어 있습니다.",
                    features = listOf("침대", "간이주방", "수납공간"),
                    seller = mockSeller.copy(name = "박캠퍼", totalListings = 5, totalSales = 3),
                    isLiked = true,
                    likeCount = 15
                )
                "3", "f3" -> VehicleDetailViewData(
                    id = vehicleId,
                    title = "스타리아 캠퍼",
                    priceText = "7,200만원",
                    yearText = "2023년",
                    mileageText = "8,000km",
                    typeText = "모터홈",
                    location = "인천 송도",
                    images = listOf("testImage3", "testImage1", "testImage2"),
                    description = "최신형 스타리아 기반의 고급 캠퍼입니다. 넓은 실내 공간과 최신 편의시설을 갖추고 있습니다.",
                    features = listOf("화장실", "샤워실", "침대", "주방", "에어컨", "난방", "태양광패널", "냉장고", "전자레인지", "소파베드", "테이블", "가스레인지", "오디오", "TV", "수납공간", "외부 차양", "인버터", "물탱크"),
                    seller = mockSeller.copy(name = "최딜러", isDealer = true, totalListings = 25, totalSales = 20),
                    isLiked = false,
                    likeCount = 31
                )
                else -> VehicleDetailViewData(
                    id = vehicleId,
                    title = "차량 정보",
                    priceText = "가격 정보 없음",
                    yearText = "-",
                    mileageText = "-",
                    typeText = "-",
                    location = "-",
                    images = listOf("testImage1"),
                    description = "차량 정보를 불러올 수 없습니다.",
                    features = emptyList(),
                    seller = mockSeller,
                    isLiked = false,
                    likeCount = 0
                )
            }
        }
    }
}