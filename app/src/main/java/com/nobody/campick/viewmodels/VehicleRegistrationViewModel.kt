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

    private val koreanPlateRegex = Pattern.compile("^\\d{2,3}[가-힣]\\d{4}$")

    init {
        loadProductInfo()
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

    fun removeVehicleImage(imageId: String) {
        val updatedImages = _vehicleImages.value.filter { it.id != imageId }
        _vehicleImages.value = updatedImages

        if (updatedImages.isNotEmpty() && updatedImages.none { it.isMain }) {
            setMainImage(updatedImages.first().id)
        }
    }

    fun setMainImage(imageId: String) {
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

        if (newErrors.isEmpty()) {
            submitVehicleRegistration()
        }
    }

    private fun submitVehicleRegistration() {
        _isSubmitting.value = true

        viewModelScope.launch {
            try {
                val mainImageUrl = _uploadedImageUrls.value.firstOrNull() ?: ""
                val productImageUrls = _uploadedImageUrls.value.drop(1)

                val cleanPrice = _price.value.replace(",", "")
                val cleanMileage = _mileage.value.replace(",", "")

                val request = VehicleRegistrationRequest(
                    generation = _generation.value,
                    mileage = cleanMileage,
                    vehicleType = _vehicleType.value,
                    vehicleModel = _vehicleModel.value,
                    price = cleanPrice,
                    location = _location.value,
                    plateHash = _plateHash.value,
                    title = _title.value,
                    description = _description.value,
                    productImageUrl = productImageUrls,
                    option = _vehicleOptions.value,
                    mainProductImageUrl = mainImageUrl
                )

                // TODO: Implement API call
                submitToAPI(request)

            } catch (e: Exception) {
                _alertMessage.value = "데이터 처리 중 오류가 발생했습니다."
                _showErrorAlert.value = true
                _isSubmitting.value = false
            }
        }
    }

    private suspend fun submitToAPI(request: VehicleRegistrationRequest) {
        // TODO: Implement actual API call
        // This would typically use a repository pattern
        try {
            // Simulate API call
            kotlinx.coroutines.delay(2000)

            // Simulate success response
            _alertMessage.value = "매물이 성공적으로 등록되었습니다."
            _showSuccessAlert.value = true
            _isSubmitting.value = false

        } catch (e: Exception) {
            _alertMessage.value = "네트워크 오류가 발생했습니다: ${e.localizedMessage}"
            _showErrorAlert.value = true
            _isSubmitting.value = false
        }
    }

    private fun loadProductInfo() {
        _isLoadingProductInfo.value = true

        viewModelScope.launch {
            try {
                // TODO: Implement actual API call
                // Simulate API call
                kotlinx.coroutines.delay(1000)

                // Simulate response
                val mockResponse = ProductInfoResponse(
                    type = listOf("모터홈", "픽업트럭", "SUV"),
                    model = listOf("현대 포레스트", "기아 쏘렌토", "Toyota Hilux"),
                    option = listOf("샤워실", "화장실", "침대", "주방", "에어컨")
                )

                _availableTypes.value = mockResponse.type
                _availableModels.value = mockResponse.model
                _availableOptions.value = mockResponse.option

                _vehicleOptions.value = mockResponse.option.map { optionName ->
                    VehicleOption(optionName = optionName, isInclude = false)
                }

                _isLoadingProductInfo.value = false

            } catch (e: Exception) {
                // Use default values on failure
                _availableTypes.value = listOf("모터홈", "픽업트럭", "SUV")
                _availableModels.value = listOf("현대 포레스트", "기아 쏘렌토", "Toyota Hilux")
                _availableOptions.value = listOf("샤워실", "화장실", "침대", "주방", "에어컨")

                _vehicleOptions.value = _availableOptions.value.map { optionName ->
                    VehicleOption(optionName = optionName, isInclude = false)
                }

                _isLoadingProductInfo.value = false
            }
        }
    }

    fun dismissSuccessAlert() {
        _showSuccessAlert.value = false
    }

    fun dismissErrorAlert() {
        _showErrorAlert.value = false
    }
}