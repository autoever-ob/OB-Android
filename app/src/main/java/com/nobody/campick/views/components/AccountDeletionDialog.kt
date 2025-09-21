package com.nobody.campick.views.components

import android.animation.ValueAnimator
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.compose.ui.graphics.toArgb
import com.nobody.campick.R
import com.nobody.campick.resources.theme.AppColors

class AccountDeletionDialog(
    context: Context,
    private val onConfirm: () -> Unit,
    private val onCancel: () -> Unit
) : Dialog(context) {

    private val requiredText = "이 앱을 탈퇴하겠습니다"
    private lateinit var confirmButton: Button
    private lateinit var editTextConfirmation: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_account_deletion)

        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window?.setDimAmount(0.7f)

        setupViews()
        animateShow()
    }

    private fun setupViews() {
        val titleTextView = findViewById<TextView>(R.id.textViewTitle)
        val messageTextView = findViewById<TextView>(R.id.textViewMessage)
        val instructionTextView = findViewById<TextView>(R.id.textViewInstruction)
        val requiredTextView = findViewById<TextView>(R.id.textViewRequiredText)
        val iconImageView = findViewById<ImageView>(R.id.imageViewIcon)
        confirmButton = findViewById(R.id.buttonConfirm)
        val cancelButton = findViewById<Button>(R.id.buttonCancel)
        editTextConfirmation = findViewById(R.id.editTextConfirmation)

        titleTextView.text = "회원 탈퇴"
        messageTextView.text = "정말로 회원 탈퇴를 하시겠습니까?\n\n탈퇴 후에는 모든 데이터가 삭제되며\n복구할 수 없습니다."
        instructionTextView.text = "탈퇴를 원하시면 아래 문구를 정확히 입력해주세요:"
        requiredTextView.text = "\"$requiredText\""

        iconImageView.setImageResource(R.drawable.ic_error_circle)
        iconImageView.setColorFilter(AppColors.red.toArgb())

        updateConfirmButtonState(false)

        editTextConfirmation.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val isMatching = s.toString().trim() == requiredText
                updateConfirmButtonState(isMatching)
            }
        })

        // Set click listeners
        confirmButton.setOnClickListener {
            if (editTextConfirmation.text.toString().trim() == requiredText) {
                animateHide {
                    onConfirm()
                    dismiss()
                }
            }
        }

        cancelButton.setOnClickListener {
            animateHide {
                onCancel()
                dismiss()
            }
        }

        // Handle back button and outside touch
        setOnCancelListener {
            animateHide {
                onCancel()
                dismiss()
            }
        }
    }

    private fun updateConfirmButtonState(isEnabled: Boolean) {
        confirmButton.isEnabled = isEnabled
        if (isEnabled) {
            confirmButton.setBackgroundColor(AppColors.red.toArgb())
            confirmButton.alpha = 1.0f
        } else {
            confirmButton.setBackgroundColor(AppColors.gray30.toArgb())
            confirmButton.alpha = 0.6f
        }
    }

    private fun animateShow() {
        val dialogView = findViewById<android.view.View>(R.id.dialogContainer)

        dialogView.scaleX = 0.6f
        dialogView.scaleY = 0.6f
        dialogView.alpha = 0f
        dialogView.rotationX = -15f

        val scaleAnimator = ValueAnimator.ofFloat(0.6f, 1.0f)
        val alphaAnimator = ValueAnimator.ofFloat(0f, 1f)
        val rotationAnimator = ValueAnimator.ofFloat(-15f, 0f)

        scaleAnimator.addUpdateListener { animator ->
            val value = animator.animatedValue as Float
            dialogView.scaleX = value
            dialogView.scaleY = value
        }

        alphaAnimator.addUpdateListener { animator ->
            dialogView.alpha = animator.animatedValue as Float
        }

        rotationAnimator.addUpdateListener { animator ->
            dialogView.rotationX = animator.animatedValue as Float
        }

        scaleAnimator.duration = 400
        alphaAnimator.duration = 400
        rotationAnimator.duration = 400

        val interpolator = AccelerateDecelerateInterpolator()
        scaleAnimator.interpolator = interpolator
        alphaAnimator.interpolator = interpolator
        rotationAnimator.interpolator = interpolator

        scaleAnimator.start()
        alphaAnimator.start()
        rotationAnimator.start()

        Handler(Looper.getMainLooper()).postDelayed({
            editTextConfirmation.alpha = 0f
            editTextConfirmation.visibility = android.view.View.VISIBLE
            val inputAlphaAnimator = ValueAnimator.ofFloat(0f, 1f)
            inputAlphaAnimator.addUpdateListener { animator ->
                editTextConfirmation.alpha = animator.animatedValue as Float
            }
            inputAlphaAnimator.duration = 300
            inputAlphaAnimator.start()
        }, 200)
    }

    private fun animateHide(onComplete: () -> Unit) {
        val dialogView = findViewById<android.view.View>(R.id.dialogContainer)

        val scaleAnimator = ValueAnimator.ofFloat(1.0f, 0.6f)
        val alphaAnimator = ValueAnimator.ofFloat(1f, 0f)
        val rotationAnimator = ValueAnimator.ofFloat(0f, -15f)

        scaleAnimator.addUpdateListener { animator ->
            val value = animator.animatedValue as Float
            dialogView.scaleX = value
            dialogView.scaleY = value
        }

        alphaAnimator.addUpdateListener { animator ->
            dialogView.alpha = animator.animatedValue as Float
        }

        rotationAnimator.addUpdateListener { animator ->
            dialogView.rotationX = animator.animatedValue as Float
        }

        scaleAnimator.duration = 250
        alphaAnimator.duration = 250
        rotationAnimator.duration = 250

        val interpolator = AccelerateDecelerateInterpolator()
        scaleAnimator.interpolator = interpolator
        alphaAnimator.interpolator = interpolator
        rotationAnimator.interpolator = interpolator

        scaleAnimator.start()
        alphaAnimator.start()
        rotationAnimator.start()

        Handler(Looper.getMainLooper()).postDelayed({
            onComplete()
        }, 250)
    }
}