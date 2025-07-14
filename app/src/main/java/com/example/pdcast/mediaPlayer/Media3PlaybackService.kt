package com.example.pdcast.mediaPlayer

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.LruCache
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.MediaNotification
import androidx.media3.session.CommandButton
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerNotificationManager
import com.example.pdcast.MainActivity
import com.example.pdcast.R
import kotlinx.coroutines.*
import java.net.URL

/**
 * Modern Media3-based media playback service
 * Replaces the old ExoPlayer 2.x implementation
 */
@UnstableApi
class Media3PlaybackService : MediaSessionService() {

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    private lateinit var memoryCache: LruCache<String, Bitmap>
    private lateinit var player: ExoPlayer
    private lateinit var mediaSession: MediaSession
    private lateinit var playerNotificationManager: PlayerNotificationManager

    private val PLAYER_CHANNEL_ID = "podplay_player_channel"
    private val NOTIFICATION_ID = 1

    override fun onCreate() {
        super.onCreate()
        initializePlayer()
        initializeMediaSession()
        initializeNotificationManager()
        initializeMemoryCache()
    }

    private fun initializePlayer() {
        player = ExoPlayer.Builder(this)
            .build()
            .also { exoPlayer ->
                exoPlayer.addListener(playerListener)
            }
    }

    private fun initializeMediaSession() {
        mediaSession = MediaSession.Builder(this, player)
            .setCallback(MediaSessionCallback())
            .build()
    }

