package com.example.pdcast.mediaPlayer

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.util.LruCache
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.media.MediaBrowserServiceCompat
import androidx.media.app.NotificationCompat as NotificationCompatX
import androidx.media.session.MediaButtonReceiver
import com.bumptech.glide.load.engine.cache.MemoryCache
import com.example.pdcast.MainActivity
import com.example.pdcast.R
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import kotlinx.coroutines.*
import java.net.URL


class MediaPlaybackService : MediaBrowserServiceCompat(), PdCastMediaCallback.PodPlayMediaListener {


    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    private lateinit var memoryCache: LruCache<String, Bitmap>

    private lateinit var mediaSession: MediaSessionCompat

    private var mediaPlayer:MediaPlayer ?= null
    private lateinit var mediaSessionConnector: MediaSessionConnector

    private val PLAYER_CHANNEL_ID = "podplay_player_channel"


    override fun onCreate() {
        super.onCreate()

        val activityIntent = packageManager?.getLaunchIntentForPackage(packageName)?.let {
            PendingIntent.getActivity(this, 0, it, 0)
        }

        mediaSession = MediaSessionCompat(this, TAG).apply {
            setSessionActivity(activityIntent)
            isActive = true
        }

        sessionToken = mediaSession.sessionToken
        val callBack = PdCastMediaCallback(this, mediaSession)
        callBack.listener = this

        mediaSession.setCallback(callBack)

        val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
        val cacheSize = maxMemory / 8

        memoryCache = object : LruCache<String, Bitmap>(cacheSize) {
            override fun sizeOf(key: String?, value: Bitmap?): Int {
                return value!!.byteCount / 1024
            }
        }



    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        mediaSession.controller.transportControls.stop()
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot {
        return MediaBrowserServiceCompat.BrowserRoot(
            PODPLAY_EMPTY_ROOT_MEDIA_ID, null
        )
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        if (parentId == PODPLAY_EMPTY_ROOT_MEDIA_ID) {
            result.sendResult(null)
        }


    }

    private fun getPlayPauseActions(): Pair<NotificationCompat.Action, NotificationCompat.Action> {

        val playAction = NotificationCompat.Action(
            R.drawable.play_arrow_black, getString(R.string.play),
            MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_PLAY)
        )

        val pauseAction = NotificationCompat.Action(
            R.drawable.pause_black, getString(R.string.pause),
            MediaButtonReceiver.buildMediaButtonPendingIntent(
                this,
                PlaybackStateCompat.ACTION_PAUSE
            )
        )
        return Pair(pauseAction, playAction)
    }


    private fun isPlaying(): Boolean {
        return if (mediaSession.controller.playbackState != null) {
            mediaSession.controller.playbackState.state ==
                    PlaybackStateCompat.STATE_PLAYING
        } else {
            false
        }
    }

