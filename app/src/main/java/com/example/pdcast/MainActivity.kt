package com.example.pdcast

import android.content.*
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.graphics.drawable.toDrawable
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.NavController.OnDestinationChangedListener
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.onNavDestinationSelected
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.example.pdcast.data.api.RssFeedService
import com.example.pdcast.data.database.PodcastSubscribedDatabase
import com.example.pdcast.data.repository.PodcastRepository
import com.example.pdcast.data.repository.RssFeedPodcastRepository
import com.example.pdcast.databinding.ActivityMainBinding
import com.example.pdcast.mediaPlayer.MediaPlaybackService
import com.example.pdcast.ui.MainViewModel
import com.example.pdcast.ui.MainViewModelFactory
import com.example.pdcast.util.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    private lateinit var navController: NavController

    private lateinit var controller: MediaControllerCompat

    private lateinit var mediaBrowser: MediaBrowserCompat

    private var mediaControllerCallback: MainActivity.MediaControllerCallback? = null

    private lateinit var podcastRepository: PodcastRepository

    private lateinit var rssFeedPodcastRepository: RssFeedPodcastRepository

    lateinit var mainViewModel: MainViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            this.supportActionBar!!.hide()
        } catch (e: NullPointerException) {
        }
        setContentView(binding.root)


