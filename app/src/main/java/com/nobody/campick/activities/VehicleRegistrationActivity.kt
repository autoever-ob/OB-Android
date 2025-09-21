package com.nobody.campick.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import android.text.Editable
import android.text.TextWatcher
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.core.view.isVisible
import java.text.DecimalFormat
import java.util.regex.Pattern
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.compose.ui.graphics.toArgb
import androidx.recyclerview.widget.LinearLayoutManager
import com.nobody.campick.R
import com.nobody.campick.adapters.VehicleImageAdapter
import com.nobody.campick.databinding.ActivityVehicleRegistrationBinding
import com.nobody.campick.resources.theme.AppColors
import com.nobody.campick.viewmodels.VehicleRegistrationViewModel
import com.nobody.campick.views.components.CommonHeader
import kotlinx.coroutines.launch

class VehicleRegistrationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVehicleRegistrationBinding
    private val viewModel: VehicleRegistrationViewModel by viewModels()

    companion object {
        private const val CAMERA_PERMISSION_REQUEST_CODE = 1001
    }

    private lateinit var imageAdapter: VehicleImageAdapter

    // 한국 차량번호판 정규표현식
    private val koreanPlateRegex = Pattern.compile("^\\d{2,3}[가-힣]\\d{4}$")

    // 숫자 포맷터 (쉼표 추가)
    private val numberFormatter = DecimalFormat("#,###")

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            showImageSourceDialog()
        } else {
            Toast.makeText(this, "이미지 선택을 위해 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
        }
    }

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                viewModel.addVehicleImage(uri)
            }
        }
    }

    private var tempCameraImageUri: Uri? = null

    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            tempCameraImageUri?.let { uri ->
                viewModel.addVehicleImage(uri)
            }
        }
    }

    private val pickMainImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                viewModel.addVehicleImageAsMain(uri)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVehicleRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupTopBar()
        setupRecyclerViews()
        setupTextWatchers()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupTopBar() {
        binding.commonHeader.setupHeader(
            type = CommonHeader.HeaderType.Custom(
                title = "차량 매물 등록",
                leftIcon = R.drawable.ic_close,
                rightIcon = R.drawable.ic_add_circle,
                leftAction = { finish() },
                rightAction = { checkPermissionAndPickImage() }
            )
        )
    }

    private fun setupRecyclerViews() {
        imageAdapter = VehicleImageAdapter(
            onGalleryClick = {
                openImagePicker()
            },
            onCameraClick = {
                openCamera()
            },
            onMainImageClick = {
                openMainImagePicker()
            },
            onImageClick = { imageId ->
                viewModel.setMainImage(imageId)
            },
            onImageRemove = { imageId ->
                viewModel.removeVehicleImage(imageId)
            },
            onSetMainImage = { imageId ->
                viewModel.setMainImage(imageId)
            }
        )

        binding.recyclerViewImages.apply {
            layoutManager = androidx.recyclerview.widget.GridLayoutManager(this@VehicleRegistrationActivity, 3)
            adapter = imageAdapter
        }

    }

    private fun setupTextWatchers() {
        binding.editTextTitle.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                viewModel.updateTitle(s.toString())
            }
        })

        binding.editTextGeneration.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                viewModel.updateGeneration(s.toString())
            }
        })

        binding.editTextMileage.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val formatted = formatNumber(s.toString())
                if (formatted != s.toString()) {
                    binding.editTextMileage.removeTextChangedListener(this)
                    binding.editTextMileage.setText(formatted)
                    binding.editTextMileage.setSelection(formatted.length)
                    binding.editTextMileage.addTextChangedListener(this)
                }
                viewModel.updateMileage(formatted)
            }
        })

        binding.editTextPrice.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val formatted = formatNumber(s.toString())
                if (formatted != s.toString()) {
                    binding.editTextPrice.removeTextChangedListener(this)
                    binding.editTextPrice.setText(formatted)
                    binding.editTextPrice.setSelection(formatted.length)
                    binding.editTextPrice.addTextChangedListener(this)
                }
                viewModel.updatePrice(formatted)
            }
        })

        binding.editTextLocation.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                viewModel.updateLocation(s.toString())
            }
        })

        binding.editTextPlateHash.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val text = s.toString()
                updatePlateHashValidation(text)
                viewModel.updatePlateHash(text)
            }
        })

        // 포커스 해제 시 포맷팅
        binding.editTextPlateHash.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val currentText = binding.editTextPlateHash.text.toString()
                val formattedText = formatPlateNumber(currentText)
                if (formattedText != currentText) {
                    binding.editTextPlateHash.setText(formattedText)
                    binding.editTextPlateHash.setSelection(formattedText.length)
                }
            }
        }

        binding.editTextDescription.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                viewModel.updateDescription(s.toString())
            }
        })
    }

    private fun setupClickListeners() {

        binding.buttonVehicleType.setOnClickListener {
            showVehicleTypeDialog()
        }

        binding.buttonVehicleModel.setOnClickListener {
            showVehicleModelDialog()
        }

        binding.buttonVehicleOptions.setOnClickListener {
            showVehicleOptionsDialog()
        }

        binding.buttonSubmit.setOnClickListener {
            viewModel.handleSubmit()
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.vehicleImages.collect { images ->
                imageAdapter.submitList(images)
                binding.textViewImageCount.text = "${images.size}/10"
            }
        }

        lifecycleScope.launch {
            viewModel.vehicleType.collect { type ->
                binding.buttonVehicleType.text = type.ifEmpty { "차량 종류 선택" }
            }
        }

        lifecycleScope.launch {
            viewModel.vehicleModel.collect { model ->
                binding.buttonVehicleModel.text = model.ifEmpty { "차량 브랜드/모델 선택" }
            }
        }

        lifecycleScope.launch {
            viewModel.vehicleOptions.collect { options ->
                updateVehicleOptionsButtonText(options)
            }
        }

        lifecycleScope.launch {
            viewModel.mileage.collect { mileage ->
                if (binding.editTextMileage.text.toString() != mileage) {
                    binding.editTextMileage.setText(mileage)
                    binding.editTextMileage.setSelection(mileage.length)
                }
            }
        }

        lifecycleScope.launch {
            viewModel.price.collect { price ->
                if (binding.editTextPrice.text.toString() != price) {
                    binding.editTextPrice.setText(price)
                    binding.editTextPrice.setSelection(price.length)
                }
            }
        }

        lifecycleScope.launch {
            viewModel.errors.collect { errors ->
                updateErrorStates(errors)
            }
        }

        lifecycleScope.launch {
            viewModel.isSubmitting.collect { isSubmitting ->
                binding.buttonSubmit.isEnabled = !isSubmitting
                binding.progressBar.isVisible = isSubmitting
                binding.buttonSubmit.text = if (isSubmitting) "등록 중..." else "매물 등록하기"
            }
        }

        lifecycleScope.launch {
            viewModel.showSuccessAlert.collect { show ->
                if (show) {
                    showSuccessDialog()
                }
            }
        }

        lifecycleScope.launch {
            viewModel.showErrorAlert.collect { show ->
                if (show) {
                    showErrorDialog()
                }
            }
        }

        lifecycleScope.launch {
            viewModel.isLoadingProductInfo.collect { isLoading ->
                binding.progressBarProductInfo.isVisible = isLoading
            }
        }
    }

    private fun checkPermissionAndPickImage() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED -> {
                showImageSourceDialog()
            }
            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) -> {
                AlertDialog.Builder(this)
                    .setTitle("권한 필요")
                    .setMessage("이미지를 선택하기 위해 저장소 접근 권한이 필요합니다.")
                    .setPositiveButton("확인") { _, _ ->
                        requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                    }
                    .setNegativeButton("취소", null)
                    .show()
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    }

    private fun showImageSourceDialog() {
        AlertDialog.Builder(this)
            .setTitle("이미지 선택")
            .setItems(arrayOf("갤러리", "카메라")) { _, which ->
                when (which) {
                    0 -> openImagePicker()
                    1 -> openCamera()
                }
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImageLauncher.launch(intent)
    }

    private fun openCamera() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST_CODE
            )
            return
        }

        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "JPEG_${timeStamp}_"
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        try {
            val imageFile = File.createTempFile(imageFileName, ".jpg", storageDir)
            tempCameraImageUri = FileProvider.getUriForFile(
                this,
                "${packageName}.fileprovider",
                imageFile
            )

            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, tempCameraImageUri)
            takePictureLauncher.launch(takePictureIntent)
        } catch (e: Exception) {
            Toast.makeText(this, "카메라 실행 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openMainImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickMainImageLauncher.launch(intent)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CAMERA_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openCamera()
                } else {
                    Toast.makeText(this, "카메라 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showVehicleTypeDialog() {
        val types = viewModel.availableTypes.value.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("차량 종류 선택")
            .setItems(types) { _, which ->
                viewModel.updateVehicleType(types[which])
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun showVehicleModelDialog() {
        val models = viewModel.availableModels.value.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("차량 브랜드/모델 선택")
            .setItems(models) { _, which ->
                viewModel.updateVehicleModel(models[which])
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun showVehicleOptionsDialog() {
        val options = viewModel.vehicleOptions.value
        val optionNames = options.map { it.optionName }.toTypedArray()
        val checkedItems = options.map { it.isInclude }.toBooleanArray()

        AlertDialog.Builder(this)
            .setTitle("차량 옵션 선택")
            .setMultiChoiceItems(optionNames, checkedItems) { _, which, isChecked ->
                checkedItems[which] = isChecked
            }
            .setPositiveButton("확인") { _, _ ->
                val updatedOptions = options.mapIndexed { index, option ->
                    option.copy(isInclude = checkedItems[index])
                }
                viewModel.updateVehicleOptions(updatedOptions)
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun updateVehicleOptionsButtonText(options: List<com.nobody.campick.models.vehicle.VehicleOption>) {
        val selectedOptions = options.filter { it.isInclude }
        val buttonText = if (selectedOptions.isEmpty()) {
            "옵션을 선택하세요"
        } else {
            selectedOptions.joinToString(", ") { it.optionName }
        }
        binding.buttonVehicleOptions.text = buttonText

        // 선택된 옵션이 있으면 텍스트 색상을 흰색으로, 없으면 투명도 적용
        val textColor = if (selectedOptions.isNotEmpty()) {
            AppColors.primaryText.toArgb()
        } else {
            AppColors.brandWhite60.toArgb()
        }
        binding.buttonVehicleOptions.setTextColor(textColor)
    }

    private fun updateErrorStates(errors: Map<String, String>) {
        // Update input container backgrounds based on error state
        updateInputContainerError(binding.titleInputContainer, errors.containsKey("title"))
        updateInputContainerError(binding.generationInputContainer, errors.containsKey("generation"))
        updateInputContainerError(binding.mileageInputContainer, errors.containsKey("mileage"))
        updateInputContainerError(binding.priceInputContainer, errors.containsKey("price"))
        updateInputContainerError(binding.locationInputContainer, errors.containsKey("location"))
        updateInputContainerError(binding.plateHashInputContainer, errors.containsKey("plateHash"))
        updateInputContainerError(binding.descriptionInputContainer, errors.containsKey("description"))

        // Update error text views
        binding.textViewTitleError.apply {
            text = errors["title"]
            isVisible = errors.containsKey("title")
        }

        binding.textViewGenerationError.apply {
            text = errors["generation"]
            isVisible = errors.containsKey("generation")
        }

        binding.textViewMileageError.apply {
            text = errors["mileage"]
            isVisible = errors.containsKey("mileage")
        }

        binding.textViewPriceError.apply {
            text = errors["price"]
            isVisible = errors.containsKey("price")
        }

        binding.textViewLocationError.apply {
            text = errors["location"]
            isVisible = errors.containsKey("location")
        }

        binding.textViewPlateHashError.apply {
            text = errors["plateHash"]
            isVisible = errors.containsKey("plateHash")
        }

        binding.textViewDescriptionError.apply {
            text = errors["description"]
            isVisible = errors.containsKey("description")
        }

        binding.textViewImageError.apply {
            text = errors["images"]
            isVisible = errors.containsKey("images")
        }

        binding.textViewVehicleTypeError.apply {
            text = errors["vehicleType"]
            isVisible = errors.containsKey("vehicleType")
        }

        binding.textViewVehicleModelError.apply {
            text = errors["vehicleModel"]
            isVisible = errors.containsKey("vehicleModel")
        }
    }

    private fun updateInputContainerError(container: FrameLayout, hasError: Boolean) {
        container.setBackgroundResource(
            if (hasError) R.drawable.styled_input_error_background
            else R.drawable.styled_input_background
        )
    }

    private fun showSuccessDialog() {
        AlertDialog.Builder(this)
            .setTitle("등록 완료")
            .setMessage(viewModel.alertMessage.value)
            .setPositiveButton("확인") { _, _ ->
                viewModel.dismissSuccessAlert()
                finish()
            }
            .setCancelable(false)
            .show()
    }

    private fun showErrorDialog() {
        AlertDialog.Builder(this)
            .setTitle("등록 실패")
            .setMessage(viewModel.alertMessage.value)
            .setPositiveButton("확인") { _, _ ->
                viewModel.dismissErrorAlert()
            }
            .show()
    }

    // 차량번호 검증 함수
    private fun isValidPlate(text: String): Boolean {
        return if (text.isEmpty()) true
        else text.length >= 6 && koreanPlateRegex.matcher(text).matches()
    }

    // 차량번호 포맷팅 함수 (Swift 버전과 동일한 로직)
    private fun formatPlateNumber(input: String): String {
        // 숫자와 완성형 한글만 남기기
        val cleaned = input.replace(Regex("[^0-9가-힣]"), "")

        if (cleaned.isEmpty()) return cleaned

        // 숫자와 한글 분리
        var numbers = ""
        var hangul = ""
        var finalNumbers = ""
        var foundHangul = false

        for (char in cleaned) {
            when {
                char.isDigit() && !foundHangul -> {
                    if (numbers.length < 3) {
                        numbers += char
                    }
                }
                !char.isDigit() && !foundHangul && numbers.length >= 2 -> {
                    hangul = char.toString()
                    foundHangul = true
                }
                char.isDigit() && foundHangul -> {
                    if (finalNumbers.length < 4) {
                        finalNumbers += char
                    }
                }
            }
        }

        return numbers + hangul + finalNumbers
    }

    // 차량번호 검증 상태 UI 업데이트
    private fun updatePlateHashValidation(text: String) {
        val isValid = isValidPlate(text)
        val hasError = text.length >= 6 && !isValid

        // 입력 컨테이너 테두리 색상 변경
        updateInputContainerError(binding.plateHashInputContainer, hasError)

        // 상태 아이콘 표시
        if (text.length >= 6) {
            binding.imageViewPlateHashStatus.isVisible = true
            binding.imageViewPlateHashStatus.setImageResource(
                if (isValid) R.drawable.ic_check_circle else R.drawable.ic_error_circle
            )
        } else {
            binding.imageViewPlateHashStatus.isVisible = false
        }

        // 에러 메시지 표시
        if (hasError) {
            binding.textViewPlateHashError.text = "올바른 번호판 형식을 입력하세요 (예: 123가4567)"
            binding.textViewPlateHashError.isVisible = true
        } else {
            binding.textViewPlateHashError.isVisible = false
        }
    }

    // 숫자 포맷팅 (Swift formatNumber와 동일한 로직)
    private fun formatNumber(value: String): String {
        // 숫자만 추출
        val numbers = value.replace(Regex("[^0-9]"), "")

        return if (numbers.isNotEmpty() && numbers.toLongOrNull() != null && numbers.toLong() > 0) {
            numberFormatter.format(numbers.toLong())
        } else {
            ""
        }
    }
}