    private fun getNotificationIntent(): PendingIntent {
        val openActivityIntent = Intent(this, MainActivity::class.java)
        openActivityIntent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        return PendingIntent.getActivity(
            this@MediaPlaybackService, 0, openActivityIntent,
            PendingIntent.FLAG_CANCEL_CURRENT
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE)
                    as NotificationManager
        if (notificationManager.getNotificationChannel(PLAYER_CHANNEL_ID) == null) {
            val channel = NotificationChannel(
                PLAYER_CHANNEL_ID, "Player",
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(
        mediaDescription: MediaDescriptionCompat,
        bitmap: Bitmap?
    ): Notification {

        Log.d(
            TAG, "createNotification: is Playing : ${isPlaying()}," +
                    "Notification Details : ${mediaDescription.title}"
        )


        val notificationIntent = getNotificationIntent()

        val (pauseAction, playAction) = getPlayPauseActions()

        val notification = NotificationCompat.Builder(
            this@MediaPlaybackService, PLAYER_CHANNEL_ID
        )

        notification
            .setContentTitle(mediaDescription.title)
            .setContentText(mediaDescription.subtitle)
            .setLargeIcon(bitmap)
            .setContentIntent(notificationIntent)
            .setDeleteIntent(
                MediaButtonReceiver.buildMediaButtonPendingIntent(
                    this,
                    PlaybackStateCompat.ACTION_STOP
                )
            )
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setSmallIcon(R.drawable.ic_baseline_album)
            .addAction(if (isPlaying()) pauseAction else playAction)
            .setStyle(
                NotificationCompatX.MediaStyle()
                    .setMediaSession(mediaSession.sessionToken)
                    .setShowActionsInCompactView()
                    .setShowCancelButton(true)
                    .setCancelButtonIntent(
                        MediaButtonReceiver.buildMediaButtonPendingIntent(
                            this,
                            PlaybackStateCompat.ACTION_STOP
                        )
                    )
            )

        return notification.build()
    }


    @OptIn(DelicateCoroutinesApi::class)
    private fun displayNotification() {

        val mediaDescription = mediaSession.controller.metadata.description
        var bitmapByGlide: Bitmap? = null

        if (mediaSession.controller.metadata == null) {
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }
        CoroutineScope(Dispatchers.IO).launch {
            if (getBitMapFromMemoryCache(mediaDescription.iconUri.toString()) == null) {
                val bitmap = mediaDescription.iconUri?.let {
                    URL(it.toString()).openStream().use { stream ->
                        BitmapFactory.decodeStream(stream)
                    }
//                val bitmap = Glide.with(this@MediaPlaybackService).asBitmap().load(mediaDescription.iconUri).submit().get()

                }
                bitmap?.let { setBitMapToMemoryCache(mediaDescription.iconUri.toString(), bitmap) }
            }


            val notification = createNotification(
                mediaDescription,
                getBitMapFromMemoryCache(mediaDescription.iconUri.toString())
            )

            ContextCompat.startForegroundService(
                this@MediaPlaybackService,
                Intent(
                    this@MediaPlaybackService,
                    MediaPlaybackService::class.java
                )
            )
            startForeground(NOTIFICATION_ID, notification)

            if (mediaSession.controller.metadata == null) {
                Log.d(TAG, "displayNotification: ${mediaDescription.title}")
            }
        }

    }

    companion object {
        private const val TAG = "MediaPlaybackService"
        const val PREPARED = "prepared"

        private const val PODPLAY_EMPTY_ROOT_MEDIA_ID =
            "podplay_empty_root_media_id"
        private const val NOTIFICATION_ID = 1

    }

    override fun onStateChanged(@PlaybackStateCompat.State state: Int) {
        if (state == PlaybackStateCompat.STATE_PAUSED ||
            state == PlaybackStateCompat.STATE_PLAYING
        ) displayNotification()
        val intent = Intent().apply {
            action = PREPARED
            putExtra("MEDIA_STATE", state)
        }
        sendBroadcast(intent)
    }

    override fun onStopPlaying() {
        stopSelf()
        stopForeground(true)
        val controller = mediaSession.controller

        if (controller.playbackState != null) {
            val sharedPref = this.getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE
            )
            with(sharedPref.edit()) {
                putLong("NowPlayingPosition", controller.playbackState.position)
                apply()
            }
        }

    }


    override fun onPausePlaying() {
        stopForeground(false)
    }

    override fun onSeekCompleted() {
        val intent = Intent().apply {
            action = PREPARED
            putExtra("SEEK_COMPLETED", true)
        }
        sendBroadcast(intent)
    }


    override fun onMediaPlayerPrepared() {
        val intent = Intent().apply {
            action = PREPARED
            putExtra("MEDIA_PREPARED", true)
        }
        sendBroadcast(intent)
    }

    override fun onMediaPlayerPreparedWithMediaPlayer(mediaPlayer: MediaPlayer) {
        this.mediaPlayer = mediaPlayer
    }


    private fun getBitMapFromMemoryCache(key: String): Bitmap? {
        return memoryCache.get(key)
    }


    private fun setBitMapToMemoryCache(key: String, bitmap: Bitmap) {
        if (getBitMapFromMemoryCache(key) == null) {
            memoryCache.put(key, bitmap)
        }
    }


}
