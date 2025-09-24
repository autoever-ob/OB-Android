package com.nobody.campick.fragments.signup

import android.app.AlertDialog
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.nobody.campick.R
import com.nobody.campick.databinding.FragmentEmailStepBinding
import com.nobody.campick.models.auth.UserType
import com.nobody.campick.viewmodels.SignupViewModel
import kotlinx.coroutines.launch

class EmailStepFragment : Fragment() {

    private var _binding: FragmentEmailStepBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SignupViewModel by activityViewModels()

    private var countDownTimer: CountDownTimer? = null
    private var remainingSeconds = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEmailStepBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUserTypeButtons()
        setupAgreementCheckboxes()
        setupEmailInput()
        setupVerificationCodeInput()
        observeViewModel()
    }

    private fun setupUserTypeButtons() {
        binding.btnNormalUser.setOnClickListener {
            viewModel.setUserType(UserType.NORMAL)
        }
        binding.btnDealer.setOnClickListener {
            viewModel.setUserType(UserType.DEALER)
        }
    }

    private fun setupAgreementCheckboxes() {
        binding.cbTerms.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setTermsAgreed(isChecked)
        }

        binding.cbPrivacy.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setPrivacyAgreed(isChecked)
        }

        binding.btnViewTerms.setOnClickListener {
            showAgreementDialog("서비스 이용 약관", getServiceTermsContent())
        }

        binding.btnViewPrivacy.setOnClickListener {
            showAgreementDialog("개인정보 수집 및 이용", getPrivacyContent())
        }
    }

    private fun setupEmailInput() {
        binding.etEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                viewModel.setEmail(s?.toString() ?: "")
            }
        })

        binding.btnSendCode.setOnClickListener {
            viewModel.showEmailCodeField()
            binding.etVerificationCode.setText("")
            lifecycleScope.launch {
                viewModel.sendEmailCode()
            }
        }
    }

    private fun setupVerificationCodeInput() {
        binding.etVerificationCode.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val digits = s?.toString()?.filter { it.isDigit() } ?: ""
                if (digits != s?.toString()) {
                    binding.etVerificationCode.setText(digits)
                    binding.etVerificationCode.setSelection(digits.length)
                }
                viewModel.setEmailCode(digits)
                binding.tvExpiredNotice.isVisible = false
            }
        })

        binding.btnNext.setOnClickListener {
            if (remainingSeconds == 0) {
                binding.tvExpiredNotice.isVisible = true
                binding.etVerificationCode.setText("")
            } else {
                viewModel.emailNext()
            }
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.userType.collect { userType ->
                updateUserTypeUI(userType)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.email.collect { email ->
                if (binding.etEmail.text?.toString() != email) {
                    binding.etEmail.setText(email)
                }
                updateSendButtonState()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.showEmailCodeField.collect { show ->
                binding.layoutVerification.isVisible = show
                if (show) {
                    startTimer()
                    binding.etVerificationCode.requestFocus()
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.emailCode.collect { code ->
                binding.btnNext.isVisible = code.isNotEmpty()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.termsAgreed.collect { agreed ->
                binding.cbTerms.isChecked = agreed
                updateSendButtonState()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.privacyAgreed.collect { agreed ->
                binding.cbPrivacy.isChecked = agreed
                updateSendButtonState()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.showEmailMismatchModal.collect { show ->
                if (show) {
                    showMismatchDialog()
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.showEmailDuplicateModal.collect { show ->
                if (show) {
                    showDuplicateDialog()
                }
            }
        }
    }

    private fun updateUserTypeUI(userType: UserType?) {
        binding.layoutAgreements.isVisible = userType != null

        when (userType) {
            UserType.NORMAL -> {
                binding.btnNormalUser.alpha = 1.0f
                binding.btnDealer.alpha = 0.8f
            }
            UserType.DEALER -> {
                binding.btnNormalUser.alpha = 0.8f
                binding.btnDealer.alpha = 1.0f
            }
            null -> {
                binding.btnNormalUser.alpha = 0.6f
                binding.btnDealer.alpha = 0.6f
            }
        }
    }

    private fun updateSendButtonState() {
        val termsAgreed = binding.cbTerms.isChecked
        val privacyAgreed = binding.cbPrivacy.isChecked
        val emailNotEmpty = binding.etEmail.text?.isNotEmpty() == true
        val canSend = termsAgreed && privacyAgreed && emailNotEmpty

        binding.btnSendCode.isEnabled = canSend
        binding.btnSendCode.alpha = if (canSend) 1.0f else 0.5f

        val expired = binding.layoutVerification.isVisible && remainingSeconds == 0
        binding.btnSendCode.text = if (expired) "재전송하기" else "인증하기"
    }

    private fun startTimer() {
        countDownTimer?.cancel()
        remainingSeconds = 180

        countDownTimer = object : CountDownTimer(180000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                remainingSeconds = (millisUntilFinished / 1000).toInt()
                updateTimerUI()
            }

            override fun onFinish() {
                remainingSeconds = 0
                updateTimerUI()
                updateSendButtonState()
            }
        }.start()
    }

    private fun updateTimerUI() {
        val minutes = remainingSeconds / 60
        val seconds = remainingSeconds % 60
        binding.tvTimer.text = String.format("%02d:%02d", minutes, seconds)

        val isExpired = remainingSeconds == 0
        val color = if (isExpired) {
            ContextCompat.getColor(requireContext(), android.R.color.holo_red_light)
        } else {
            ContextCompat.getColor(requireContext(), android.R.color.white)
        }

        binding.tvTimer.setTextColor(color)
        binding.ivTimer.setColorFilter(color)
        binding.tvTimer.alpha = if (isExpired) 1.0f else 0.7f
        binding.ivTimer.alpha = if (isExpired) 1.0f else 0.7f
    }

    private fun showMismatchDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("인증번호가 일치하지 않습니다.")
            .setMessage("받으신 인증번호를 다시 확인한 뒤 입력해주세요.")
            .setPositiveButton("다시 입력하기") { dialog, _ ->
                viewModel.hideEmailMismatchModal()
                binding.etVerificationCode.setText("")
                binding.etVerificationCode.requestFocus()
                dialog.dismiss()
            }
            .show()
    }

    private fun showDuplicateDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("이미 가입된 이메일입니다.")
            .setMessage("기존 계정으로 로그인하거나 비밀번호를 찾을 수 있습니다.")
            .setPositiveButton("다시 로그인하기") { dialog, _ ->
                viewModel.hideEmailDuplicateModal()
                requireActivity().finish()
                dialog.dismiss()
            }
            .setNegativeButton("비밀번호를 찾으시겠습니까?") { dialog, _ ->
                viewModel.hideEmailDuplicateModal()
                dialog.dismiss()
            }
            .show()
    }

    private fun showAgreementDialog(title: String, content: String) {
        AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage(content)
            .setPositiveButton("확인") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun getServiceTermsContent(): String {
        return """
캠픽 서비스 이용 약관

제1조(목적)
이 약관은 캠핑카 중고 거래 플랫폼인 캠픽(이하 "회사")가 제공하는 모바일 및 온라인 서비스(이하 "서비스")의 이용과 관련하여 회사와 회원 간의 권리, 의무 및 책임사항을 규정함을 목적으로 합니다.

제2조(용어의 정의)
1. "회원"은 본 약관에 동의하고 회사가 제공하는 서비스를 이용하는 자를 말합니다.
2. "판매자"는 캠핑카 또는 관련 물품을 판매할 목적으로 서비스를 이용하는 회원을 말합니다.
3. "구매자"는 판매자가 등록한 매물을 조회하거나 구매하려는 회원을 말합니다.
4. "매물"은 판매자가 서비스에 등록한 캠핑카, 캠핑 트레일러 등 중고 상품을 의미합니다.

제3조(약관의 게시와 개정)
1. 회사는 본 약관을 서비스 초기 화면 또는 연결 화면에 게시합니다.
2. 회사는 필요한 경우 관련 법령을 위반하지 않는 범위 내에서 본 약관을 개정할 수 있으며, 개정 시 최소 7일 전에 공지합니다.
3. 회원이 변경된 약관에 동의하지 않을 경우 서비스 이용을 중단하고 회원 탈퇴를 요청할 수 있습니다.

제4조(서비스의 제공)
1. 회사는 회원에게 다음 각 호의 서비스를 제공합니다.
  ① 중고 캠핑카 매물 등록/조회 서비스
  ② 채팅 및 메시지 서비스
  ③ 차량 정보, 시세, 리뷰 등 관련 콘텐츠 제공
  ④ 기타 회사가 정하는 부가 서비스
2. 회사는 서비스 개선을 위해 언제든지 콘텐츠, UI/UX 등을 변경할 수 있습니다.

제5조(회원의 의무)
1. 회원은 다음 행위를 해서는 안 됩니다.
  ① 허위 정보 등록 또는 타인의 정보를 도용하는 행위
  ② 법령 또는 본 약관에서 금지하는 행위
  ③ 서비스 운영을 방해하는 행위
2. 판매자는 등록한 매물에 대한 소유권, 상태, 사고 이력 등을 정확하게 제공해야 하며, 허위 정보를 제공할 경우 발생하는 문제에 대한 책임을 부담합니다.

제6조(거래에 대한 책임)
1. 회사는 거래 당사자 간의 매매 계약에 직접 관여하지 않습니다. 매물 검수, 대금 결제, 명의 이전 등은 당사자 책임으로 진행합니다.
2. 회사는 거래 과정에서 발생한 분쟁에 대해 법령이 허용하는 범위 내에서 책임을 부담하지 않습니다.

제7조(서비스 중단)
1. 회사는 시스템 점검, 보안 문제, 천재지변 등의 사유로 서비스 제공을 일시 중단할 수 있으며, 가능한 경우 사전 공지합니다.
2. 서비스 중단으로 회원에게 손해가 발생하더라도 회사의 고의 또는 중대한 과실이 없는 한 책임을 지지 않습니다.

제8조(지적재산권)
서비스에 게시된 콘텐츠, 상표, 로고 등은 회사 또는 해당 권리자의 자산이며, 무단 복제, 배포 등을 금지합니다.

제9조(책임 제한)
1. 회사는 회원 간의 거래로 인해 발생한 손해에 대해 책임을 지지 않습니다.
2. 회사의 책임은 법령이 허용하는 범위 내에서 최근 3개월 동안 회원이 회사에 지불한 금액을 한도로 합니다.

제10조(준거법 및 관할)
본 약관은 대한민국 법령에 따르며, 분쟁에 대해서는 민사소송법상의 관할 법원에 소송을 제기할 수 있습니다.

제11조(애플 앱스토어 관련 안내)
애플 앱스토어를 통해 서비스를 설치한 경우, 애플은 본 서비스의 거래 및 콘텐츠에 관한 어떠한 책임도 지지 않으며, 환불 및 고객 지원은 회사에서 제공합니다. 애플 기기 사용자는 애플의 이용 약관과 해당 국가의 법령을 준수해야 합니다.

부칙
본 약관은 2025년 9월 20일부터 시행합니다.
        """.trimIndent()
    }

    private fun getPrivacyContent(): String {
        return """
캠픽 개인정보 수집 및 이용 동의서

제1조(수집하는 개인정보 항목)
회사는 아래와 같은 개인정보를 수집합니다.
1. 필수 항목: 이름 또는 닉네임, 이메일 주소, 비밀번호, 휴대전화 번호, 지역(도/시·군·구)
2. 선택 항목: 프로필 사진, 매물 사진, 상세 설명에 포함된 연락처 등

제2조(개인정보의 수집 및 이용 목적)
회사는 다음과 같은 목적으로 개인정보를 이용합니다.
1. 회원 가입 및 본인 확인, 서비스 제공을 위한 기본 기능 제공
2. 매물 등록/관리, 거래 지원(연락, 채팅, 알림), 불법/부정 이용 방지
3. 고객 문의 응대, 공지사항 및 이벤트 안내
4. 서비스 품질 향상 및 통계 분석

제3조(개인정보의 보유 및 이용 기간)
1. 회원 탈퇴 시 즉시 파기합니다. 다만 관련 법령에 따라 보존할 필요가 있는 정보는 법정 기간 동안 보관합니다.
  - 전자상거래 등에서의 소비자 보호에 관한 법률: 계약 또는 청약철회 기록 5년, 대금 결제 및 재화 공급 기록 5년, 소비자 불만/분쟁 처리 기록 3년
  - 통신비밀보호법: 접속 기록 3개월

제4조(개인정보의 제3자 제공)
회사는 이용자의 동의 없이 개인정보를 제3자에게 제공하지 않습니다. 단, 법령에 근거한 요청이 있는 경우, 또는 사용자가 동의한 경우(예: 탁송/보험 서비스 연계)에는 예외로 합니다.

제5조(개인정보 처리의 위탁)
회사는 안정적인 서비스 제공을 위해 일부 업무를 외부 전문 업체에 위탁할 수 있으며, 위탁 시 개인정보 보호에 필요한 사항을 계약을 통해 규정합니다. 주요 위탁 업무 예: 클라우드 서버 운영, 푸시 알림, 데이터 분석.

제6조(이용자의 권리)
이용자는 언제든지 자신의 개인정보를 열람, 정정, 삭제, 처리 정지를 요청할 수 있으며, 동의를 철회할 수 있습니다. 다만, 필수 정보 삭제 시 서비스 이용이 제한될 수 있습니다.

제7조(개인정보의 파기)
개인정보는 수집 및 이용 목적이 달성된 후 지체 없이 파기합니다. 전자적 파일 형태로 저장된 정보는 복구 불가능한 방법으로 삭제하며, 종이 문서에 기록된 정보는 분쇄 또는 소각합니다.

제8조(안전성 확보 조치)
회사는 개인정보 유출 방지를 위해 암호화, 접근 통제, 침입 탐지 시스템 등을 운영하며, 정기적인 보안 점검을 수행합니다. 보안 사고 발생 시 관련 법령에 따라 신속히 통지합니다.

제9조(개인정보 보호 책임자)
성명: 캠픽 개인정보 보호 담당자
연락처: privacy@campick.com

제10조(정책 변경)
본 개인정보 처리방침은 법령이나 서비스 변경에 따라 수정될 수 있으며, 변경 사항은 사전에 공지합니다. 변경 이후에도 서비스를 계속 이용하는 경우 변경된 내용에 동의한 것으로 간주합니다.

본 동의서는 2025년 9월 20일부터 적용됩니다.
        """.trimIndent()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        countDownTimer?.cancel()
        _binding = null
    }
}