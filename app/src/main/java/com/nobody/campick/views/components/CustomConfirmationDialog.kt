package com.nobody.campick.views.components

import android.animation.ValueAnimator
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.compose.ui.graphics.toArgb
import com.nobody.campick.R
import com.nobody.campick.resources.theme.AppColors

open class CustomConfirmationDialog(
    context: Context,
    private val title: String,
    private val message: String,
    private val confirmButtonText: String,
    private val cancelButtonText: String,
    private val isDestructive: Boolean = false,
    private val onConfirm: () -> Unit,
    private val onCancel: () -> Unit
) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_custom_confirmation)

        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window?.setDimAmount(0.6f)

        setupViews()
        animateShow()
    }

    private fun setupViews() {
        val titleTextView = findViewById<TextView>(R.id.textViewTitle)
        val messageTextView = findViewById<TextView>(R.id.textViewMessage)
        val iconImageView = findViewById<ImageView>(R.id.imageViewIcon)
        val confirmButton = findViewById<Button>(R.id.buttonConfirm)
        val cancelButton = findViewById<Button>(R.id.buttonCancel)

        titleTextView.text = title
        messageTextView.text = message
        confirmButton.text = confirmButtonText
        cancelButton.text = cancelButtonText

        if (isDestructive) {
            iconImageView.setImageResource(R.drawable.ic_error_circle)
            iconImageView.setColorFilter(AppColors.red.toArgb())
            confirmButton.setBackgroundColor(AppColors.red.toArgb())
        } else {
            iconImageView.setImageResource(R.drawable.ic_check_circle)
            iconImageView.setColorFilter(AppColors.brandOrange.toArgb())
            confirmButton.setBackgroundColor(AppColors.brandOrange.toArgb())
        }

        confirmButton.setOnClickListener {
            animateHide {
                onConfirm()
                dismiss()
            }
        }

        cancelButton.setOnClickListener {
            animateHide {
                onCancel()
                dismiss()
            }
        }

        setOnCancelListener {
            animateHide {
                onCancel()
                dismiss()
            }
        }
    }

    private fun animateShow() {
        val dialogView = findViewById<android.view.View>(R.id.dialogContainer)

        dialogView.scaleX = 0.7f
        dialogView.scaleY = 0.7f
        dialogView.alpha = 0f

        val scaleAnimator = ValueAnimator.ofFloat(0.7f, 1.0f)
        val alphaAnimator = ValueAnimator.ofFloat(0f, 1f)

        scaleAnimator.addUpdateListener { animator ->
            val value = animator.animatedValue as Float
            dialogView.scaleX = value
            dialogView.scaleY = value
        }

        alphaAnimator.addUpdateListener { animator ->
            dialogView.alpha = animator.animatedValue as Float
        }

        scaleAnimator.duration = 300
        alphaAnimator.duration = 300
        scaleAnimator.interpolator = AccelerateDecelerateInterpolator()
        alphaAnimator.interpolator = AccelerateDecelerateInterpolator()

        scaleAnimator.start()
        alphaAnimator.start()
    }

    private fun animateHide(onComplete: () -> Unit) {
        val dialogView = findViewById<android.view.View>(R.id.dialogContainer)

        val scaleAnimator = ValueAnimator.ofFloat(1.0f, 0.7f)
        val alphaAnimator = ValueAnimator.ofFloat(1f, 0f)

        scaleAnimator.addUpdateListener { animator ->
            val value = animator.animatedValue as Float
            dialogView.scaleX = value
            dialogView.scaleY = value
        }

        alphaAnimator.addUpdateListener { animator ->
            dialogView.alpha = animator.animatedValue as Float
        }

        scaleAnimator.duration = 200
        alphaAnimator.duration = 200
        scaleAnimator.interpolator = AccelerateDecelerateInterpolator()
        alphaAnimator.interpolator = AccelerateDecelerateInterpolator()

        scaleAnimator.start()
        alphaAnimator.start()

        Handler(Looper.getMainLooper()).postDelayed({
            onComplete()
        }, 200)
    }
}

// 로그아웃 전용 다이얼로그
class LogoutDialog(
    context: Context,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) : CustomConfirmationDialog(
    context = context,
    title = "로그아웃",
    message = "정말로 로그아웃 하시겠습니까?",
    confirmButtonText = "로그아웃",
    cancelButtonText = "취소",
    isDestructive = false,
    onConfirm = onConfirm,
    onCancel = onCancel
)