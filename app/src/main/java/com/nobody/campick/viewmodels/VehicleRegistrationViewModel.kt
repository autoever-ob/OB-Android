package com.nobody.campick.viewmodels

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nobody.campick.models.vehicle.VehicleImage
import com.nobody.campick.models.vehicle.VehicleOption
import com.nobody.campick.models.vehicle.VehicleRegistrationRequest
import com.nobody.campick.models.vehicle.ProductInfoResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.*
import java.util.regex.Pattern

class VehicleRegistrationViewModel : ViewModel() {

    private val _vehicleImages = MutableStateFlow<List<VehicleImage>>(emptyList())
    val vehicleImages: StateFlow<List<VehicleImage>> = _vehicleImages.asStateFlow()

    private val _uploadedImageUrls = MutableStateFlow<List<String>>(emptyList())
    val uploadedImageUrls: StateFlow<List<String>> = _uploadedImageUrls.asStateFlow()

    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title.asStateFlow()

    private val _mileage = MutableStateFlow("")
    val mileage: StateFlow<String> = _mileage.asStateFlow()

    private val _vehicleType = MutableStateFlow("")
    val vehicleType: StateFlow<String> = _vehicleType.asStateFlow()

    private val _price = MutableStateFlow("")
    val price: StateFlow<String> = _price.asStateFlow()

    private val _description = MutableStateFlow("")
    val description: StateFlow<String> = _description.asStateFlow()

    private val _generation = MutableStateFlow("")
    val generation: StateFlow<String> = _generation.asStateFlow()

    private val _vehicleModel = MutableStateFlow("")
    val vehicleModel: StateFlow<String> = _vehicleModel.asStateFlow()

    private val _location = MutableStateFlow("")
    val location: StateFlow<String> = _location.asStateFlow()

    private val _plateHash = MutableStateFlow("")
    val plateHash: StateFlow<String> = _plateHash.asStateFlow()

    private val _vehicleOptions = MutableStateFlow<List<VehicleOption>>(emptyList())
    val vehicleOptions: StateFlow<List<VehicleOption>> = _vehicleOptions.asStateFlow()

    private val _errors = MutableStateFlow<Map<String, String>>(emptyMap())
    val errors: StateFlow<Map<String, String>> = _errors.asStateFlow()

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    private val _isLoadingProductInfo = MutableStateFlow(false)
    val isLoadingProductInfo: StateFlow<Boolean> = _isLoadingProductInfo.asStateFlow()

    private val _availableTypes = MutableStateFlow<List<String>>(emptyList())
    val availableTypes: StateFlow<List<String>> = _availableTypes.asStateFlow()

    private val _availableModels = MutableStateFlow<List<String>>(emptyList())
    val availableModels: StateFlow<List<String>> = _availableModels.asStateFlow()

    private val _availableOptions = MutableStateFlow<List<String>>(emptyList())
    val availableOptions: StateFlow<List<String>> = _availableOptions.asStateFlow()

    private val _showSuccessAlert = MutableStateFlow(false)
    val showSuccessAlert: StateFlow<Boolean> = _showSuccessAlert.asStateFlow()

    private val _showErrorAlert = MutableStateFlow(false)
    val showErrorAlert: StateFlow<Boolean> = _showErrorAlert.asStateFlow()

    private val _alertMessage = MutableStateFlow("")
    val alertMessage: StateFlow<String> = _alertMessage.asStateFlow()

    private val _editingProductId = MutableStateFlow<String?>(null)
    val editingProductId: StateFlow<String?> = _editingProductId.asStateFlow()

    private val _isLoadingForEdit = MutableStateFlow(false)
    val isLoadingForEdit: StateFlow<Boolean> = _isLoadingForEdit.asStateFlow()

    private val koreanPlateRegex = Pattern.compile("^\\d{2,3}[가-힣]\\d{4}$")

    init {
        loadProductInfo()
    }

