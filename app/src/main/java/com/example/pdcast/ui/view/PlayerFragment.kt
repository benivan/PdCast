package com.example.pdcast.ui.view

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.pdcast.R
import com.example.pdcast.databinding.FragmentPlayerBinding
import com.example.pdcast.mediaPlayer.MediaPlaybackService
import com.example.pdcast.ui.MainViewModel
import com.example.pdcast.ui.PlayerViewModel
import com.google.android.material.transition.MaterialFadeThrough
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class PlayerFragment : Fragment() {

    private var _binding: FragmentPlayerBinding? = null
    private val binding: FragmentPlayerBinding get() = _binding!!


    private val mainViewModel: MainViewModel by activityViewModels()
    private val viewModel: PlayerViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        _binding = FragmentPlayerBinding.inflate(layoutInflater, container, false)

        mainViewModel.getController()?.let { viewModel.currentPosition(it) }

        val intentFilter = IntentFilter(MediaPlaybackService.PREPARED)
        requireActivity().registerReceiver(preparedListener, intentFilter)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        enterTransition = MaterialFadeThrough()
        exitTransition = MaterialFadeThrough()


        if (previouslyPlayedData()) {
            val data = getSharedPreData()
            binding.PodcastName.text = data.elementAt(0)
            binding.podcastEpisodeName.text = data.elementAt(1)
            Glide.with(this).load(data.elementAt(2)).into(binding.podcastImage)
            binding.endDuration.text = convertSecondIntoDuration(data.elementAt(3)!!.toInt())
            binding.playerSeekBar.max = data.elementAt(3)!!.toInt() * 1000

            if (mainViewModel.getController()?.playbackState == null) {
                binding.playerSeekBar.progress = data.elementAt(4)?.toInt() ?: 0
                binding.startDuration.text =
                    convertSecondIntoDuration(data.elementAt(4)!!.toInt() / 1000)
            }


            binding.playerPlayPauseButton.setOnClickListener {

                if (mainViewModel.getController()?.playbackState != null) {
                    if (mainViewModel.getController()?.playbackState?.state == PlaybackStateCompat.STATE_PAUSED) {
                        mainViewModel.getController()?.transportControls?.play()
                    }
                    if (mainViewModel.getController()?.playbackState?.state == PlaybackStateCompat.STATE_PLAYING) {
                        mainViewModel.getController()?.transportControls?.pause()
                    }
                } else if (mainViewModel.getController()?.playbackState == null) {
                    mainViewModel.startPlayFromUri(true)
                }
            }

            binding.playerForButton.setOnClickListener {
                mainViewModel.getController()?.let { it1 -> seekBy(it1, 30) }
                if (mainViewModel.getController()?.playbackState?.state == PlaybackStateCompat.STATE_PAUSED) {
                    mainViewModel.getController()?.transportControls?.play()
                }
            }

            binding.playerPreButton.setOnClickListener {
                mainViewModel.getController()?.let { it1 -> seekBy(it1, -10) }
                if (mainViewModel.getController()?.playbackState?.state == PlaybackStateCompat.STATE_PAUSED) {
                    mainViewModel.getController()?.transportControls?.play()
                }
            }
        }

        mainViewModel.isPlaying
            .onEach {
                if (it) binding.playerPlayPauseButton.setImageResource(R.drawable.pause)
                else binding.playerPlayPauseButton.setImageResource(R.drawable.play_arrow)
            }.launchIn(lifecycleScope)

        mainViewModel.bufferLevel
            .onEach {
                binding.playerSeekBar.secondaryProgress = it
            }

        viewModel.position
            .onEach {
                if (it == 0) {
                    val position = getPositionFromShared()
                    binding.playerSeekBar.progress = position
                    binding.startDuration.text =
                        convertSecondIntoDuration(position / 1000)
                } else {
                    binding.playerSeekBar.progress = it
                    binding.startDuration.text =
                        convertSecondIntoDuration(it / 1000)
                }
            }.launchIn(lifecycleScope)

        binding.playerSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    mainViewModel.getController()?.transportControls?.seekTo(progress.toLong())
                    binding.playerSeekBar.progress = progress
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit

            override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit

        })

    }


    override fun onStop() {
        super.onStop()
        requireActivity().unregisterReceiver(preparedListener)
    }

    override fun onResume() {
        Log.d(TAG, "onResume: ")
        super.onResume()
    }

    private fun previouslyPlayedData(): Boolean {
        val sharedPref = requireActivity().getSharedPreferences(
            getString(R.string.preference_file_key), Context.MODE_PRIVATE
        )
        return sharedPref.getBoolean("IsPlayedBefore", false)
    }

    private fun getPositionFromShared(): Int {
        val sharedPref = requireActivity().getSharedPreferences(
            getString(R.string.preference_file_key), Context.MODE_PRIVATE
        )
        return sharedPref.getLong("NowPlayingPosition", 0L).toInt()
    }


    private fun getSharedPreData(): List<String?> {
        val sharedPref = requireActivity().getSharedPreferences(
            getString(R.string.preference_file_key), Context.MODE_PRIVATE
        )
        return listOf(
            sharedPref.getString("NowPlyingPodcastName", ""),
            sharedPref.getString("NowPlyingPodcastEpisodeName", ""),
            sharedPref.getString("NowPlayingMediaImage", ""),
            sharedPref.getString("NowPlayingEpisodeDuration", ""),
            sharedPref.getLong("NowPlayingPosition", 0L).toString()
        )
    }

    private fun seekBy(controller: MediaControllerCompat, second: Int) {
        val position = controller.playbackState.position + second * 1000
        controller.transportControls.seekTo(position)
    }

    companion object {
        private const val TAG = "PlayerFragment"
    }


    private val preparedListener = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let {

                if (it.getBooleanExtra("MEDIA_PREPARED", false)) {
                    viewModel.currentPosition(mainViewModel.getController()!!)
                }
                Unit
            }
        }
    }

    private fun convertSecondIntoDuration(duration: Int): String {
        var time = duration
        var convertedDuration: String = ""

        val hour = time / 3600

        time %= 3600
        val min = time / 60

        time %= 60
        val second = time

        convertedDuration = "$hour:$min:$second"
        return convertedDuration
    }


}