//        val podcastSubscribeDao =
//            PodcastSubscribedDatabase.getDatabase(this).podcastSubscribeDao()
//        podcastRepository = PodcastRepository(podcastSubscribeDao)

        val rssFeedService = RssFeedService

        val rssFeedPodcastDao =
            PodcastSubscribedDatabase.getDatabase(this).rssFeedPodcastDao()
        rssFeedPodcastRepository = RssFeedPodcastRepository(rssFeedPodcastDao, rssFeedService)


        val viewModelFactory = MainViewModelFactory(rssFeedPodcastRepository, application)
        mainViewModel = ViewModelProvider(this, viewModelFactory)[MainViewModel::class.java]

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment

        navController = navHostFragment.navController

        binding.bottomNavigation.setupWithNavController(navController)

        val appBarConfiguration = AppBarConfiguration(
            topLevelDestinationIds = setOf(),
            fallbackOnNavigateUpListener = ::onSupportNavigateUp
        )
        findViewById<Toolbar>(R.id.toolbar)?.setupWithNavController(
            navController,
            appBarConfiguration
        )

        mainViewModel.playFromUri
            .onEach {
                if (it) {
                    playFromSharedPreference()
                    mainViewModel.startPlayFromUri(false)
                }
            }.launchIn(lifecycleScope)


        val sharedPref = this@MainActivity.getSharedPreferences(
            getString(R.string.preference_file_key), Context.MODE_PRIVATE
        )
        val nowPlayingMediaImage = sharedPref.getString("NowPlayingMediaImage", "")
        val nowPlayingDuration = sharedPref.getString("NowPlayingEpisodeDuration", "0")
        val nowPlayingPosition = sharedPref.getLong("NowPlayingPosition", 0L)

        if (previouslyPlayedData()) {
            binding.mainPlayerProgressBar.progress =
                (nowPlayingPosition.toInt() / (nowPlayingDuration!!.toInt() * 1000)) * 100
            CoroutineScope(Dispatchers.IO).launch {
                Log.d(TAG, "onCreate: $nowPlayingMediaImage")
                val a = getPaletteColor(nowPlayingMediaImage, this@MainActivity)
                Log.d(TAG, "onMetadataChanged: $a")
                mainViewModel.paletteColor(a)
            }
        }


        setListeners()
        binding.bottomPlayerLayout.isVisible = previouslyPlayedData()
        binding.playPauseButton.setBackgroundColorToImageButton(Color.WHITE)


        //Palette Color from mainViewModel
        mainViewModel.paletteColor.flowWithLifecycle(lifecycle).onEach {
            val vibrantColor = getVibrantColorFromPalette(it)
            val vibrantDark = getDarkVibrantColor(it)
            val progressBarTintList = if (vibrantDark != 0) {
                vibrantDark
            } else vibrantColor
//            Log.d(TAG, "onCreate: ${it}")
//            Log.d(TAG, "onCreate COLOR: ${vibrantColor}")
//            Log.d(TAG, "onCreate: $vibrantDark")
            val color = vibrantColor.toDrawable()
            color.alpha = 225 / 8
            binding.bottomPlayerLayout.background = color
            binding.playPauseButton.setBackgroundColorToImageButton(vibrantColor)
            binding.preButton.setSvgColor(vibrantColor)
            binding.forButton.setSvgColor(vibrantColor)
            binding.mainPlayerProgressBar.progressTintList = ColorStateList.valueOf(vibrantColor)
            binding.mainPlayerProgressBar.setBackgroundColor(getTransparentColor(vibrantColor, 4))
            binding.heartButton.setColorFilter(vibrantColor)
        }.launchIn(lifecycleScope)



        mainViewModel.currentPlayingPosition.flowWithLifecycle(lifecycle).onEach {
            val duration = sharedPref.getString("NowPlayingEpisodeDuration", "0")
            val progressValue = (it.toDouble() / (duration!!.toDouble() * 1000)) * 100
            Log.d(TAG, "onCreate: ProgressValue -> ${progressValue.toInt()}")
            binding.mainPlayerProgressBar.progress = progressValue.toInt()

        }.launchIn(lifecycleScope)
    }

    private fun setListeners() {
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
            if (previouslyPlayedData()) {
//                navController.navigateUp()
                navController.navigate(R.id.playerFragment)
            }
        }
    }

    private val navDestinationChangedListener = OnDestinationChangedListener { _, destination, _ ->
        binding.bottomNavigation.isVisible = destination.id != R.id.playerFragment
        binding.bottomPlayerLayout.isVisible = destination.id != R.id.playerFragment
        if (destination.id == R.id.homeFragment) changeColorOfTheStatusBar(this,Color.WHITE)
        if (destination.id == R.id.playerFragment) changeColorOfTheStatusBar(this,Color.BLACK)

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


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return item.onNavDestinationSelected(navController) || super.onOptionsItemSelected(item)
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
            val changedMetaDataUri = metadata?.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI)
            Log.d(TAG, "onMetadataChanged: $changedMetaDataUri")

            if (changedMetaDataUri != null) {
                val sharedPref = this@MainActivity.getSharedPreferences(
                    getString(R.string.preference_file_key), Context.MODE_PRIVATE
                )
                val nowPlayingMediaImage = sharedPref.getString("NowPlayingMediaImage", "")

                Glide.with(binding.bottomPlayImage).load(nowPlayingMediaImage)
                    .into(binding.bottomPlayImage)
                Log.d(TAG, "onMetadataChanged: $nowPlayingMediaImage")

                CoroutineScope(Dispatchers.IO).launch {
                    val a = getPaletteColor(nowPlayingMediaImage, this@MainActivity)
                    mainViewModel.paletteColor(a)
                }
            }
        }

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            super.onPlaybackStateChanged(state)
            if (state?.state == PlaybackStateCompat.STATE_PAUSED) {
                binding.playPauseButton.setImageResource(R.drawable.play_arrow)
//                mainViewModel.setPlaying(false)
            }
            if (state?.state == PlaybackStateCompat.STATE_PLAYING) {
                binding.playPauseButton.setImageResource(R.drawable.pause)
            }
        }
    }


    inner class MediaBrowserCallBacks : MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            super.onConnected()
            registerMediaController(mediaBrowser.sessionToken)
            controller = MediaControllerCompat.getMediaController(this@MainActivity)

            if (controller.playbackState != null) {
                mainViewModel.setPlaying(true)
                mainViewModel.setController(controller)
                if (controller.playbackState.state == PlaybackStateCompat.STATE_PAUSED) {
                    binding.playPauseButton.setImageResource(R.drawable.play_arrow)
                }
                if (controller.playbackState.state == PlaybackStateCompat.STATE_PLAYING) {
                    binding.playPauseButton.setImageResource(R.drawable.pause)
                    mainViewModel.setPlaying(true)
                }
            }
            if (previouslyPlayedData()) {
                val sharedPref = this@MainActivity.getSharedPreferences(
                    getString(R.string.preference_file_key), Context.MODE_PRIVATE
                )
                val nowPlayingMediaImage = sharedPref.getString("NowPlayingMediaImage", "")

                Glide.with(binding.bottomPlayImage).load(nowPlayingMediaImage)
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

    override fun onStart() {
//        changeColorOfTheStatusBar(this,Color.WHITE)
        super.onStart()
        Log.d(TAG, "onStart: ")
        navController.addOnDestinationChangedListener(navDestinationChangedListener)
        val intentFilter = IntentFilter(MediaPlaybackService.PREPARED)
        registerReceiver(preparedListener, intentFilter)
        CoroutineScope(Dispatchers.IO).launch {


        }
    }

    override fun onResume() {
//        changeColorOfTheStatusBar(this,Color.WHITE)
        super.onResume()
        Log.d(TAG, "onResume: ")
        initMediaBrowser()
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
        Log.d(TAG, "onPause: ")
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
        navController.removeOnDestinationChangedListener(navDestinationChangedListener)
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
                        mainViewModel.setController(controller)
                    }

                } else if (it.getBooleanExtra("SEEK_COMPLETED", false)) {
                    Log.d(TAG, "onReceive: seek completed")
                } else if (it.hasExtra("MEDIA_STATE")) {
                    val state = it.getIntExtra("MEDIA_STATE", 1000010)
                    Log.d(TAG, "onReceive: state is ${PState.of(state)}")
                    if (state == 2) mainViewModel.setPlaying(false)
                    if (state == 3) mainViewModel.setPlaying(true)
                } else if (it.hasExtra("BUFFERING_LEVEL")) {
                    mainViewModel.bufferingLevel(

                    )
                }
                Unit
            }
        }
    }

    fun previouslyPlayedData(): Boolean {
        val sharedPref = this.getSharedPreferences(
            getString(R.string.preference_file_key), Context.MODE_PRIVATE
        )
        return sharedPref.getBoolean("IsPlayedBefore", false)
    }

    companion object {
        private const val TAG = "MainActivity"
    }


}
