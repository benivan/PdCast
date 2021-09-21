package com.example.pdcast

import android.app.Activity
import android.content.*
import android.graphics.drawable.Drawable
import android.media.MediaPlayer
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.MediaController
import android.widget.SeekBar
import androidx.activity.viewModels
import androidx.appcompat.widget.Toolbar
import androidx.core.net.toUri
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.onNavDestinationSelected
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.example.pdcast.databinding.ActivityMainBinding
import com.example.pdcast.databinding.LayoutBottomSheetBinding
import com.example.pdcast.mediaPlayer.MediaPlaybackService
import com.example.pdcast.mediaPlayer.PdCastMediaCallback
import com.example.pdcast.ui.PlayerVIewModel
import com.example.pdcast.util.PState
import com.example.pdcast.util.PState.*
import com.example.pdcast.util.PlayerState
import com.google.android.exoplayer2.util.Util
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlin.math.log


class MainActivity : AppCompatActivity() {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    private lateinit var navController: NavController

    private lateinit var controller: MediaControllerCompat

    private lateinit var mediaBrowser: MediaBrowserCompat

    private var mediaControllerCallback: MainActivity.MediaControllerCallback? = null


    private val playerViewModel: PlayerVIewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)


        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment

        val navController = navHostFragment.navController

        setupBottomNAvMenu(navController)


        val appBarConfiguration = AppBarConfiguration(
            topLevelDestinationIds = setOf(),
            fallbackOnNavigateUpListener = ::onSupportNavigateUp
        )
        findViewById<Toolbar>(R.id.toolbar)?.setupWithNavController(
            navController,
            appBarConfiguration
        )
        binding.playPauseButton.setOnClickListener {
            if (controller.playbackState != null) {
                if (controller.playbackState.state == PlaybackStateCompat.STATE_PLAYING) {
                    controller.transportControls.pause()
                }
                if (controller.playbackState.state == PlaybackStateCompat.STATE_PAUSED) {
                    controller.transportControls.play()
                }
            }
            if (controller.playbackState == null) {
                Log.d(TAG, "onCreate: controller is null")
                if (previouslyPlayedData()) {
                    playFromSharedPreference()
                }
            }
        }

        binding.preButton.setOnClickListener {
            if (controller.playbackState != null) {
                if (controller.playbackState.state == PlaybackStateCompat.STATE_PAUSED) {
                    controller.transportControls.play()
                    seekBy(controller, -10)
                } else {
                    seekBy(controller, -10)
                }

            }
        }

        binding.forButton.setOnClickListener {
            if (controller.playbackState != null) {
                if (controller.playbackState.state == PlaybackStateCompat.STATE_PAUSED) {
                    controller.transportControls.play()
                    seekBy(controller, 30)
                } else {
                    seekBy(controller, 30)
                }

            }
        }

        binding.bottomPlayerLayout.setOnClickListener {
//            navController.navigateUp()
            navController.navigate(R.id.playerFragment)

        }

    }

    private fun playFromSharedPreference() {
        val sharedPref = this.getSharedPreferences(
            getString(R.string.preference_file_key), Context.MODE_PRIVATE
        )
        val previousPosition = sharedPref.getLong("NowPlayingPosition", 0L)
        val uri = sharedPref.getString("NowPlayingMediaLink", "")?.toUri()
        Log.d(TAG, "playFromSharedPreference: $previousPosition")
        val bundle = Bundle()

        bundle.putString(
            MediaMetadataCompat.METADATA_KEY_TITLE,
            sharedPref.getString("NowPlyingPodcastName", "")
        )

        bundle.putString(
            MediaMetadataCompat.METADATA_KEY_ARTIST,
            sharedPref.getString("NowPlyingPodcastEpisodeName", "")
        )
        bundle.putString(
            MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI,
            sharedPref.getString("NowPlayingMediaImage", "")
        )

        controller.transportControls.playFromUri(uri, bundle)
    }


    private fun previouslyPlayedData(): Boolean {
        val sharedPref = this.getSharedPreferences(
            getString(R.string.preference_file_key), Context.MODE_PRIVATE
        )
        return sharedPref.getBoolean("IsPlayedBefore", false)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return item.onNavDestinationSelected(navController) || super.onOptionsItemSelected(item)
    }

    private fun setupBottomNAvMenu(navController: NavController) {
        val bottomNav = binding.bottomNavigation
        bottomNav.setupWithNavController(navController)
    }

    private fun seekBy(controller: MediaControllerCompat, second: Int) {
        val position = controller.playbackState.position + second * 1000
        controller.transportControls.seekTo(position)
    }

    private fun initMediaBrowser() {
        mediaBrowser = MediaBrowserCompat(
            this,
            ComponentName(this, MediaPlaybackService::class.java),
            MediaBrowserCallBacks(), null
        )
    }

    private fun registerMediaController(token: MediaSessionCompat.Token) {

        val mediaController = MediaControllerCompat(this, token)

        MediaControllerCompat.setMediaController(
            this,
            mediaController
        )

        mediaControllerCallback = MediaControllerCallback()
        mediaController.registerCallback(mediaControllerCallback!!)
    }

    inner class MediaControllerCallback : MediaControllerCompat.Callback() {
        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            super.onMetadataChanged(metadata)
            println(
                "metadata changed to " +
                        "${metadata?.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI)}"
            )
        }

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {

            if (state?.state == PlaybackStateCompat.STATE_PAUSED) {
                binding.playPauseButton.setImageResource(R.drawable.play_arrow)
            }
            if (state?.state == PlaybackStateCompat.STATE_PLAYING) {
                binding.playPauseButton.setImageResource(R.drawable.pause)
            }
            super.onPlaybackStateChanged(state)
        }
    }

    override fun onStart() {
        super.onStart()
        val intentFilter = IntentFilter(MediaPlaybackService.PREPARED)
        registerReceiver(preparedListener, intentFilter)
    }


    inner class MediaBrowserCallBacks : MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            super.onConnected()
            registerMediaController(mediaBrowser.sessionToken)
            controller = MediaControllerCompat.getMediaController(this@MainActivity)

            if (controller.playbackState != null){
                if (controller.playbackState.state == PlaybackStateCompat.STATE_PAUSED) {
                    binding.playPauseButton.setImageResource(R.drawable.play_arrow)
                }
                if (controller.playbackState.state == PlaybackStateCompat.STATE_PLAYING) {
                    binding.playPauseButton.setImageResource(R.drawable.pause)
                }
            }

            if(previouslyPlayedData()) {
                val sharedPref = this@MainActivity.getSharedPreferences(
                    getString(R.string.preference_file_key), Context.MODE_PRIVATE
                )
                val nowPlayingMediaImage = sharedPref.getString("NowPlayingMediaImage", "")
                Glide.with(this@MainActivity).load(nowPlayingMediaImage)
                    .into(binding.bottomPlayImage)
            }
            println("onConnected")
        }

        override fun onConnectionSuspended() {
            super.onConnectionSuspended()
            println("onConnectionSuspended")
        }

        override fun onConnectionFailed() {
            super.onConnectionFailed()
            println("onConnectionFailed")
        }
    }


    override fun onResume() {
        super.onResume()
        initMediaBrowser()
        binding.bottomPlayerLayout.visibility = View.VISIBLE
        if (mediaBrowser.isConnected) {
            Log.d(TAG, "onStart: media browser connected")
            if (MediaControllerCompat.getMediaController(this) == null) {
                Log.d(TAG, "onStart: setting the controller")
                registerMediaController(mediaBrowser.sessionToken)
            }
        } else {
            mediaBrowser.connect()
        }

    }

    override fun onPause() {
        super.onPause()
        if (MediaControllerCompat.getMediaController(this) != null) {
            mediaControllerCallback?.let {
                MediaControllerCompat.getMediaController(this)
                    .unregisterCallback(it)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop: ")
        unregisterReceiver(preparedListener)
        if (MediaControllerCompat.getMediaController(this) != null) {
            mediaControllerCallback?.let {
                MediaControllerCompat.getMediaController(this)
                    .unregisterCallback(it)
            }
        }
    }



    private val preparedListener = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let {

                if (it.getBooleanExtra("MEDIA_PREPARED", false)) {
                    Log.d(TAG, "onReceive: prepared media player")
                    if (previouslyPlayedData()) {
                        val sharedPref = this@MainActivity.getSharedPreferences(
                            getString(R.string.preference_file_key), Context.MODE_PRIVATE
                        )
                        val previousPosition = sharedPref.getLong("NowPlayingPosition", 0L)
                        val nowPlayingMediaImage = sharedPref.getString("NowPlayingMediaImage", "")
                        Glide.with(this@MainActivity).load(nowPlayingMediaImage)
                            .into(binding.bottomPlayImage)
                        controller.transportControls.seekTo(previousPosition)

                    }

                } else if (it.getBooleanExtra("SEEK_COMPLETED", false)) {
                    Log.d(TAG, "onReceive: seek completed")
                } else if (it.hasExtra("MEDIA_STATE")) {
                    val state = it.getIntExtra("MEDIA_STATE", 1000010)
                    Log.d(TAG, "onReceive: state is ${PState.of(state)}")
                }
                Unit
            }
        }
    }


    companion object {
        private const val TAG = "MainActivity"
    }


}
