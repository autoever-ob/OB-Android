package com.nobody.campick.fragments

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.compose.ui.graphics.toArgb
import com.bumptech.glide.Glide
import com.nobody.campick.R
import com.nobody.campick.resources.theme.AppColors
import com.nobody.campick.adapters.ProfileProductAdapter
import com.nobody.campick.databinding.FragmentProfileBinding
import com.nobody.campick.models.Product
import com.nobody.campick.viewmodels.ProfileViewModel
import com.nobody.campick.views.components.CustomConfirmationDialog
import com.nobody.campick.views.components.LogoutDialog
import com.nobody.campick.views.components.AccountDeletionDialog
import com.nobody.campick.views.components.ProfileEditDialog
import com.nobody.campick.views.components.CommonHeader
import com.nobody.campick.utils.ImageUtils
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by viewModels()
    private lateinit var productAdapter: ProfileProductAdapter

    private var isOwnProfile: Boolean = true

    // 이미지 피커를 위한 ActivityResultLauncher
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            handleImageSelection(selectedUri)
        }
    }

    companion object {
        fun newInstance(memberId: String? = null): ProfileFragment {
            return ProfileFragment().apply {
                arguments = Bundle().apply {
                    memberId?.let { putString("member_id", it) }
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        setupRecyclerView()
        observeViewModel()
        loadProfile()
    }

    private fun setupUI() {
        binding.apply {
            // 에러 상태에서 재시도 버튼
            buttonRetry.setOnClickListener {
                loadProfile()
            }

            // 스켈레톤 UI 설정
            loadingContainer.setContent {
                androidx.compose.material3.MaterialTheme {
                    com.nobody.campick.views.components.ProfileSkeletonView(
                        isOwnProfile = isOwnProfile
                    )
                }
            }
        }

        // 탭 네비게이션 설정 (findViewById 사용)
        setupTabNavigation()

        // 설정 섹션 클릭 리스너 (findViewById 사용)
        setupSettingsSection()
    }

    private fun setupCommonHeader() {
        binding.commonHeader.setupHeader(
            type = CommonHeader.HeaderType.Navigation(
                title = "내 프로필",
                showBackButton = !isOwnProfile, // 자신의 프로필이 아닐 때만 뒤로가기 버튼 표시
                showRightButton = false
            ),
            onBackClick = {
                requireActivity().finish()
            }
        )
    }

    private fun setupRecyclerView() {
        productAdapter = ProfileProductAdapter(
            onProductClick = { product ->
                handleProductClick(product)
            },
            onLoadMore = {
                handleLoadMore()
            }
        )

        binding.recyclerViewProducts.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = productAdapter
        }
    }

    private fun setupTabNavigation() {
        // 포함된 레이아웃의 뷰들은 findViewById로 접근
        val tabSelling = view?.findViewById<View>(R.id.tabSelling)
        val tabSold = view?.findViewById<View>(R.id.tabSold)

        tabSelling?.setOnClickListener {
            viewModel.setActiveTab(ProfileViewModel.TabType.SELLING)
        }

        tabSold?.setOnClickListener {
            viewModel.setActiveTab(ProfileViewModel.TabType.SOLD)
        }
    }

    private fun setupSettingsSection() {
        // 포함된 레이아웃의 뷰들은 findViewById로 접근
        val settingChangePassword = view?.findViewById<View>(R.id.settingChangePassword)
        val settingLogout = view?.findViewById<View>(R.id.settingLogout)
        val settingDeleteAccount = view?.findViewById<View>(R.id.settingDeleteAccount)


        settingLogout?.setOnClickListener {
            showLogoutConfirmDialog()
        }

        settingDeleteAccount?.setOnClickListener {
            showAccountDeletionDialog()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            // 프로필 정보 관찰
            viewModel.profileData.collect { profile ->
                profile?.let {
                    updateProfileHeader(it)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            // 로딩 상태 관찰
            viewModel.isLoading.collect { isLoading ->
                binding.loadingContainer.visibility = if (isLoading) View.VISIBLE else View.GONE
                binding.contentContainer.visibility = if (isLoading) View.GONE else View.VISIBLE
                binding.errorContainer.visibility = View.GONE
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            // 에러 메시지 관찰
            viewModel.errorMessage.collect { error ->
                error?.let {
                    binding.textViewErrorMessage.text = it
                    binding.errorContainer.visibility = View.VISIBLE
                    binding.loadingContainer.visibility = View.GONE
                    binding.contentContainer.visibility = View.GONE
                    viewModel.clearError()
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            // 활성 탭 관찰
            viewModel.activeTab.collect { tab ->
                updateTabSelection(tab)
                updateProductList()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            // 로그인 리다이렉트 관찰
            viewModel.shouldRedirectToLogin.collect { shouldRedirect ->
                if (shouldRedirect) {
                    // TODO: 로그인 화면으로 이동
                    Toast.makeText(requireContext(), "로그인이 필요합니다", Toast.LENGTH_SHORT).show()
                    viewModel.clearRedirectToLogin()
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            // 프로필 업데이트 상태 관찰
            viewModel.isUpdatingProfile.collect { isUpdating ->
                // TODO: 로딩 표시
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            // 이미지 업로드 상태 관찰
            viewModel.isUploadingImage.collect { isUploading ->
                // TODO: 로딩 표시
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            // 프로필 업데이트 성공 관찰
            viewModel.profileUpdateSuccess.collect { success ->
                if (success) {
                    Toast.makeText(requireContext(), "프로필이 업데이트되었습니다", Toast.LENGTH_SHORT).show()
                    viewModel.clearProfileUpdateSuccess()
                }
            }
        }

        // 카운트 정보 관찰
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.soldProductCount.collect { count ->
                view?.findViewById<TextView>(R.id.textViewSoldCount)?.text = count.toString()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.sellOrReserveProductCount.collect { count ->
                view?.findViewById<TextView>(R.id.textViewSellingCount)?.text = count.toString()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.allProductCount.collect { count ->
                view?.findViewById<TextView>(R.id.textViewTotalListings)?.text = count.toString()
            }
        }

        // 탭 변경 관찰
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.activeTab.collect { activeTab ->
                updateTabSelection(activeTab)
                updateProductList()
            }
        }

        // 판매중 상품 관찰
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.sellingProducts.collect { products ->
                if (viewModel.activeTab.value == ProfileViewModel.TabType.SELLING) {
                    updateProductList()
                }
            }
        }

        // 판매완료 상품 관찰
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.soldProducts.collect { products ->
                if (viewModel.activeTab.value == ProfileViewModel.TabType.SOLD) {
                    updateProductList()
                }
            }
        }
    }

    private fun updateProfileHeader(profile: com.nobody.campick.models.ProfileData) {
        // 포함된 레이아웃의 뷰들은 findViewById로 접근
        val textViewNickname = view?.findViewById<TextView>(R.id.textViewNickname)
        val imageViewAvatar = view?.findViewById<ImageView>(R.id.imageViewAvatar)
        val textViewRating = view?.findViewById<TextView>(R.id.textViewRating)
        val textViewDescription = view?.findViewById<TextView>(R.id.textViewDescription)
        val textViewTotalListings = view?.findViewById<TextView>(R.id.textViewTotalListings)
        val textViewSellingCount = view?.findViewById<TextView>(R.id.textViewSellingCount)
        val textViewSoldCount = view?.findViewById<TextView>(R.id.textViewSoldCount)
        val buttonEdit = view?.findViewById<View>(R.id.buttonEdit)
        val buttonSendMessage = view?.findViewById<View>(R.id.buttonSendMessage)
        val settingsSection = view?.findViewById<View>(R.id.settingsSection)

        // 사용자 이름
        textViewNickname?.text = profile.nickname

        // 프로필 이미지
        imageViewAvatar?.let { imageView ->
            if (!profile.profileImage.isNullOrEmpty()) {
                Glide.with(requireContext())
                    .load(profile.profileImage)
                    .placeholder(R.drawable.ic_person)
                    .error(R.drawable.ic_person)
                    .circleCrop()
                    .into(imageView)
            } else {
                imageView.setImageResource(R.drawable.ic_person)
            }
        }

        // 평점
        textViewRating?.text = String.format("%.1f", profile.rating ?: 0.0)

        // 자기소개
        textViewDescription?.let { descView ->
            if (!profile.description.isNullOrEmpty()) {
                descView.text = profile.description
                descView.visibility = View.VISIBLE
            } else {
                descView.visibility = View.GONE
            }
        }

        // 통계 정보 (새로운 카운트 API 결과 사용)
        textViewTotalListings?.text = viewModel.allProductCount.value.toString()
        textViewSellingCount?.text = viewModel.sellOrReserveProductCount.value.toString()
        textViewSoldCount?.text = viewModel.soldProductCount.value.toString()

        // 버튼 표시 설정
        if (isOwnProfile) {
            buttonEdit?.visibility = View.VISIBLE
            buttonSendMessage?.visibility = View.GONE
            settingsSection?.visibility = View.VISIBLE
        } else {
            buttonEdit?.visibility = View.GONE
            buttonSendMessage?.visibility = View.VISIBLE
            settingsSection?.visibility = View.GONE
        }

        // 편집 버튼 클릭
        buttonEdit?.setOnClickListener {
            showEditProfileDialog()
        }

        // 메시지 버튼 클릭
        buttonSendMessage?.setOnClickListener {
            startChatWithUser(profile.id.toString())
        }
    }

    private fun updateTabSelection(activeTab: ProfileViewModel.TabType) {
        // 포함된 레이아웃의 뷰들은 findViewById로 접근
        val textViewTabSelling = view?.findViewById<TextView>(R.id.textViewTabSelling)
        val indicatorSelling = view?.findViewById<View>(R.id.indicatorSelling)
        val textViewTabSold = view?.findViewById<TextView>(R.id.textViewTabSold)
        val indicatorSold = view?.findViewById<View>(R.id.indicatorSold)

        when (activeTab) {
            ProfileViewModel.TabType.SELLING -> {
                textViewTabSelling?.setTextColor(AppColors.brandOrange.toArgb())
                indicatorSelling?.visibility = View.VISIBLE
                textViewTabSold?.setTextColor(AppColors.brandWhite60.toArgb())
                indicatorSold?.visibility = View.GONE
            }
            ProfileViewModel.TabType.SOLD -> {
                textViewTabSelling?.setTextColor(AppColors.brandWhite60.toArgb())
                indicatorSelling?.visibility = View.GONE
                textViewTabSold?.setTextColor(AppColors.brandOrange.toArgb())
                indicatorSold?.visibility = View.VISIBLE
            }
        }
    }

    private fun updateProductList() {
        val products = viewModel.getCurrentProducts()
        val hasMore = viewModel.hasMoreProducts()
        productAdapter.submitList(products, hasMore)
    }

    private fun loadProfile() {
        val memberId = arguments?.getString("member_id")
        isOwnProfile = memberId.isNullOrEmpty()

        // 프로필 소유자에 따라 헤더 업데이트
        setupCommonHeader()

        val targetMemberId = if (memberId.isNullOrEmpty()) {
            // 현재 로그인한 사용자의 memberId 사용
            com.nobody.campick.managers.UserState.memberId.value
        } else {
            memberId
        }

        println("🔍 ProfileFragment.loadProfile - targetMemberId: $targetMemberId")

        // 프로필 데이터 로드
        viewModel.loadProfile(targetMemberId)

        // 매물 카운트 로드
        viewModel.loadProductCounts(targetMemberId)
    }

    private fun handleProductClick(product: Product) {
        val intent = com.nobody.campick.activities.VehicleDetailActivity.newIntent(
            context = requireContext(),
            vehicleId = product.productId.toString()
        )
        startActivity(intent)
    }

    private fun handleLoadMore() {
        // iOS와 동일하게 내 매물 페이지로 이동
        val memberId = arguments?.getString("member_id")
        val intent = Intent(requireContext(), com.nobody.campick.activities.MyProductsActivity::class.java).apply {
            putExtra("memberId", memberId ?: com.nobody.campick.managers.UserState.memberId.value)
        }
        startActivity(intent)
    }

    private fun showEditProfileDialog() {
        viewModel.profileData.value?.let { profile ->
            val editDialog = ProfileEditDialog(
                context = requireContext(),
                profile = profile,
                onSave = { nickname, description, phoneNumber ->
                    viewModel.updateProfileInfo(nickname, phoneNumber, description)
                },
                onCancel = {
                    // 취소시 특별한 처리가 필요하지 않음
                },
                onImagePickerNeeded = {
                    openImagePicker()
                }
            )
            editDialog.show()
        }
    }

    private fun startChatWithUser(userId: String) {
        // TODO: 채팅 화면으로 이동
        Toast.makeText(requireContext(), "채팅 시작: $userId", Toast.LENGTH_SHORT).show()
    }


    private fun showLogoutConfirmDialog() {
        val logoutDialog = LogoutDialog(
            context = requireContext(),
            onConfirm = {
                performLogout()
            },
            onCancel = {
                // 취소시 특별한 처리가 필요하지 않음
            }
        )
        logoutDialog.show()
    }

    private fun showAccountDeletionDialog() {
        val accountDeletionDialog = AccountDeletionDialog(
            context = requireContext(),
            onConfirm = {
                performAccountDeletion()
            },
            onCancel = {
                // 취소시 특별한 처리가 필요하지 않음
            }
        )
        accountDeletionDialog.show()
    }

    private fun performLogout() {
        viewModel.logout {
            // 로그아웃 성공시 토큰 클리어 및 로그인 화면으로 이동
            // TODO: TokenManager.clearTokens() 및 로그인 화면 이동
            Toast.makeText(requireContext(), "로그아웃 되었습니다", Toast.LENGTH_SHORT).show()
        }
    }

    private fun performAccountDeletion() {
        viewModel.deleteMember {
            // 회원탈퇴 성공시 토큰 클리어 및 로그인 화면으로 이동
            // TODO: TokenManager.clearTokens() 및 로그인 화면 이동
            Toast.makeText(requireContext(), "계정이 탈퇴되었습니다", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * 이미지 피커 열기
     */
    private fun openImagePicker() {
        imagePickerLauncher.launch("image/*")
    }

    /**
     * 선택된 이미지 처리
     */
    private fun handleImageSelection(uri: Uri) {
        lifecycleScope.launch {
            try {
                // 이미지를 Bitmap으로 로드하고 1MB 이하로 압축
                val bitmap = ImageUtils.loadCompressedBitmapFromUri(requireContext(), uri, 1.0)

                if (bitmap != null) {
                    // 1MB 이하로 압축
                    val compressedImageData = ImageUtils.compressImage(bitmap, 1.0)

                    if (compressedImageData != null) {
                        // 즉시 서버에 업로드
                        viewModel.uploadProfileImage(compressedImageData) { response ->
                            // 프로필 이미지 업로드 성공
                            // TODO: 로컬 스토리지에 새로운 프로필 이미지 URL 저장
                            Toast.makeText(
                                requireContext(),
                                "프로필 이미지가 업데이트되었습니다",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "이미지 압축에 실패했습니다",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    bitmap.recycle()
                } else {
                    Toast.makeText(
                        requireContext(),
                        "이미지를 불러올 수 없습니다",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "이미지 처리 중 오류가 발생했습니다: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}