    fun loadProductForEdit(productId: String) {
        viewModelScope.launch {
            _isLoadingForEdit.value = true
            _editingProductId.value = productId

            try {
                val result = com.nobody.campick.services.VehicleService.fetchProductDetail(productId)
                println("📡 API 응답 결과: $result")
                when (result) {
                    is com.nobody.campick.services.network.ApiResult.Success -> {
                        val detail = result.data
                        println("📦 받은 데이터: title=${detail.title}, type=${detail.vehicleType}, model=${detail.vehicleModel}")

                        // 폼 필드 채우기
                        _title.value = detail.title
                        _vehicleType.value = detail.vehicleType
                        _vehicleModel.value = detail.vehicleModel
                        _generation.value = detail.generation?.toString() ?: ""
                        _mileage.value = detail.mileage
                        _location.value = detail.location
                        _plateHash.value = detail.plateHash
                        _price.value = detail.price
                        _description.value = detail.description

                        println("✏️ StateFlow 업데이트: title=${_title.value}, type=${_vehicleType.value}")

                        // 옵션 설정 (ProductOptionDTO를 VehicleOption으로 변환)
                        _vehicleOptions.value = detail.option.map { optionDto ->
                            VehicleOption(
                                optionName = optionDto.optionName,
                                isInclude = optionDto.isInclude
                            )
                        }

                        // 이미지 설정 (URL을 VehicleImage로 변환)
                        val images = detail.productImageUrl.mapIndexed { index, url ->
                            VehicleImage(
                                id = "uploaded_$index",
                                imageUri = android.net.Uri.parse(url),
                                uploadedUrl = url,
                                isMain = index == 0
                            )
                        }
                        _vehicleImages.value = images
                        _uploadedImageUrls.value = detail.productImageUrl

                        println("✅ 수정 데이터 로드 완료: ${detail.title}, images=${images.size}")
                    }
                    is com.nobody.campick.services.network.ApiResult.Error -> {
                        println("❌ API 에러: ${result.message}")
                        _alertMessage.value = "수정할 매물 정보를 불러오지 못했습니다: ${result.message}"
                        _showErrorAlert.value = true
                    }
                }
            } catch (e: Exception) {
                _alertMessage.value = "수정할 매물 정보를 불러오는 중 오류가 발생했습니다."
                _showErrorAlert.value = true
            } finally {
                _isLoadingForEdit.value = false
            }
        }
    }

    fun updateTitle(value: String) {
        _title.value = value
        clearError("title")
    }

    fun updateMileage(value: String) {
        _mileage.value = formatNumber(value)
        clearError("mileage")
    }

    fun updateVehicleType(value: String) {
        _vehicleType.value = value
        clearError("vehicleType")
    }

    fun updatePrice(value: String) {
        _price.value = formatNumber(value)
        clearError("price")
    }

    fun updateDescription(value: String) {
        _description.value = value
        clearError("description")
    }

    fun updateGeneration(value: String) {
        _generation.value = value
        clearError("generation")
    }

    fun updateVehicleModel(value: String) {
        _vehicleModel.value = value
        clearError("vehicleModel")
    }


    fun updateLocation(value: String) {
        _location.value = value
        clearError("location")
    }

    fun updatePlateHash(value: String) {
        _plateHash.value = value
        clearError("plateHash")
    }

    fun updateVehicleOptions(options: List<VehicleOption>) {
        _vehicleOptions.value = options
    }

    fun addVehicleImage(imageUri: Uri) {
        val newImage = VehicleImage(
            imageUri = imageUri,
            isMain = _vehicleImages.value.isEmpty()
        )
        _vehicleImages.value = _vehicleImages.value + newImage
        clearError("images")
    }

    fun addVehicleImageAndUpload(imageUri: Uri, context: android.content.Context) {
        println("🔄 이미지 추가 및 업로드 시작: $imageUri")
        val newImage = VehicleImage(
            imageUri = imageUri,
            isMain = _vehicleImages.value.isEmpty()
        )
        _vehicleImages.value = _vehicleImages.value + newImage
        clearError("images")

        // 즉시 업로드
        viewModelScope.launch {
            uploadSingleImage(newImage, context)
        }
    }

    fun addVehicleImageAsMain(imageUri: Uri) {
        // 기존 메인 이미지들을 모두 일반 이미지로 변경
        val updatedExistingImages = _vehicleImages.value.map { it.copy(isMain = false) }

        // 새 메인 이미지를 맨 앞에 추가
        val newMainImage = VehicleImage(
            imageUri = imageUri,
            isMain = true
        )
        _vehicleImages.value = listOf(newMainImage) + updatedExistingImages
        clearError("images")
    }

