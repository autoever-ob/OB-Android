package com.nobody.campick.resources.theme

import androidx.compose.ui.graphics.Color

/**
 * 앱 전체에서 사용하는 색상을 중앙 관리하는 객체
 * Swift의 AppColors와 동일한 역할
 */
object AppColors {
    /** 화면 공통 배경 */
    val background = Color(0xFF0B211A)

    /** 기본 텍스트 */
    val primaryText = Color.White

    /** 브랜드 오렌지 */
    val brandOrange = Color(0xFFF97316)

    /** 브랜드 라이트 오렌지 */
    val brandLightOrange = Color(0xFFFB923C)

    /** 브랜드 라이트 그린 */
    val brandLightGreen = Color(0xFF22C55E)

    /** 브랜드 배경 */
    val brandBackground = Color(0xFF0B211A)

    // 색상과 숫자가 같이 있는 경우는 해당 색상에 투명도(n)이 적용된 것임
    /** 브랜드 화이트 70% 투명도 */
    val brandWhite70 = Color.White.copy(alpha = 0.7f)

    /** 브랜드 화이트 80% 투명도 */
    val brandWhite80 = Color.White.copy(alpha = 0.8f)

    /** 브랜드 화이트 90% 투명도 */
    val brandWhite90 = Color.White.copy(alpha = 0.9f)

    /** 브랜드 화이트 60% 투명도 */
    val brandWhite60 = Color.White.copy(alpha = 0.6f)

    /** 브랜드 화이트 50% 투명도 */
    val brandWhite50 = Color.White.copy(alpha = 0.5f)

    /** 브랜드 화이트 40% 투명도 */
    val brandWhite40 = Color.White.copy(alpha = 0.4f)

    /** 브랜드 화이트 20% 투명도 */
    val brandWhite20 = Color.White.copy(alpha = 0.2f)

    /** 브랜드 화이트 10% 투명도 */
    val brandWhite10 = Color.White.copy(alpha = 0.1f)

    /** 브랜드 화이트 5% 투명도 */
    val brandWhite05 = Color.White.copy(alpha = 0.05f)

    /** 레드 */
    val red = Color(0xFFFF5722)

    /** 회색 30% 투명도 */
    val gray30 = Color(0x4D808080)

    /** 브랜드 오렌지 80% 투명도 */
    val brandOrange80 = brandOrange.copy(alpha = 0.8f)

    /** 브랜드 오렌지 20% 투명도 */
    val brandOrange20 = brandOrange.copy(alpha = 0.2f)

    /** 브랜드 라이트 그린 20% 투명도 */
    val brandLightGreen20 = brandLightGreen.copy(alpha = 0.2f)

    /** 빨간색 80% 투명도 */
    val red80 = red.copy(alpha = 0.8f)

    /** 탭 구분선 */
    val tabDivider = Color(0x4D9E9E9E)

    /** 입력 필드 테두리 */
    val inputBorder = brandWhite20

    /** 입력 필드 에러 테두리 */
    val inputBorderError = Color(0xFFFF0000)

    /** 필드 라벨 */
    val fieldLabel = brandWhite80

    /** 에러 텍스트 */
    val errorText = Color(0xFFFF6B6B)
}

/**
 * Color 확장 함수 - Hex 문자열로부터 Color 생성
 */
fun Color.Companion.fromHex(hex: String): Color {
    val cleanHex = hex.removePrefix("#")
    val colorLong = cleanHex.toLong(16)

    return when (cleanHex.length) {
        6 -> Color((0xFF000000 or colorLong).toInt())
        8 -> Color(colorLong.toInt())
        else -> Color.Transparent
    }
}