package com.example.pdcast.util

import android.graphics.*
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.widget.ImageButton
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.RequiresApi
import kotlin.math.max


fun getVibrantColorFromPalette(it: PaletteColor): Int {
    return when {
        it.vibrant != 0 -> {
            it.vibrant
        }
        it.vibrantLight != 0 -> {
            it.vibrantLight
        }

        it.vibrantDark != 0 -> {
            it.vibrantDark
        }
        else -> getLightMutedColorFromPalette(it)
    }
}

fun getDarkVibrantColor(it:PaletteColor):Int {
    return - maxOf(-it.vibrant,-it.vibrantDark,-it.mutedLight)
}
fun getLightMutedColorFromPalette(it: PaletteColor): Int {
    return when {
        it.mutedLight != 0 -> {
            it.mutedLight
        }
        it.muted != 0 -> {
            it.muted
        }
        it.mutedDark != 0 -> {
            it.mutedDark
        }
        else -> -5722464
    }

}


fun getMutedColorFromPalette(it: PaletteColor): Int {
    val mostDarkest = maxOf(-it.muted, -it.mutedLight, -it.mutedDark)
    return if (mostDarkest != 0) {
        -mostDarkest
    } else -15722464
}

fun ImageButton.setSvgColor(@ColorRes color: Int) = setColorFilter(color, PorterDuff.Mode.MULTIPLY)

//@RequiresApi(Build.VERSION_CODES.Q)
//fun Button.setColor(@ColorInt color: Int) {
//    background = setBackgroundColor()
//}

fun ImageButton.setBackgroundColorToImageButton(@ColorInt color: Int) {
    val gradientDrawable = background.mutate() as GradientDrawable
    gradientDrawable.setColor(color)
}
@RequiresApi(Build.VERSION_CODES.Q)
fun getColorFilter(color:Int):ColorFilter{
    return BlendModeColorFilter(color,BlendMode.MULTIPLY)
}

fun getDarkerColorFromPalette(it: PaletteColor): Int {
    val darkColor = maxOf(
        -it.mutedLight,
        -it.muted,
        -it.mutedDark,
        -it.vibrantLight,
        -it.vibrantDark,
        -it.vibrant
    )
    return if (darkColor != 0) {
        -darkColor
    } else -15722464
}

fun getTransparentColor(color: Int,alpha:Int): Int {
    var alpha = Color.alpha(color)
    val red = Color.red(color)
    val green = Color.green(color)
    val blue = Color.blue(color)

    alpha  /=alpha
    return Color.argb(alpha, red, green, blue);

}

interface PaletteColorIsReadyCallback {
    fun onPaletteColorIsReady(paletteColor: List<Int>)
}