    fun addVehicleImageAsMainAndUpload(imageUri: Uri, context: android.content.Context) {
        println("🔄 메인 이미지 추가 및 업로드 시작: $imageUri")
        // 기존 메인 이미지들을 모두 일반 이미지로 변경
        val updatedExistingImages = _vehicleImages.value.map { it.copy(isMain = false) }

        // 새 메인 이미지를 맨 앞에 추가
        val newMainImage = VehicleImage(
            imageUri = imageUri,
            isMain = true
        )
        _vehicleImages.value = listOf(newMainImage) + updatedExistingImages
        clearError("images")

        // 즉시 업로드
        viewModelScope.launch {
            uploadSingleImage(newMainImage, context)
        }
    }

    fun removeVehicleImage(imageId: String) {
        val imageToRemove = _vehicleImages.value.find { it.id == imageId }

        // iOS와 동일: uploadedImageUrls에서도 제거
        if (imageToRemove?.uploadedUrl != null) {
            _uploadedImageUrls.value = _uploadedImageUrls.value.filter { it != imageToRemove.uploadedUrl }
        }

        val updatedImages = _vehicleImages.value.filter { it.id != imageId }
        _vehicleImages.value = updatedImages

        // iOS와 동일: 메인 이미지를 삭제한 경우 첫 번째 이미지를 메인으로 설정
        if (imageToRemove?.isMain == true && updatedImages.isNotEmpty()) {
            setMainImage(updatedImages.first().id)
        }
    }

    fun setMainImage(imageId: String) {
        // iOS와 동일: 모든 이미지의 isMain을 false로 하고, 선택된 이미지만 true로 설정
        _vehicleImages.value = _vehicleImages.value.map { image ->
            image.copy(isMain = image.id == imageId)
        }
    }

    private fun formatNumber(value: String): String {
        val numbers = value.replace(Regex("[^0-9]"), "")
        return if (numbers.isNotEmpty()) {
            val number = numbers.toLongOrNull() ?: return ""
            NumberFormat.getNumberInstance(Locale.getDefault()).format(number)
        } else {
            ""
        }
    }

    private fun isValidKoreanPlate(plateNumber: String): Boolean {
        return koreanPlateRegex.matcher(plateNumber).matches()
    }

    private fun clearError(key: String) {
        _errors.value = _errors.value.toMutableMap().apply { remove(key) }
    }

    fun handleSubmit() {
        val newErrors = mutableMapOf<String, String>()

        if (_title.value.trim().isEmpty()) {
            newErrors["title"] = "매물 제목을 입력해주세요"
        }

        if (_vehicleImages.value.isEmpty()) {
            newErrors["images"] = "최소 1장의 이미지를 업로드해주세요"
        }

        if (_generation.value.trim().isEmpty()) {
            newErrors["generation"] = "연식을 입력해주세요"
        }

        if (_mileage.value.trim().isEmpty()) {
            newErrors["mileage"] = "주행거리를 입력해주세요"
        }

        if (_vehicleType.value.isEmpty()) {
            newErrors["vehicleType"] = "차량 종류를 선택해주세요"
        }

        if (_vehicleModel.value.trim().isEmpty()) {
            newErrors["vehicleModel"] = "차량 브랜드/모델을 입력해주세요"
        }

        if (_price.value.trim().isEmpty()) {
            newErrors["price"] = "판매 가격을 입력해주세요"
        }

        if (_location.value.trim().isEmpty()) {
            newErrors["location"] = "판매 지역을 입력해주세요"
        }

        val trimmedPlateHash = _plateHash.value.trim()
        if (trimmedPlateHash.isEmpty()) {
            newErrors["plateHash"] = "차량 번호를 입력해주세요"
        } else if (!isValidKoreanPlate(trimmedPlateHash)) {
            newErrors["plateHash"] = "올바른 번호판 형식을 입력하세요 (예: 123가4567)"
        }

        if (_description.value.trim().isEmpty()) {
            newErrors["description"] = "상세 설명을 입력해주세요"
        }

        _errors.value = newErrors
    }

    /**
     * 폼 유효성 검사 통과 여부
     */
    fun isValidForSubmission(): Boolean {
        return _errors.value.isEmpty()
    }

