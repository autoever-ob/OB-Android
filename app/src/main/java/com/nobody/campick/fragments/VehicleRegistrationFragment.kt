package com.nobody.campick.fragments

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.compose.ui.graphics.toArgb
import com.nobody.campick.R
import com.nobody.campick.activities.MainTabActivity
import com.nobody.campick.adapters.VehicleImageAdapter
import com.nobody.campick.databinding.FragmentVehicleRegistrationBinding
import com.nobody.campick.resources.theme.AppColors
import com.nobody.campick.viewmodels.VehicleRegistrationViewModel
import com.nobody.campick.views.components.VehicleSelectionDialog
import com.nobody.campick.views.components.VehicleMultiSelectionDialog
import com.nobody.campick.views.components.CommonHeader
import com.nobody.campick.views.components.YearPickerDialog
import com.nobody.campick.views.components.LocationPickerDialog
import com.nobody.campick.views.components.MileagePickerDialog
import com.nobody.campick.views.components.VehicleOptionsPickerDialog
import com.nobody.campick.views.components.VehicleTypeModelPickerDialog
import kotlinx.coroutines.launch
import java.io.File
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

class VehicleRegistrationFragment : Fragment() {

    private var _binding: FragmentVehicleRegistrationBinding? = null
    private val binding get() = _binding!!
    private val viewModel: VehicleRegistrationViewModel by viewModels()

