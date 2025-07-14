package com.example.pdcast.util

import android.content.ComponentName
import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.pdcast.mediaPlayer.Media3PlaybackService
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Helper class for Media3 integration
 * Provides modern media playback capabilities
 */
class Media3Helper(private val context: Context) {
    
    private var mediaController: MediaController? = null
    private var controllerFuture: ListenableFuture<MediaController>? = null
    
    suspend fun initialize(): MediaController {
        return suspendCancellableCoroutine { continuation ->
            val sessionToken = SessionToken(context, ComponentName(context, Media3PlaybackService::class.java))
            
            controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
            
            controllerFuture?.addListener({
                try {
                    val controller = controllerFuture?.get()
                    mediaController = controller
                    if (controller != null) {
                        continuation.resume(controller)
                    } else {
                        continuation.resumeWithException(Exception("Failed to create MediaController"))
                    }
                } catch (e: Exception) {
                    continuation.resumeWithException(e)
                }
            }, MoreExecutors.directExecutor())
            
            continuation.invokeOnCancellation {
                controllerFuture?.cancel(true)
            }
        }
    }
    
    fun playPodcast(episodeUrl: String, title: String, artist: String, artworkUrl: String?) {
        val mediaItem = MediaItem.Builder()
            .setUri(episodeUrl)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(title)
                    .setArtist(artist)
                    .setArtworkUri(artworkUrl?.let { android.net.Uri.parse(it) })
                    .build()
            )
            .build()
        
        mediaController?.setMediaItem(mediaItem)
        mediaController?.prepare()
        mediaController?.play()
    }
    
    fun pause() {
        mediaController?.pause()
    }
    
    fun play() {
        mediaController?.play()
    }
    
    fun seekTo(position: Long) {
        mediaController?.seekTo(position)
    }
    
    fun stop() {
        mediaController?.stop()
    }
    
    fun release() {
        mediaController?.release()
        controllerFuture?.cancel(true)
        controllerFuture = null
        mediaController = null
    }
    
    fun isPlaying(): Boolean {
        return mediaController?.isPlaying == true
    }
    
    fun getCurrentPosition(): Long {
        return mediaController?.currentPosition ?: 0L
    }
    
    fun getDuration(): Long {
        return mediaController?.duration ?: 0L
    }
}