    private fun initializeNotificationManager() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }

        playerNotificationManager = PlayerNotificationManager.Builder(
            this,
            NOTIFICATION_ID,
            PLAYER_CHANNEL_ID
        )
            .setMediaDescriptionAdapter(MediaDescriptionAdapter())
            .setNotificationListener(NotificationListener())
            .build()
            .also { notificationManager ->
                notificationManager.setPlayer(player)
                notificationManager.setMediaSessionToken(mediaSession.sessionCompatToken)
            }
    }

    private fun initializeMemoryCache() {
        val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
        val cacheSize = maxMemory / 8

        memoryCache = object : LruCache<String, Bitmap>(cacheSize) {
            override fun sizeOf(key: String?, value: Bitmap?): Int {
                return value!!.byteCount / 1024
            }
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession {
        return mediaSession
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        player.release()
        mediaSession.release()
        playerNotificationManager.setPlayer(null)
    }

    private val playerListener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            when (playbackState) {
                Player.STATE_READY -> {
                    Log.d(TAG, "Player is ready")
                    sendBroadcast(Intent().apply {
                        action = PREPARED
                        putExtra("MEDIA_PREPARED", true)
                    })
                }
                Player.STATE_ENDED -> {
                    Log.d(TAG, "Playback ended")
                    saveCurrentPosition()
                }
                Player.STATE_BUFFERING -> {
                    Log.d(TAG, "Player is buffering")
                }
                Player.STATE_IDLE -> {
                    Log.d(TAG, "Player is idle")
                }
            }
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            Log.d(TAG, "Is playing changed: $isPlaying")
            sendBroadcast(Intent().apply {
                action = PREPARED
                putExtra("MEDIA_STATE", if (isPlaying) Player.STATE_READY else Player.STATE_ENDED)
            })
        }

        override fun onPositionDiscontinuity(
            oldPosition: Player.PositionInfo,
            newPosition: Player.PositionInfo,
            reason: Int
        ) {
            if (reason == Player.DISCONTINUITY_REASON_SEEK) {
                sendBroadcast(Intent().apply {
                    action = PREPARED
                    putExtra("SEEK_COMPLETED", true)
                })
            }
        }
    }

    private fun saveCurrentPosition() {
        val sharedPref = getSharedPreferences(
            getString(R.string.preference_file_key), Context.MODE_PRIVATE
        )
        with(sharedPref.edit()) {
            putLong("NowPlayingPosition", player.currentPosition)
            apply()
        }
    }

    private inner class MediaSessionCallback : MediaSession.Callback {
        override fun onPlaybackResumption(
            mediaSession: MediaSession,
            controller: MediaController
        ): com.google.common.util.concurrent.ListenableFuture<MediaSession.MediaItemsWithStartPosition> {
            // Handle resumption from previous session
            return super.onPlaybackResumption(mediaSession, controller)
        }

        override fun onAddMediaItems(
            mediaSession: MediaSession,
            controller: MediaController,
            mediaItems: MutableList<MediaItem>
        ): com.google.common.util.concurrent.ListenableFuture<MutableList<MediaItem>> {
            // Process media items and return them
            return super.onAddMediaItems(mediaSession, controller, mediaItems)
        }
    }

    private inner class MediaDescriptionAdapter : PlayerNotificationManager.MediaDescriptionAdapter {
        override fun getCurrentContentTitle(player: Player): String {
            return player.currentMediaItem?.mediaMetadata?.title?.toString() ?: ""
        }

        override fun getCurrentContentText(player: Player): String {
            return player.currentMediaItem?.mediaMetadata?.artist?.toString() ?: ""
        }

        override fun getCurrentLargeIcon(
            player: Player,
            callback: PlayerNotificationManager.BitmapCallback
        ): Bitmap? {
            val artworkUri = player.currentMediaItem?.mediaMetadata?.artworkUri
            return if (artworkUri != null) {
                val cacheKey = artworkUri.toString()
                getBitMapFromMemoryCache(cacheKey) ?: run {
                    // Load bitmap asynchronously
                    serviceScope.launch {
                        try {
                            val bitmap = URL(artworkUri.toString()).openStream().use { stream ->
                                BitmapFactory.decodeStream(stream)
                            }
                            bitmap?.let { setBitMapToMemoryCache(cacheKey, it) }
                            callback.onBitmap(bitmap)
                        } catch (e: Exception) {
                            Log.e(TAG, "Error loading artwork", e)
                            callback.onBitmap(null)
                        }
                    }
                    null
                }
            } else {
                null
            }
        }

        override fun createCurrentContentIntent(player: Player): PendingIntent? {
            val openActivityIntent = Intent(this@Media3PlaybackService, MainActivity::class.java)
            openActivityIntent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            return PendingIntent.getActivity(
                this@Media3PlaybackService, 0, openActivityIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        }
    }

    private inner class NotificationListener : PlayerNotificationManager.NotificationListener {
        override fun onNotificationCancelled(notificationId: Int, dismissedByUser: Boolean) {
            stopForeground(true)
            if (dismissedByUser) {
                player.stop()
            }
        }

        override fun onNotificationPosted(
            notificationId: Int,
            notification: Notification,
            ongoing: Boolean
        ) {
            if (ongoing) {
                startForeground(notificationId, notification)
            } else {
                stopForeground(false)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (notificationManager.getNotificationChannel(PLAYER_CHANNEL_ID) == null) {
            val channel = NotificationChannel(
                PLAYER_CHANNEL_ID,
                "Player",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Media player notifications"
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun getBitMapFromMemoryCache(key: String): Bitmap? {
        return memoryCache.get(key)
    }

    private fun setBitMapToMemoryCache(key: String, bitmap: Bitmap) {
        if (getBitMapFromMemoryCache(key) == null) {
            memoryCache.put(key, bitmap)
        }
    }

    /**
     * Helper method to play media from URI with metadata
     */
    fun playFromUri(uri: String, metadata: Bundle?) {
        val mediaItem = MediaItem.Builder()
            .setUri(uri)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(metadata?.getString("title"))
                    .setArtist(metadata?.getString("artist"))
                    .setArtworkUri(metadata?.getString("artworkUri")?.let { android.net.Uri.parse(it) })
                    .build()
            )
            .build()

        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()
    }

    companion object {
        private const val TAG = "Media3PlaybackService"
        const val PREPARED = "prepared"
    }
}