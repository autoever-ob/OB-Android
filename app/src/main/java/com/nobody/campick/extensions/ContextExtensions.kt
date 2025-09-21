package com.nobody.campick.extensions

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.platform.LocalContext

/**
 * Context 관련 확장 함수들
 */

/**
 * CompositionLocal for Context
 */
val LocalAppContext = compositionLocalOf<Context> {
    error("No Context provided")
}

/**
 * Context를 제공하는 Composable
 */
@Composable
fun ProvideAppContext(
    context: Context = LocalContext.current,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalAppContext provides context) {
        content()
    }
}

/**
 * Context에서 dip을 px로 변환
 */
fun Context.dpToPx(dp: Float): Float {
    return dp * resources.displayMetrics.density
}

/**
 * Context에서 px을 dip로 변환
 */
fun Context.pxToDp(px: Float): Float {
    return px / resources.displayMetrics.density
}

/**
 * Context에서 sp를 px로 변환
 */
fun Context.spToPx(sp: Float): Float {
    return sp * resources.displayMetrics.scaledDensity
}

/**
 * Context에서 px를 sp로 변환
 */
fun Context.pxToSp(px: Float): Float {
    return px / resources.displayMetrics.scaledDensity
}