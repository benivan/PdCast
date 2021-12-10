package com.example.pdcast.util

import android.content.Context
import androidx.palette.graphics.Palette
import com.bumptech.glide.Glide
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resumeWithException

data class PaletteColor(
    val vibrant:Int,
    val vibrantLight:Int,
    val vibrantDark: Int,
    val muted :Int,
    val mutedLight:Int,
    val mutedDark:Int,
)

suspend fun getPaletteColor(uri: String?, context:Context) = suspendCancellableCoroutine<PaletteColor> {

    try {
        val theBitmap = Glide.
        with(context).asBitmap().load(uri).submit().get()
        Palette.from(theBitmap).generate { palette ->
            val colorPalette = PaletteColor(
                vibrant = palette!!.getVibrantColor(0x000000),
                vibrantLight = palette.getLightVibrantColor(0x000000),
                vibrantDark = palette.getDarkVibrantColor(0x000000),
                muted = palette.getMutedColor(0x000000),
                mutedLight = palette.getLightMutedColor(0x000000),
                mutedDark = palette.getDarkMutedColor(0x000000)
            )
            it.resumeWith(Result.success(colorPalette))
        }


    } catch (e: Exception) {
        it.resumeWithException(e)
    }


}