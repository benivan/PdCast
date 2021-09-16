package com.example.pdcast.mediaPlayer

import android.content.Context
import android.media.*
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.annotation.RequiresApi
import kotlinx.coroutines.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException


private const val TAG = "PdCastMediaCallback"

class PdCastMediaCallback(
    val context: Context,
    val mediaSession: MediaSessionCompat,
    var mediaPlayer: MediaPlayer? = null
) : MediaSessionCompat.Callback() {


    var listener: PodPlayMediaListener? = null

    private var mediaUri: Uri? = null
    private var newMedia: Boolean = false
    private var mediaExtras: Bundle? = null

    private var focusRequest: AudioFocusRequest? = null

    private fun setNewMedia(uri: Uri?) {
        newMedia = true
        mediaUri = uri
    }


    @RequiresApi(Build.VERSION_CODES.P)
    override fun onPlayFromUri(uri: Uri?, extras: Bundle?) {
        super.onPlayFromUri(uri, extras)

        if (mediaUri == uri) {
            newMedia = false
//            mediaExtras = null
        } else {
            mediaExtras = extras
            setNewMedia(uri)
        }


        mediaSession.setMetadata(
            MediaMetadataCompat.Builder()
                .putString(
                    MediaMetadataCompat.METADATA_KEY_MEDIA_URI,
                    uri.toString()
                )
                .putString(
                    MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI,
                    extras?.getString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI)
                )
                .build()
        )
        onPlay()
        setState(PlaybackStateCompat.STATE_PLAYING)
    }

    override fun onPlay() {
        super.onPlay()
        if (ensureAudioFocus()) {
            mediaSession.isActive = true
            initializeMediaPlayer()
            prepareMedia {
                startPlaying()
            }

        }
    }

    override fun onSeekTo(pos: Long) {
        Log.d(TAG, "onSeekTo: $pos ")
        super.onSeekTo(pos)
        if (mediaPlayer == null) {
            Log.d(TAG, "onSeekTo: Null")
        }
        mediaPlayer?.seekTo(pos.toInt())

        Log.d(TAG, "onSeekTo: $pos")
        val playbackState: PlaybackStateCompat? = mediaSession.controller.playbackState

        if (playbackState != null) {
            setState(PlaybackStateCompat.STATE_PLAYING)

        } else {
            setState(PlaybackStateCompat.STATE_PAUSED)
        }
    }


    override fun onStop() {
        super.onStop()
        stopPlaying()
        listener?.onStopPlaying()
    }

    override fun onPause() {
        super.onPause()
        pausePlaying()
    }

    private fun setState(state: Int) {
        var position: Long = -1
        mediaPlayer?.let {
            position = it.currentPosition.toLong()
        }
        val playbackState = PlaybackStateCompat.Builder()
            .setActions(
                PlaybackStateCompat.ACTION_PLAY or
                        PlaybackStateCompat.ACTION_STOP or
                        PlaybackStateCompat.ACTION_PLAY_PAUSE or
                        PlaybackStateCompat.ACTION_PAUSE or
                        PlaybackStateCompat.ACTION_SEEK_TO or
                        PlaybackStateCompat.ACTION_FAST_FORWARD
            )
            .setState(state, position, 1.0f)

            .build()
        mediaSession.setPlaybackState(playbackState)

        listener?.onStateChanged(state)
    }


    private fun ensureAudioFocus(): Boolean {
        val audioManager = this.context.getSystemService(
            Context.AUDIO_SERVICE
        ) as AudioManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN).run {
                setAudioAttributes(AudioAttributes.Builder().run {
                    setUsage(AudioAttributes.USAGE_MEDIA)
                    setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    build()
                })
                build()
            }
            this.focusRequest = focusRequest

            val result = audioManager.requestAudioFocus(focusRequest)
            return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        } else {
            val result = audioManager.requestAudioFocus(
                null,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            )
            return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        }

    }

    private fun removeAudioFocus() {
        val audioManager = this.context.getSystemService(
            Context.AUDIO_SERVICE
        ) as AudioManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            focusRequest?.let {
                audioManager.abandonAudioFocusRequest(it)
            }
        } else {
            audioManager.abandonAudioFocus(null)
        }
    }


    private fun initializeMediaPlayer() {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer()

            mediaPlayer!!.setOnCompletionListener {
                setState(PlaybackStateCompat.STATE_PAUSED)
            }
            mediaPlayer!!.setOnSeekCompleteListener {
                listener?.onSeekCompleted()
            }
            mediaPlayer!!.setOnPreparedListener{
                listener?.onMediaPlayerPrepared()
            }

        }
    }

    private fun prepareMedia(prepared: () -> Unit) {

        val handler = CoroutineExceptionHandler { _, throwable ->
            Log.e(TAG, "prepareMedia: ", throwable)
        }

        CoroutineScope(Dispatchers.IO).launch(handler) {
            if (newMedia) {
                newMedia = false
                mediaPlayer?.let { mediaPlayer ->
                    mediaUri?.let { mediaUri ->
                        prepareMediaController(mediaUri)
                        mediaSession.setMetadata(
                            MediaMetadataCompat.Builder()
                                .putString(
                                    MediaMetadataCompat.METADATA_KEY_MEDIA_URI,
                                    mediaUri.toString()
                                )
                                .build()
                        )
                    }
                }
            }
            mediaExtras?.let { mediaExtras ->
                mediaSession.setMetadata(
                    MediaMetadataCompat.Builder()
                        .putString(
                            MediaMetadataCompat.METADATA_KEY_TITLE,
                            mediaExtras.getString(MediaMetadataCompat.METADATA_KEY_TITLE)
                        )
                        .putString(
                            MediaMetadataCompat.METADATA_KEY_ARTIST,
                            mediaExtras.getString(MediaMetadataCompat.METADATA_KEY_ARTIST)
                        )
                        .putString(
                            MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI,
                            mediaExtras.getString(
                                MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI
                            )
                        )
                        .build()
                )
            }
            prepared()
        }
    }

    private fun startPlaying() {
        Log.d(TAG, "startPlaying")
        mediaPlayer?.let {
            if (!it.isPlaying) {
                it.start()
                setState(PlaybackStateCompat.STATE_PLAYING)
            }

        }
    }

    private fun pausePlaying() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
                setState(PlaybackStateCompat.STATE_PAUSED)
            }

        }
    }


    private fun stopPlaying() {
        removeAudioFocus()
        mediaSession.isActive = false

        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
                setState(PlaybackStateCompat.STATE_STOPPED)
            }

        }
    }


    interface PodPlayMediaListener {
        fun onStateChanged(@PlaybackStateCompat.State state: Int)
        fun onStopPlaying()
        fun onPausePlaying()
        fun onSeekCompleted()
        fun onMediaPlayerPrepared()
    }

    private suspend fun prepareMediaController(mediaUri: Uri) = suspendCancellableCoroutine<Unit> {
        try {
            mediaPlayer?.run {
                reset()
                setDataSource(context, mediaUri)
                prepare()
            }
            it.resume(Unit)
        } catch (e: Exception) {
            it.resumeWithException(e)
        }

    }


//    fun getSome(block: MediaPlayer.() -> Unit) {
//        block(mediaPlayer!!)
//    }

}
