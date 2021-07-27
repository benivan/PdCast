package com.example.pdcast

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.onNavDestinationSelected
import androidx.navigation.ui.setupWithNavController
import com.example.pdcast.databinding.ActivityMainBinding
import com.example.pdcast.ui.PlayerVIewModel
import com.example.pdcast.util.Resource
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.util.Util
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class MainActivity : AppCompatActivity() {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    private lateinit var navController: NavController

    private val playerViewModel: PlayerVIewModel by viewModels()

    private var player: SimpleExoPlayer? = null

    private var mediaUrl: String? = null

    private var isFirstTime:Boolean = true

    private var playWhenReady = true
    private var currentWindow = 0
    private var playbackPosition = 0L


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

        playerViewModel.mediaUrl.flowWithLifecycle(lifecycle).onEach {

            when (it) {
                is Resource.Failure -> {
                    Log.d(TAG, "onCreate: Failure")
                }

                is Resource.Loading -> {
                    Log.d(TAG, "onCreate: Loading")
                }

                is Resource.Success -> {
                    Log.d(TAG, "onCreate: Success")
                    mediaUrl = it.data
                    binding.mainPlayer.visibility = View.VISIBLE
                    if (!isFirstTime){
                        clearPlayer()
                    }
                    initializePlayer()
                    isFirstTime = false
                }

                is Resource.None -> {
                    Log.d(TAG, "onCreate: None")
                }


            }
        }.launchIn(lifecycleScope)


        binding.mainPlayer.visibility = View.GONE

    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return item.onNavDestinationSelected(navController) || super.onOptionsItemSelected(item)
    }

    private fun setupBottomNAvMenu(navController: NavController) {
        val bottomNav = binding.bottomNavigation
        bottomNav.setupWithNavController(navController)
    }

    private fun initializePlayer() {
        player = SimpleExoPlayer.Builder(this)
            .build()
            .also { exoPlayer ->
                binding.mainPlayer.player = exoPlayer
                val mediaItem =
                    mediaUrl?.let { MediaItem.fromUri(it) }
                mediaItem?.let { exoPlayer.setMediaItem(it) }
                exoPlayer.playWhenReady = playWhenReady
                exoPlayer.seekTo(currentWindow, playbackPosition)
                exoPlayer.prepare()
            }

    }


//    override fun onStart() {
//        super.onStart()
//        if (!isFirstTime) {
//            if (Util.SDK_INT >= 24) {
//                initializePlayer()
//            }
//        }
//    }

    override fun onResume() {
        super.onResume()
        if (!isFirstTime) {
            if ((Util.SDK_INT < 24 || player == null)) {
                initializePlayer()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (Util.SDK_INT < 24) {
            releasePlayer()
        }
    }

    override fun onStop() {
        super.onStop()
        if (Util.SDK_INT >= 24) {
            releasePlayer()
        }
    }

    private fun clearPlayer(){
        player?.run {
            playWhenReady = false
            playbackPosition = 0L
            currentWindow = 0

            release()

        }
        player = null
    }

    private fun releasePlayer() {
        player?.run {
            playbackPosition = this.currentPosition
            currentWindow = this.currentWindowIndex
            playWhenReady = this.playWhenReady
            release()
        }
        player = null
    }

    companion object {
        private const val TAG = "MainActivity"
    }

}


