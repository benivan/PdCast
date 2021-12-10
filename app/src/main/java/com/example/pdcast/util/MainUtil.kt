package com.example.pdcast.util


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
        else -> -15722464
    }
}

fun getMutedColorFromPalette(it: PaletteColor): Int {
    val mostDarkest = maxOf(-it.muted, -it.mutedLight, -it.mutedDark)
    return if (mostDarkest != 0) {
        -mostDarkest
    } else -15722464
}

fun getDarkerColorFromPalette(it:PaletteColor):Int{
    val darkColor = maxOf(-it.mutedLight,-it.muted,-it.mutedDark,-it.vibrantLight,-it.vibrantDark,-it.vibrant)
    return if(darkColor != 0){
        -darkColor
    }else -15722464
}


