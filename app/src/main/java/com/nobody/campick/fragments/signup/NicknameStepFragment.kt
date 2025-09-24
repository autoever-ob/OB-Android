package com.nobody.campick.fragments.signup

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.nobody.campick.databinding.FragmentNicknameStepBinding
import com.nobody.campick.viewmodels.SignupViewModel
import kotlinx.coroutines.launch
import java.io.File

class NicknameStepFragment : Fragment() {

    private var _binding: FragmentNicknameStepBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SignupViewModel by activityViewModels()

    private var cameraImageUri: Uri? = null

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                viewModel.setSelectedImageUri(uri)
                loadImage(uri)
            }
        }
    }

    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            cameraImageUri?.let { uri ->
                viewModel.setSelectedImageUri(uri)
                loadImage(uri)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNicknameStepBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupNicknameInput()
        setupImageButtons()
        observeViewModel()
    }

    private fun setupNicknameInput() {
        binding.etNickname.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val nickname = s?.toString() ?: ""
                viewModel.setNickname(nickname)
            }
        })

        binding.btnComplete.setOnClickListener {
            viewModel.nicknameNext()
        }
    }

    private fun setupImageButtons() {
        binding.btnCamera.setOnClickListener {
            openCamera()
        }

        binding.btnGallery.setOnClickListener {
            openGallery()
        }
    }

    private fun openCamera() {
        val imageFile = File(requireContext().cacheDir, "profile_${System.currentTimeMillis()}.jpg")
        cameraImageUri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.fileprovider",
            imageFile
        )

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri)
        }
        cameraLauncher.launch(intent)
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(intent)
    }

    private fun loadImage(uri: Uri) {
        Glide.with(this)
            .load(uri)
            .circleCrop()
            .into(binding.ivAvatar)
        binding.ivAvatar.alpha = 1.0f
        binding.ivAvatar.setPadding(0, 0, 0, 0)
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.nickname.collect { nickname ->
                if (binding.etNickname.text?.toString() != nickname) {
                    binding.etNickname.setText(nickname)
                }
                val valid = nickname.trim().length >= 2
                binding.btnComplete.isVisible = valid
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.selectedImageUri.collect { uri ->
                uri?.let { loadImage(it) }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isSubmitting.collect { submitting ->
                binding.btnComplete.text = if (submitting) "처리 중..." else "가입 완료"
                binding.btnComplete.isEnabled = !submitting
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.submitError.collect { error ->
                binding.tvError.isVisible = error != null
                binding.tvError.text = error
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}