    /**
     * 메인 이미지 추가 (4:3 비율로 크롭된 이미지)
     */
    fun addMainImageFromUri(imageUri: Uri) {
        // 기존 메인 이미지들을 모두 일반 이미지로 변경
        val updatedExistingImages = _vehicleImages.value.map { it.copy(isMain = false) }

        // 새 메인 이미지를 맨 앞에 추가
        val newMainImage = VehicleImage(
            imageUri = imageUri,
            isMain = true
        )
        _vehicleImages.value = listOf(newMainImage) + updatedExistingImages
        clearError("images")
    }

    fun submitVehicleRegistration(context: android.content.Context) {
        println("🚀 submitVehicleRegistration 호출됨")
        _isSubmitting.value = true

        viewModelScope.launch {
            try {
                println("📸 이미지 개수: ${_vehicleImages.value.size}")
                println("🔗 업로드된 URL 개수: ${_uploadedImageUrls.value.size}")

                // 이미지가 있는데 업로드된 URL이 없다면 경고만 출력 (임시)
                if (_vehicleImages.value.isNotEmpty() && _uploadedImageUrls.value.isEmpty()) {
                    println("⚠️ 경고: 이미지가 있지만 업로드되지 않았음. 로그인이 필요할 수 있습니다.")
                    // 임시로 빈 URL 리스트로 진행
                }

                // 2. 매물 정보 등록 (이미지가 1장이면 mainProductImageUrl만, 2장 이상이면 productImageUrl에 배치)
                val mainImageUrl = _vehicleImages.value.find { it.isMain }?.uploadedUrl ?: ""
                val otherImageUrls = _vehicleImages.value
                    .filter { !it.isMain && it.uploadedUrl != null }
                    .mapNotNull { it.uploadedUrl }

                // 이미지가 1장만 있는 경우: mainProductImageUrl만 사용, productImageUrl은 빈 리스트
                // 이미지가 2장 이상인 경우: 메인 이미지를 첫 번째로, 나머지 이미지들을 뒤에 배치
                val allImageUrls = if (_vehicleImages.value.size == 1) {
                    emptyList() // 1장만 있으면 productImageUrl은 비움
                } else if (mainImageUrl.isNotEmpty()) {
                    listOf(mainImageUrl) + otherImageUrls
                } else {
                    otherImageUrls
                }

                val cleanPrice = _price.value.replace(",", "")
                val cleanMileage = _mileage.value.replace(",", "")

                println("📝 매물 정보 준비:")
                println("  제목: ${_title.value}")
                println("  차종: ${_vehicleType.value}")
                println("  모델: ${_vehicleModel.value}")
                println("  연식: ${_generation.value}")
                println("  주행거리: $cleanMileage")
                println("  가격: $cleanPrice")
                println("  지역: ${_location.value}")
                println("  번호판: ${_plateHash.value}")
                println("  이미지 개수: ${_vehicleImages.value.size}")
                println("  메인 이미지: $mainImageUrl")
                println("  모든 이미지 (productImageUrl): $allImageUrls")

                val request = VehicleRegistrationRequest(
                    generation = _generation.value.toIntOrNull() ?: 0,
                    mileage = cleanMileage,
                    vehicleType = _vehicleType.value,
                    vehicleModel = _vehicleModel.value,
                    price = cleanPrice,
                    location = _location.value,
                    plateHash = _plateHash.value,
                    title = _title.value,
                    description = _description.value,
                    productImageUrl = allImageUrls, // 1장이면 빈 리스트, 2장 이상이면 메인 이미지가 0번 인덱스
                    option = _vehicleOptions.value,
                    mainProductImageUrl = mainImageUrl // 항상 메인 이미지 URL 설정
                )

                println("🌐 API 호출 시작")
                submitToAPI(request)

            } catch (e: Exception) {
                _alertMessage.value = "데이터 처리 중 오류가 발생했습니다."
                _showErrorAlert.value = true
                _isSubmitting.value = false
            }
        }
    }

