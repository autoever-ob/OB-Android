package com.nobody.campick.views.components

import android.animation.ValueAnimator
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
import androidx.compose.ui.graphics.toArgb
import com.bumptech.glide.Glide
import com.nobody.campick.R
import com.nobody.campick.models.ProfileData
import com.nobody.campick.resources.theme.AppColors

class ProfileEditDialog(
    context: Context,
    private val profile: ProfileData,
    private val onSave: (nickname: String, description: String, phoneNumber: String) -> Unit,
    private val onCancel: () -> Unit,
    private val onImagePickerNeeded: () -> Unit
) : Dialog(context) {

    private lateinit var editTextNickname: EditText
    private lateinit var editTextDescription: EditText
    private lateinit var editTextPhoneNumber: EditText
    private lateinit var saveButton: Button
    private lateinit var profileImageView: ImageView
    private lateinit var progressBar: ProgressBar

    private var isLoading = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_profile_edit)

        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window?.setDimAmount(0.6f)

        // 다이얼로그 윈도우 크기 설정 - 화면 너비의 90% 사용
        window?.attributes?.let { attributes ->
            val displayMetrics = context.resources.displayMetrics
            val screenWidth = displayMetrics.widthPixels
            attributes.width = (screenWidth * 0.9).toInt() // 화면 너비의 90%
            attributes.height = android.view.WindowManager.LayoutParams.WRAP_CONTENT
            window?.attributes = attributes
        }

        setupViews()
        loadProfileData()
        animateShow()
    }

    private fun setupViews() {
        val titleTextView = findViewById<TextView>(R.id.textViewTitle)
        val closeButton = findViewById<ImageView>(R.id.buttonClose)
        editTextNickname = findViewById(R.id.editTextNickname)
        editTextDescription = findViewById(R.id.editTextDescription)
        editTextPhoneNumber = findViewById(R.id.editTextPhoneNumber)
        saveButton = findViewById(R.id.buttonSave)
        profileImageView = findViewById(R.id.imageViewProfile)
        progressBar = findViewById(R.id.progressBar)

        titleTextView.text = "프로필 수정"

        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                updateSaveButtonState()
            }
        }

        editTextNickname.addTextChangedListener(textWatcher)

        // Phone number formatting
        editTextPhoneNumber.addTextChangedListener(object : TextWatcher {
            private var isFormatting = false

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (isFormatting) return

                isFormatting = true
                val formatted = formatPhoneNumber(s.toString())
                if (formatted != s.toString()) {
                    editTextPhoneNumber.setText(formatted)
                    editTextPhoneNumber.setSelection(formatted.length)
                }
                isFormatting = false
                updateSaveButtonState()
            }
        })

        editTextDescription.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val characterCount = findViewById<TextView>(R.id.textViewCharacterCount)
                characterCount.text = "${s?.length ?: 0} / 500"

                if ((s?.length ?: 0) > 500) {
                    characterCount.setTextColor(AppColors.red.toArgb())
                } else {
                    characterCount.setTextColor(AppColors.brandWhite60.toArgb())
                }
            }
        })

        // Set click listeners
        closeButton.setOnClickListener {
            animateHide {
                onCancel()
                dismiss()
            }
        }

        profileImageView.setOnClickListener {
            if (!isLoading) {
                onImagePickerNeeded()
            }
        }

        saveButton.setOnClickListener {
            if (!isLoading && isFormValid()) {
                setLoading(true)
                val nickname = editTextNickname.text.toString().trim()
                val description = editTextDescription.text.toString().trim()
                val phoneNumber = editTextPhoneNumber.text.toString().replace("-", "")

                onSave(nickname, description, phoneNumber)
            }
        }

        setOnCancelListener {
            animateHide {
                onCancel()
                dismiss()
            }
        }
    }

    private fun loadProfileData() {
        editTextNickname.setText(profile.nickname)
        editTextDescription.setText(profile.description ?: "")
        editTextPhoneNumber.setText(profile.mobileNumber ?: "")

        if (!profile.profileImage.isNullOrEmpty()) {
            Glide.with(context)
                .load(profile.profileImage)
                .placeholder(R.drawable.ic_person)
                .error(R.drawable.ic_person)
                .into(profileImageView)
        } else {
            profileImageView.setImageResource(R.drawable.ic_person)
        }

        updateSaveButtonState()
    }

    private fun updateSaveButtonState() {
        val isValid = isFormValid()
        saveButton.isEnabled = isValid && !isLoading

        if (isValid && !isLoading) {
            saveButton.setBackgroundColor(AppColors.brandOrange.toArgb())
            saveButton.alpha = 1.0f
        } else {
            saveButton.setBackgroundColor(AppColors.gray30.toArgb())
            saveButton.alpha = 0.6f
        }
    }

    private fun isFormValid(): Boolean {
        val nickname = editTextNickname.text.toString().trim()
        val description = editTextDescription.text.toString().trim()

        return nickname.isNotEmpty() && description.length <= 500
    }

    private fun formatPhoneNumber(input: String): String {
        val digits = input.filter { it.isDigit() }

        return when {
            digits.length <= 3 -> digits
            digits.length <= 7 -> "${digits.substring(0, 3)}-${digits.substring(3)}"
            digits.length <= 11 -> {
                "${digits.substring(0, 3)}-${digits.substring(3, 7)}-${digits.substring(7)}"
            }
            else -> "${digits.substring(0, 3)}-${digits.substring(3, 7)}-${digits.substring(7, 11)}"
        }
    }

    fun setLoading(loading: Boolean) {
        isLoading = loading
        progressBar.visibility = if (loading) android.view.View.VISIBLE else android.view.View.GONE
        updateSaveButtonState()
    }

    fun updateProfileImage(imageUri: Uri) {
        Glide.with(context)
            .load(imageUri)
            .into(profileImageView)
    }

    private fun animateShow() {
        val dialogView = findViewById<android.view.View>(R.id.dialogContainer)

        dialogView.scaleX = 0.8f
        dialogView.scaleY = 0.8f
        dialogView.alpha = 0f
        dialogView.translationY = 100f

        val scaleAnimator = ValueAnimator.ofFloat(0.8f, 1.0f)
        val alphaAnimator = ValueAnimator.ofFloat(0f, 1f)
        val translationAnimator = ValueAnimator.ofFloat(100f, 0f)

        scaleAnimator.addUpdateListener { animator ->
            val value = animator.animatedValue as Float
            dialogView.scaleX = value
            dialogView.scaleY = value
        }

        alphaAnimator.addUpdateListener { animator ->
            dialogView.alpha = animator.animatedValue as Float
        }

        translationAnimator.addUpdateListener { animator ->
            dialogView.translationY = animator.animatedValue as Float
        }

        scaleAnimator.duration = 350
        alphaAnimator.duration = 350
        translationAnimator.duration = 350

        val interpolator = AccelerateDecelerateInterpolator()
        scaleAnimator.interpolator = interpolator
        alphaAnimator.interpolator = interpolator
        translationAnimator.interpolator = interpolator

        scaleAnimator.start()
        alphaAnimator.start()
        translationAnimator.start()
    }

    private fun animateHide(onComplete: () -> Unit) {
        val dialogView = findViewById<android.view.View>(R.id.dialogContainer)

        val scaleAnimator = ValueAnimator.ofFloat(1.0f, 0.8f)
        val alphaAnimator = ValueAnimator.ofFloat(1f, 0f)
        val translationAnimator = ValueAnimator.ofFloat(0f, 100f)

        scaleAnimator.addUpdateListener { animator ->
            val value = animator.animatedValue as Float
            dialogView.scaleX = value
            dialogView.scaleY = value
        }

        alphaAnimator.addUpdateListener { animator ->
            dialogView.alpha = animator.animatedValue as Float
        }

        translationAnimator.addUpdateListener { animator ->
            dialogView.translationY = animator.animatedValue as Float
        }

        scaleAnimator.duration = 250
        alphaAnimator.duration = 250
        translationAnimator.duration = 250

        val interpolator = AccelerateDecelerateInterpolator()
        scaleAnimator.interpolator = interpolator
        alphaAnimator.interpolator = interpolator
        translationAnimator.interpolator = interpolator

        scaleAnimator.start()
        alphaAnimator.start()
        translationAnimator.start()

        Handler(Looper.getMainLooper()).postDelayed({
            onComplete()
        }, 250)
    }
}