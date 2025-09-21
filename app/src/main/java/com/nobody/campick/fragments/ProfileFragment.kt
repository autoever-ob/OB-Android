package com.nobody.campick.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
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
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by viewModels()
    private lateinit var productAdapter: ProfileProductAdapter

    private var isOwnProfile: Boolean = true

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
                showRightButton = isOwnProfile, // 자신의 프로필일 때만 설정 버튼 표시
                rightButtonIcon = if (isOwnProfile) R.drawable.ic_lock else null,
                rightButtonAction = if (isOwnProfile) {
                    { showPasswordChangeDialog() }
                } else null
            ),
            onBackClick = {
                requireActivity().supportFragmentManager.popBackStack()
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

        settingChangePassword?.setOnClickListener {
            showPasswordChangeDialog()
        }

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
            viewModel.profileResponse.collect { profile ->
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

        // 통계 정보
        textViewTotalListings?.text = viewModel.totalListings.toString()
        textViewSellingCount?.text = viewModel.sellingCount.toString()
        textViewSoldCount?.text = viewModel.soldCount.toString()

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

        viewModel.loadProfile(memberId)
    }

    private fun handleProductClick(product: Product) {
        // TODO: 상품 상세 화면으로 이동
        Toast.makeText(requireContext(), "상품 클릭: ${product.title}", Toast.LENGTH_SHORT).show()
    }

    private fun handleLoadMore() {
        val memberId = arguments?.getString("member_id")
        when (viewModel.activeTab.value) {
            ProfileViewModel.TabType.SELLING -> {
                viewModel.loadMoreSellingProducts(memberId)
            }
            ProfileViewModel.TabType.SOLD -> {
                viewModel.loadMoreSoldProducts(memberId)
            }
        }
    }

    private fun showEditProfileDialog() {
        viewModel.profileResponse.value?.let { profile ->
            val editDialog = ProfileEditDialog(
                context = requireContext(),
                profile = profile,
                onSave = { nickname, description, phoneNumber ->
                    // TODO: 프로필 업데이트 처리
                    Toast.makeText(requireContext(), "프로필이 업데이트되었습니다", Toast.LENGTH_SHORT).show()
                    // editDialog.dismiss() - 다이얼로그에서 자동으로 처리됨
                },
                onCancel = {
                    // 취소시 특별한 처리가 필요하지 않음
                },
                onImagePickerNeeded = {
                    // TODO: 이미지 피커 실행
                    Toast.makeText(requireContext(), "이미지 선택", Toast.LENGTH_SHORT).show()
                }
            )
            editDialog.show()
        }
    }

    private fun startChatWithUser(userId: String) {
        // TODO: 채팅 화면으로 이동
        Toast.makeText(requireContext(), "채팅 시작: $userId", Toast.LENGTH_SHORT).show()
    }

    private fun showPasswordChangeDialog() {
        val passwordChangeDialog = CustomConfirmationDialog(
            context = requireContext(),
            title = "비밀번호 변경",
            message = "비밀번호를 변경하시겠습니까?",
            confirmButtonText = "변경하기",
            cancelButtonText = "취소",
            isDestructive = false,
            onConfirm = {
                // TODO: 비밀번호 변경 화면으로 이동
                Toast.makeText(requireContext(), "비밀번호 변경", Toast.LENGTH_SHORT).show()
            },
            onCancel = {
                // 취소시 특별한 처리가 필요하지 않음
            }
        )
        passwordChangeDialog.show()
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
        // TODO: 로그아웃 처리
        Toast.makeText(requireContext(), "로그아웃 되었습니다", Toast.LENGTH_SHORT).show()
    }

    private fun performAccountDeletion() {
        // TODO: 계정 탈퇴 처리
        Toast.makeText(requireContext(), "계정이 탈퇴되었습니다", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}