    private suspend fun submitToAPI(request: VehicleRegistrationRequest) {
        try {
            val editingId = _editingProductId.value

            if (editingId != null) {
                println("🔧 매물 수정 API 호출 (id: $editingId)")
                when (val result = com.nobody.campick.services.VehicleService.updateProduct(editingId, request)) {
                    is com.nobody.campick.services.network.ApiResult.Success -> {
                        _alertMessage.value = "성공적으로 매물 정보가 수정되었습니다."
                        _showSuccessAlert.value = true
                        resetForm()
                    }
                    is com.nobody.campick.services.network.ApiResult.Error -> {
                        _alertMessage.value = "매물 수정에 실패했습니다: ${result.message}"
                        _showErrorAlert.value = true
                    }
                }
            } else {
                println("🆕 매물 등록 API 호출")
                when (val result = com.nobody.campick.services.VehicleService.registerVehicle(request)) {
                    is com.nobody.campick.services.network.ApiResult.Success -> {
                        _alertMessage.value = "매물이 성공적으로 등록되었습니다."
                        _showSuccessAlert.value = true
                        resetForm()
                    }
                    is com.nobody.campick.services.network.ApiResult.Error -> {
                        _alertMessage.value = "매물 등록에 실패했습니다: ${result.message}"
                        _showErrorAlert.value = true
                    }
                }
            }

        } catch (e: Exception) {
            _alertMessage.value = "네트워크 오류가 발생했습니다: ${e.localizedMessage}"
            _showErrorAlert.value = true
        } finally {
            _isSubmitting.value = false
        }
    }

    /**
     * 단일 이미지 업로드 처리 (iOS 로직과 동일)
     */
    private suspend fun uploadSingleImage(vehicleImage: VehicleImage, context: android.content.Context) {
        try {
            println("📤 단일 이미지 업로드 시작: ${vehicleImage.isMain}메인, ID: ${vehicleImage.id}")

            // 1. Uri에서 Bitmap 로드
            val bitmap = com.nobody.campick.utils.ImageUtils.loadCompressedBitmapFromUri(
                context, vehicleImage.imageUri
            ) ?: return

            // 2. 메인 이미지인 경우 4:3 비율로 크롭 처리
            val processedBitmap = if (vehicleImage.isMain) {
                com.nobody.campick.utils.ImageUtils.processMainImage(bitmap)
            } else {
                bitmap
            }

            // 3. Swift와 동일한 압축 로직 적용 (1MB 이하)
            val compressedImageData = com.nobody.campick.utils.ImageUtils.compressImage(
                processedBitmap, maxSizeInMB = 1.0
            ) ?: return

            // 4. API 업로드
            when (val result = com.nobody.campick.services.VehicleService.uploadImage(compressedImageData)) {
                is com.nobody.campick.services.network.ApiResult.Success -> {
                    // iOS와 동일: uploadedImageUrls에 추가하고 vehicleImage.uploadedUrl 설정
                    _uploadedImageUrls.value = _uploadedImageUrls.value + result.data

                    // 해당 이미지의 uploadedUrl 설정 (iOS와 동일)
                    val updatedImages = _vehicleImages.value.map { image ->
                        if (image.id == vehicleImage.id) {
                            image.copy(uploadedUrl = result.data)
                        } else {
                            image
                        }
                    }
                    _vehicleImages.value = updatedImages

                    val sizeString = com.nobody.campick.utils.ImageUtils.getImageSizeString(compressedImageData)
                    println("✅ 단일 이미지 업로드 성공: ${vehicleImage.isMain}메인, 크기: $sizeString, URL: ${result.data}")
                }
                is com.nobody.campick.services.network.ApiResult.Error -> {
                    _alertMessage.value = "이미지 업로드에 실패했습니다: ${result.message}"
                    _showErrorAlert.value = true
                    println("❌ 단일 이미지 업로드 실패: ${result.message}")
                }
            }

            // 메모리 정리
            if (processedBitmap != bitmap) {
                processedBitmap.recycle()
            }
            bitmap.recycle()

        } catch (e: Exception) {
            _alertMessage.value = "이미지 업로드 중 오류가 발생했습니다: ${e.localizedMessage}"
            _showErrorAlert.value = true
            println("❌ 단일 이미지 업로드 예외: ${e.message}")
        }
    }

