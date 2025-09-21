package com.nobody.campick.extensions

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import com.nobody.campick.R

/**
 * 폰트 관련 확장 함수들
 * iOS의 Font.swift와 동일한 역할
 */

/**
 * 커스텀 폰트 패밀리 정의
 */
object AppFonts {

    /**
     * Pacifico 폰트 패밀리
     */
    val pacifico = FontFamily(
        Font(R.font.pacifico_regular, FontWeight.Normal, FontStyle.Normal)
    )

    /**
     * 시스템 기본 폰트
     */
    val system = FontFamily.Default

    /**
     * 세리프 폰트
     */
    val serif = FontFamily.Serif

    /**
     * 모노스페이스 폰트
     */
    val monospace = FontFamily.Monospace
}

/**
 * TextStyle 확장 함수들
 */

/**
 * 기본 Pacifico 폰트 스타일
 * @param size 폰트 크기 (기본값: 40sp)
 */
fun TextStyle.Companion.basicFont(size: Int = 40): TextStyle {
    return TextStyle(
        fontFamily = AppFonts.pacifico,
        fontSize = size.sp
    )
}

/**
 * 제목용 폰트 스타일
 */
fun TextStyle.Companion.titleFont(size: Int = 24): TextStyle {
    return TextStyle(
        fontFamily = AppFonts.system,
        fontSize = size.sp,
        fontWeight = FontWeight.Bold
    )
}

/**
 * 부제목용 폰트 스타일
 */
fun TextStyle.Companion.subtitleFont(size: Int = 18): TextStyle {
    return TextStyle(
        fontFamily = AppFonts.system,
        fontSize = size.sp,
        fontWeight = FontWeight.SemiBold
    )
}

/**
 * 본문용 폰트 스타일
 */
fun TextStyle.Companion.bodyFont(size: Int = 16): TextStyle {
    return TextStyle(
        fontFamily = AppFonts.system,
        fontSize = size.sp,
        fontWeight = FontWeight.Normal
    )
}

/**
 * 캡션용 폰트 스타일
 */
fun TextStyle.Companion.captionFont(size: Int = 12): TextStyle {
    return TextStyle(
        fontFamily = AppFonts.system,
        fontSize = size.sp,
        fontWeight = FontWeight.Normal
    )
}

/**
 * 버튼용 폰트 스타일
 */
fun TextStyle.Companion.buttonFont(size: Int = 16): TextStyle {
    return TextStyle(
        fontFamily = AppFonts.system,
        fontSize = size.sp,
        fontWeight = FontWeight.Medium
    )
}

/**
 * 라벨용 폰트 스타일
 */
fun TextStyle.Companion.labelFont(size: Int = 14): TextStyle {
    return TextStyle(
        fontFamily = AppFonts.system,
        fontSize = size.sp,
        fontWeight = FontWeight.Medium
    )
}

/**
 * 헤더용 폰트 스타일
 */
fun TextStyle.Companion.headerFont(size: Int = 32): TextStyle {
    return TextStyle(
        fontFamily = AppFonts.system,
        fontSize = size.sp,
        fontWeight = FontWeight.Bold
    )
}

/**
 * 브랜드 로고용 폰트 스타일 (Pacifico)
 */
fun TextStyle.Companion.brandFont(size: Int = 28): TextStyle {
    return TextStyle(
        fontFamily = AppFonts.pacifico,
        fontSize = size.sp
    )
}

/**
 * 경고/에러 메시지용 폰트 스타일
 */
fun TextStyle.Companion.errorFont(size: Int = 14): TextStyle {
    return TextStyle(
        fontFamily = AppFonts.system,
        fontSize = size.sp,
        fontWeight = FontWeight.Medium
    )
}

/**
 * 하이라이트 텍스트용 폰트 스타일
 */
fun TextStyle.Companion.highlightFont(size: Int = 16): TextStyle {
    return TextStyle(
        fontFamily = AppFonts.system,
        fontSize = size.sp,
        fontWeight = FontWeight.SemiBold
    )
}

/**
 * 플레이스홀더 텍스트용 폰트 스타일
 */
fun TextStyle.Companion.placeholderFont(size: Int = 16): TextStyle {
    return TextStyle(
        fontFamily = AppFonts.system,
        fontSize = size.sp,
        fontWeight = FontWeight.Normal
    )
}