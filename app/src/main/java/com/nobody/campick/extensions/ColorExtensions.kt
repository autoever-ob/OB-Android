package com.nobody.campick.extensions

import androidx.compose.ui.graphics.Color

/**
 * Color 확장 함수들
 * iOS의 Color+Hex.swift와 동일한 역할
 */

/**
 * Hex 문자열로부터 Color 생성
 * @param hex 헥스 코드 문자열 (예: "0B211A", "#0B211A")
 * @param alpha 투명도 (0.0 ~ 1.0)
 */
fun Color.Companion.fromHex(hex: String, alpha: Float = 1.0f): Color {
    val cleanHex = hex.trim()
        .removePrefix("#")
        .replace(Regex("[^0-9A-Fa-f]"), "")

    return when (cleanHex.length) {
        6 -> {
            // RRGGBB 형식
            val colorInt = cleanHex.toInt(16)
            val r = (colorInt shr 16) and 0xFF
            val g = (colorInt shr 8) and 0xFF
            val b = colorInt and 0xFF

            Color(
                red = r / 255f,
                green = g / 255f,
                blue = b / 255f,
                alpha = alpha
            )
        }
        8 -> {
            // AARRGGBB 형식
            val colorInt = cleanHex.toLong(16)
            val a = ((colorInt shr 24) and 0xFF) / 255f
            val r = ((colorInt shr 16) and 0xFF) / 255f
            val g = ((colorInt shr 8) and 0xFF) / 255f
            val b = (colorInt and 0xFF) / 255f

            Color(red = r, green = g, blue = b, alpha = if (alpha < 1.0f) alpha else a)
        }
        3 -> {
            // RGB 형식 (각 자리를 두 번 반복)
            val r = cleanHex[0].toString().repeat(2).toInt(16)
            val g = cleanHex[1].toString().repeat(2).toInt(16)
            val b = cleanHex[2].toString().repeat(2).toInt(16)

            Color(
                red = r / 255f,
                green = g / 255f,
                blue = b / 255f,
                alpha = alpha
            )
        }
        else -> Color.Transparent
    }
}

/**
 * Color를 Hex 문자열로 변환
 */
fun Color.toHex(includeAlpha: Boolean = false): String {
    val r = (red * 255).toInt().coerceIn(0, 255)
    val g = (green * 255).toInt().coerceIn(0, 255)
    val b = (blue * 255).toInt().coerceIn(0, 255)
    val a = (alpha * 255).toInt().coerceIn(0, 255)

    return if (includeAlpha && alpha < 1.0f) {
        "#%02X%02X%02X%02X".format(a, r, g, b)
    } else {
        "#%02X%02X%02X".format(r, g, b)
    }
}

/**
 * 투명도 적용된 새로운 Color 반환
 */
fun Color.withAlpha(alpha: Float): Color {
    return copy(alpha = alpha.coerceIn(0f, 1f))
}

/**
 * 밝기 조절된 새로운 Color 반환
 */
fun Color.withBrightness(brightness: Float): Color {
    val factor = brightness.coerceIn(0f, 2f)
    return Color(
        red = (red * factor).coerceIn(0f, 1f),
        green = (green * factor).coerceIn(0f, 1f),
        blue = (blue * factor).coerceIn(0f, 1f),
        alpha = alpha
    )
}

/**
 * 색상을 더 밝게 만들기
 */
fun Color.lighter(factor: Float = 0.2f): Color {
    val brightness = 1f + factor.coerceIn(0f, 1f)
    return withBrightness(brightness)
}

/**
 * 색상을 더 어둡게 만들기
 */
fun Color.darker(factor: Float = 0.2f): Color {
    val brightness = 1f - factor.coerceIn(0f, 1f)
    return withBrightness(brightness)
}

/**
 * 색상의 밝기 계산 (0.0 ~ 1.0)
 */
fun Color.luminance(): Float {
    return 0.299f * red + 0.587f * green + 0.114f * blue
}

/**
 * 색상이 밝은지 어두운지 판단
 */
fun Color.isLight(): Boolean {
    return luminance() > 0.5f
}

/**
 * 색상이 어두운지 판단
 */
fun Color.isDark(): Boolean {
    return !isLight()
}

/**
 * 대비되는 텍스트 색상 반환 (흰색 또는 검은색)
 */
fun Color.contrastingTextColor(): Color {
    return if (isLight()) Color.Black else Color.White
}