    /**
     * 이미지 업로드 처리 (Swift와 동일한 압축 로직 적용)
     */
    suspend fun uploadImages(context: android.content.Context): Boolean {
        val vehicleImages = _vehicleImages.value
        val uploadedUrls = mutableListOf<String>()

        try {
            for (vehicleImage in vehicleImages) {
                // 1. Uri에서 Bitmap 로드
                val bitmap = com.nobody.campick.utils.ImageUtils.loadCompressedBitmapFromUri(
                    context, vehicleImage.imageUri
                ) ?: continue

                // 2. 메인 이미지인 경우 4:3 비율로 크롭 처리
                val processedBitmap = if (vehicleImage.isMain) {
                    com.nobody.campick.utils.ImageUtils.processMainImage(bitmap)
                } else {
                    bitmap
                }

                // 3. Swift와 동일한 압축 로직 적용 (1MB 이하)
                val compressedImageData = com.nobody.campick.utils.ImageUtils.compressImage(
                    processedBitmap, maxSizeInMB = 1.0
                ) ?: continue

                // 4. API 업로드
                when (val result = com.nobody.campick.services.VehicleService.uploadImage(compressedImageData)) {
                    is com.nobody.campick.services.network.ApiResult.Success -> {
                        uploadedUrls.add(result.data)

                        // 압축된 이미지 크기 로그
                        val sizeString = com.nobody.campick.utils.ImageUtils.getImageSizeString(compressedImageData)
                        println("✅ 이미지 업로드 성공: ${vehicleImage.isMain}메인 여부, 크기: $sizeString")
                    }
                    is com.nobody.campick.services.network.ApiResult.Error -> {
                        _alertMessage.value = "이미지 업로드에 실패했습니다: ${result.message}"
                        _showErrorAlert.value = true

                        // 메모리 정리
                        if (processedBitmap != bitmap) {
                            processedBitmap.recycle()
                        }
                        if (bitmap != null) {
                            bitmap.recycle()
                        }

                        return false
                    }
                }

                // 메모리 정리
                if (processedBitmap != bitmap) {
                    processedBitmap.recycle()
                }
                bitmap.recycle()
            }

            _uploadedImageUrls.value = uploadedUrls
            return true

        } catch (e: Exception) {
            _alertMessage.value = "이미지 업로드 중 오류가 발생했습니다: ${e.localizedMessage}"
            _showErrorAlert.value = true
            return false
        }
    }

    /**
     * 폼 초기화
     */
    private fun resetForm() {
        _vehicleImages.value = emptyList()
        _uploadedImageUrls.value = emptyList()
        _title.value = ""
        _mileage.value = ""
        _vehicleType.value = ""
        _price.value = ""
        _description.value = ""
        _generation.value = ""
        _vehicleModel.value = ""
        _location.value = ""
        _plateHash.value = ""
        _vehicleOptions.value = _vehicleOptions.value.map { it.copy(isInclude = false) }
        _errors.value = emptyMap()
    }

    private fun loadProductInfo() {
        _isLoadingProductInfo.value = true

        viewModelScope.launch {
            try {
                when (val result = com.nobody.campick.services.VehicleService.fetchProductInfo()) {
                    is com.nobody.campick.services.network.ApiResult.Success -> {
                        val response = result.data
                        _availableTypes.value = response.type
                        _availableModels.value = response.model
                        _availableOptions.value = response.option

                        _vehicleOptions.value = response.option.map { optionName ->
                            VehicleOption(optionName = optionName, isInclude = false)
                        }
                    }
                    is com.nobody.campick.services.network.ApiResult.Error -> {
                        // API 실패시 기본값 사용
                        useDefaultValues()
                    }
                }

                _isLoadingProductInfo.value = false

            } catch (e: Exception) {
                // 예외 발생시 기본값 사용
                useDefaultValues()
                _isLoadingProductInfo.value = false
            }
        }
    }

    private fun useDefaultValues() {
        _availableTypes.value = listOf("모터홈", "픽업트럭", "SUV", "캠핑밴")
        _availableModels.value = listOf("현대 포레스트", "기아 쏘렌토", "Toyota Hilux", "기아 봉고")
        _availableOptions.value = listOf("샤워실", "화장실", "침대", "주방", "에어컨", "난방", "냉장고", "전자레인지", "태양광패널")

        _vehicleOptions.value = _availableOptions.value.map { optionName ->
            VehicleOption(optionName = optionName, isInclude = false)
        }
    }

    fun dismissSuccessAlert() {
        _showSuccessAlert.value = false
    }

    fun dismissErrorAlert() {
        _showErrorAlert.value = false
    }
}