    companion object {
        private const val CAMERA_PERMISSION_REQUEST_CODE = 1001
        private const val ARG_EDITING_PRODUCT_ID = "editing_product_id"

        fun newInstance(editingProductId: String): VehicleRegistrationFragment {
            return VehicleRegistrationFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_EDITING_PRODUCT_ID, editingProductId)
                }
            }
        }
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
            Toast.makeText(requireContext(), "이미지 선택을 위해 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
        }
    }

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                println("📱 갤러리에서 이미지 선택됨: $uri")
                viewModel.addVehicleImageAndUpload(uri, requireContext())
            }
        }
    }

    private var tempCameraImageUri: Uri? = null

    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            tempCameraImageUri?.let { uri ->
                println("📸 카메라에서 이미지 촬영됨: $uri")
                viewModel.addVehicleImageAndUpload(uri, requireContext())
            }
        }
    }

    private val pickMainImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                println("🖼️ 메인 이미지 선택됨: $uri")
                viewModel.addVehicleImageAsMainAndUpload(uri, requireContext())
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVehicleRegistrationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // IME 설정 강제 적용
        activity?.window?.setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        // 수정 모드 확인 (iOS VehicleRegistrationView와 동일)
        val editingProductId = arguments?.getString(ARG_EDITING_PRODUCT_ID)
        val isEditMode = editingProductId != null

        println("🔧 Fragment 생성: editingProductId=$editingProductId, isEditMode=$isEditMode")

        if (editingProductId != null) {
            println("🔧 수정 모드로 진입: productId=$editingProductId")
            viewModel.loadProductForEdit(editingProductId)
        }

        setupHeader(isEditMode)
        setupRecyclerViews()
        setupTextWatchers()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupHeader(isEditMode: Boolean) {
        println("📌 setupHeader 호출: isEditMode=$isEditMode")
        val headerTitle = if (isEditMode) "매물 수정" else "매물 등록"
        println("📌 헤더 제목 설정: '$headerTitle', 뒤로가기 버튼: $isEditMode")

        binding.commonHeader.setupHeader(
            type = CommonHeader.HeaderType.Navigation(
                title = headerTitle,
                showBackButton = isEditMode,
                showRightButton = false
            ),
            onBackClick = {
                // 수정 모드에서 뒤로가기: Activity 종료 (VehicleDetailActivity로 돌아감)
                activity?.finish()
            }
        )
    }

    private fun setupRecyclerViews() {
        imageAdapter = VehicleImageAdapter(
            onAddImageClick = { view ->
                showImageSourcePopup(view)
            },
            onImageClick = { imageId ->
                // 이미지 클릭은 더 이상 사용하지 않음 (메인 이미지 설정으로 대체)
            },
            onImageRemove = { imageId ->
                viewModel.removeVehicleImage(imageId)
            },
            onSetMainImage = { imageId ->
                viewModel.setMainImage(imageId)
            }
        )

        binding.recyclerViewImages.apply {
            val gridLayoutManager = androidx.recyclerview.widget.GridLayoutManager(requireContext(), 3)
            gridLayoutManager.isAutoMeasureEnabled = true
            layoutManager = gridLayoutManager
            adapter = imageAdapter
            setHasFixedSize(false)
            isNestedScrollingEnabled = false
            overScrollMode = View.OVER_SCROLL_NEVER
        }
    }

    private fun setupTextWatchers() {
        // 제목 필드에 포커스 리스너 및 입력 확인 추가
        binding.editTextTitle.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                println("Title EditText focused")
            }
        }

        binding.editTextTitle.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val newValue = s.toString()
                if (viewModel.title.value != newValue) {
                    viewModel.updateTitle(newValue)
                }
            }
        })

        binding.editTextPlateNumber.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val plateNumber = s.toString()

                // ViewModel에 값 업데이트 (값이 다를 때만)
                if (viewModel.plateHash.value != plateNumber) {
                    viewModel.updatePlateHash(plateNumber)
                }

                // 실시간 검증 및 아이콘 표시
                if (plateNumber.isNotEmpty()) {
                    val isValid = koreanPlateRegex.matcher(plateNumber).matches()
                    binding.plateValidationIcon.isVisible = true
                    binding.plateValidationIcon.setImageResource(
                        if (isValid) android.R.drawable.ic_dialog_info
                        else android.R.drawable.ic_dialog_alert
                    )
                    binding.plateValidationIcon.setColorFilter(
                        ContextCompat.getColor(
                            requireContext(),
                            if (isValid) android.R.color.holo_green_dark
                            else android.R.color.holo_red_dark
                        )
                    )
                } else {
                    binding.plateValidationIcon.isVisible = false
                }
            }
        })


        binding.editTextPrice.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val input = s.toString().replace(",", "")
                if (input != viewModel.price.value.replace(",", "")) {
                    viewModel.updatePrice(input)
                }
            }
        })


        binding.editTextDescription.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val newValue = s.toString()
                if (viewModel.description.value != newValue) {
                    viewModel.updateDescription(newValue)
                }
            }
        })
    }

    private fun setupClickListeners() {
        binding.buttonVehicleModel.setOnClickListener {
            showVehicleTypeModelPickerDialog()
        }

        binding.buttonYear.setOnClickListener {
            showYearPickerDialog()
        }

        binding.editTextMileage.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val text = s.toString().replace(",", "")
                if (text.isNotEmpty()) {
                    val formatted = formatNumberWithComma(text)
                    if (formatted != s.toString()) {
                        binding.editTextMileage.removeTextChangedListener(this)
                        binding.editTextMileage.setText(formatted)
                        binding.editTextMileage.setSelection(formatted.length)
                        binding.editTextMileage.addTextChangedListener(this)
                    }
                }
                viewModel.updateMileage(text)
            }
        })

        binding.buttonLocation.setOnClickListener {
            showLocationPickerDialog()
        }

        binding.buttonVehicleOptions.setOnClickListener {
            showVehicleOptionsDialog()
        }

        binding.buttonSubmit.setOnClickListener {
            println("🔘 매물등록 버튼 클릭됨")
            viewModel.handleSubmit()
            println("📋 폼 검증 완료, 오류 개수: ${viewModel.errors.value.size}")
            viewModel.errors.value.forEach { (key, message) ->
                println("❌ 검증 오류 - $key: $message")
            }

            if (viewModel.isValidForSubmission()) {
                println("✅ 폼 검증 통과, API 호출 시작")
                viewModel.submitVehicleRegistration(requireContext())
            } else {
                println("❌ 폼 검증 실패, API 호출하지 않음")
            }
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
                updateVehicleTypeModelButtonText()
            }
        }

        lifecycleScope.launch {
            viewModel.vehicleModel.collect { model ->
                updateVehicleTypeModelButtonText()
            }
        }

        lifecycleScope.launch {
            viewModel.vehicleOptions.collect { options ->
                updateVehicleOptionsButtonText(options)
            }
        }

        lifecycleScope.launch {
            viewModel.generation.collect { year ->
                binding.buttonYear.text = if (year.isNotEmpty()) "${year}년" else "연식 선택"
                binding.buttonYear.setTextColor(
                    if (year.isNotEmpty())
                        ContextCompat.getColor(requireContext(), R.color.primary_text)
                    else
                        ContextCompat.getColor(requireContext(), R.color.brand_white_60)
                )
            }
        }

        lifecycleScope.launch {
            viewModel.mileage.collect { mileage ->
                if (binding.editTextMileage.text.toString().replace(",", "") != mileage) {
                    binding.editTextMileage.setText(formatNumberWithComma(mileage))
                }
            }
        }

        lifecycleScope.launch {
            viewModel.location.collect { location ->
                binding.buttonLocation.text = if (location.isNotEmpty()) location else "판매 지역 선택"
                binding.buttonLocation.setTextColor(
                    if (location.isNotEmpty())
                        ContextCompat.getColor(requireContext(), R.color.primary_text)
                    else
                        ContextCompat.getColor(requireContext(), R.color.brand_white_60)
                )
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
            viewModel.title.collect { title ->
                println("🔄 Title StateFlow 업데이트: '$title'")
                if (binding.editTextTitle.text.toString() != title) {
                    println("✏️ EditText에 title 설정: '$title'")
                    binding.editTextTitle.setText(title)
                }
            }
        }

        lifecycleScope.launch {
            viewModel.description.collect { description ->
                println("🔄 Description StateFlow 업데이트: '$description'")
                if (binding.editTextDescription.text.toString() != description) {
                    println("✏️ EditText에 description 설정")
                    binding.editTextDescription.setText(description)
                }
            }
        }

        lifecycleScope.launch {
            viewModel.plateHash.collect { plate ->
                println("🔄 PlateHash StateFlow 업데이트: '$plate'")
                if (binding.editTextPlateNumber.text.toString() != plate) {
                    println("✏️ EditText에 plate 설정: '$plate'")
                    binding.editTextPlateNumber.setText(plate)
                }
            }
        }

        lifecycleScope.launch {
            viewModel.showSuccessAlert.collect { show ->
                if (show) {
                    AlertDialog.Builder(requireContext())
                        .setTitle("성공")
                        .setMessage(viewModel.alertMessage.value)
                        .setPositiveButton("확인") { _, _ ->
                            viewModel.dismissSuccessAlert()
                            // 등록/수정 성공 시 홈 탭으로 이동 (iOS와 동일)
                            (activity as? com.nobody.campick.activities.MainTabActivity)?.navigateToHome()
                        }
                        .show()
                }
            }
        }

        lifecycleScope.launch {
            viewModel.showErrorAlert.collect { show ->
                if (show) {
                    AlertDialog.Builder(requireContext())
                        .setTitle("오류")
                        .setMessage(viewModel.alertMessage.value)
                        .setPositiveButton("확인") { _, _ ->
                            viewModel.dismissErrorAlert()
                        }
                        .show()
                }
            }
        }

        lifecycleScope.launch {
            viewModel.isSubmitting.collect { isSubmitting ->
                binding.buttonSubmit.isEnabled = !isSubmitting
                val editingProductId = arguments?.getString(ARG_EDITING_PRODUCT_ID)
                binding.buttonSubmit.text = when {
                    isSubmitting && editingProductId != null -> "수정 중..."
                    isSubmitting -> "등록 중..."
                    editingProductId != null -> "매물 수정"
                    else -> "매물 등록"
                }
            }
        }

    }

    private fun checkPermissionAndPickImage() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED -> {
                showImageSourceDialog()
            }
            ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) -> {
                AlertDialog.Builder(requireContext())
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
        AlertDialog.Builder(requireContext())
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
                requireContext(),
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST_CODE
            )
            return
        }

        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "JPEG_${timeStamp}_"
        val storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        try {
            val imageFile = File.createTempFile(imageFileName, ".jpg", storageDir)
            tempCameraImageUri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.fileprovider",
                imageFile
            )

            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, tempCameraImageUri)
            takePictureLauncher.launch(takePictureIntent)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "카메라 실행 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
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
                    Toast.makeText(requireContext(), "카메라 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showVehicleTypeModelPickerDialog() {
        val currentType = viewModel.vehicleType.value
        val currentModel = viewModel.vehicleModel.value
        val availableTypes = viewModel.availableTypes.value

        VehicleTypeModelPickerDialog(
            context = requireContext(),
            lifecycleOwner = viewLifecycleOwner,
            selectedType = currentType,
            selectedModel = currentModel,
            availableTypes = availableTypes,
            onTypeModelSelected = { type: String, model: String ->
                viewModel.updateVehicleType(type)
                viewModel.updateVehicleModel(model)
            }
        ).show()
    }

    private fun updateVehicleTypeModelButtonText() {
        val type = viewModel.vehicleType.value
        val model = viewModel.vehicleModel.value

        val displayText = when {
            type.isEmpty() -> "차량 종류와 모델을 선택하세요"
            model.isEmpty() -> "$type → 모델 선택"
            else -> "$type → $model"
        }

        binding.buttonVehicleModel.text = displayText
        binding.buttonVehicleModel.setTextColor(
            if (type.isEmpty())
                ContextCompat.getColor(requireContext(), R.color.brand_white_60)
            else
                ContextCompat.getColor(requireContext(), R.color.primary_text)
        )
    }

    private fun showYearPickerDialog() {
        val currentYear = viewModel.generation.value.toIntOrNull()
        YearPickerDialog(requireContext(), currentYear) { selectedYear ->
            viewModel.updateGeneration(selectedYear.toString())
        }.show()
    }

    private fun showMileagePickerDialog() {
        val currentMileage = viewModel.mileage.value.replace(",", "").toIntOrNull()
        MileagePickerDialog(requireContext(), currentMileage) { selectedMileage ->
            viewModel.updateMileage(numberFormatter.format(selectedMileage))
        }.show()
    }

    private fun showLocationPickerDialog() {
        val currentLocation = viewModel.location.value
        LocationPickerDialog(requireContext(), currentLocation) { selectedLocation ->
            viewModel.updateLocation(selectedLocation)
        }.show()
    }

    private fun showVehicleOptionsDialog() {
        VehicleOptionsPickerDialog(requireContext(), viewModel.vehicleOptions.value) { updatedOptions ->
            viewModel.updateVehicleOptions(updatedOptions)
        }.show()
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
        // Error state updates can be implemented here
        // Similar to the Activity version
    }

    private fun showImageSourcePopup(anchorView: View) {
        val popup = PopupMenu(requireContext(), anchorView)
        popup.menuInflater.inflate(R.menu.image_source_menu, popup.menu)

        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_gallery -> {
                    openImagePicker()
                    true
                }
                R.id.menu_camera -> {
                    openCamera()
                    true
                }
                else -> false
            }
        }

        popup.show()
    }

    private fun formatNumberWithComma(number: String): String {
        if (number.isEmpty()) return ""
        val numberValue = number.toLongOrNull() ?: return number
        return String.format("%,d", numberValue)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}