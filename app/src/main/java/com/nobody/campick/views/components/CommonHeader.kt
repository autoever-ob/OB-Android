package com.nobody.campick.views.components

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.compose.ui.graphics.toArgb
import com.nobody.campick.R
import com.nobody.campick.resources.theme.AppColors

class CommonHeader @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    sealed class HeaderType {
        object Brand : HeaderType()
        data class Navigation(
            val title: String,
            val showBackButton: Boolean = true,
            val showRightButton: Boolean = false,
            val rightButtonIcon: Int? = null,
            val rightButtonAction: (() -> Unit)? = null
        ) : HeaderType()
        data class Custom(
            val title: String,
            val leftIcon: Int? = null,
            val rightIcon: Int? = null,
            val leftAction: (() -> Unit)? = null,
            val rightAction: (() -> Unit)? = null
        ) : HeaderType()
    }

    private val leftButton: ImageView
    private val titleText: TextView
    private val rightButton: ImageView

    init {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        weightSum = 1f

        // 패딩 설정 (16dp)
        val padding = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 16f,
            context.resources.displayMetrics
        ).toInt()
        setPadding(padding, padding, padding, padding)

        // 배경색 설정
        setBackgroundColor(AppColors.brandBackground.toArgb())

        // 왼쪽 버튼
        leftButton = ImageView(context).apply {
            val size = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 40f,
                context.resources.displayMetrics
            ).toInt()
            layoutParams = LayoutParams(size, size)
            scaleType = ImageView.ScaleType.CENTER
            background = context.getDrawable(R.drawable.circle_background_white)?.apply {
                alpha = 25 // 10% opacity (255 * 0.1)
            }
            visibility = View.GONE
        }

        // 제목 텍스트를 감싸는 컨테이너
        val titleContainer = LinearLayout(context).apply {
            layoutParams = LayoutParams(0, LayoutParams.MATCH_PARENT, 1f)
            orientation = HORIZONTAL
            gravity = Gravity.CENTER
        }

        // 제목 텍스트
        titleText = TextView(context).apply {
            layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
            gravity = Gravity.CENTER
            textAlignment = View.TEXT_ALIGNMENT_CENTER
            setTextColor(AppColors.primaryText.toArgb())
            textSize = 18f
            visibility = View.GONE
        }

        titleContainer.addView(titleText)

        // 오른쪽 버튼
        rightButton = ImageView(context).apply {
            val size = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 40f,
                context.resources.displayMetrics
            ).toInt()
            layoutParams = LayoutParams(size, size)
            scaleType = ImageView.ScaleType.CENTER
            background = context.getDrawable(R.drawable.circle_background_white)?.apply {
                alpha = 25 // 10% opacity
            }
            visibility = View.GONE
        }

        addView(leftButton)
        addView(titleContainer)
        addView(rightButton)
    }

    fun setupHeader(
        type: HeaderType,
        onBackClick: (() -> Unit)? = null,
        onProfileClick: (() -> Unit)? = null
    ) {
        when (type) {
            is HeaderType.Brand -> {
                setupBrandHeader(onProfileClick)
            }
            is HeaderType.Navigation -> {
                setupNavigationHeader(type, onBackClick)
            }
            is HeaderType.Custom -> {
                setupCustomHeader(type)
            }
        }
    }

    private fun setupBrandHeader(onProfileClick: (() -> Unit)?) {
        // 왼쪽: Campick 브랜드 로고
        leftButton.visibility = View.GONE

        titleText.apply {
            text = "Campick"
            textSize = 30f
            gravity = Gravity.START or Gravity.CENTER_VERTICAL
            layoutParams = (layoutParams as LayoutParams).apply {
                gravity = Gravity.START or Gravity.CENTER_VERTICAL
            }

            // Pacifico 폰트 적용
            try {
                val pacifico = ResourcesCompat.getFont(context, R.font.pacifico_regular)
                typeface = pacifico
            } catch (e: Exception) {
                typeface = Typeface.DEFAULT
            }

            setTextColor(AppColors.primaryText.toArgb())
            visibility = View.VISIBLE
        }

        // 오른쪽: 프로필 버튼
        rightButton.apply {
            setImageResource(R.drawable.ic_person)
            background = context.getDrawable(R.drawable.circle_background_red)
            visibility = View.VISIBLE
            setOnClickListener { onProfileClick?.invoke() }
        }
    }

    private fun setupNavigationHeader(
        type: HeaderType.Navigation,
        onBackClick: (() -> Unit)?
    ) {
        // 왼쪽: 뒤로가기 버튼
        if (type.showBackButton) {
            leftButton.apply {
                setImageResource(R.drawable.ic_arrow_back)
                visibility = View.VISIBLE
                setOnClickListener { onBackClick?.invoke() }
            }
        } else {
            leftButton.visibility = View.INVISIBLE // 공간 유지
        }

        // 중앙: 제목
        titleText.apply {
            text = type.title
            textSize = 18f
            setTextColor(AppColors.primaryText.toArgb())
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            textAlignment = View.TEXT_ALIGNMENT_CENTER
            visibility = View.VISIBLE
        }

        // 오른쪽: 설정 버튼 (optional)
        if (type.showRightButton && type.rightButtonIcon != null) {
            rightButton.apply {
                setImageResource(type.rightButtonIcon)
                visibility = View.VISIBLE
                setOnClickListener { type.rightButtonAction?.invoke() }
            }
        } else {
            rightButton.visibility = View.INVISIBLE // 공간 유지
        }
    }

    private fun setupCustomHeader(type: HeaderType.Custom) {
        // 왼쪽 버튼
        if (type.leftIcon != null) {
            leftButton.apply {
                setImageResource(type.leftIcon)
                visibility = View.VISIBLE
                setOnClickListener { type.leftAction?.invoke() }
            }
        } else {
            leftButton.visibility = View.INVISIBLE
        }

        // 제목
        titleText.apply {
            text = type.title
            textSize = 18f
            setTextColor(AppColors.primaryText.toArgb())
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            textAlignment = View.TEXT_ALIGNMENT_CENTER
            visibility = View.VISIBLE
        }

        // 오른쪽 버튼
        if (type.rightIcon != null) {
            rightButton.apply {
                setImageResource(type.rightIcon)
                visibility = View.VISIBLE
                setOnClickListener { type.rightAction?.invoke() }
            }
        } else {
            rightButton.visibility = View.INVISIBLE
        }
    }

    // 헤더 높이 설정을 위한 메서드
    fun setHeaderHeight(heightDp: Float) {
        val heightPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, heightDp,
            context.resources.displayMetrics
        ).toInt()
        layoutParams = layoutParams.apply { height = heightPx }
    }

    // 배경색 설정을 위한 메서드
    fun setHeaderBackground(color: Int) {
        setBackgroundColor(color)
    }

    // 제목 스타일 커스터마이징
    fun setTitleStyle(
        textSize: Float? = null,
        textColor: Int? = null,
        typeface: Typeface? = null
    ) {
        titleText.apply {
            textSize?.let { this.textSize = it }
            textColor?.let { setTextColor(it) }
            typeface?.let { this.typeface = it }
        }
    }
}