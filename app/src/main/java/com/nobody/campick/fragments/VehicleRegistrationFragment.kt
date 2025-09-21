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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.compose.ui.graphics.toArgb
import com.nobody.campick.R
import com.nobody.campick.adapters.VehicleImageAdapter
import com.nobody.campick.databinding.FragmentVehicleRegistrationBinding
import com.nobody.campick.resources.theme.AppColors
import com.nobody.campick.viewmodels.VehicleRegistrationViewModel
import com.nobody.campick.views.components.VehicleSelectionDialog
import com.nobody.campick.views.components.VehicleMultiSelectionDialog
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

        setupRecyclerViews()
        setupTextWatchers()
        setupClickListeners()
        observeViewModel()
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
            layoutManager = androidx.recyclerview.widget.GridLayoutManager(requireContext(), 3)
            adapter = imageAdapter
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
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                println("Title text changing: $s")
            }
            override fun afterTextChanged(s: Editable?) {
                println("Title text changed: ${s.toString()}")
                viewModel.updateTitle(s.toString())
            }
        })

        binding.editTextPlateNumber.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val plateNumber = s.toString()

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
                val input = s.toString().replace(",", "")
                if (input != viewModel.mileage.value.replace(",", "")) {
                    viewModel.updateMileage(input)
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

        binding.editTextLocation.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                viewModel.updateLocation(s.toString())
            }
        })

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

    private fun showVehicleTypeDialog() {
        val types = viewModel.availableTypes.value
        val currentType = viewModel.vehicleType.value

        VehicleSelectionDialog(
            context = requireContext(),
            title = "차량 종류 선택",
            options = types,
            selectedOption = currentType.ifEmpty { null }
        ) { selectedType ->
            viewModel.updateVehicleType(selectedType)
        }.show()
    }

    private fun showVehicleModelDialog() {
        val models = viewModel.availableModels.value
        val currentModel = viewModel.vehicleModel.value

        VehicleSelectionDialog(
            context = requireContext(),
            title = "차량 브랜드/모델 선택",
            options = models,
            selectedOption = currentModel.ifEmpty { null }
        ) { selectedModel ->
            viewModel.updateVehicleModel(selectedModel)
        }.show()
    }

    private fun showVehicleOptionsDialog() {
        val options = viewModel.vehicleOptions.value

        VehicleMultiSelectionDialog(
            context = requireContext(),
            title = "차량 옵션 선택",
            options = options
        ) { updatedOptions ->
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}